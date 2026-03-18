package com.example.streamingapp

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView

class MainActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var ipAddressText: TextView
    private lateinit var serverIpInput: EditText
    
    private val videoServer = VideoServer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playerView = findViewById(R.id.player_view)
        ipAddressText = findViewById(R.id.ip_address_text)
        serverIpInput = findViewById(R.id.server_ip_input)
        
        val startServerButton: Button = findViewById(R.id.start_server_button)
        val connectButton: Button = findViewById(R.id.connect_button)
        val playButton: Button = findViewById(R.id.play_button)

        val myIp = NetworkUtils.getLocalIpAddress()
        ipAddressText.text = "My IP: $myIp"

        startServerButton.setOnClickListener {
            try {
                videoServer.start()
                startServerButton.isEnabled = false
                startServerButton.text = "Server Active"
                Toast.makeText(this, "Server started on port 8080", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to start server: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        connectButton.setOnClickListener {
            val serverIp = serverIpInput.text.toString().trim()
            if (serverIp.isNotEmpty()) {
                Toast.makeText(this, "Connected to $serverIp", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter Server IP", Toast.LENGTH_SHORT).show()
            }
        }

        playButton.setOnClickListener {
            val serverIp = serverIpInput.text.toString().trim()
            if (serverIp.isNotEmpty()) {
                val streamUrl = "http://$serverIp:8080/video"
                playStream(streamUrl)
            } else {
                Toast.makeText(this, "Please connect to a server first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun playStream(url: String) {
        releasePlayer()
        initializePlayer(url)
    }

    @OptIn(UnstableApi::class)
    private fun initializePlayer(url: String) {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
        
        val mediaSourceFactory = DefaultMediaSourceFactory(this)
            .setDataSourceFactory(httpDataSourceFactory)

        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(url))
            .setMimeType(MimeTypes.APPLICATION_M3U8)
            .build()

        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().also { exoPlayer ->
                playerView.player = exoPlayer
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
            }
    }

    private fun releasePlayer() {
        playerView.player = null
        player?.release()
        player = null
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        videoServer.stop()
        releasePlayer()
    }
}
