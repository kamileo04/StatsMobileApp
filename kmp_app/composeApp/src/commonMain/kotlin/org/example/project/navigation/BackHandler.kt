package org.example.project.navigation

import androidx.compose.runtime.Composable

@Composable
expect fun BackHandler(isEnabled: Boolean = true, onBack: () -> Unit)
