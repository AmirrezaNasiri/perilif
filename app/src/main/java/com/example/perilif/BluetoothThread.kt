package com.example.perilif

import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.Message
import android.util.Log
import java.io.*

class BluetoothThread(mmSocket: BluetoothSocket, uih: Handler) : Thread() {
    private val mmInStream: InputStream?
    private val mmOutStream: OutputStream?
    private var uih: Handler
    override fun run() {
        //byte[] buffer = new byte[1024];
        //int bytes;
        val br = BufferedReader(InputStreamReader(mmInStream))
        Log.i("[THREAD-CT]", "Starting thread")
        while (true) {
            try {
                // bytes = mmInStream.read(buffer);
                val resp = br.readLine()
                //Transfer these data to the UI thread
                val msg = Message()
                msg.what = RESPONSE_MESSAGE
                msg.obj = resp
                uih.sendMessage(msg)
            } catch (e: IOException) {
                break
            }
        }
        Log.i("[THREAD-CT]", "While loop ended")
    }

    fun write(bytes: ByteArray?) {
        try {
            Log.i("[THREAD-CT]", "Writting bytes")
            mmOutStream!!.write(bytes)
        } catch (e: IOException) {
        }
    }

    companion object {
        const val RESPONSE_MESSAGE = 10
    }

    init {
        var tmpIn: InputStream? = null
        var tmpOut: OutputStream? = null
        this.uih = uih
        Log.i("[THREAD-CT]", "Creating thread")
        try {
            tmpIn = mmSocket.inputStream
            tmpOut = mmSocket.outputStream
        } catch (e: IOException) {
            Log.e("[THREAD-CT]", "Error:" + e.message)
        }
        mmInStream = tmpIn
        mmOutStream = tmpOut
        try {
            mmOutStream!!.flush()
            Log.i("[THREAD-CT]", "IO's obtained")
        } catch (e: IOException) {
        }

    }
}