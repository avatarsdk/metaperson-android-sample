package com.avatarsdk.metaperson

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat


class WebUiActivity : AppCompatActivity() {

    var webView: WebView? = null
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private fun prepareJsApi() {
        //find your values at https://accounts.avatarsdk.com/developer/
        val clientId = getString(R.string.CLIENT_ID)
        val clientSecret = getString(R.string.CLIENT_SECRET)

        webView!!.evaluateJavascript(
        "function onWindowMessage(evt) {\n" +
                    "if (evt.type === 'message') { \n" +
                        "if (evt.data?.source === 'metaperson_creator') { \n" +
                            "let data = evt.data; \n" +
                            "let evtName = data?.eventName; \n" +
                            "if (evtName === 'mobile_loaded') { \n" +
                                "onMobileLoaded(evt, data); \n" +
                            "} else if (evtName === 'model_exported') { \n" +
                                "metapersonJsApi.showToast(evt.data.url);\n" +
                            "}\n" +
                        "} \n" +
                    "} \n" +
              "} \n" +
                "\n" +
                "function onMobileLoaded(evt, data) {\n" +
                "    let authenticationMessage = {\n" +
                "        'eventName': 'authenticate',\n" +
                "        'clientId': '$clientId',\n" +
                "        'clientSecret': '$clientSecret',\n" +
                "        'exportTemplateCode': '',\n" +
                "    };\n" +
                "    evt.source.postMessage(authenticationMessage, '*');\n" +
                "    let exportParametersMessage = {\n" +
                "        'eventName': 'set_export_parameters',\n" +
                "        'format': 'glb',\n" +
                "        'lod': 1,\n" +
                "        'textureProfile': '2K.png'\n" +
                "    };\n" +
                "    evt.source.postMessage(exportParametersMessage, '*');\n" +
                "\n" +
                "}\n" +
                "document.addEventListener('DOMContentLoaded', function onDocumentReady() {\n" +
                "          window.addEventListener('message', onWindowMessage);\n" +
                "});\n"
            , null)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        webView!!.webViewClient = object: WebViewClient() {
            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                prepareJsApi()
            }
        }

        webView!!.webChromeClient = object: WebChromeClient(){
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                this@WebUiActivity.filePathCallback = filePathCallback

                fileChooserParams?.let {
                    if (it.isCaptureEnabled){
                        if (hasPermissionAccess()) {
                            openCameraResultContract.launch(null)
                        } else {
                            requestPermission.launch(arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ))
                        }
                    } else {
                        openDocumentContract.launch("image/*")
                    }
                }
                return true
            }

            override fun onPermissionRequest(request: PermissionRequest?) {
                Log.d("PERMISSION", "onPermissionRequest: ${request?.resources} ")
                request?.grant(arrayOf(Manifest.permission.CAMERA))

            }
        }

        webView!!.settings.javaScriptEnabled = true
        webView!!.settings.cacheMode = WebSettings.LOAD_DEFAULT
        webView!!.settings.databaseEnabled =true
        webView!!.settings.useWideViewPort = true
        webView!!.settings.loadWithOverviewMode = true
        webView!!.settings.allowFileAccess = true
        webView!!.settings.domStorageEnabled = true
        webView!!.settings.allowFileAccessFromFileURLs = true
        webView!!.settings.javaScriptCanOpenWindowsAutomatically = true
        setContentView(webView)
        webView!!.loadUrl("https://mobile.metaperson.avatarsdk.com/")
        webView!!.addJavascriptInterface(WebAppInterface(this, webView!!), "metapersonJsApi")

    }

    private val openDocumentContract = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ){
        if(it == null){
            Toast.makeText(this, "No Image Selected !!", Toast.LENGTH_SHORT).show()
            filePathCallback?.onReceiveValue(null)
        } else {
            it?.let {
                filePathCallback?.onReceiveValue(arrayOf(it))
            }
        }
    }

    private val openCameraResultContract  = registerForActivityResult(ActivityResultContracts.TakePicturePreview()){

        if(it == null){
            Toast.makeText(this, "No Image captured !!", Toast.LENGTH_SHORT).show()
            filePathCallback?.onReceiveValue(null)
        } else {
            it?.let {
                Log.d("ON RESULT", "no data bitmap: ${it}")
                val path = MediaStore.Images.Media.insertImage(contentResolver, it, "fromCamera.jpeg", "")
                filePathCallback?.onReceiveValue(arrayOf(Uri.parse(path)))
            }
        }
    }

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){ permissionMap ->
        if (!permissionMap.values.all { it }){
            Toast.makeText(this, "Camera permission not granted.", Toast.LENGTH_SHORT).show()
            filePathCallback?.onReceiveValue(null)
        } else {
            openCameraResultContract.launch(null)
        }

    }

    private fun hasPermissionAccess(): Boolean{
        return arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).all {
            ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}