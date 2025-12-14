package com.example.billmanager.ui.base

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.billmanager.R

abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var btnBack: ImageButton
    protected lateinit var tvTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    protected fun setupHeader(title: String, enableBack: Boolean = true) {
        btnBack = findViewById(R.id.btnBack)
        tvTitle = findViewById(R.id.tvTitle)

        tvTitle.text = title

        if (enableBack) {
            btnBack.setOnClickListener { finish() }
        } else {
            btnBack.visibility = ImageButton.INVISIBLE
        }
    }
}
