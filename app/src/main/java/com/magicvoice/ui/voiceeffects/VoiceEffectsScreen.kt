package com.magicvoice.ui.voiceeffects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.magicvoice.domain.model.VoiceEffect

@Composable
fun VoiceEffectsScreen(onNavigateBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(VoiceEffect.getDefaultEffects()) { effect ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) { Text(effect.name, modifier = Modifier.padding(16.dp)) }
            }
        }
        Button(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}
