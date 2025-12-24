package com.example.billmanager.ui.settings

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.billmanager.R

class HelpActivity : AppCompatActivity() {
    private lateinit var btnBack: ImageButton
    private lateinit var tvTitle: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        setControl()
        setEvent()
    }

    private fun setControl(){
        btnBack = findViewById(R.id.btnBack)
        tvTitle = findViewById(R.id.tvTitle)
        tvTitle.text = "Hướng dẫn sử dụng"
    }

    private fun setEvent(){
        btnBack.setOnClickListener { finish() }
    }
}