package org.example.project.sensor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberSensorManager(): SensorManager {
    val context = LocalContext.current
    return remember { SensorManager(context) }
}
