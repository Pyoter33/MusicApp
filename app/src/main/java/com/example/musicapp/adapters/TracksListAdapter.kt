package com.example.musicapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.databinding.ItemTrackBinding
import com.example.musicapp.models.Track
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TracksListAdapter @Inject constructor() :
    ListAdapter<Track, TracksListAdapter.TrackViewHolder>(TrackDiffUtil()) {

    private lateinit var onItemClickListener: TrackClickListener

    fun setOnItemClickListener(listener: TrackClickListener) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        return TrackViewHolder.create(parent, onItemClickListener)
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
        fun bind(currentTrack: Track) {
            binding.itemTrackLayout.isSelected = currentTrack.playing
            binding.textTrackName.text = currentTrack.name
            binding.textTrackArtist.text = currentTrack.artist
            binding.textTrackLength.text = currentTrack.length

            binding.root.setOnClickListener {
                listener.onClick(currentTrack, adapterPosition)
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

class TrackDiffUtil : DiffUtil.ItemCallback<Track>() {
    override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
        return oldItem.id == newItem.id
    }
}

interface TrackClickListener {
    fun onClick(currentTrack: Track, position: Int)
}