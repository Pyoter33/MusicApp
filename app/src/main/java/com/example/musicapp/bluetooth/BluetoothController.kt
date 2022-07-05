package com.example.musicapp.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.musicapp.viewmodels.TrackListViewModel
import com.example.musicapp.views.fragments.TracksListFragment
import dagger.hilt.android.qualifiers.ActivityContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import androidx.fragment.app.activityViewModels


class BluetoothController @Inject constructor(@ActivityContext private val context: Context) {

    private val uuid = "00000000-0000-1000-8000-00805F9B34FB"

    fun onStartAcceptThread(bluetoothAdapter: BluetoothAdapter, handler: BluetoothHandler) {
        AcceptThread(bluetoothAdapter, uuid, handler).start()
    }

    fun onStartConnectThread(device: BluetoothDevice, handler: BluetoothHandler) {
        ConnectThread(device, uuid, handler).start()
    }

    fun onMessageReadHeader(message: Message, fos: FileOutputStream): String? {
            val array = message.obj as ByteArray
            if (!array.contains(-1)) return null
            var separatorIndex = 0
            for (i in array.indices) {
                if (array[i] == (-1).toByte()) {
                    separatorIndex = i
                    break
                }
            }
            val textByteArray = array.copyOfRange(0, separatorIndex)
            val dataByteArray = array.copyOfRange(separatorIndex + 1, array.size)

            val name = String(textByteArray.copyOf(), Charsets.UTF_8)
            Toast.makeText(context, "Downloading $name...", Toast.LENGTH_SHORT)
                .show()

            try {
                fos.write(dataByteArray)
            } catch (e: Exception) {
                Log.e("bluetooth", e.message!!)
            }
            return name
    }

    fun onMessageRead(message: Message, fos: FileOutputStream) {
        try {
            fos.write(message.obj as ByteArray)
        } catch (e: Exception) {
            Log.e("bluetooth", e.message!!)
        }
    }

    fun onMessageDisconnected(newFile: File, fos: FileOutputStream, name: String?) { //fix when sudden disconnect
        if (name == null) {
            return
        }
        newFile.copyTo(File(newFile.parent, name))
        newFile.delete()
        fos.close()
        Toast.makeText(
            context,
            "File saved in the music folder",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun onMessageWrite() {
        Toast.makeText(context, "Sending...", Toast.LENGTH_SHORT).show()
    }

    fun onMessageConnected(message: Message, path: String, handler: BluetoothHandler) {
        val socket = message.obj as BluetoothSocket
        val file = File(path)
        val fileName = file.name
        val sendThread =
            SendThread(socket, handler, file.readBytes(), fileName.toByteArray())
        sendThread.start()
    }

    fun onMessageAccepted(message: Message, handler: BluetoothHandler): File {
        val socket = message.obj as BluetoothSocket
        val downloadsDirectory = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_MUSIC)
        val newFile = File(
            downloadsDirectory,
            "temp"
        )
        if (!newFile.exists()) {
            newFile.createNewFile()
        }
        val thread = ReadThread(socket, handler)
        thread.start()
        return newFile
    }

    fun onMessageCantConnect(message: Message) {
        val textMessage = message.obj as String
        Toast.makeText(context, textMessage, Toast.LENGTH_SHORT).show()
    }

    fun onMessageEndWrite(message: Message) {
        val socket = message.obj as BluetoothSocket
        Toast.makeText(context, "Ended", Toast.LENGTH_SHORT).show()
        socket.close()
    }
}