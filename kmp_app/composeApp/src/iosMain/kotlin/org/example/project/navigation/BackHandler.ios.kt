package org.example.project.navigation

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(isEnabled: Boolean, onBack: () -> Unit) {
    // No-Op for iOS - generally iOS handled by swipe. Additional implementation possible with UIKit.
}
