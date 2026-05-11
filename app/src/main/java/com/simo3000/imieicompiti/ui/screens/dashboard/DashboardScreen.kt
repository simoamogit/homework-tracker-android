package com.simo3000.imieicompiti.ui.screens.dashboard

import androidx.compose.runtime.Immutable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simo3000.imieicompiti.data.api.Task
import com.simo3000.imieicompiti.ui.components.AddTaskDialog
import com.simo3000.imieicompiti.ui.components.DaySectionHeader
import com.simo3000.imieicompiti.ui.components.EditTaskDialog
import com.simo3000.imieicompiti.ui.components.TaskCard
import java.time.LocalDate

@Immutable
private sealed interface DListItem {
    @Immutable data class Header(val dateKey: String, val doneCount: Int, val totalCount: Int) : DListItem
    @Immutable data class Row(val task: Task) : DListItem
    @Immutable data class Gap(val id: String) : DListItem
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    onNavigateToArchive:  () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var taskToEdit    by remember { mutableStateOf<Task?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showUserMenu  by remember { mutableStateOf(false) }

    val today = remember { LocalDate.now().toString() }

    // Ricalcola SOLO quando uiState cambia, non ad ogni recomposition
    val flatItems by remember {
        derivedStateOf {
            val state = uiState

            val presentTasks = state.tasks.filter { it.date.take(10) >= today }

            val filtered = if (state.searchQuery.isBlank()) presentTasks else {
                val q = state.searchQuery.lowercase()
                presentTasks.filter {
                    it.subject.lowercase().contains(q)  ||
                            it.category.lowercase().contains(q) ||
                            it.description.lowercase().contains(q)
                }
            }

            val grouped = filtered
                .groupBy { it.date.take(10) }
                .toSortedMap()

            val dates = grouped.keys.filter { dateKey ->
                if (!state.hideCompletedDays) true
                else grouped[dateKey]?.all { it.completed } == false
            }

            buildList {
                dates.forEachIndexed { idx, dateKey ->
                    if (idx > 0) add(DListItem.Gap("gap_$dateKey"))
                    val tasks = grouped[dateKey] ?: emptyList()
                    val done  = tasks.count { it.completed }
                    add(DListItem.Header(dateKey, done, tasks.size))
                    tasks.forEach { task -> add(DListItem.Row(task)) }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    OutlinedTextField(
                        value         = uiState.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder   = { Text("Cerca...", fontSize = 14.sp) },
                        singleLine    = true,
                        modifier      = Modifier
                            .padding(start = 12.dp, end = 4.dp)
                            .height(48.dp)
                            .width(200.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                        colors    = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToArchive) {
                        Icon(
                            Icons.Default.Archive,
                            contentDescription = "Archivio",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { viewModel.toggleHideCompletedDays() }) {
                        Icon(
                            imageVector = if (uiState.hideCompletedDays)
                                Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Nascondi giorni completati",
                            tint = if (uiState.hideCompletedDays)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box {
                        IconButton(onClick = { showUserMenu = true }) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = "Account",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        DropdownMenu(
                            expanded         = showUserMenu,
                            onDismissRequest = { showUserMenu = false }
                        ) {
                            DropdownMenuItem(
                                text    = {
                                    Text(
                                        viewModel.getEmail() ?: "",
                                        fontSize = 12.sp,
                                        color    = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = {},
                                enabled = false
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text        = { Text("Impostazioni") },
                                leadingIcon = { Icon(Icons.Default.Settings, null) },
                                onClick     = { showUserMenu = false; onNavigateToSettings() }
                            )
                            DropdownMenuItem(
                                text        = { Text("Esci") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.ExitToApp,
                                        null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = { showUserMenu = false; onLogout() }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Aggiungi", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->

        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Caricamento...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            uiState.error != null -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier            = Modifier.padding(24.dp)
                    ) {
                        Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.loadTasks() }) { Text("Riprova") }
                    }
                }
            }

            flatItems.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier            = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = when {
                                uiState.searchQuery.isNotBlank() -> "Nessun risultato"
                                uiState.hideCompletedDays        -> "Tutto completato"
                                else                             -> "Nessun compito"
                            },
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = when {
                                uiState.searchQuery.isNotBlank() -> "Prova con un termine diverso."
                                uiState.hideCompletedDays        -> "Tutti i giorni sono completati."
                                else                             -> "Aggiungi un compito con il pulsante +"
                            },
                            fontSize = 13.sp,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier       = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(
                        start  = 16.dp,
                        end    = 16.dp,
                        top    = 16.dp,
                        bottom = 88.dp
                    )
                ) {
                    items(
                        items       = flatItems,
                        key         = { item ->
                            when (item) {
                                is DListItem.Header -> "h_${item.dateKey}"
                                is DListItem.Row    -> item.task.id
                                is DListItem.Gap    -> item.id
                            }
                        },
                        contentType = { item ->
                            when (item) {
                                is DListItem.Header -> 0
                                is DListItem.Row    -> 1
                                is DListItem.Gap    -> 2
                            }
                        }
                    ) { item ->
                        when (item) {
                            is DListItem.Header -> DaySectionHeader(
                                dateKey    = item.dateKey,
                                doneCount  = item.doneCount,
                                totalCount = item.totalCount
                            )
                            is DListItem.Row -> TaskCard(
                                task     = item.task,
                                onToggle = { id, c -> viewModel.toggleTask(id, c) },
                                onEdit   = { task -> taskToEdit = task },
                                onDelete = { id -> viewModel.deleteTask(id) }
                            )
                            is DListItem.Gap -> Spacer(Modifier.height(20.dp))
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { date, subject, category, description ->
                viewModel.createTask(date, subject, category, description)
            }
        )
    }

    taskToEdit?.let { task ->
        EditTaskDialog(
            task      = task,
            onDismiss = { taskToEdit = null },
            onConfirm = { date, subject, category, description ->
                viewModel.updateTask(task.id, date, subject, category, description)
                taskToEdit = null
            }
        )
    }
}