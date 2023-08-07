package com.avatarsdk.metaperson

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.avatarsdk.metaperson.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.createButton.setOnClickListener {
            openMetapersonCreator()
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
}