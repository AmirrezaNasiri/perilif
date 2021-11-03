package com.example.perilif

import android.view.View
import android.widget.ProgressBar
import android.widget.Switch
import kotlinx.coroutines.FlowPreview
import kotlin.concurrent.schedule
import org.json.JSONObject
import java.util.*
import android.os.Handler
import android.os.Looper


class Controller(private var context: MainActivity) {
    private var debouncing = false

    @FlowPreview
    fun updateCycles() {
        toggleLoadingVisibility(true)
        toggleTurboActivity(false)

        if (!debouncing) {
            debouncing = true
            Handler(Looper.getMainLooper()).postDelayed({
                val data = JSONObject()
                context.pads.forEach { data.put("p${it.key}c", it.value.targetCycle) }
                send("c", data)
                debouncing = false
                context.toast.show("Cycles are updated.")
            }, 1_000L)
        }
    }

    @FlowPreview
    fun toggleTurbo() {
        if (context.findViewById<ProgressBar>(R.id.switch_turbo).isEnabled) {
            disableTurbo()
        } else {
            enableTurbo()
        }
    }

    @FlowPreview
    fun enableTurbo() {
        val data = JSONObject()
        context.pads.forEach { data.put("p${it.key}c", 100) }
        send("c", data)
        toggleTurboEnable(true)
        context.pads.forEach { it.value.disable() }

        Handler(Looper.getMainLooper()).postDelayed({
            disableTurbo()
        }, 10_000L)

        context.toast.show("Turbo enabled. 🔥ing up ...")
    }

    @FlowPreview
    fun disableTurbo() {
        this.updateCycles()
        toggleTurboEnable(false)
        context.toast.show("Turbo disabled.")
    }

    private fun send(type: String, data: JSONObject) {
        toggleLoadingVisibility(true)
        context.pads.forEach { it.value.disable() }
        toggleTurboActivity(false)

        context.logger.log(">>", data.toString())
        data.put("_t", type)
        context.btt?.write(data.toString().toByteArray())

        context.pads.forEach { it.value.enable() }
        toggleLoadingVisibility(false)
        toggleTurboActivity(true)
    }

    private fun toggleTurboEnable(enabled: Boolean) {
        context.findViewById<Switch>(R.id.switch_turbo).isEnabled = enabled
    }

    private fun toggleTurboActivity(enabled: Boolean) {
        context.findViewById<Switch>(R.id.switch_turbo).isActivated = enabled
    }

    private fun toggleLoadingVisibility(visible: Boolean) {
        context.findViewById<ProgressBar>(R.id.command_loadingbar).visibility = if (visible) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
    }
}