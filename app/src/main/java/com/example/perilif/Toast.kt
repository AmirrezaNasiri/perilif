package com.example.perilif

import android.widget.TextView
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast


class Toast(private var context: MainActivity) {
    fun show(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}