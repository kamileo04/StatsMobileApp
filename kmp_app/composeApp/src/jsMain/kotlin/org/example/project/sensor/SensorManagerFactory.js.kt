package org.example.project.sensor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberSensorManager(): SensorManager {
    return remember { SensorManager() }
}
