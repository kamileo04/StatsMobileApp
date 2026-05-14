package org.example.project.sensor

actual class SensorManager {
    actual fun startListening(onData: (SensorData) -> Unit) {
        // WasmJS/browser has no accelerometer — no-op
    }
    actual fun stopListening() {}
}
