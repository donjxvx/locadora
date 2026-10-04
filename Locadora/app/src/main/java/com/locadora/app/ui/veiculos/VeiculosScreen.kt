package com.locadora.app.ui.veiculos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.locadora.app.data.local.StatusVeiculo
import com.locadora.app.data.local.Veiculo
import com.locadora.app.domain.Formatters
import com.locadora.app.ui.appViewModelFactory
import com.locadora.app.ui.common.ErroBox
import com.locadora.app.ui.common.LoadingBox
import com.locadora.app.ui.common.MensagemBox
import com.locadora.app.ui.common.UiState
import com.locadora.app.ui.common.rotulo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VeiculosScreen(
    onVoltar: () -> Unit,
    onNovoVeiculo: () -> Unit,
    vm: VeiculosViewModel = viewModel(factory = appViewModelFactory())
) {
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Frota") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNovoVeiculo,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Novo veículo") }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                is UiState.Loading -> LoadingBox()
                is UiState.Error -> ErroBox(s.message)
                is UiState.Success -> {
                    if (s.data.isEmpty()) {
                        MensagemBox("Nenhum veículo cadastrado.\nToque em \"Novo veículo\".")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(s.data, key = { it.id }) { veiculo ->
                                VeiculoCard(veiculo, onAlternar = { vm.alternarManutencao(veiculo) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VeiculoCard(veiculo: Veiculo, onAlternar: () -> Unit) {
    val corStatus: Color = when (veiculo.status) {
        StatusVeiculo.DISPONIVEL -> MaterialTheme.colorScheme.primary
        StatusVeiculo.ALUGADO -> MaterialTheme.colorScheme.tertiary
        StatusVeiculo.MANUTENCAO -> MaterialTheme.colorScheme.error
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "${veiculo.marca} ${veiculo.modelo}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text("Placa: ${veiculo.placa} • Ano: ${veiculo.ano}")
            Text("Diária: ${Formatters.moeda(veiculo.valorDiaria)}")
            Text(
                "Status: ${veiculo.status.rotulo()}",
                color = corStatus,
                fontWeight = FontWeight.Bold
            )
            when (veiculo.status) {
                StatusVeiculo.DISPONIVEL ->
                    TextButton(onClick = onAlternar) { Text("Enviar para manutenção") }
                StatusVeiculo.MANUTENCAO ->
                    TextButton(onClick = onAlternar) { Text("Liberar da manutenção") }
                StatusVeiculo.ALUGADO -> Unit
            }
        }
    }
}
