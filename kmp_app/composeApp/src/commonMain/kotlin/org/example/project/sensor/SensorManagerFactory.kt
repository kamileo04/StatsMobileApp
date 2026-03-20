package org.example.project.sensor

import androidx.compose.runtime.Composable

/**
 * Composable factory — each platform creates a SensorManager using
 * whatever platform-specific dependencies it needs (e.g. Context on Android).
 */
@Composable
expect fun rememberSensorManager(): SensorManager
