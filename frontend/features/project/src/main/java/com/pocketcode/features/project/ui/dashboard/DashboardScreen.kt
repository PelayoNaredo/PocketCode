package com.pocketcode.features.project.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.jetpackcamera.core.ui.theme.PocketCodeTheme
import com.google.jetpackcamera.core.ui.theme.medium_spacing
import com.pocketcode.domain.project.model.Project

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onProjectClick: (Project) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    PocketCodeTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Projects") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { /* TODO: Show create project dialog */ }) {
                    Icon(Icons.Filled.Add, contentDescription = "Create Project")
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (val state = uiState) {
                    is DashboardUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is DashboardUiState.Success -> {
                        if (state.projects.isEmpty()) {
                            EmptyProjectList()
                        } else {
                            ProjectList(projects = state.projects, onProjectClick = onProjectClick)
                        }
                    }
                    is DashboardUiState.Error -> {
                        Text(
                            text = state.message,
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectList(
    projects: List<Project>,
    onProjectClick: (Project) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(medium_spacing),
        verticalArrangement = Arrangement.spacedBy(medium_spacing)
    ) {
        items(projects) { project ->
            ProjectListItem(project = project, onClick = { onProjectClick(project) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectListItem(
    project: Project,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Text(
            text = project.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(medium_spacing)
        )
    }
}

@Composable
fun EmptyProjectList() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No projects found.\nClick the '+' button to create a new one.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}
