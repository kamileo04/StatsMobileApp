package org.example.project.sensor

/**
 * Expect class — platform-specific implementations provide accelerometer data.
 */
expect class SensorManager {
    fun startListening(onData: (SensorData) -> Unit)
    fun stopListening()
}
