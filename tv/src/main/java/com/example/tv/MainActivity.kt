package com.example.tv

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

class MainActivity : FragmentActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var webServer: WebServer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        playerView = findViewById(R.id.player_view)

        // Inicializamos el servidor y le decimos qué hacer cuando reciba un comando
        webServer = WebServer { videoId ->
            // Ktor funciona en un hilo de fondo. Para actualizar la UI (el video),
            // debemos volver al hilo principal.
            runOnUiThread {
                playVideo(videoId)
            }
        }
    }

    // Hacemos pública esta función para que el servidor pueda llamarla
    fun playVideo(videoId: String) {
        // ... el resto de la función playVideo se queda exactamente igual ...
        val resourceId = resources.getIdentifier(videoId, "raw", packageName)
        if (resourceId == 0) {
            return
        }
        val videoUri = Uri.parse("android.resource://$packageName/$resourceId")
        val mediaItem = MediaItem.fromUri(videoUri)
        player?.setMediaItem(mediaItem)
        player?.repeatMode = Player.REPEAT_MODE_ONE
        player?.playWhenReady = true
        player?.prepare()
    }

    // ... la función initializePlayer se queda igual ...
    private fun initializePlayer() {
        player = ExoPlayer.Builder(this).build()
        playerView.player = player
    }

    // ... la función releasePlayer se queda igual ...
    private fun releasePlayer() {
        player?.release()
        player = null
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()

        // ¡¡¡EL CAMBIO FINAL!!!
        // Lanzamos el servidor en un hilo de fondo para no congelar la UI.
        lifecycleScope.launch(Dispatchers.IO) {
            webServer.start()
        }
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()

        // También detenemos el servidor en un hilo de fondo.
        lifecycleScope.launch(Dispatchers.IO) {
            webServer.stop()
        }
    }
}