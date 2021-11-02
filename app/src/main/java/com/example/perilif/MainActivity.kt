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
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import java.io.IOException
import java.util.*
import org.json.JSONObject




class MainActivity : AppCompatActivity() {
    val MODULE_MAC = "00:18:E5:04:BF:63"
    val REQUEST_ENABLE_BT = 1
    private val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

    var bta: BluetoothAdapter? = null
    var mmSocket: BluetoothSocket? = null
    var mmDevice: BluetoothDevice? = null
    var btt: ConnectedThread? = null
    private var btnCopyLogs: Button? = null
    private var switchLight: Button? = null
    private var switchRelay: Button? = null

    var lightflag = false
    var relayFlag = true
    var mHandler: Handler? = null

    private val logger: Logger = Logger(this@MainActivity)
    private val toast: Toast = Toast(this@MainActivity)

    private var maxCurrent: Int = 0;
    private var lastCurrent: Int = 0;

    /*private var device = mapOf("maxCurrent" to 0, "lastCurrent" to 0, "turbo" to false)
    private var padA = mapOf("maxCurrent" to 0, "lastCurrent" to 0, "")*/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        btnCopyLogs = findViewById(R.id.copy_logs)
        switchLight = findViewById(R.id.switchlight)
        switchRelay = findViewById(R.id.relay)

        Log.i("[BLUETOOTH]", "Creating listeners")

        btnCopyLogs?.setOnClickListener {
            logger.copy()
        }

        switchLight?.setOnClickListener {
            Log.i("[BLUETOOTH]", "Attempting to send data")
            if (mmSocket!!.isConnected && btt != null) { //if we have connection to the bluetoothmodule
                lightflag = if (!lightflag) {
                    val sendtxt = "LY"
                    btt!!.write(sendtxt.toByteArray())
                    true
                } else {
                    val sendtxt = "LN"
                    btt!!.write(sendtxt.toByteArray())
                    false
                }
            } else {
                toast.show("Something went wrong!")
            }
        }
        switchRelay?.setOnClickListener {
            Log.i("[BLUETOOTH]", "Attempting to send data")
            if (mmSocket!!.isConnected && btt != null) { //if we have connection to the bluetoothmodule
                relayFlag = if (relayFlag) {
                    val sendtxt = "RY"
                    btt!!.write(sendtxt.toByteArray())
                    false
                } else {
                    val sendtxt = "RN"
                    btt!!.write(sendtxt.toByteArray())
                    true
                }

                //disable the button and wait for 4 seconds to enable it again
                switchRelay?.setEnabled(false)
                Thread(Runnable {
                    try {
                        Thread.sleep(4000)
                    } catch (e: InterruptedException) {
                        return@Runnable
                    }
                    runOnUiThread { switchRelay?.setEnabled(true) }
                }).start()
            } else {
                toast.show("Something went wrong!")
            }
        }
        bta = BluetoothAdapter.getDefaultAdapter()

        //if bluetooth is not enabled then create Intent for user to turn it on
        if (!bta!!.isEnabled()) {
            val enableBTIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT)
        } else {
            initiateBluetoothProcess()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_ENABLE_BT) {
            initiateBluetoothProcess()
        }
    }

    fun initiateBluetoothProcess() {
        if (bta!!.isEnabled) {

            //attempt to connect to bluetooth module
            val tmp: BluetoothSocket?
            mmDevice = bta!!.getRemoteDevice(MODULE_MAC)

            //create socket
            try {
                tmp = mmDevice?.createRfcommSocketToServiceRecord(MY_UUID)
                mmSocket = tmp
                mmSocket?.connect()
                Log.i("[BLUETOOTH]", "Connected to: " + mmDevice?.getName())
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

                        if (type === "ir") {
                            maxCurrent = data.getInt("pmc");
                        } else if (type === "pr") {
                            // lastCurrent = data.getInt("c");
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
}