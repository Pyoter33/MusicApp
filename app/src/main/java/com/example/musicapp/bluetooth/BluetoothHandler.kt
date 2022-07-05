package com.example.musicapp.bluetooth

import android.os.Handler
import android.os.Looper
import android.os.Message
import javax.inject.Inject

class BluetoothHandler @Inject constructor() : Handler(Looper.getMainLooper()) {

    private var messageReceive: (Message) -> Unit = {}

    override fun handleMessage(msg: Message) {
        messageReceive(msg)
    }

    fun setOnMessageReceiveListener(action: (Message) -> Unit) {
        messageReceive = action
    }

}