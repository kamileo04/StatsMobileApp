package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.example.project.navigation.Screen
import org.example.project.ui.screens.JsonViewerScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val functionalities = remember {
        listOf(
            "Analiza JSON" to {
                currentScreen = Screen.JsonViewer(
                    moduleName = "Analiza JSON",
                    path = "C:/Projekty/statyApka/data.json"
                )
            },
            "Zależności 1" to { currentScreen = Screen.Details("Zależności 1") },
            "Zależności 2" to { currentScreen = Screen.Details("Zależności 2") },
            "Zależności 3" to { currentScreen = Screen.Details("Zależności 3") }
        )
    }

    MaterialTheme {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Funkcje",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Divider()
                    Spacer(Modifier.height(8.dp))
                    functionalities.forEach { (title, action) ->
                        NavigationDrawerItem(
                            label = { Text(title) },
                            selected = false,
                            onClick = {
                                action()
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = when (val screen = currentScreen) {
                                    is Screen.Home -> "Staty Apka"
                                    is Screen.Details -> screen.title
                                    is Screen.JsonViewer -> screen.moduleName
                                },
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Text(
                                    text = "☰",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary,
                        )
                    )
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                    when (val screen = currentScreen) {
                        is Screen.Home -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Wybierz funkcjonalność z menu bocznego.",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                        is Screen.Details -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Standardowa strona dla: ${screen.title}")
                            }
                        }
                        is Screen.JsonViewer -> {
                            JsonViewerScreen(screen.path)
                        }
                    }
                }
            }
        }
    }
}
