package org.example.project.navigation

sealed class Screen {
    data object Home : Screen()
    data class Details(val title: String) : Screen()
    data class JsonViewer(val moduleName: String, val path: String) : Screen()
}
