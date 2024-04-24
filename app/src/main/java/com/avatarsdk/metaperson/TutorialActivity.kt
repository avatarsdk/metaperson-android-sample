package com.avatarsdk.metaperson

import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.webkit.*
import com.avatarsdk.metaperson.databinding.ActivityMainBinding
import com.avatarsdk.metaperson.databinding.ActivityTutorialBinding

class TutorialActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTutorialBinding
    var webView: WebView? = null
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return true
    }
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityTutorialBinding.inflate(layoutInflater)
        setContentView(binding.root)
        webView = binding.webView
        webView!!.webViewClient = object: WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return false
            }
        }
        supportActionBar!!.setHomeButtonEnabled(true);
        supportActionBar!!.setDisplayHomeAsUpEnabled(true);

        webView!!.settings.javaScriptEnabled = true
        webView!!.settings.cacheMode = WebSettings.LOAD_DEFAULT
        webView!!.settings.databaseEnabled =true
        webView!!.settings.useWideViewPort = true
        webView!!.settings.loadWithOverviewMode = true
        webView!!.settings.allowFileAccess = true
        webView!!.settings.domStorageEnabled = true
        webView!!.settings.javaScriptCanOpenWindowsAutomatically = true
        //setContentView(webView)
        webView!!.loadUrl("https://www.youtube.com/embed/e9E7GFrG0A4")


    }
}