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
import android.widget.Switch
import androidx.appcompat.widget.Toolbar
import kotlinx.coroutines.FlowPreview
import java.io.IOException
import java.util.*
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private val moduleMac = "00:18:E5:04:BF:63"
    private val requestEnableBluetooth = 1
    private val myUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

    private var bta: BluetoothAdapter? = null
    private var mmSocket: BluetoothSocket? = null
    private var mmDevice: BluetoothDevice? = null
    var btt: ConnectedThread? = null
    private var btnCopyLogs: Button? = null
    private var mHandler: Handler? = null
    var swTurbo: Switch? = null

    private val commander = Controller(this@MainActivity)
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
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        btnCopyLogs = findViewById(R.id.copy_logs)

        Log.i("[BLUETOOTH]", "Creating listeners")

        btnCopyLogs?.setOnClickListener {
            logger.copy()
        }

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

        bta = BluetoothAdapter.getDefaultAdapter()

        //if bluetooth is not enabled then create Intent for user to turn it on
        if (!bta!!.isEnabled) {
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
        if (bta!!.isEnabled) {

            //attempt to connect to bluetooth module
            val tmp: BluetoothSocket?
            mmDevice = bta!!.getRemoteDevice(moduleMac)

            //create socket
            try {
                tmp = mmDevice?.createRfcommSocketToServiceRecord(myUuid)
                mmSocket = tmp
                mmSocket?.connect()
                Log.i("[BLUETOOTH]", "Connected to: " + mmDevice?.name)
            } catch (e: IOException) {
                try {
                    mmSocket!!.close()
                } catch (c: IOException) {
                    return
                }
            }
            Log.i("[BLUETOOTH]", "Creating handler")
            mHandler = object : Handler(Looper.getMainLooper()) {
                override fun handleMessage(msg: Message) {
                    //super.handleMessage(msg);
                    if (msg.what == ConnectedThread.RESPONSE_MESSAGE) {
                        val txt = msg.obj as String
                        val data = JSONObject(txt)
                        val type = data.getString("_t")

                        when {
                            type === "ir" -> {
                                device.setCurrent(data.getInt("pmc"))
                            }
                            type === "pr" -> {
                                device.setCurrent(data.getInt("c"))
                                pads.forEach { (name: String, pad: Pad) -> pad.setCycle(data.getInt("p${name}c"))
                                }
                            }
                            type === "btn" -> {
                                commander.toggleTurbo()
                            }
                        }

                        logger.log("<<", txt)
                    }
                }
            }
            Log.i("[BLUETOOTH]", "Creating and running Thread")
            btt = ConnectedThread(mmSocket!!, mHandler!!)
            btt!!.start()
        }
    }

    private fun isBluetoothReady(): Boolean {
        return mmSocket!!.isConnected && btt != null
    }
}