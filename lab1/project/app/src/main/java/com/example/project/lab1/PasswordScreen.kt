package com.example.project.lab1

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.project.R

enum class PasswordVisibilityMode {
    ShowCharacters,
    ShowAsterisks
}

@Composable
fun PasswordScreen(
    modifier: Modifier = Modifier
) {
    var password by remember { mutableStateOf("") }
    var visibilityMode by remember { mutableStateOf(PasswordVisibilityMode.ShowAsterisks) }
    var resultText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val visualTransformation: VisualTransformation = when (visibilityMode) {
        PasswordVisibilityMode.ShowCharacters -> VisualTransformation.None
        PasswordVisibilityMode.ShowAsterisks -> PasswordVisualTransformation()
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.lab1_password_hint),
            style = MaterialTheme.typography.titleMedium
        )
        TextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = visualTransformation,
            singleLine = true
        )

        Text(
            text = stringResource(R.string.lab1_show_characters),
            style = MaterialTheme.typography.titleSmall
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RadioButton(
                selected = visibilityMode == PasswordVisibilityMode.ShowCharacters,
                onClick = { visibilityMode = PasswordVisibilityMode.ShowCharacters }
            )
            Text(stringResource(R.string.lab1_show_characters))
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RadioButton(
                selected = visibilityMode == PasswordVisibilityMode.ShowAsterisks,
                onClick = { visibilityMode = PasswordVisibilityMode.ShowAsterisks }
            )
            Text(stringResource(R.string.lab1_show_asterisks))
        }

        Button(
            onClick = {
                if (password.isNotBlank()) {
                    resultText = password
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.lab1_complete_all_data),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.lab1_ok))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.lab1_result),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = resultText,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
