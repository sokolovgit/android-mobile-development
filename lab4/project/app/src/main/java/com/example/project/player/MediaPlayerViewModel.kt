package com.example.project.player

import android.app.Application
import android.net.Uri
import androidx.annotation.RawRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.project.R
import com.example.project.download.downloadUrlToFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class MediaPlayerViewModel(application: Application) : AndroidViewModel(application) {

    val player: ExoPlayer = ExoPlayer.Builder(application).build()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _downloading = MutableStateFlow(false)
    val downloading: StateFlow<Boolean> = _downloading.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(playing: Boolean) {
            _isPlaying.value = playing
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            _isBuffering.value = playbackState == Player.STATE_BUFFERING
        }

        override fun onPlayerError(error: PlaybackException) {
            _statusMessage.value = error.message ?: error.errorCodeName
        }
    }

    init {
        player.addListener(listener)
    }

    fun clearStatus() {
        _statusMessage.value = null
    }

    fun playMediaUri(uri: Uri) {
        _statusMessage.value = null
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
        player.play()
    }

    fun playRawDemo(@RawRes resId: Int) {
        val pkg = getApplication<Application>().packageName
        val uri = Uri.parse("android.resource://$pkg/$resId")
        playMediaUri(uri)
    }

    fun play() {
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun stopPlayback() {
        player.stop()
        player.seekTo(0)
    }

    fun downloadAndPlay(urlText: String) {
        val url = urlText.trim()
        if (url.isEmpty()) {
            _statusMessage.value = getApplication<Application>().getString(R.string.error_empty_url)
            return
        }
        viewModelScope.launch {
            _downloading.value = true
            _statusMessage.value = null
            try {
                val parsed = Uri.parse(url)
                if (parsed.scheme.isNullOrBlank()) {
                    error(getApplication<Application>().getString(R.string.error_invalid_url))
                }
                val segment = parsed.lastPathSegment?.substringAfterLast('/') ?: "media"
                var safeName = segment.replace(Regex("[^a-zA-Z0-9._-]"), "_").take(120)
                if (safeName.isEmpty()) safeName = "media_download"
                if (!safeName.contains('.')) {
                    safeName += when {
                        url.contains(".mp3", ignoreCase = true) -> ".mp3"
                        url.contains(".wav", ignoreCase = true) -> ".wav"
                        url.contains(".mp4", ignoreCase = true) -> ".mp4"
                        url.contains(".webm", ignoreCase = true) -> ".webm"
                        else -> ".bin"
                    }
                }
                val dest = File(getApplication<Application>().filesDir, "downloads/$safeName")
                downloadUrlToFile(url, dest).getOrThrow()
                playMediaUri(Uri.fromFile(dest))
            } catch (e: Exception) {
                _statusMessage.value = e.message
                    ?: getApplication<Application>().getString(R.string.error_download_failed)
            } finally {
                _downloading.value = false
            }
        }
    }

    override fun onCleared() {
        player.removeListener(listener)
        player.release()
        super.onCleared()
    }
}
