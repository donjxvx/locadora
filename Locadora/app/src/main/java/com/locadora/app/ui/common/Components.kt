package com.locadora.app.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.locadora.app.data.local.StatusVeiculo

@Composable
fun LoadingBox(modifier: Modifier = Modifier.fillMaxSize()) {
    Box(modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun MensagemBox(
    texto: String,
    modifier: Modifier = Modifier.fillMaxSize(),
    cor: Color = Color.Unspecified
) {
    Box(modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Text(texto, textAlign = TextAlign.Center, color = cor)
    }
}

@Composable
fun ErroBox(texto: String) {
    MensagemBox(texto = texto, cor = MaterialTheme.colorScheme.error)
}

fun StatusVeiculo.rotulo(): String = when (this) {
    StatusVeiculo.DISPONIVEL -> "Disponível"
    StatusVeiculo.ALUGADO -> "Alugado"
    StatusVeiculo.MANUTENCAO -> "Manutenção"
}
