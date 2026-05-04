package com.simo3000.imieicompiti.ui.screens.dashboard

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
import com.simo3000.imieicompiti.ui.components.DaySection
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var showAddDialog   by remember { mutableStateOf(false) }
    var showUserMenu    by remember { mutableStateOf(false) }

    val today = LocalDate.now().toString()

    // Filtra solo oggi e futuro
    val presentTasks = uiState.tasks.filter { task ->
        val d = try { task.date.substring(0, 10) } catch (e: Exception) { "" }
        d >= today
    }

    // Ricerca
    val filteredTasks = if (uiState.searchQuery.isBlank()) presentTasks else {
        val q = uiState.searchQuery.lowercase()
        presentTasks.filter {
            it.subject.lowercase().contains(q) ||
                    it.category.lowercase().contains(q) ||
                    it.description.lowercase().contains(q)
        }
    }

    // Raggruppa per data
    val grouped = filteredTasks
        .groupBy { try { it.date.substring(0, 10) } catch (e: Exception) { "" } }
        .toSortedMap()

    val sortedDates = grouped.keys.filter { dateKey ->
        if (!uiState.hideCompletedDays) true
        else grouped[dateKey]?.all { it.completed } == false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    // Occhio
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
                    // User menu
                    Box {
                        IconButton(onClick = { showUserMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Account",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showUserMenu,
                            onDismissRequest = { showUserMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = viewModel.getEmail() ?: "",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = {},
                                enabled = false
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Impostazioni") },
                                leadingIcon = { Icon(Icons.Default.Settings, null) },
                                onClick = { showUserMenu = false /* Step 7 */ }
                            )
                            DropdownMenuItem(
                                text = { Text("Esci") },
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
                navigationIcon = {
                    // Barra di ricerca inline
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Cerca...", fontSize = 14.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .padding(start = 12.dp, end = 4.dp)
                            .height(48.dp)
                            .width(200.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Aggiungi compito",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.loadTasks() }) {
                            Text("Riprova")
                        }
                    }
                }
            }

            sortedDates.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = when {
                                uiState.searchQuery.isNotBlank() -> "Nessun risultato"
                                uiState.hideCompletedDays -> "Tutto completato"
                                else -> "Nessun compito"
                            },
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = when {
                                uiState.searchQuery.isNotBlank() -> "Prova con un termine diverso."
                                uiState.hideCompletedDays -> "Tutti i giorni sono completati."
                                else -> "Aggiungi un compito con il pulsante +"
                            },
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(
                        start  = 16.dp,
                        end    = 16.dp,
                        top    = 16.dp,
                        bottom = 88.dp // spazio per il FAB
                    ),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(sortedDates) { dateKey ->
                        DaySection(
                            dateKey  = dateKey,
                            tasks    = grouped[dateKey] ?: emptyList(),
                            onToggle = { id, completed -> viewModel.toggleTask(id, completed) },
                            onEdit   = { task -> taskToEdit = task },
                            onDelete = { id -> viewModel.deleteTask(id) }
                        )
                    }
                }
            }
        }
    }

    // Dialog aggiunta — placeholder, lo completiamo Step 5
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Nuovo compito") },
            text  = { Text("Form aggiunta — Step 5") },
            confirmButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Chiudi") }
            }
        )
    }

    // Dialog modifica — placeholder, lo completiamo Step 5
    taskToEdit?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToEdit = null },
            title = { Text("Modifica compito") },
            text  = { Text("Form modifica — Step 5") },
            confirmButton = {
                TextButton(onClick = { taskToEdit = null }) { Text("Chiudi") }
            }
        )
    }
}