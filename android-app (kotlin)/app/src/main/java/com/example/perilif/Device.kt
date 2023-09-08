package com.example.perilif

import android.widget.TextView
import android.widget.ProgressBar

class Device(private var context: MainActivity) {
    private var maxCurrent: Int = 0
    private var lastCurrent: Int = 0

    fun setCurrent(current: Int) {
        lastCurrent = current
        if (lastCurrent > maxCurrent) {
            maxCurrent = lastCurrent
        }

        render()
    }

    private fun getPercentage(): Int {
        if (maxCurrent == 0) {
            return 0
        }
        return 100 * lastCurrent / maxCurrent
    }

    private fun render() {
        val bar = context.findViewById<ProgressBar>(R.id.powerbar)
        val label = context.findViewById<TextView>(R.id.powerbar_text)
        val percent = getPercentage()
        val caption = "${lastCurrent}mA / ${maxCurrent}mA [%$percent]"

        bar.progress = percent
        Helper.changeProgressBarColor(bar, context)

        label.text = caption
    }
}