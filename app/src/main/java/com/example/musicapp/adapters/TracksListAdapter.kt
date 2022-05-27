package com.example.musicapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.databinding.ItemTrackBinding
import com.example.musicapp.models.ListViewTrack
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class TracksListAdapter @Inject constructor() :
    ListAdapter<ListViewTrack, TracksListAdapter.TrackViewHolder>(TrackDiffUtil()) {

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
        fun bind(currentListViewTrack: ListViewTrack) {
            binding.itemTrackLayout.isSelected = currentListViewTrack.isPlaying
            binding.textTrackName.text = currentListViewTrack.name
            binding.textTrackArtist.text = currentListViewTrack.artist
            binding.textTrackLength.text = SimpleDateFormat("m:ss", Locale.ENGLISH).format(currentListViewTrack.length)
            currentListViewTrack.imageByteArray.let {
                if (it.isNotEmpty()) {
                    Glide.with(itemView).load(it).into(binding.viewImageTrack)
                } else {
                    Glide.with(itemView).load(R.drawable.icon_music_note).into(binding.viewImageTrack)
                }
            }

            binding.root.setOnClickListener {
                listener.onClick(adapterPosition)
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

class TrackDiffUtil : DiffUtil.ItemCallback<ListViewTrack>() {
    override fun areItemsTheSame(oldItem: ListViewTrack, newItem: ListViewTrack): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: ListViewTrack, newItem: ListViewTrack): Boolean {
        return oldItem.id == newItem.id
    }
}

interface TrackClickListener {
    fun onClick(position: Int)
}