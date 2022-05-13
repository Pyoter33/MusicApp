package com.example.musicapp

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.musicapp.services.MusicPlayerService
import com.example.musicapp.viewmodels.InsertTracksViewModel
import com.example.musicapp.viewmodels.TrackListViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    private val viewModel: TrackListViewModel by viewModels() //shared view model for future fragments

    private val insertTracksViewModel: InsertTracksViewModel by viewModels()

    private val closeActivityBroadcastReceiver = object : BroadcastReceiver() { //finish activity if service is stopped
        override fun onReceive(context: Context?, intent: Intent?) {
            finish()
        }
    }

    private val resumePauseBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.isCurrentPaused.value?.let { value ->
                if (value) {
                    viewModel.resumeTrack()
                } else {
                    viewModel.pauseTrack()
                }
            }
        }
    }

    private val nextTrackBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.playNextTrack()
        }
    }

    private val previousTrackBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.playPreviousTrack()
        }
    }

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            viewModel.registerListener()
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Toast.makeText(this@MainActivity, "Service stopped!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val closeActivityFilter = IntentFilter("android.intent.CLOSE_ACTIVITY")
        val resumePauseFilter = IntentFilter("android.intent.RESUME_PAUSE_TRACK")
        val previousTrackFilter = IntentFilter("android.intent.PLAY_PREVIOUS_TRACK")
        val nextTrackFilter = IntentFilter("android.intent.PLAY_NEXT_TRACK")

        registerReceiver(closeActivityBroadcastReceiver, closeActivityFilter)
        registerReceiver(resumePauseBroadcastReceiver, resumePauseFilter)
        registerReceiver(previousTrackBroadcastReceiver, previousTrackFilter)
        registerReceiver(nextTrackBroadcastReceiver, nextTrackFilter)

        checkPermissions()
        startService()
    }

    override fun onStart() {
        super.onStart()
        insertTracksViewModel.insertTracks()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(closeActivityBroadcastReceiver)
        unregisterReceiver(resumePauseBroadcastReceiver)
        unregisterReceiver(previousTrackBroadcastReceiver)
        unregisterReceiver(nextTrackBroadcastReceiver)
        unbindService(connection)
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

    private fun checkPermissions() {
        val permission = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "Permission Granted", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(this@MainActivity, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}