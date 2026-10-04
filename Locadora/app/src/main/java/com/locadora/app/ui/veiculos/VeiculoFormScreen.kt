package com.locadora.app.ui.veiculos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.locadora.app.ui.appViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VeiculoFormScreen(
    onVoltar: () -> Unit,
    vm: VeiculoFormViewModel = viewModel(factory = appViewModelFactory())
) {
    val s by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(s.salvo) {
        if (s.salvo) onVoltar()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Novo veículo") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = s.marca,
                onValueChange = vm::onMarca,
                label = { Text("Marca") },
                isError = s.erroMarca != null,
                supportingText = { s.erroMarca?.let { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = s.modelo,
                onValueChange = vm::onModelo,
                label = { Text("Modelo") },
                isError = s.erroModelo != null,
                supportingText = { s.erroModelo?.let { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = s.placa,
                onValueChange = vm::onPlaca,
                label = { Text("Placa (AAA-1234 ou AAA1A23)") },
                isError = s.erroPlaca != null,
                supportingText = { s.erroPlaca?.let { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = s.ano,
                onValueChange = vm::onAno,
                label = { Text("Ano") },
                isError = s.erroAno != null,
                supportingText = { s.erroAno?.let { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = s.diaria,
                onValueChange = vm::onDiaria,
                label = { Text("Valor da diária (R$)") },
                isError = s.erroDiaria != null,
                supportingText = { s.erroDiaria?.let { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { vm.salvar() },
                enabled = !s.salvando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (s.salvando) "Salvando..." else "Salvar")
            }
        }
    }
}
