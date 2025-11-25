package com.example.billmanager

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    lateinit var imgUser: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setControl()
        setEvent()
    }

    private fun setControl() {
        imgUser = findViewById<ImageView>(R.id.imgUser)
    }

    private fun setEvent() {
        imgUser.setOnClickListener {
            val intent = Intent(this, MainProfile::class.java)
            startActivity(intent)
            finish()
        }
    }
}