package com.example.app.lab2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.app.R

@Composable
fun PasswordResultScreen(
    viewModel: PasswordViewModel,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val submittedPassword = uiState.submittedPassword ?: ""

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.lab2_result),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = submittedPassword,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                viewModel.cancelAndClear()
                onCancel()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.lab2_cancel))
        }
    }
}
