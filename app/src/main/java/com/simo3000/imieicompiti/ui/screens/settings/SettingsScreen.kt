package com.simo3000.imieicompiti.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simo3000.imieicompiti.ui.components.AppButton

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var newSubject  by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("") }

    // Mostra snackbar al salvataggio
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("Impostazioni salvate.")
            viewModel.clearSaveSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Impostazioni", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // ── MATERIE ──────────────────────────────────────
            SettingsSection(title = "Materie") {
                // Chips esistenti
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.subjects.forEach { subject ->
                        InputChip(
                            selected = false,
                            onClick = {},
                            label = { Text(subject) },
                            trailingIcon = {
                                IconButton(
                                    onClick = { viewModel.removeSubject(subject) },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Rimuovi $subject",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        )
                    }
                }
                // Campo aggiunta
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newSubject,
                        onValueChange = { newSubject = it },
                        placeholder = { Text("Nuova materia", fontSize = 14.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f).height(52.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                    )
                    IconButton(
                        onClick = {
                            viewModel.addSubject(newSubject)
                            newSubject = ""
                        },
                        modifier = Modifier
                            .size(52.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Aggiungi materia",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // ── CATEGORIE ────────────────────────────────────
            SettingsSection(title = "Categorie") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.categories.forEach { category ->
                        InputChip(
                            selected = false,
                            onClick = {},
                            label = { Text(category) },
                            trailingIcon = {
                                IconButton(
                                    onClick = { viewModel.removeCategory(category) },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Rimuovi $category",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCategory,
                        onValueChange = { newCategory = it },
                        placeholder = { Text("Nuova categoria", fontSize = 14.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f).height(52.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                    )
                    IconButton(
                        onClick = {
                            viewModel.addCategory(newCategory)
                            newCategory = ""
                        },
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Aggiungi categoria",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            uiState.error?.let { err ->
                Text(err, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            AppButton(
                text = "Salva impostazioni",
                onClick = { viewModel.save() },
                isLoading = uiState.isSaving
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        content()
    }
}