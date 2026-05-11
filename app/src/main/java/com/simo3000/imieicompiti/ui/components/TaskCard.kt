package com.simo3000.imieicompiti.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
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

@Composable
fun TaskCard(
    task: Task,
    onToggle: (String, Boolean) -> Unit,
    onEdit: (Task) -> Unit,
    onDelete: (String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Calcolato una volta per istanza, non ad ogni frame
    val today    = remember { LocalDate.now().toString() }
    val taskDate = remember(task.date) { task.date.take(10) }
    val isToday   = taskDate == today && !task.completed
    val isOverdue = taskDate < today  && !task.completed

    // BorderStroke ricreato solo quando cambia il colore del tema
    val outlineColor = MaterialTheme.colorScheme.outline
    val border = remember(outlineColor) {
        BorderStroke(1.dp, outlineColor.copy(alpha = 0.25f))
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Elimina compito") },
            text  = { Text("Sei sicuro di voler eliminare questo compito?") },
            confirmButton = {
                TextButton(onClick = { onDelete(task.id); showDeleteDialog = false }) {
                    Text("Elimina", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Annulla") }
            }
        )
    }

    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onToggle(task.id, !task.completed) },
        shape     = RoundedCornerShape(8.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = border
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked         = task.completed,
                onCheckedChange = { onToggle(task.id, !task.completed) },
                colors          = CheckboxDefaults.colors(
                    checkedColor   = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.padding(top = 2.dp)
            )

            Column(
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            ) {
                Row(
                    verticalAlignment    = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text          = task.subject,
                        fontSize      = 11.sp,
                        fontWeight    = FontWeight.Bold,
                        color         = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 0.5.sp
                    )
                    Text("·", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text          = task.category,
                        fontSize      = 11.sp,
                        color         = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.4.sp
                    )

                    // Badge — Box+background invece di Surface (più leggero)
                    if (isToday) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BadgeTodayBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Oggi", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BadgeTodayText)
                        }
                    }
                    if (isOverdue) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BadgeOverdueBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Scaduto", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BadgeOverdueText)
                        }
                    }
                }

                Spacer(Modifier.height(3.dp))

                Text(
                    text           = task.description,
                    fontSize       = 14.sp,
                    color          = if (task.completed)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    textDecoration = if (task.completed) TextDecoration.LineThrough else null,
                    lineHeight     = 18.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onEdit(task) }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Modifica",
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Elimina",
                        tint     = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}