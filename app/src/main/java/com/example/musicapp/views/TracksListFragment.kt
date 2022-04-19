package com.example.musicapp.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.R
import com.example.musicapp.adapters.TrackClickListener
import com.example.musicapp.adapters.TracksListAdapter
import com.example.musicapp.databinding.FragmentTracksListBinding
import com.example.musicapp.models.UITrack
import com.example.musicapp.viewmodels.TrackListViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class TracksListFragment @Inject constructor() : Fragment(), TrackClickListener {

    private lateinit var binding: FragmentTracksListBinding
    @Inject
    lateinit var adapter: TracksListAdapter
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
        binding.currentTrack = viewModel.currentTrack
        binding.lifecycleOwner = this

        onResumePauseButtonClicked()
        onNextButtonClicked()
        observeTrackProgression()
        onPreviousButtonClicked()
        observePositionToNotify()
        observeUITrackList()
    }

    private fun observeTrackProgression() {
        viewModel.trackProgression.observe(viewLifecycleOwner) {
            binding.trackProgressIndicator.progress = it
        }
    }

    private fun observeUITrackList() {
        viewModel.trackList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            adapter.onItemClicked(this)
        }
    }

    private fun observePositionToNotify() {
        viewModel.positionToNotify.observe(viewLifecycleOwner) { position ->
            adapter.notifyItemChanged(position)
        }
    }

    private fun onNextButtonClicked() {
        binding.nextButton.setOnClickListener {
            viewModel.playNextTrack()
            binding.resumePauseButton.isSelected = true
        }
    }

    private fun onPreviousButtonClicked() {
        binding.previousButton.setOnClickListener {
            viewModel.playPreviousTrack()
            binding.resumePauseButton.isSelected = true
        }
    }

    private fun onResumePauseButtonClicked() {
        binding.resumePauseButton.setOnClickListener { button ->
            viewModel.resumePauseTrack()
            button.isSelected = !button.isSelected
        }
    }

    override fun onClick(currentUITrack: UITrack, position: Int) {
        if (!binding.layoutTrackController.isVisible) {
            binding.layoutTrackController.visibility = View.VISIBLE
        }
        viewModel.updateTracks(currentUITrack, position)
        binding.resumePauseButton.isSelected = true
    }
}