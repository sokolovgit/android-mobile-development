package com.example.app.lab2

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.app.R

@Composable
fun PasswordInputScreen(
    viewModel: PasswordViewModel,
    onNavigateToResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val visualTransformation: VisualTransformation = when (uiState.visibilityMode) {
        PasswordVisibilityMode.ShowCharacters -> VisualTransformation.None
        PasswordVisibilityMode.ShowAsterisks -> PasswordVisualTransformation()
    }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.lab2_password_hint),
            style = MaterialTheme.typography.titleMedium
        )
        TextField(
            value = uiState.password,
            onValueChange = viewModel::updatePassword,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = visualTransformation,
            singleLine = true
        )

        Text(
            text = stringResource(R.string.lab2_show_characters),
            style = MaterialTheme.typography.titleSmall
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RadioButton(
                selected = uiState.visibilityMode == PasswordVisibilityMode.ShowCharacters,
                onClick = { viewModel.setVisibilityMode(PasswordVisibilityMode.ShowCharacters) }
            )
            Text(stringResource(R.string.lab2_show_characters))
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RadioButton(
                selected = uiState.visibilityMode == PasswordVisibilityMode.ShowAsterisks,
                onClick = { viewModel.setVisibilityMode(PasswordVisibilityMode.ShowAsterisks) }
            )
            Text(stringResource(R.string.lab2_show_asterisks))
        }

        Button(
            onClick = {
                if (viewModel.submitPassword()) {
                    onNavigateToResult()
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.lab2_complete_all_data),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.lab2_ok))
        }
    }
}
