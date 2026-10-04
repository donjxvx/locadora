package com.locadora.app.ui.locacao

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.locadora.app.data.local.Veiculo
import com.locadora.app.domain.Contato
import com.locadora.app.domain.Formatters
import com.locadora.app.ui.appViewModelFactory
import com.locadora.app.ui.common.UiState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaLocacaoScreen(
    contatoSelecionado: Contato?,
    onSelecionarContato: () -> Unit,
    onVoltar: () -> Unit,
    onConcluido: () -> Unit,
    vm: NovaLocacaoViewModel = viewModel(factory = appViewModelFactory())
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val veiculos by vm.veiculos.collectAsStateWithLifecycle()

    var mostrarSaida by remember { mutableStateOf(false) }
    var mostrarPrevista by remember { mutableStateOf(false) }

    // RF03.5: recebe o contato escolhido na tela de contatos (via SavedStateHandle)
    LaunchedEffect(contatoSelecionado) {
        contatoSelecionado?.let { vm.setCliente(it) }
    }
    // RF04.5: após salvar, volta ao Dashboard
    LaunchedEffect(state.salvo) {
        if (state.salvo) onConcluido()
    }

    if (mostrarSaida) {
        DataPickerDialog(
            inicial = state.saida,
            onDismiss = { mostrarSaida = false },
            onConfirmar = { vm.setSaida(it) }
        )
    }
    if (mostrarPrevista) {
        DataPickerDialog(
            inicial = state.prevista ?: state.saida,
            onDismiss = { mostrarPrevista = false },
            onConfirmar = { vm.setPrevista(it) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nova locação") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("1. Escolha o veículo", style = MaterialTheme.typography.titleMedium)
            }
            when (val v = veiculos) {
                is UiState.Loading -> item { CircularProgressIndicator() }
                is UiState.Error -> item {
                    Text(v.message, color = MaterialTheme.colorScheme.error)
                }
                is UiState.Success -> {
                    if (v.data.isEmpty()) {
                        item { Text("Nenhum veículo disponível no momento.") }
                    } else {
                        items(v.data, key = { it.id }) { veiculo ->
                            VeiculoOpcao(
                                veiculo = veiculo,
                                selecionado = state.veiculo?.id == veiculo.id,
                                onClick = { vm.selecionarVeiculo(veiculo) }
                            )
                        }
                    }
                }
            }

            item {
                Text("2. Cliente", style = MaterialTheme.typography.titleMedium)
            }
            item {
                OutlinedButton(onClick = onSelecionarContato, modifier = Modifier.fillMaxWidth()) {
                    val c = state.cliente
                    Text(if (c == null) "Selecionar da agenda de contatos" else "${c.nome} • ${c.telefone}")
                }
            }

            item {
                Text("3. Período", style = MaterialTheme.typography.titleMedium)
            }
            item {
                OutlinedButton(onClick = { mostrarSaida = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Data de saída: ${state.saida?.let { Formatters.formatarData(it) } ?: "selecionar"}")
                }
            }
            item {
                OutlinedButton(onClick = { mostrarPrevista = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Entrega prevista: ${state.prevista?.let { Formatters.formatarData(it) } ?: "selecionar"}"
                    )
                }
            }

            item {
                val dias = state.dias
                val total = state.total
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (dias != null && dias <= 0L) {
                        Text(
                            "A entrega deve ser depois da saída.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (total != null && dias != null) {
                        Text("Dias: $dias")
                        Text(
                            "Total estimado: ${Formatters.moeda(total)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            state.erro?.let { erro ->
                item { Text(erro, color = MaterialTheme.colorScheme.error) }
            }

            item {
                Button(
                    onClick = { vm.confirmar() },
                    enabled = state.podeConfirmar,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (state.salvando) "Salvando..." else "Confirmar locação")
                }
            }
        }
    }
}

@Composable
private fun VeiculoOpcao(veiculo: Veiculo, selecionado: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (selecionado) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "${veiculo.marca} ${veiculo.modelo}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text("Placa: ${veiculo.placa} • ${Formatters.moeda(veiculo.valorDiaria)}/dia")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DataPickerDialog(
    inicial: LocalDate?,
    onDismiss: () -> Unit,
    onConfirmar: (LocalDate) -> Unit
) {
    val base = inicial ?: LocalDate.now()
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = base.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                pickerState.selectedDateMillis?.let { millis ->
                    onConfirmar(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                }
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    ) {
        DatePicker(state = pickerState)
    }
}
