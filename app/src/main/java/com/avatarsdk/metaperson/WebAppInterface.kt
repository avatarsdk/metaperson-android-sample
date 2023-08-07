package com.avatarsdk.metaperson

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast

class WebAppInterface(
    private val context: Context,
    private val webView: WebView,
) {
    @JavascriptInterface
    fun showToast(toast: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Exported Uri", toast)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Avatar successfully exported: $toast", Toast.LENGTH_SHORT).show()
    }
}