package com.example.perilif

import android.widget.TextView
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.widget.ProgressBar
import android.graphics.drawable.Drawable

class Device(private var context: MainActivity) {
    var maxCurrent: Int = 0
    var lastCurrent: Int = 0

    fun setCurrent(current: Int) {
        lastCurrent = current
        if (lastCurrent > maxCurrent) {
            maxCurrent = lastCurrent
        }

        render()
    }

    fun getPercentage(): Int {
        if (maxCurrent == 0) {
            return 0
        }
        return 100 * lastCurrent / maxCurrent
    }

    fun render() {
        val bar = context.findViewById<ProgressBar>(R.id.powerbar)
        val label = context.findViewById<TextView>(R.id.powerbar_text)
        val percent = getPercentage()
        val caption = "$lastCurrent / $maxCurrent [%$percent]"

        bar.setProgress(percent)
        Helper.changeProgressBarColor(bar, context)

        label.setText(caption)
    }
}