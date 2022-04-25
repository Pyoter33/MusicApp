package com.example.musicapp

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.musicapp.services.MusicPlayerServiceImpl
import com.example.musicapp.services.ServiceBinder
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

    private val viewModel: TrackListViewModel by viewModels() //shared view model for service binding and possibly future fragments

    private val broadcastReceiver = object : BroadcastReceiver() { //finish activity if service is stopped
        override fun onReceive(context: Context?, intent: Intent?) {
            finish()
        }
    }

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            viewModel.setService((iBinder as (ServiceBinder)).getService()) //bind service and pass it to view model
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            viewModel.setService(null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val filter = IntentFilter("android.intent.CLOSE_ACTIVITY")
        registerReceiver(broadcastReceiver, filter)
        startService()
    }

    override fun onStart() {
        super.onStart()
        checkPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
        unbindService(connection)
    }

    private fun startService() {
        Intent(this, MusicPlayerServiceImpl::class.java).also { intent ->
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
}