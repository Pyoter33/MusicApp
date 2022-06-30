package com.example.musicapp.views.dialogs

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.adapters.BluetoothDevicesListAdapter
import com.example.musicapp.databinding.DialogDevicesBinding
import com.example.musicapp.viewmodels.TrackListViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BluetoothDevicesDialog @Inject constructor(private val bluetoothDeviceClickListener: BluetoothDeviceClickListener, private val viewModel: TrackListViewModel) :
    DialogFragment() {

    private lateinit var builder: AlertDialog.Builder
    @Inject lateinit var adapter: BluetoothDevicesListAdapter

    @SuppressLint("MissingPermission")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val viewRecycler = DialogDevicesBinding.inflate(requireActivity().layoutInflater)
        viewRecycler.viewRecyclerDevices.adapter = adapter
        viewRecycler.viewRecyclerDevices.layoutManager = LinearLayoutManager(context)

        viewModel.devices.observe(this) { devices ->
            Log.i("bluetooth", "observe")
            adapter.submitList(devices)
            adapter.onItemClicked(bluetoothDeviceClickListener)
        }

        builder = AlertDialog.Builder(activity)
        val dialog = AlertDialog.Builder(activity).create()

        dialog.setTitle("Connect to a bluetooth device:")
        dialog.setView(viewRecycler.root)

        return dialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.devices.removeObservers(this)
        viewModel.resetBluetoothDevices()
    }
}

interface BluetoothDeviceClickListener {
    fun onClick(device: BluetoothDevice)
}