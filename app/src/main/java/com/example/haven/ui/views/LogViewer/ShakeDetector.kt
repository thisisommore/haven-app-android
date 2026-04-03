package com.example.haven.ui.views

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlin.math.sqrt

/**
 * Shake detector composable that triggers on shake gesture
 * Mirrors iOS ShakeDetector functionality
 */
@Composable
fun ShakeDetector(
    onShake: () -> Unit,
    minShakeIntervalMs: Long = 1000L,
    shakeThreshold: Float = 15f
) {
    val context = LocalContext.current
    var lastShakeTime by remember { mutableLongStateOf(0L) }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    // Calculate acceleration magnitude
                    val acceleration = sqrt(x * x + y * y + z * z)
                    val delta = acceleration - SensorManager.GRAVITY_EARTH

                    // Check if shake is strong enough
                    if (delta > shakeThreshold) {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastShakeTime > minShakeIntervalMs) {
                            lastShakeTime = currentTime
                            onShake()
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(
            sensorListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_UI
        )

        onDispose {
            sensorManager.unregisterListener(sensorListener)
        }
    }
}
