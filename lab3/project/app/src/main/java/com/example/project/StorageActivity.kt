package com.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project.data.PasswordEntry
import com.example.project.ui.theme.ProjectTheme
import java.text.DateFormat
import java.util.Date

class StorageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProjectTheme {
                StorageScreen(
                    onNavigateUp = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageScreen(
    onNavigateUp: () -> Unit,
    storageViewModel: StorageViewModel = viewModel()
) {
    val entries by storageViewModel.entries.collectAsStateWithLifecycle()
    var entryToEdit by remember { mutableStateOf<PasswordEntry?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.lab3_storage_title)) },
                navigationIcon = {
                    TextButton(onClick = onNavigateUp) {
                        Text(stringResource(R.string.lab3_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        if (entries.isEmpty()) {
            Text(
                text = stringResource(R.string.lab3_storage_empty),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    StorageEntryCard(
                        entry = entry,
                        onEdit = { entryToEdit = entry },
                        onDelete = { storageViewModel.delete(entry) }
                    )
                }
            }
        }
    }

    entryToEdit?.let { editing ->
        EditEntryDialog(
            entry = editing,
            onDismiss = { entryToEdit = null },
            onConfirm = { updated ->
                storageViewModel.update(updated)
                entryToEdit = null
            }
        )
    }
}

@Composable
private fun StorageEntryCard(
    entry: PasswordEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(entry.createdAt) {
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(entry.createdAt))
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = entry.password,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(R.string.lab3_entry_meta, entry.visibilityMode, dateStr),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                OutlinedButton(onClick = onEdit) {
                    Text(stringResource(R.string.lab3_edit))
                }
                Button(onClick = onDelete) {
                    Text(stringResource(R.string.lab3_delete))
                }
            }
        }
    }
}

@Composable
private fun EditEntryDialog(
    entry: PasswordEntry,
    onDismiss: () -> Unit,
    onConfirm: (PasswordEntry) -> Unit
) {
    var text by remember(entry.id) { mutableStateOf(entry.password) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.lab3_edit_title)) },
        text = {
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        entry.copy(password = text)
                    )
                }
            ) {
                Text(stringResource(R.string.lab3_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.lab3_cancel_dialog))
            }
        }
    )
}
