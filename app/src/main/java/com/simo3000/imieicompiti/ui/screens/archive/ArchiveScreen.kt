package com.simo3000.imieicompiti.ui.screens.archive

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
import com.simo3000.imieicompiti.ui.components.DaySection
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    onBack: () -> Unit,
    viewModel: ArchiveViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val today   = LocalDate.now().toString()

    // Solo compiti passati
    val pastTasks = uiState.tasks.filter { task ->
        val d = try { task.date.substring(0, 10) } catch (e: Exception) { "" }
        d < today
    }

    // Ricerca
    val filtered = if (uiState.searchQuery.isBlank()) pastTasks else {
        val q = uiState.searchQuery.lowercase()
        pastTasks.filter {
            it.subject.lowercase().contains(q) ||
                    it.category.lowercase().contains(q) ||
                    it.description.lowercase().contains(q)
        }
    }

    // Raggruppa per data — ordine decrescente (più recente prima)
    val grouped     = filtered
        .groupBy { try { it.date.substring(0, 10) } catch (e: Exception) { "" } }
        .toSortedMap(reverseOrder())
    val sortedDates = grouped.keys.toList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Archivio", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Cerca...", fontSize = 14.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .height(48.dp)
                            .width(170.dp),
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
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
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
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.loadTasks() }) { Text("Riprova") }
                    }
                }
            }

            sortedDates.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = if (uiState.searchQuery.isNotBlank())
                                "Nessun risultato" else "Archivio vuoto",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (uiState.searchQuery.isNotBlank())
                                "Prova con un termine diverso."
                            else
                                "I compiti passati appariranno qui.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(sortedDates) { dateKey ->
                        DaySection(
                            dateKey  = dateKey,
                            tasks    = grouped[dateKey] ?: emptyList(),
                            onToggle = { id, completed -> viewModel.toggleTask(id, completed) },
                            onEdit   = { /* archivio read-only per edit */ },
                            onDelete = { id -> viewModel.deleteTask(id) }
                        )
                    }
                }
            }
        }
    }
}