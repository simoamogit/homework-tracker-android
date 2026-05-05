package com.simo3000.imieicompiti.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simo3000.imieicompiti.data.api.RetrofitClient
import com.simo3000.imieicompiti.data.api.Task
import com.simo3000.imieicompiti.data.local.TokenStore
import com.simo3000.imieicompiti.data.repository.SettingsRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onConfirm: (date: String, subject: String, category: String, description: String) -> Unit
) {
    val context    = LocalContext.current
    val displayFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    val initialDate = remember {
        try { LocalDate.parse(task.date.substring(0, 10)) }
        catch (e: Exception) { LocalDate.now() }
    }

    var selectedDate  by remember { mutableStateOf(initialDate) }
    var subject       by remember { mutableStateOf(task.subject) }
    var category      by remember { mutableStateOf(task.category) }
    var description   by remember { mutableStateOf(task.description) }
    var error         by remember { mutableStateOf<String?>(null) }

    var subjects      by remember { mutableStateOf<List<String>>(emptyList()) }
    var categories    by remember { mutableStateOf<List<String>>(emptyList()) }

    var showDatePicker   by remember { mutableStateOf(false) }
    var subjectExpanded  by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val tokenStore = TokenStore(context)
        val repo = SettingsRepository(
            RetrofitClient.api,
            "Bearer ${tokenStore.getToken()}"
        )
        val result = repo.getSettings()
        if (result.isSuccess) {
            val s = result.getOrNull()!!
            // Aggiungi i valori attuali se non sono nelle liste
            subjects   = (s.subjects   + task.subject).distinct()
            categories = (s.categories + task.category).distinct()
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Annulla") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifica compito") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Data
                OutlinedTextField(
                    value = selectedDate.format(displayFmt),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Data") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Scegli data")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Materia
                ExposedDropdownMenuBox(
                    expanded = subjectExpanded,
                    onExpandedChange = { subjectExpanded = it }
                ) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Materia") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = subjectExpanded,
                        onDismissRequest = { subjectExpanded = false }
                    ) {
                        subjects.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = { subject = s; subjectExpanded = false }
                            )
                        }
                    }
                }

                // Categoria
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c) },
                                onClick = { category = c; categoryExpanded = false }
                            )
                        }
                    }
                }

                // Descrizione
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrizione") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        subject.isBlank()     -> error = "Seleziona una materia."
                        category.isBlank()    -> error = "Seleziona una categoria."
                        description.isBlank() -> error = "Inserisci una descrizione."
                        else -> {
                            onConfirm(
                                selectedDate.toString(),
                                subject,
                                category,
                                description.trim()
                            )
                            onDismiss()
                        }
                    }
                }
            ) { Text("Salva modifiche") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        }
    )
}