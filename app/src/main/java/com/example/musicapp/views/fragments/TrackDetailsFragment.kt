package com.example.musicapp.views.fragments

import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.musicapp.R
import com.example.musicapp.databinding.FragmentTrackDetailsBinding
import com.example.musicapp.models.ListViewTrack
import com.example.musicapp.presentation.theme.AppTheme
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
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_track_details, container, false)

        return binding.root
//        return ComposeView(requireContext()).apply {
//            val isDark =
//                resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
//            setContent {
//                AppTheme(isDarkTheme = isDark) {
//                    TrackDetailsView()
//                }
//            }
//        }
    }

    @Composable
    fun TrackDetailsView() {
        val currentTrack by viewModel.currentTrack.observeAsState()
        val trackProgression by viewModel.trackProgression.observeAsState()
        val isCurrentPaused by viewModel.isCurrentPaused.observeAsState()

        currentTrack?.let { TrackDetailsView(it, trackProgression, isCurrentPaused) }
            ?: findNavController().popBackStack()
    }

    @Composable
    fun TrackDetailsView(track: ListViewTrack, trackProgression: Int?, isCurrentPaused: Boolean?) {
        val orientation = LocalConfiguration.current.orientation

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            Column(modifier = Modifier.background(MaterialTheme.colors.background)) {
                Spacer(modifier = Modifier.height(100.dp))
                TrackImageView(track = track, 350, Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(32.dp))
                TrackInfoView(track = track)
                SliderView(track = track, trackProgression = trackProgression)
                ControllerView(isCurrentPaused = isCurrentPaused)
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.background(MaterialTheme.colors.background)
            ) {
                TrackImageView(track = track, size = 300, Modifier.padding(start = 16.dp))
                Column(verticalArrangement = Arrangement.Center) {
                    TrackInfoView(track = track)
                    SliderView(track = track, trackProgression = trackProgression)
                    ControllerView(isCurrentPaused = isCurrentPaused)
                }
            }
        }
    }

    @Composable
    fun TrackImageView(track: ListViewTrack, size: Int, modifier: Modifier) {
        Column(modifier = modifier) {
            if (track.imageByteArray.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeByteArray(
                    track.imageByteArray,
                    0,
                    track.imageByteArray.size
                ).asImageBitmap()

                Image(
                    bitmap = bitmap,
                    contentDescription = "Album cover",
                    modifier = Modifier
                        .size(size.dp)
                        .background(color = MaterialTheme.colors.primary)
                        .align(alignment = Alignment.CenterHorizontally)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.icon_music_note),
                    contentDescription = "Album cover",
                    modifier = Modifier
                        .size(size.dp)
                        .background(color = MaterialTheme.colors.primary)
                        .align(alignment = Alignment.CenterHorizontally)
                )
            }
        }
    }

    @Composable
    fun TrackInfoView(track: ListViewTrack) {
        val trackText = TextStyle(
            color = MaterialTheme.colors.onPrimary,
            fontSize = 21.sp,
            fontWeight = FontWeight.Normal
        )

        val trackTextArtist = trackText.copy(
            fontSize = 18.sp,
            color = MaterialTheme.colors.onSecondary
        )

        Text(text = track.name, style = trackText, modifier = Modifier.padding(start = 32.dp).semantics { testTag = "TrackInfoName" })
        Text(
            text = track.artist,
            style = trackTextArtist,
            modifier = Modifier.padding(start = 32.dp)
        )
    }

    @Composable
    fun SliderView(track: ListViewTrack, trackProgression: Int?) {
        val trackTextBold = TextStyle(
            color = MaterialTheme.colors.onPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        var isSeek by remember {
            mutableStateOf(false)
        }
        var sliderPosition: Float by rememberSaveable { mutableStateOf(0f) }

        if (!isSeek) {
            sliderPosition = trackProgression?.toFloat() ?: 0f
        }
        Spacer(Modifier.height(4.dp))

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Slider(
                value = sliderPosition,
                valueRange = 0f..track.length.toFloat(),
                onValueChange = {
                    sliderPosition = it
                    isSeek = true
                },
                onValueChangeFinished = {
                    viewModel.seekOnTrack(sliderPosition.toInt())
                    viewModel.setTrackProgression(sliderPosition.toInt())
                    isSeek = false
                },
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colors.primary,
                    activeTrackColor = MaterialTheme.colors.primary,
                    inactiveTrackColor = MaterialTheme.colors.primary.copy(
                        0.3f
                    )
                ),
                modifier = Modifier.height(30.dp)
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth(),
            ) {
                Text(
                    text = SimpleDateFormat("m:ss", Locale.ENGLISH).format(
                        sliderPosition
                    ), style = trackTextBold
                )
                Text(
                    text = SimpleDateFormat("m:ss", Locale.ENGLISH).format(
                        track.length
                    ), style = trackTextBold,
                    modifier = Modifier.semantics { testTag = "TrackInfoTime" }
                )
            }

        }
    }

    @Composable
    fun ControllerView(isCurrentPaused: Boolean?) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 50.dp, vertical = 8.dp)
        ) {
            IconButton(
                onClick = { viewModel.playPreviousTrack() },
                Modifier.semantics { testTag = "ButtonPreviousTrack" }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_previous_arrow),
                    contentDescription = "",
                    tint = MaterialTheme.colors.primary
                )
            }
            IconButton(
                onClick = { if (isCurrentPaused == true) viewModel.resumeTrack() else viewModel.pauseTrack() },
                Modifier.semantics { testTag = "ButtonResumePauseTrack" }
            ) {
                Icon(
                    painter = painterResource(id = if (isCurrentPaused == true) R.drawable.icon_play_arrow else R.drawable.icon_pause),
                    contentDescription = "",
                    tint = MaterialTheme.colors.primary
                )
            }
            IconButton(
                onClick = { viewModel.playNextTrack() },
                Modifier.semantics { testTag = "ButtonNextTrack" }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_next_arrow),
                    contentDescription = "",
                    tint = MaterialTheme.colors.primary
                )
            }
        }
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
        viewModel.currentTrack.observe(viewLifecycleOwner) { track ->
            binding.textTrackLength.text =
                SimpleDateFormat("m:ss", Locale.ENGLISH).format(track?.length ?: 0)
            track?.imageByteArray?.let {
                if (it.isNotEmpty()) {
                    Glide.with(this).asBitmap().load(it).into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            binding.viewImageTrack.setImageBitmap(resource)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {

                        }
                    })
                } else {
                    Glide.with(this).load(R.drawable.icon_music_note).into(binding.viewImageTrack)
                }
            }
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
            if (viewModel.isCurrentPaused.value!!) {
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
        if (fromUser) {
            binding.textTrackProgression.text =
                SimpleDateFormat("m:ss", Locale.ENGLISH).format(progress)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        stopObserveCurrentTrack()
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        viewModel.seekOnTrack(seekBar.progress)
        viewModel.setTrackProgression(seekBar.progress)
        observeCurrentTrack()
    }

}