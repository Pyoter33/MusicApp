package com.example.musicapp.views

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.R
import com.example.musicapp.adapters.TrackClickListener
import com.example.musicapp.adapters.TracksListAdapter
import com.example.musicapp.databinding.FragmentTracksListBinding
import com.example.musicapp.models.Track
import com.example.musicapp.viewmodels.TrackListViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TracksListFragment @Inject constructor() : Fragment(), TrackClickListener {

    private lateinit var binding: FragmentTracksListBinding
    @Inject lateinit var adapter: TracksListAdapter
    private val viewModel: TrackListViewModel by viewModels()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tracks_list, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewRecyclerTracks.adapter = adapter
        binding.viewRecyclerTracks.layoutManager = LinearLayoutManager(context)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        adapter.submitList(viewModel.list)
        adapter.setOnItemClickListener(this)
        onPlayPauseButtonClicked()
        onNextButtonClicked()
        onPreviousButtonClicked()
        observePositionToNotify()
    }

    private fun observePositionToNotify() {
        viewModel.positionToNotify.observe(viewLifecycleOwner) { position ->
            adapter.notifyItemChanged(position)
        }
    }

    private fun onNextButtonClicked() {
        binding.nextButton.setOnClickListener {
            viewModel.playNextTrack()
            binding.playPauseButton.isSelected = true
        }
    }

    private fun onPreviousButtonClicked() {
        binding.previousButton.setOnClickListener {
            viewModel.playPreviousTrack() //keep here or in xml onClick?
            binding.playPauseButton.isSelected = true //duplicate or put in observe?
        }
    }

    private fun onPlayPauseButtonClicked() {
        binding.playPauseButton.setOnClickListener { button ->
           button.isSelected = !button.isSelected
        }
    }

    override fun onClick(currentTrack: Track, position: Int) { //using interface or lambda?
        if(!currentTrack.playing) {
            if(!binding.layoutTrackController.isVisible) {
                binding.layoutTrackController.visibility = View.VISIBLE
            }
            viewModel.updateTracks(currentTrack, position)
            binding.playPauseButton.isSelected = true
        }
    }
}