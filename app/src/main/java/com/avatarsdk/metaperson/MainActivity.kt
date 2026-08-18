package com.avatarsdk.metaperson

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.avatarsdk.metaperson.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Edge-to-edge is mandatory from targetSdk 36. The app background is a dark navy
        // (avatar_sdk_violet), so force light system bar icons instead of following the
        // system light/dark setting.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarInsets(binding.root)

        binding.createButton.setOnClickListener {
            openMetapersonCreator()
        }
        binding.watchTutorial.setOnClickListener {
            openTutorial()
        }
        binding.mailButton.setOnClickListener{
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@avatarsdk.com")
            }
            startActivity(Intent.createChooser(emailIntent, "Send feedback"))
        }
    }

    private fun openMetapersonCreator() {
        val intent = Intent(this, WebUiActivity::class.java)
        startActivity(intent)
    }
    /**
     * Opens the tutorial in the YouTube app (via app links) or a browser.
     *
     * The video used to play in an in-app WebView, but YouTube's embedded player requires a
     * verifiable embedding origin that a WebView cannot provide - loading the embed top-level
     * fails with "Error 153" and loading it inside HTML fails with "Error 152", because
     * loadDataWithBaseURL documents get an opaque origin. Handing the URL to a real video
     * player avoids the problem entirely and gives proper controls and fullscreen.
     */
    private fun openTutorial() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(TUTORIAL_VIDEO_URL))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.tutorial_no_player, Toast.LENGTH_LONG).show()
        }
    }

    private companion object {
        const val TUTORIAL_VIDEO_URL = "https://www.youtube.com/watch?v=e9E7GFrG0A4"
    }
}