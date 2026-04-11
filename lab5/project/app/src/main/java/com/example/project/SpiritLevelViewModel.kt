package com.example.project

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

private const val LowPassAlpha = 0.88f
private const val LevelThresholdDeg = 1f

data class SpiritLevelUiState(
    val pitchDeg: Float = 0f,
    val rollDeg: Float = 0f,
    val isLevel: Boolean = false,
    val sensorAvailable: Boolean = false
)

class SpiritLevelViewModel(application: Application) : AndroidViewModel(application) {

    private val sensorManager =
        application.getSystemService(Application.SENSOR_SERVICE) as SensorManager

    private val gravitySensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)

    private val accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val activeSensor: Sensor? = gravitySensor ?: accelerometer

    private var filteredX = 0f
    private var filteredY = 0f
    private var filteredZ = 0f
    private var filterInitialized = false

    private var rawPitchDeg = 0f
    private var rawRollDeg = 0f

    private var offsetPitchDeg = 0f
    private var offsetRollDeg = 0f

    private val _uiState = MutableStateFlow(
        SpiritLevelUiState(sensorAvailable = activeSensor != null)
    )
    val uiState: StateFlow<SpiritLevelUiState> = _uiState.asStateFlow()

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor != activeSensor) return
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            if (!filterInitialized) {
                filteredX = x
                filteredY = y
                filteredZ = z
                filterInitialized = true
            } else {
                filteredX = LowPassAlpha * x + (1f - LowPassAlpha) * filteredX
                filteredY = LowPassAlpha * y + (1f - LowPassAlpha) * filteredY
                filteredZ = LowPassAlpha * z + (1f - LowPassAlpha) * filteredZ
            }

            val pitchRad =
                atan2(
                    -filteredX.toDouble(),
                    sqrt((filteredY * filteredY + filteredZ * filteredZ).toDouble())
                )
            val rollRad = atan2(filteredY.toDouble(), filteredZ.toDouble())

            rawPitchDeg = Math.toDegrees(pitchRad).toFloat()
            rawRollDeg = Math.toDegrees(rollRad).toFloat()

            val displayPitch = rawPitchDeg - offsetPitchDeg
            val displayRoll = rawRollDeg - offsetRollDeg
            val isLevel =
                abs(displayPitch) < LevelThresholdDeg && abs(displayRoll) < LevelThresholdDeg

            _uiState.update { prev ->
                prev.copy(
                    pitchDeg = displayPitch,
                    rollDeg = displayRoll,
                    isLevel = isLevel
                )
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    fun startListening() {
        val sensor = activeSensor ?: return
        sensorManager.registerListener(
            listener,
            sensor,
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    fun stopListening() {
        sensorManager.unregisterListener(listener)
    }

    fun calibrate() {
        offsetPitchDeg = rawPitchDeg
        offsetRollDeg = rawRollDeg
        _uiState.update { prev ->
            prev.copy(pitchDeg = 0f, rollDeg = 0f, isLevel = true)
        }
    }
}
