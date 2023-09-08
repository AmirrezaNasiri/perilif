package com.example.perilif

import android.util.Log
import android.widget.TextView
import android.widget.Button
import android.widget.ProgressBar

class Pad(
        private var context: MainActivity,
        val name: String,
    ) {
    private var currentCycle: Int = 0
    var targetCycle: Int = 0
    var oldTargetCycle: Int = 0

    fun setCycle(cycle: Int) {
        currentCycle = cycle

        render()
    }

    fun syncTargetCycle() {
        targetCycle = currentCycle
    }

    fun pauseTargetCycle() {
        oldTargetCycle = targetCycle
    }

    fun resumeTargetCycle() {
        targetCycle = oldTargetCycle
    }

    fun increaseTargetCycle() {
        if (targetCycle <= 90) {
            targetCycle += 10
        }
    }

    fun decreaseTargetCycle() {
        if (targetCycle >= 10) {
            targetCycle -= 10
        }
    }

    fun enable() {
        findViewBySuffix<Button>("increase").isEnabled = true
        findViewBySuffix<Button>("decrease").isEnabled = true
    }

    fun disable() {
        findViewBySuffix<Button>("increase").isEnabled = false
        findViewBySuffix<Button>("decrease").isEnabled = false
    }

    private fun render() {
        val bar = findViewBySuffix<ProgressBar>("powerbar")
        val label = findViewBySuffix<TextView>("powerbar_text")
        val caption = "%$currentCycle"

        bar.progress = currentCycle

        Helper.changeProgressBarColor(bar, context)

        label.text = caption
    }

    fun <T> findViewBySuffix(suffix: String): T {
        val id = "pad_${name}_${suffix}"
        return context.findViewById(
            context.resources.getIdentifier(id, "id", context.packageName)
        )
    }
}