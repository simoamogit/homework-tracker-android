package com.simo3000.imieicompiti.ui.screens.settings

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simo3000.imieicompiti.ui.components.AppButton
import com.simo3000.imieicompiti.ui.theme.ThemeViewModel
import com.simo3000.imieicompiti.ui.theme.ThemeMode

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // ThemeViewModel scoped all'Activity — condiviso con MainActivity
    val activity     = LocalContext.current as ComponentActivity
    val themeViewModel: ThemeViewModel = viewModel(activity)
    val themeMode by themeViewModel.themeMode.collectAsState()

    var newSubject   by remember { mutableStateOf("") }
    var newCategory  by remember { mutableStateOf("") }

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

            // ── ASPETTO ──────────────────────────────────────
            SettingsSection(title = "Aspetto") {
                Text("Tema", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val options = listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK)
                    val labels  = listOf("Sistema", "Chiaro", "Scuro")
                    options.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = themeMode == mode,
                            onClick  = { themeViewModel.setThemeMode(mode) },
                            shape    = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                            label    = { Text(labels[index], fontSize = 13.sp) }
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // ── MATERIE ──────────────────────────────────────
            SettingsSection(title = "Materie") {
                if (uiState.subjects.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement   = Arrangement.spacedBy(4.dp)
                    ) {
                        uiState.subjects.forEach { subject ->
                            InputChip(
                                selected = false,
                                onClick  = {},
                                label    = { Text(subject, fontSize = 13.sp) },
                                trailingIcon = {
                                    IconButton(
                                        onClick  = { viewModel.removeSubject(subject) },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Rimuovi",
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            )
                        }
                    }
                } else {
                    Text(
                        "Nessuna materia. Aggiungine una.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value         = newSubject,
                        onValueChange = { newSubject = it },
                        placeholder   = { Text("Es. Matematica", fontSize = 13.sp) },
                        singleLine    = true,
                        modifier      = Modifier.weight(1f).height(52.dp),
                        textStyle     = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                    )
                    FilledTonalIconButton(
                        onClick  = { viewModel.addSubject(newSubject); newSubject = "" },
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Aggiungi materia")
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // ── CATEGORIE ────────────────────────────────────
            SettingsSection(title = "Categorie") {
                if (uiState.categories.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement   = Arrangement.spacedBy(4.dp)
                    ) {
                        uiState.categories.forEach { category ->
                            InputChip(
                                selected = false,
                                onClick  = {},
                                label    = { Text(category, fontSize = 13.sp) },
                                trailingIcon = {
                                    IconButton(
                                        onClick  = { viewModel.removeCategory(category) },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Rimuovi",
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            )
                        }
                    }
                } else {
                    Text(
                        "Nessuna categoria. Aggiungine una.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value         = newCategory,
                        onValueChange = { newCategory = it },
                        placeholder   = { Text("Es. Compito scritto", fontSize = 13.sp) },
                        singleLine    = true,
                        modifier      = Modifier.weight(1f).height(52.dp),
                        textStyle     = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                    )
                    FilledTonalIconButton(
                        onClick  = { viewModel.addCategory(newCategory); newCategory = "" },
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Aggiungi categoria")
                    }
                }
            }

            uiState.error?.let { err ->
                Text(err, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            AppButton(
                text      = "Salva impostazioni",
                onClick   = { viewModel.save() },
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
            text       = title,
            fontWeight = FontWeight.Bold,
            fontSize   = 15.sp,
            color      = MaterialTheme.colorScheme.onSurface
        )
        content()
    }
}