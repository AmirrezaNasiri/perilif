package com.example.perilif

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import kotlinx.coroutines.FlowPreview
import java.io.IOException
import java.util.*
import org.json.JSONObject
import androidx.appcompat.widget.SwitchCompat
import android.animation.ObjectAnimator

import android.animation.PropertyValuesHolder

import android.view.View
import com.rohitss.uceh.UCEHandler



class MainActivity : AppCompatActivity() {
    private val moduleMac = "00:18:E5:04:BF:63"
    private val requestEnableBluetooth = 1
    private val myUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

    private var bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var bluetoothDevice: BluetoothDevice? = null
    var bluetoothThread: BluetoothThread? = null
    private var bluetoothHandler: Handler? = null

    private val commander = Commander(this@MainActivity)
    val logger = Logger(this@MainActivity)
    val toast = Toast(this@MainActivity)
    val device = Device(this@MainActivity)

    var pads = mapOf(
        "a" to Pad(this@MainActivity, "a"),
        "b" to Pad(this@MainActivity, "b"),
        "c" to Pad(this@MainActivity, "c"),
    )

    @FlowPreview
    override fun onCreate(savedInstanceState: Bundle?) {
        UCEHandler.Builder(this).build();

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Handle the copy button
        findViewById<Button>(R.id.copy_logs).setOnClickListener {
            logger.copy()
        }

        // Handle the pads' control
        pads.forEach {
            val pad = it.value

            pad.findViewBySuffix<Button>("increase").setOnClickListener {
                pad.increaseTargetCycle()

                if (isBluetoothReady()) {
                    commander.updateCycles()
                }
            }

            pad.findViewBySuffix<Button>("decrease").setOnClickListener {
                pad.decreaseTargetCycle()

                if (isBluetoothReady()) {
                    commander.updateCycles()
                }
            }
        }

        // Handle the turbo switch control
        findViewById<SwitchCompat>(R.id.switch_turbo).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                commander.enableTurbo()
            } else {
                commander.disableTurbo()
            }
        }

        // Animate the heart
        val scaleDown: ObjectAnimator = ObjectAnimator.ofPropertyValuesHolder(
            findViewById<View>(R.id.heart),
            PropertyValuesHolder.ofFloat("scaleX", 1.1f),
            PropertyValuesHolder.ofFloat("scaleY", 1.1f)
        )
        scaleDown.duration = 310
        scaleDown.repeatCount = ObjectAnimator.INFINITE
        scaleDown.repeatMode = ObjectAnimator.REVERSE
        scaleDown.start()

        // If bluetooth is not enabled, ask user to do so
        if (!bluetoothAdapter.isEnabled) {
            val enableBTIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBTIntent, requestEnableBluetooth)
        } else {
            initiateBluetoothProcess()
        }
    }

    @FlowPreview
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == requestEnableBluetooth) {
            initiateBluetoothProcess()
        }
    }

    @FlowPreview
    fun initiateBluetoothProcess() {
        if (bluetoothAdapter.isEnabled) {

            // Attempt to connect to the device
            val tmp: BluetoothSocket?
            bluetoothDevice = bluetoothAdapter.getRemoteDevice(moduleMac)

            // Create bluetooth socket
            try {
                tmp = bluetoothDevice?.createRfcommSocketToServiceRecord(myUuid)
                bluetoothSocket = tmp
                bluetoothSocket?.connect()

                Log.d("[BLUETOOTH]", "Connected to: " + bluetoothDevice?.name)
            } catch (e: IOException) {
                try {
                    bluetoothSocket!!.close()
                } catch (c: IOException) {
                    return
                }
            }
            Log.i("[BLUETOOTH]", "Creating handler")
            bluetoothHandler = object : Handler(Looper.getMainLooper()) {
                override fun handleMessage(msg: Message) {
                    if (msg.what == BluetoothThread.RESPONSE_MESSAGE) {
                        val txt = msg.obj as String
                        Log.d("[BLUETOOTH]", "Received: $txt")
                        logger.log("<<", txt)
                        val data = JSONObject(txt)

                        when (data.getString("_t")) {
                            "ir" -> {
                                device.setCurrent(data.getInt("pmc"))
                            }
                            "pr" -> {
                                device.setCurrent(data.getInt("c"))
                                pads.forEach {
                                    (name: String, pad: Pad) -> pad.setCycle(data.getInt("p${name}c"))
                                }
                                if (!commander.isBusy){
                                   pads.forEach { it.value.syncTargetCycle() }
                                }
                            }
                        }
                    }
                }
            }

            Log.d("[BLUETOOTH]", "Creating and running Thread")
            bluetoothThread = BluetoothThread(bluetoothSocket!!, bluetoothHandler!!)
            bluetoothThread!!.start()

            if (isBluetoothReady()) {
                commander.updateCycles(40)
            }
        }
    }

    private fun isBluetoothReady(): Boolean {
        return bluetoothSocket!!.isConnected && bluetoothThread != null
    }
}