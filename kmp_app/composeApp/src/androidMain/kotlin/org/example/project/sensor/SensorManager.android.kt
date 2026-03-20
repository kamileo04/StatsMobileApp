package org.example.project.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager as AndroidSensorManager

actual class SensorManager(private val context: Context) {
    private var sensorManager: AndroidSensorManager? = null
    private var listener: SensorEventListener? = null

    actual fun startListening(onData: (SensorData) -> Unit) {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as AndroidSensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return

        listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    // Android axes: x=right, y=up, z=out of screen
                    // negate x so tilting right gives positive x (ball goes right)
                    onData(SensorData(x = -event.values[0], y = event.values[1], z = event.values[2]))
                }
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        sensorManager?.registerListener(listener, accelerometer, AndroidSensorManager.SENSOR_DELAY_GAME)
    }

    actual fun stopListening() {
        sensorManager?.unregisterListener(listener)
        listener = null
        sensorManager = null
    }
}
