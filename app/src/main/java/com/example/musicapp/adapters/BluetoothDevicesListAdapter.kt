package com.example.musicapp.adapters

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.databinding.ItemDeviceBinding
import com.example.musicapp.databinding.ItemTrackBinding
import com.example.musicapp.models.ListViewTrack
import com.example.musicapp.views.dialogs.BluetoothDeviceClickListener
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class BluetoothDevicesListAdapter @Inject constructor() : ListAdapter<BluetoothDevice, BluetoothDevicesListAdapter.BluetoothDeviceViewHolder>(BluetoothDeviceDiffUtil()) {

    private lateinit var onItemClickedListener: BluetoothDeviceClickListener

    fun onItemClicked(listener: BluetoothDeviceClickListener) {
        onItemClickedListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BluetoothDeviceViewHolder {
        return BluetoothDeviceViewHolder.create(parent, onItemClickedListener)
    }

    override fun onBindViewHolder(holder: BluetoothDeviceViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    @SuppressLint("MissingPermission")
    class BluetoothDeviceViewHolder(
        private val binding: ItemDeviceBinding,
        private val listener: BluetoothDeviceClickListener
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(device: BluetoothDevice) {
            binding.textDeviceName.text = device.name
            binding.itemDeviceLayout.setOnClickListener {
                listener.onClick(device)
            }
        }

        companion object {
            fun create(parent: ViewGroup, listener: BluetoothDeviceClickListener): BluetoothDeviceViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemDeviceBinding.inflate(layoutInflater, parent, false)
                return BluetoothDeviceViewHolder(
                    binding, listener
                )
            }
        }
    }
}

class BluetoothDeviceDiffUtil : DiffUtil.ItemCallback<BluetoothDevice>() {
    override fun areItemsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean {
        return oldItem.address == newItem.address
    }
}
