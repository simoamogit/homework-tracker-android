package com.simo3000.imieicompiti.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simo3000.imieicompiti.data.api.Task
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatDateLabel(dateStr: String): String {
    return try {
        val date = LocalDate.parse(dateStr)
        val formatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ITALIAN)
        date.format(formatter).replaceFirstChar { it.uppercase() }
    } catch (e: Exception) {
        dateStr
    }
}

@Composable
fun DaySection(
    dateKey: String,
    tasks: List<Task>,
    onToggle: (String, Boolean) -> Unit,
    onEdit: (Task) -> Unit,
    onDelete: (String) -> Unit
) {
    val doneCount  = tasks.count { it.completed }
    val totalCount = tasks.size
    val percent    = if (totalCount > 0) doneCount.toFloat() / totalCount else 0f
    val allDone    = doneCount == totalCount

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatDateLabel(dateKey),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (allDone)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                else
                    MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$doneCount/$totalCount",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Progress bar
        LinearProgressIndicator(
            progress = { percent },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )

        // Tasks
        tasks.forEach { task ->
            TaskCard(
                task     = task,
                onToggle = onToggle,
                onEdit   = onEdit,
                onDelete = onDelete
            )
        }
    }
}