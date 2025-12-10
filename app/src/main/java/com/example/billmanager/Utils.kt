package com.example.billmanager

import android.text.InputType
import android.view.MotionEvent
import android.widget.EditText

fun setupShowHidePassword(editText: EditText) {
    var isPasswordVisible = false
    editText.setOnTouchListener { _, event ->
        if (event.rawX >= editText.right - editText.compoundDrawables[2].bounds.width() && event.action == MotionEvent.ACTION_UP) {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                editText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                editText.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.icon_eye_on,
                    0
                )
            } else {
                editText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_eye, 0)
            }
            // Giữ nguyên con trỏ ở cuối
            editText.setSelection(editText.text.length)
            return@setOnTouchListener true
        }
        false
    }
}