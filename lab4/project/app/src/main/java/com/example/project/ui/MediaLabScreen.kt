package com.example.project.ui

import android.app.Application
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.ui.PlayerView
import com.example.project.R
import com.example.project.player.MediaPlayerViewModel

private enum class MediaKind { Audio, Video }

@Composable
fun MediaLabScreen(modifier: Modifier = Modifier) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: MediaPlayerViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    )
    val context = LocalContext.current
    var kind by remember { mutableStateOf(MediaKind.Audio) }
    var urlInput by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    val status by viewModel.statusMessage.collectAsStateWithLifecycle()
    val downloading by viewModel.downloading.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val isBuffering by viewModel.isBuffering.collectAsStateWithLifecycle()

    LaunchedEffect(status) {
        if (!status.isNullOrBlank()) {
            snackbarHostState.showSnackbar(status!!)
            viewModel.clearStatus()
        }
    }

    val openDocument = rememberLauncherForActivityResult(
        contract = OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
            }
            viewModel.playMediaUri(uri)
        }
    }

    val mimeTypes = when (kind) {
        MediaKind.Audio -> arrayOf("audio/*")
        MediaKind.Video -> arrayOf("video/*")
    }

    val stateLabel = stringResource(
        if (isPlaying) R.string.state_playing else R.string.state_paused
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.media_lab_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = stringResource(R.string.media_lab_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = stringResource(R.string.mode_label),
                style = MaterialTheme.typography.titleSmall
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { kind = MediaKind.Audio },
                    modifier = Modifier.weight(1f),
                    enabled = kind != MediaKind.Audio
                ) {
                    Text(stringResource(R.string.mode_audio))
                }
                OutlinedButton(
                    onClick = { kind = MediaKind.Video },
                    modifier = Modifier.weight(1f),
                    enabled = kind != MediaKind.Video
                ) {
                    Text(stringResource(R.string.mode_video))
                }
            }

            if (kind == MediaKind.Video) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                useController = true
                                player = viewModel.player
                            }
                        },
                        update = { it.player = viewModel.player }
                    )
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    AudioPlaybackPanel(
                        player = viewModel.player,
                        isPlaying = isPlaying,
                        isBuffering = isBuffering,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
            }

            Text(
                text = stringResource(R.string.playback_state, stateLabel),
                style = MaterialTheme.typography.labelLarge
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { viewModel.play() },
                    modifier = Modifier.weight(1f),
                    enabled = !isPlaying
                ) {
                    Text(stringResource(R.string.action_play))
                }
                Button(
                    onClick = { viewModel.pause() },
                    modifier = Modifier.weight(1f),
                    enabled = isPlaying
                ) {
                    Text(stringResource(R.string.action_pause))
                }
                Button(
                    onClick = { viewModel.stopPlayback() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.action_stop))
                }
            }

            Text(
                text = stringResource(R.string.internal_section),
                style = MaterialTheme.typography.titleSmall
            )
            Button(
                onClick = {
                    when (kind) {
                        MediaKind.Audio -> viewModel.playRawDemo(R.raw.demo_audio)
                        MediaKind.Video -> viewModel.playRawDemo(R.raw.demo_video)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.action_demo_internal))
            }

            Text(
                text = stringResource(R.string.device_section),
                style = MaterialTheme.typography.titleSmall
            )
            Button(
                onClick = { openDocument.launch(mimeTypes) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.action_pick_file))
            }

            Text(
                text = stringResource(R.string.network_section),
                style = MaterialTheme.typography.titleSmall
            )
            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.url_label)) },
                singleLine = true,
                placeholder = { Text(stringResource(R.string.url_hint)) }
            )
            Button(
                onClick = { viewModel.downloadAndPlay(urlInput) },
                enabled = !downloading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.action_download_play))
            }
            if (downloading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
