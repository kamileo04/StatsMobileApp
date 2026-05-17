@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package org.example.project.sensor

import kotlinx.cinterop.useContents
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSOperationQueue

actual class SensorManager {
    private val motionManager = CMMotionManager()

    actual fun startListening(onData: (SensorData) -> Unit) {
        if (motionManager.accelerometerAvailable) {
            motionManager.accelerometerUpdateInterval = 1.0 / 60.0
            motionManager.startAccelerometerUpdatesToQueue(NSOperationQueue.mainQueue()) { data, _ ->
                val acc = data?.acceleration ?: return@startAccelerometerUpdatesToQueue
                // iOS axes: x=right, y=up (in portrait). Negate y because iOS y is inverted vs gravity direction needed.
                acc.useContents {
                    onData(SensorData(x = x.toFloat(), y = (-y).toFloat(), z = z.toFloat()))
                }
            }
        }
    }

    actual fun stopListening() {
        motionManager.stopAccelerometerUpdates()
    }
}
