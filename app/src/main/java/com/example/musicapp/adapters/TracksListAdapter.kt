package com.example.musicapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.databinding.ItemTrackBinding
import com.example.musicapp.models.UITrack
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class TracksListAdapter @Inject constructor() :
    ListAdapter<UITrack, TracksListAdapter.TrackViewHolder>(TrackDiffUtil()) {

    private lateinit var onItemClickedListener: TrackClickListener

    fun onItemClicked(listener: TrackClickListener) {
        onItemClickedListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        return TrackViewHolder.create(parent, onItemClickedListener)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class TrackViewHolder(
        private val binding: ItemTrackBinding,
        private val listener: TrackClickListener
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(currentUITrack: UITrack) {
            binding.itemTrackLayout.isSelected = currentUITrack.isPlaying
            binding.textTrackName.text = currentUITrack.name
            binding.textTrackArtist.text = currentUITrack.artist
            binding.textTrackLength.text = SimpleDateFormat("m:ss", Locale.ENGLISH).format(currentUITrack.length * 1000)

            binding.root.setOnClickListener {
                listener.onClick(currentUITrack, bindingAdapterPosition)
            }
        }

        companion object {
            fun create(parent: ViewGroup, listener: TrackClickListener): TrackViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemTrackBinding.inflate(layoutInflater, parent, false)
                return TrackViewHolder(
                    binding, listener
                )

            }
        }
    }
}

class TrackDiffUtil : DiffUtil.ItemCallback<UITrack>() {
    override fun areItemsTheSame(oldItem: UITrack, newItem: UITrack): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: UITrack, newItem: UITrack): Boolean {
        return oldItem.id == newItem.id
    }
}

interface TrackClickListener {
    fun onClick(currentUITrack: UITrack, position: Int)
}