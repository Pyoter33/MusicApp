package com.example.musicapp.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.example.musicapp.views.fragments.TracksListFragment
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


@SuppressLint("MissingPermission")
class AcceptThread(
    bluetoothAdapter: BluetoothAdapter,
    uuid: String,
    private val handler: BluetoothHandler
) : Thread() {

    private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
        bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
            "MusicApp",
            UUID.fromString(uuid)
        )
    }

    override fun run() {
        var shouldLoop = true
        var socket: BluetoothSocket? = null
        while (shouldLoop) {
            socket = try {
                mmServerSocket?.accept()
            } catch (e: IOException) {
                Log.e("bluetooth", "Socket's accept() method failed", e)
                shouldLoop = false
                mmServerSocket?.close()
                null
            }
            socket?.also {
                mmServerSocket?.close()
                shouldLoop = false
            }
        }
        socket?.let {
            handler.obtainMessage(TracksListFragment.MESSAGE_ACCEPTED, it).sendToTarget()
        }
    }
}

@SuppressLint("MissingPermission")
class ConnectThread(
    device: BluetoothDevice,
    uuid: String,
    private val handler: BluetoothHandler
) : Thread() {

    private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
        device.createRfcommSocketToServiceRecord(UUID.fromString(uuid))
    }

    override fun run() {
        try {
            mmSocket?.connect()
            handler.obtainMessage(TracksListFragment.MESSAGE_CONNECTED, mmSocket!!).sendToTarget()
        } catch (e: IOException) {
            handler.obtainMessage(
                TracksListFragment.MESSAGE_CANT_CONNECT,
                "Couldn't connect to the device!"
            ).sendToTarget()
        }
    }
}

class ReadThread(
    private val mmSocket: BluetoothSocket,
    private val handler: BluetoothHandler
) : Thread() {
    private val mmInStream: InputStream = mmSocket.inputStream

    override fun run() {
        var numBytes: Int

        while (true) {
            val available = mmInStream.available()
            val mmBuffer = if (available == 0) ByteArray(1) else if (available < 1024) ByteArray(available) else ByteArray(1024)
            numBytes = try {
                mmInStream.read(mmBuffer)
            } catch (e: IOException) {
                Log.d("bluetooth", "Input stream was disconnected", e)
                handler.obtainMessage(TracksListFragment.MESSAGE_DISCONNECTED)
                    .sendToTarget()
                break
            }
            if(numBytes != 0) {
                handler.obtainMessage(TracksListFragment.MESSAGE_READ, mmBuffer)
                    .sendToTarget()
            }
        }
    }

}

class SendThread(
    private val mmSocket: BluetoothSocket,
    private val handler: BluetoothHandler,
    private val data: ByteArray,
    private val header: ByteArray
) : Thread() {
    private val mmOutStream: OutputStream = mmSocket.outputStream

    override fun run() {
        handler.obtainMessage(
            TracksListFragment.MESSAGE_WRITE
        ).sendToTarget()

        try {
            val fullMessage = header + byteArrayOf(-1) + data
            mmOutStream.write(fullMessage)
        } catch (e: IOException) {
            Log.e("bluetooth", "Error occurred when sending data", e)
            return
        }

        handler.obtainMessage(
            TracksListFragment.MESSAGE_END_WRITE, mmSocket
        ).sendToTarget()
    }
}