package com.avatarsdk.metaperson

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import android.util.Log
import android.webkit.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat

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
                "    let uiParametersMessage = {\n" +
                "        'eventName': 'set_ui_parameters',\n" +
                "        'isExportButtonVisible': false,\n" +
                "        'isLoginButtonVisible': false,\n" +
                "    };\n" +
                "    evt.source.postMessage(uiParametersMessage, '*');\n" +
                "\n" +
                "}\n" +
                "document.addEventListener('DOMContentLoaded', function onDocumentReady() {\n" +
                "          window.addEventListener('message', onWindowMessage);\n" +
                "});\n"
            , null)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        // Edge-to-edge is mandatory from targetSdk 36; force light system bar icons because
        // the window background is a dark navy (avatar_sdk_violet).
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
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
                            openCamera()
                        } else {
                            requestPermission.launch(arrayOf(Manifest.permission.CAMERA))
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
        // The WebView *is* the content view, so it takes the insets directly.
        applySystemBarInsets(webView!!)
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

    private lateinit var imageUri: Uri

    private val openCameraResultContract = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (!success) {
            Toast.makeText(this, "No Image captured !!", Toast.LENGTH_SHORT).show()
            filePathCallback?.onReceiveValue(null)
        } else {
            Log.d("ON RESULT", "Image saved to: $imageUri")
            filePathCallback?.onReceiveValue(arrayOf(imageUri))
        }
    }

    // Function to start the camera intent
    private fun openCamera() {
        val imageFile = File.createTempFile("fromCamera", ".jpeg", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        imageUri = FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.provider", imageFile)
        openCameraResultContract.launch(imageUri)
    }


    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){ permissionMap ->
        if (!permissionMap.values.all { it }){
            Toast.makeText(this, "Camera permission not granted.", Toast.LENGTH_SHORT).show()
            filePathCallback?.onReceiveValue(null)
        } else {
            //openCameraResultContract.launch(null)
            openCamera()
        }

    }

    // Only CAMERA is needed: captures go to a FileProvider cache URI (see openCamera) and
    // gallery picks go through GetContent, neither of which requires a storage permission.
    private fun hasPermissionAccess(): Boolean{
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
}