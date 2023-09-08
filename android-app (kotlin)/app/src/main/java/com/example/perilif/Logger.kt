package com.example.perilif

import android.widget.TextView
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.method.ScrollingMovementMethod




class Logger(private var context: MainActivity) {
    fun log(tag: String, message: String) {
        findLogsTv().text =
            ("$tag $message\n" + findLogsTv().text.toString()).split("\n").take(200).joinToString("\n")
    }

    fun copy() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", findLogsTv().text)
        clipboard.setPrimaryClip(clip)
    }

    private fun findLogsTv(): TextView {
        return context.findViewById(R.id.logs)
    }

}