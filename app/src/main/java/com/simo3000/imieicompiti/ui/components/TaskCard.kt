package com.simo3000.imieicompiti.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simo3000.imieicompiti.data.api.Task
import com.simo3000.imieicompiti.ui.theme.BadgeOverdueBg
import com.simo3000.imieicompiti.ui.theme.BadgeOverdueText
import com.simo3000.imieicompiti.ui.theme.BadgeTodayBg
import com.simo3000.imieicompiti.ui.theme.BadgeTodayText
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TaskCard(
    task: Task,
    onToggle: (String, Boolean) -> Unit,
    onEdit: (Task) -> Unit,
    onDelete: (String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val today     = LocalDate.now().toString()
    val taskDate  = try {
        task.date.substring(0, 10)
    } catch (e: Exception) { "" }

    val isToday   = taskDate == today && !task.completed
    val isOverdue = taskDate < today  && !task.completed

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Elimina compito") },
            text  = { Text("Sei sicuro di voler eliminare questo compito?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(task.id)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Elimina", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(task.id, !task.completed) },
        shape  = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = task.completed,
                onCheckedChange = { onToggle(task.id, !task.completed) },
                colors = CheckboxDefaults.colors(
                    checkedColor   = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.padding(top = 2.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = task.subject,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "·",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = task.category,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.4.sp
                    )
                    if (isToday) {
                        Surface(
                            shape  = RoundedCornerShape(4.dp),
                            color  = BadgeTodayBg
                        ) {
                            Text(
                                text = "Oggi",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BadgeTodayText,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (isOverdue) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = BadgeOverdueBg
                        ) {
                            Text(
                                text = "Scaduto",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BadgeOverdueText,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = task.description,
                    fontSize = 14.sp,
                    color = if (task.completed)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    textDecoration = if (task.completed) TextDecoration.LineThrough else null,
                    lineHeight = 18.sp
                )
            }

            // Azioni — visibili solo al hover/tap
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onEdit(task) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Modifica",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Elimina",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}