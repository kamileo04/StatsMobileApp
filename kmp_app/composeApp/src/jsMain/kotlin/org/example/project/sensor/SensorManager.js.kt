package org.example.project.sensor

actual class SensorManager {
    actual fun startListening(onData: (SensorData) -> Unit) {
        // JS/browser has no accelerometer in this context — no-op
    }
    actual fun stopListening() {}
}
