package com.simo3000.imieicompiti.ui.screens.archive

import androidx.compose.runtime.Immutable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simo3000.imieicompiti.data.api.Task
import com.simo3000.imieicompiti.ui.components.DaySectionHeader
import com.simo3000.imieicompiti.ui.components.TaskCard
import java.time.LocalDate

@Immutable
private sealed interface AListItem {
    @Immutable data class Header(val dateKey: String, val doneCount: Int, val totalCount: Int) : AListItem
    @Immutable data class Row(val task: Task) : AListItem
    @Immutable data class Gap(val id: String) : AListItem
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    onBack: () -> Unit,
    viewModel: ArchiveViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val today   = remember { LocalDate.now().toString() }

    val flatItems by remember {
        derivedStateOf {
            val state = uiState
            val pastTasks = state.tasks.filter { it.date.take(10) < today }
            val filtered  = if (state.searchQuery.isBlank()) pastTasks else {
                val q = state.searchQuery.lowercase()
                pastTasks.filter {
                    it.subject.lowercase().contains(q)  ||
                            it.category.lowercase().contains(q) ||
                            it.description.lowercase().contains(q)
                }
            }
            val grouped = filtered
                .groupBy { it.date.take(10) }
                .toSortedMap(reverseOrder())
            buildList {
                grouped.keys.toList().forEachIndexed { idx, dateKey ->
                    if (idx > 0) add(AListItem.Gap("gap_$dateKey"))
                    val tasks = grouped[dateKey] ?: emptyList()
                    val done  = tasks.count { it.completed }
                    add(AListItem.Header(dateKey, done, tasks.size))
                    tasks.forEach { task -> add(AListItem.Row(task)) }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Archivio", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Indietro")
                    }
                },
                actions = {
                    OutlinedTextField(
                        value         = uiState.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder   = { Text("Cerca...", fontSize = 14.sp) },
                        singleLine    = true,
                        modifier      = Modifier.padding(end = 12.dp).height(48.dp).width(170.dp),
                        textStyle     = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
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
                        Text(
                            text       = if (uiState.searchQuery.isNotBlank()) "Nessun risultato" else "Archivio vuoto",
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text     = if (uiState.searchQuery.isNotBlank()) "Prova con un termine diverso." else "I compiti passati appariranno qui.",
                            fontSize = 13.sp,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier       = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(
                        items       = flatItems,
                        key         = { item ->
                            when (item) {
                                is AListItem.Header -> "h_${item.dateKey}"
                                is AListItem.Row    -> item.task.id
                                is AListItem.Gap    -> item.id
                            }
                        },
                        contentType = { item ->
                            when (item) {
                                is AListItem.Header -> 0
                                is AListItem.Row    -> 1
                                is AListItem.Gap    -> 2
                            }
                        }
                    ) { item ->
                        when (item) {
                            is AListItem.Header -> DaySectionHeader(
                                dateKey    = item.dateKey,
                                doneCount  = item.doneCount,
                                totalCount = item.totalCount
                            )
                            is AListItem.Row -> TaskCard(
                                task     = item.task,
                                onToggle = { id, c -> viewModel.toggleTask(id, c) },
                                onEdit   = {},
                                onDelete = { id -> viewModel.deleteTask(id) }
                            )
                            is AListItem.Gap -> Spacer(Modifier.height(20.dp))
                        }
                    }
                }
            }
        }
    }
}