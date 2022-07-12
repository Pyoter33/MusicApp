package com.example.musicapp.views.fragments

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.MainActivity
import com.example.musicapp.R
import com.example.musicapp.adapters.TrackClickListener
import com.example.musicapp.adapters.TracksListAdapter
import com.example.musicapp.bluetooth.*
import com.example.musicapp.databinding.FragmentTracksListBinding
import com.example.musicapp.models.ListViewTrack
import com.example.musicapp.presentation.theme.AppTheme
import com.example.musicapp.viewmodels.TrackListViewModel
import com.example.musicapp.views.dialogs.BluetoothDeviceClickListener
import com.example.musicapp.views.dialogs.BluetoothDevicesDialog
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@SuppressLint("MissingPermission")
@AndroidEntryPoint
class TracksListFragment @Inject constructor() : Fragment(), TrackClickListener,
    BluetoothDeviceClickListener {

    companion object {
        const val MESSAGE_READ = 1
        const val MESSAGE_WRITE = 2
        const val MESSAGE_CONNECTED = 3
        const val MESSAGE_ACCEPTED = 4
        const val MESSAGE_CANT_CONNECT = 5
        const val MESSAGE_DISCONNECTED = 6
        const val MESSAGE_END_WRITE = 7
    }

    private lateinit var binding: FragmentTracksListBinding
    private lateinit var dialog: BluetoothDevicesDialog
    private lateinit var bluetoothAdapter: BluetoothAdapter

    @Inject
    lateinit var adapter: TracksListAdapter

    @Inject
    lateinit var handler: BluetoothHandler

    @Inject
    lateinit var path: String

    @Inject
    lateinit var bluetoothController: BluetoothController

    private val viewModel: TrackListViewModel by activityViewModels() //getting shared view model
    private var trackToSend = -1

    private val foundReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action!!) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    if (device.name != null) {
                        viewModel.addBluetoothDevice(device)
                    }
                }
            }
        }
    }

    private val connectedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action!!) {
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    if (trackToSend == -1) {
                        bluetoothController.onStartAcceptThread(bluetoothAdapter, handler)
                    }
                }
            }
        }
    }

    private val simpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            onSwipe(viewHolder.adapterPosition)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tracks_list, container, false)
        return binding.root
//        return ComposeView(requireContext()).apply {
//            val isDark =
//                resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
//
//            setContent {
//                AppTheme(isDarkTheme = isDark) {
//                    TrackListView()
//                }
//            }
//        }
    }

    @Composable
    fun TrackListView() {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.background(MaterialTheme.colors.background)
        ) {
            TracksObserver(Modifier.weight(1f, fill = false))
            val currentTrack by viewModel.currentTrack.observeAsState()
            currentTrack?.let {
                TrackController(track = it)
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
    @Composable
    fun TrackItem(track: ListViewTrack, index: Int, onClickListener: TrackClickListener) {
        val trackText = TextStyle(
            color = MaterialTheme.colors.onPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal
        )

        val trackTextBold = trackText.copy(fontWeight = FontWeight.Bold)

        val trackTextArtist = trackText.copy(
            fontSize = 14.sp,
            color = MaterialTheme.colors.onSecondary
        )
        val dismissState = rememberDismissState(initialValue = DismissValue.Default)
        if (dismissState.isDismissed(DismissDirection.EndToStart)) {
            onSwipe(index)
        }

        SwipeToDismiss(
            state = dismissState,
            modifier = Modifier.padding(vertical = 4.dp),
            directions = setOf(DismissDirection.EndToStart),
            dismissThresholds = { direction ->
                FractionalThreshold(if (direction == DismissDirection.StartToEnd) 0.25f else 0.5f)
            },
            background = {
              dismissState.dismissDirection ?: return@SwipeToDismiss
                animateColorAsState(
                    when (dismissState.targetValue) {
                        DismissValue.Default -> Color.LightGray
                        DismissValue.DismissedToEnd -> Color.Green
                        DismissValue.DismissedToStart -> Color.Red
                    }
                )
            },
            dismissContent = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .border(
                            width = 1.dp,
                            color = if (track.isPlayingState) MaterialTheme.colors.secondary else MaterialTheme.colors.primary,
                            RoundedCornerShape(corner = CornerSize(10.dp))
                        )
                        .background(
                            color = if (track.isPlayingState) MaterialTheme.colors.primary else MaterialTheme.colors.secondary,
                            RoundedCornerShape(corner = CornerSize(10.dp))
                        )
                        .combinedClickable(
                            onClick = { onClickListener.onClick(index) },
                            onLongClick = { onClickListener.onLongClick(index) }
                        )
                )
                {
                    Row(
                        modifier = Modifier
                            .padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(color = MaterialTheme.colors.background)
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.icon_music_note),
                                    contentDescription = "Album cover",
                                    colorFilter = ColorFilter.tint(MaterialTheme.colors.onBackground),
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(color = MaterialTheme.colors.background)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = track.name, style = trackText)
                                Text(text = track.artist, style = trackTextArtist)
                            }
                        }
                        Text(
                            text = SimpleDateFormat("m:ss", Locale.ENGLISH).format(track.length),
                            style = trackTextBold,
                            modifier = Modifier.padding(end = 20.dp)
                        )
                    }
                }
            }
        )
    }

    @Composable
    fun TracksObserver(modifier: Modifier) {
        val trackList by viewModel.trackList.observeAsState()

        trackList?.let {
            TrackList(trackList = it, modifier)
        }
    }

    @Composable
    fun TrackList(trackList: List<ListViewTrack>, modifier: Modifier) {
        LazyColumn(modifier = modifier.padding(horizontal = 8.dp)) {
            itemsIndexed(items = trackList, key = {_, track -> track.id}) { index, track ->
                Spacer(
                    modifier = Modifier
                        .height(4.dp)
                )
                TrackItem(track, index, this@TracksListFragment)
            }
        }
    }

    val DrawableId = SemanticsPropertyKey<Int>("DrawableResId") //for tests
    var SemanticsPropertyReceiver.drawableId by DrawableId

    @Composable
    fun TrackController(track: ListViewTrack) {
        val trackProgression by viewModel.trackProgression.observeAsState()
        val isCurrentPaused by viewModel.isCurrentPaused.observeAsState()
        val trackTextBold = TextStyle(
            color = MaterialTheme.colors.onSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        val trackTextArtist = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 17.sp,
            color = MaterialTheme.colors.onSecondary
        )

        Surface(
            elevation = 20.dp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(
                    color = MaterialTheme.colors.surface,
                )
                .clickable {
                    findNavController().navigate(TracksListFragmentDirections.actionTracksListFragmentToTrackDetailsFragment())
                }.semantics { testTag = "TrackController" }
        )
        {
            Column {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                ) {
                    Column(
                        Modifier
                            .padding(start = 12.dp, top = 4.dp)
                            .weight(1f, fill = false)
                    ) {
                        Text(
                            text = track.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = trackTextBold,
                            modifier = Modifier.semantics { testTag = "TrackControllerName" }
                        )
                        Text(
                            text = track.artist,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = trackTextArtist
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.playPreviousTrack() },
                            Modifier.padding(start = 4.dp, top = 4.dp, end = 4.dp).semantics { testTag = "ButtonPreviousTrack" }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_previous_arrow),
                                contentDescription = "",
                                tint = MaterialTheme.colors.primary
                            )
                        }
                        IconButton(
                            onClick = { if (isCurrentPaused == true) viewModel.resumeTrack() else viewModel.pauseTrack() },
                            Modifier.padding(start = 4.dp, top = 4.dp, end = 4.dp).semantics { testTag = "ButtonResumePause"}
                        ) {
                            val resource = if (isCurrentPaused == true) R.drawable.icon_play_arrow else R.drawable.icon_pause
                            Log.i("test", resource.toString())
                            Icon(
                                painter = painterResource(id = resource),
                                contentDescription = "AAAA",
                                tint = MaterialTheme.colors.primary,
                                modifier = Modifier.semantics { drawableId = resource}
                            )
                        }
                        IconButton(
                            onClick = { viewModel.playNextTrack() },
                            Modifier.padding(start = 4.dp, top = 4.dp, end = 8.dp).semantics { testTag = "ButtonNextTrack" }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_next_arrow),
                                contentDescription = "",
                                tint = MaterialTheme.colors.primary
                            )
                        }
                    }
                }

                LinearProgressIndicator(
                    progress = trackProgression?.toFloat()?.div(track.length) ?: 0f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(
                            color = MaterialTheme.colors.primary.copy(
                                alpha = 0.3f
                            )
                        ),
                    color = MaterialTheme.colors.primary
                )
            }
        }
    }

    //------------------------------------------------------------------
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindLayouts()
        onResumePauseButtonClicked()
        onNextButtonClicked()
        observeTrackProgression()
        onPreviousButtonClicked()
        observePositionToNotify()
        observeUITrackList()
        observeResumePause()
        onTrackControllerLayoutClicked()
        setHandlerListener()

        bluetoothAdapter = (requireActivity() as MainActivity).bluetoothAdapter
        val foundFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        requireActivity().registerReceiver(foundReceiver, foundFilter)
        val connectedFilter = IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED)
        requireActivity().registerReceiver(connectedReceiver, connectedFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unregisterReceiver(foundReceiver)
        requireActivity().unregisterReceiver(connectedReceiver)
    }


    private fun bindLayouts() {
        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(binding.viewRecyclerTracks)
        binding.viewRecyclerTracks.adapter = adapter
        binding.viewRecyclerTracks.layoutManager = LinearLayoutManager(context)
        binding.currentTrack = viewModel.currentTrack
        binding.lifecycleOwner = this
    }

    private fun observeTrackProgression() {
        viewModel.trackProgression.observe(viewLifecycleOwner) {
            binding.trackProgressSeekBar.progress = it
        }
    }

    private fun observeResumePause() {
        viewModel.isCurrentPaused.observe(viewLifecycleOwner) {
            binding.resumePauseButton.isSelected = !it
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
        }
    }

    private fun onPreviousButtonClicked() {
        binding.previousButton.setOnClickListener {
            viewModel.playPreviousTrack()
        }
    }

    private fun onResumePauseButtonClicked() {
        binding.resumePauseButton.setOnClickListener {
            viewModel.isCurrentPaused.value?.let { value ->
                if (value) {
                    viewModel.resumeTrack()
                } else {
                    viewModel.pauseTrack()
                }
            }
        }
    }

    private fun onTrackControllerLayoutClicked() {
        binding.layoutTrackController.setOnClickListener {
            findNavController().navigate(TracksListFragmentDirections.actionTracksListFragmentToTrackDetailsFragment())
        }
    }

    private fun setHandlerListener() {
        var newFile: File? = null
        var fos: FileOutputStream? = null
        var hasHeader = true
        var name: String? = null
        handler.setOnMessageReceiveListener {
            when (it.what) {
                MESSAGE_READ -> {
                    if (hasHeader) {
                        name = bluetoothController.onMessageReadHeader(it, fos!!)
                        if (name != null) {
                            hasHeader = false
                        }
                    } else {
                        bluetoothController.onMessageRead(it, fos!!)
                    }
                }
                MESSAGE_DISCONNECTED -> {
                    bluetoothController.onMessageDisconnected(newFile!!, fos!!, name)
                    newFile = null
                    fos = null
                    hasHeader = true
                    name = null
                }
                MESSAGE_WRITE -> {
                    bluetoothController.onMessageWrite()
                }
                MESSAGE_CONNECTED -> {
                    val trackPath = viewModel.trackList.value!![trackToSend].path
                    bluetoothController.onMessageConnected(it, trackPath, handler)
                    trackToSend = -1
                }
                MESSAGE_ACCEPTED -> {
                    newFile = bluetoothController.onMessageAccepted(it, handler)
                    fos = FileOutputStream(newFile)
                }
                MESSAGE_CANT_CONNECT -> {
                    bluetoothController.onMessageCantConnect(it)
                    trackToSend = -1
                }
                MESSAGE_END_WRITE -> {
                    bluetoothController.onMessageEndWrite(it)
                }
            }
        }
    }

    override fun onClick(position: Int) {
        viewModel.updateTracks(position)
    }

    override fun onLongClick(position: Int) {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1)
            return
        }
        trackToSend = position
        bluetoothAdapter.startDiscovery()
        dialog = BluetoothDevicesDialog(this, viewModel, bluetoothAdapter.bondedDevices.toList())
        dialog.show(childFragmentManager, "BluetoothDevicesDialog")
    }

    override fun onSwipe(position: Int) {
        val path = viewModel.trackList.value!![position].path
        val fileToDelete = File(path)
        fileToDelete.delete()
    }

    override fun onClick(device: BluetoothDevice) {
        bluetoothAdapter.cancelDiscovery()
        dialog.dismiss()
        bluetoothController.onStartConnectThread(device, handler)
    }
}

