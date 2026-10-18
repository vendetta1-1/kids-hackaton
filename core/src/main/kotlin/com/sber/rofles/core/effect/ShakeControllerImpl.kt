package com.sber.rofles.core.effect

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

/**
 * Детектор тряски на акселерометре. Чувствительность — порог [threshold],
 * позже берётся из конфига (CMS).
 */
@Singleton
class ShakeControllerImpl @Inject constructor(
    @ApplicationContext context: Context,
) : ShakeController, SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _shakes = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val shakes: Flow<Unit> = _shakes.asSharedFlow()

    /** Порог тряски. Чувствительность из конфига будет передаваться сюда. */
    var threshold: Float = 25f

    override fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val values = event?.values ?: return
        val magnitude = sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2])
        if (magnitude > threshold) _shakes.tryEmit(Unit)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}