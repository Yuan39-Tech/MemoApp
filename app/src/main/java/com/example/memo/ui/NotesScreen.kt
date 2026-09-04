package com.example.memo.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.memo.data.Note
import java.util.Calendar
import java.util.Date

@Composable
fun NotesScreen(viewModel: NotesViewModel) {
    val notes by viewModel.notes.collectAsState()
    var editing by remember { mutableStateOf<Note?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { editing = Note(title = "", content = "") }) {
                Icon(Icons.Default.Add, contentDescription = "新增笔记")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = padding
        ) {
            items(notes, key = { it.id }) { note ->
                ListItem(
                    headlineContent = { Text(note.title.ifBlank { "无标题" }) },
                    supportingContent = { Text(note.content.ifBlank { "无内容" }, maxLines = 2) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { editing = note },
                    trailingContent = {
                        IconButton(onClick = { viewModel.delete(note) }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除笔记")
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }

    editing?.let { note ->
        NoteEditor(
            note = note,
            onDismiss = { editing = null },
            onSave = { title, content, reminderAt ->
                viewModel.save(note.id, title, content, reminderAt)
                editing = null
            }
        )
    }
}

@Composable
private fun NoteEditor(
    note: Note,
    onDismiss: () -> Unit,
    onSave: (String, String, Long?) -> Unit
) {
    val context = LocalContext.current
    var title by remember(note.id) { mutableStateOf(note.title) }
    var content by remember(note.id) { mutableStateOf(note.content) }
    var reminderAt by remember(note.id) { mutableStateOf(note.reminderAt) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (note.id == 0L) "新增笔记" else "编辑笔记") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("标题") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("内容") },
                    minLines = 5
                )
                OutlinedButton(onClick = {
                    val now = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    reminderAt = Calendar.getInstance().apply {
                                        set(year, month, day, hour, minute, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }.timeInMillis
                                },
                                now.get(Calendar.HOUR_OF_DAY),
                                now.get(Calendar.MINUTE),
                                true
                            ).show()
                        },
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }) {
                    Text(reminderAt?.let {
                        "提醒：${DateFormat.getDateTimeInstance().format(Date(it))}"
                    } ?: "设置提醒时间")
                }
                if (reminderAt != null) {
                    TextButton(onClick = { reminderAt = null }) { Text("取消提醒") }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(title, content, reminderAt) }) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
