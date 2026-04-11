package com.example.project.ui

import android.app.Application
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project.R
import com.example.project.SpiritLevelViewModel

private const val MaxTiltDegForBubble = 30f

@Composable
fun SpiritLevelScreen(
    viewModel: SpiritLevelViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            checkNotNull(LocalContext.current.applicationContext as? Application)
        )
    )
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.startListening()
                Lifecycle.Event.ON_PAUSE -> viewModel.stopListening()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopListening()
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.spirit_level_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            if (!uiState.sensorAvailable) {
                Text(
                    text = stringResource(R.string.spirit_sensor_missing),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
                return@Column
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                LevelCanvas(
                    pitchDeg = uiState.pitchDeg,
                    rollDeg = uiState.rollDeg,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(
                            R.string.spirit_pitch_format,
                            uiState.pitchDeg
                        ),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(
                            R.string.spirit_roll_format,
                            uiState.rollDeg
                        ),
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (uiState.isLevel) {
                        Text(
                            text = stringResource(R.string.spirit_level_ok),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Button(
                onClick = { viewModel.calibrate() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.spirit_calibrate))
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun LevelCanvas(
    pitchDeg: Float,
    rollDeg: Float,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurfaceVariant
    val tertiary = MaterialTheme.colorScheme.tertiary

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = minOf(size.width, size.height) / 2f * 0.92f
        val bubbleRadius = radius * 0.12f
        val horizonHalfLength = radius * 0.85f

        drawCircle(
            color = onSurface.copy(alpha = 0.35f),
            radius = radius,
            center = center,
            style = Stroke(width = 6.dp.toPx())
        )

        rotate(degrees = rollDeg, pivot = center) {
            drawLine(
                color = primary,
                start = Offset(center.x - horizonHalfLength, center.y),
                end = Offset(center.x + horizonHalfLength, center.y),
                strokeWidth = 5.dp.toPx()
            )
            drawLine(
                color = Color.White.copy(alpha = 0.4f),
                start = Offset(center.x - horizonHalfLength, center.y),
                end = Offset(center.x + horizonHalfLength, center.y),
                strokeWidth = 2.dp.toPx()
            )
        }

        val t = MaxTiltDegForBubble
        val nx = (rollDeg / t).coerceIn(-1f, 1f)
        val ny = (pitchDeg / t).coerceIn(-1f, 1f)
        val maxBubbleTravel = (radius - bubbleRadius - 8.dp.toPx()).coerceAtLeast(0f)
        val bubbleCenter = Offset(
            x = center.x - nx * maxBubbleTravel,
            y = center.y - ny * maxBubbleTravel
        )

        drawCircle(
            color = tertiary.copy(alpha = 0.45f),
            radius = bubbleRadius + 3.dp.toPx(),
            center = bubbleCenter
        )
        drawCircle(
            color = tertiary,
            radius = bubbleRadius,
            center = bubbleCenter
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.5f),
            radius = bubbleRadius * 0.35f,
            center = Offset(
                bubbleCenter.x - bubbleRadius * 0.25f,
                bubbleCenter.y - bubbleRadius * 0.25f
            )
        )
    }
}
