package com.example.musicapp.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.R
import com.example.musicapp.databinding.FragmentTrackDetailsBinding
import com.example.musicapp.viewmodels.TrackListViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class TrackDetailsFragment @Inject constructor() : Fragment(), SeekBar.OnSeekBarChangeListener {

    private lateinit var binding: FragmentTrackDetailsBinding

    private val viewModel: TrackListViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_track_details, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindLayouts()
        observeResumePause()
        observeTrackProgression()
        onNextButtonClicked()
        onPreviousButtonClicked()
        onResumePauseButtonClicked()
        observeCurrentTrack()
        registerSeekBarCallbacks()
    }

    private fun observeCurrentTrack() {
        viewModel.currentTrack.observe(viewLifecycleOwner) {
            binding.textTrackLength.text = SimpleDateFormat("m:ss", Locale.ENGLISH).format(it?.length ?: 0)
        }
    }

    private fun stopObserveCurrentTrack() {
        viewModel.currentTrack.removeObservers(viewLifecycleOwner)
    }

    private fun bindLayouts() {
        binding.currentTrack = viewModel.currentTrack
        binding.lifecycleOwner = this
    }

    private fun observeTrackProgression() {
        viewModel.trackProgression.observe(viewLifecycleOwner) {
            binding.trackProgressSeekBar.progress = it
            binding.textTrackProgression.text = SimpleDateFormat("m:ss", Locale.ENGLISH).format(it)
        }
    }

    private fun observeResumePause() {
        viewModel.isCurrentPaused.observe(viewLifecycleOwner) {
            binding.resumePauseButton.isSelected = !it
        }
    }

    private fun onNextButtonClicked() {
        binding.nextButton.setOnClickListener {
            viewModel.playNextTrack()
        }
    }

    private fun onPreviousButtonClicked() {
        binding.previousButton.setOnClickListener {
            viewModel.playPreviousTrack()
        }
    }

    private fun onResumePauseButtonClicked() {
        binding.resumePauseButton.setOnClickListener {
            if(viewModel.isCurrentPaused.value!!) {
                viewModel.resumeTrack()
            } else {
                viewModel.pauseTrack()
            }
        }
    }

    private fun registerSeekBarCallbacks() {
        binding.trackProgressSeekBar.setOnSeekBarChangeListener(this)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if(fromUser) {
            viewModel.seekOnTrack(progress)
            binding.textTrackProgression.text = SimpleDateFormat("m:ss", Locale.ENGLISH).format(progress)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        stopObserveCurrentTrack()
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        observeCurrentTrack()
    }

}