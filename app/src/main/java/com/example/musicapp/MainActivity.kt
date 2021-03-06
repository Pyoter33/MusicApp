package com.example.musicapp

import android.Manifest
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.*
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.musicapp.models.ListViewTrack
import com.example.musicapp.services.MusicPlayerService
import com.example.musicapp.viewmodels.TrackListViewModel
import com.example.musicapp.viewmodels.UpdateTracksViewModel
import com.example.musicapp.views.fragments.TrackDetailsFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_PERMISSIONS = 1
        private val PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    lateinit var bluetoothAdapter: BluetoothAdapter

    @Inject
    lateinit var path: String
    private lateinit var navController: NavController
    private val trackListViewModel: TrackListViewModel by viewModels() //shared view model for future fragments
    private val updateTracksViewModel: UpdateTracksViewModel by viewModels()

    private val closeActivityBroadcastReceiver =
        object : BroadcastReceiver() { //finish activity if service is stopped
            override fun onReceive(context: Context?, intent: Intent?) {
                finish()
            }
        }

    private val resumePauseBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            trackListViewModel.isCurrentPaused.value?.let { value ->
                if (value) {
                    trackListViewModel.resumeTrack()
                } else {
                    trackListViewModel.pauseTrack()
                }
            }
        }
    }

    private val nextTrackBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            trackListViewModel.playNextTrack()
        }
    }

    private val previousTrackBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            trackListViewModel.playPreviousTrack()
        }
    }

    private val reloadTracksBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateTracksViewModel.updateTracks(path)
        }

    }

    private val deleteTracksBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateTracksViewModel.deleteTracks(path)
        }
    }

    private val headsetPlugBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent!!.extras!!.getInt("state")) {
                0 -> {
                    trackListViewModel.pauseTrack()
                }
            }
        }
    }

    private val bluetoothHeadsetPlugBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent!!.extras!!.get(BluetoothProfile.EXTRA_STATE)) {
                BluetoothProfile.STATE_DISCONNECTED -> {
                    trackListViewModel.pauseTrack()
                }
            }
        }
    }

    private val connection = object : ServiceConnection {
        var isConnected = false

        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            trackListViewModel.registerListener()
            isConnected = true
            startTrackFromIntent()
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            isConnected = false
            Toast.makeText(this@MainActivity, "Service stopped!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermissions()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.findNavController()

        setupActionBarWithNavController(navController)
        val closeActivityFilter = IntentFilter("android.intent.CLOSE_ACTIVITY")
        val resumePauseFilter = IntentFilter("android.intent.RESUME_PAUSE_TRACK")
        val previousTrackFilter = IntentFilter("android.intent.PLAY_PREVIOUS_TRACK")
        val nextTrackFilter = IntentFilter("android.intent.PLAY_NEXT_TRACK")
        val reloadTracksFilter = IntentFilter("android.intent.RELOAD_TRACKS")
        val deleteTracksFilter = IntentFilter("android.intent.DELETE_TRACKS")
        val headsetPlugFilter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        val bluetoothHeadsetPlugFilter = IntentFilter(ACTION_CONNECTION_STATE_CHANGED)

        if (connection.isConnected) {
            startTrackFromIntent()
        }

        registerReceiver(closeActivityBroadcastReceiver, closeActivityFilter)
        registerReceiver(resumePauseBroadcastReceiver, resumePauseFilter)
        registerReceiver(previousTrackBroadcastReceiver, previousTrackFilter)
        registerReceiver(nextTrackBroadcastReceiver, nextTrackFilter)
        registerReceiver(reloadTracksBroadcastReceiver, reloadTracksFilter)
        registerReceiver(deleteTracksBroadcastReceiver, deleteTracksFilter)
        registerReceiver(headsetPlugBroadcastReceiver, headsetPlugFilter)
        registerReceiver(bluetoothHeadsetPlugBroadcastReceiver, bluetoothHeadsetPlugFilter)
        startService()
        getBluetoothAdapter()

    }

    override fun onStart() {
        super.onStart()
        insertTracksIfPermissionGranted()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(closeActivityBroadcastReceiver)
        unregisterReceiver(resumePauseBroadcastReceiver)
        unregisterReceiver(previousTrackBroadcastReceiver)
        unregisterReceiver(nextTrackBroadcastReceiver)
        unregisterReceiver(reloadTracksBroadcastReceiver)
        unregisterReceiver(deleteTracksBroadcastReceiver)
        unregisterReceiver(headsetPlugBroadcastReceiver)
        unregisterReceiver(bluetoothHeadsetPlugBroadcastReceiver)
        unbindService(connection)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateTracksViewModel.updateTracks(path)
            }
        }
    }

    private fun startTrackFromIntent() {
        val track = intent.data?.let {
            getIntentTrack(it)
        }?.let {
            initializeIntentTrack(it)
        }
    }

    private fun getIntentTrack(uri: Uri): ListViewTrack {
        val mmr = MediaMetadataRetriever()
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor!!.moveToFirst()
        val idx = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA)
        val path = cursor.getString(idx)
        mmr.setDataSource(path)
        val title =
            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: path.split("/")
                .last().substringBefore('.')
        val artist =
            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unknown"
        val lengthString = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val length = Integer.parseInt(lengthString!!)
        return ListViewTrack(0, title, artist, length, path, mmr.embeddedPicture ?: byteArrayOf())
    }

    private fun initializeIntentTrack(track: ListViewTrack) {
        trackListViewModel.updateTrack(track)
        val fr = TrackDetailsFragment()
        val fm = supportFragmentManager
        val fragmentTransaction = fm.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainerView, fr)
        fragmentTransaction.commit()

       setIntentTrackOnBackPress()
    }

    private fun setIntentTrackOnBackPress(){
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val closeIntent = Intent("android.intent.CLOSE_ACTIVITY")
                PendingIntent.getBroadcast(
                    this@MainActivity,
                    0,
                    closeIntent,
                    PendingIntent.FLAG_IMMUTABLE
                ).send()
            }
        })
    }

    private fun getBluetoothAdapter() {
        val bluetoothManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getSystemService(BluetoothManager::class.java)
        } else {
            getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        }

        bluetoothAdapter = bluetoothManager.adapter
    }

    private fun insertTracksIfPermissionGranted() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            updateTracksViewModel.updateTracks(path)
        }
    }

    private fun startService() {
        Intent(this, MusicPlayerService::class.java).also { intent ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                ContextCompat.startForegroundService(this, intent);
            }
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun hasPermissions(context: Context, vararg permissions: String): Boolean =
        permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    private fun checkPermissions() {
        if (!hasPermissions(this, *PERMISSIONS)
        ) {
            ActivityCompat.requestPermissions(
                this,
                PERMISSIONS,
                REQUEST_PERMISSIONS
            )
        }
    }
}