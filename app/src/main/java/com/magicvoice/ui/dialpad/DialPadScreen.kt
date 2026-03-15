package com.magicvoice.ui.dialpad

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DialPadScreen(onNavigateBack: () -> Unit, onCallInitiated: (String) -> Unit) {
    val value = remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(value = value.value, onValueChange = { value.value = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { onCallInitiated(value.value) }, modifier = Modifier.fillMaxWidth()) { Text("Call") }
        Button(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}
