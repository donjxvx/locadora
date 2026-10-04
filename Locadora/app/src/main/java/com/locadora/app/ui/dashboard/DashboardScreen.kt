package com.locadora.app.ui.dashboard

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.locadora.app.data.local.LocacaoDetalhe
import com.locadora.app.domain.Formatters
import com.locadora.app.ui.appViewModelFactory
import com.locadora.app.ui.common.ErroBox
import com.locadora.app.ui.common.LoadingBox
import com.locadora.app.ui.common.MensagemBox
import com.locadora.app.ui.common.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNovaLocacao: () -> Unit,
    onFrota: () -> Unit,
    vm: DashboardViewModel = viewModel(factory = appViewModelFactory())
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val sincronizando by vm.sincronizando.collectAsStateWithLifecycle()
    val mensagem by vm.mensagem.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(mensagem) {
        mensagem?.let {
            snackbarHostState.showSnackbar(it)
            vm.consumirMensagem()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Locações ativas") },
                actions = {
                    TextButton(onClick = onFrota) { Text("Frota") }
                    IconButton(onClick = { vm.sincronizar() }, enabled = !sincronizando) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sincronizar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNovaLocacao,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nova locação") }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                is UiState.Loading -> LoadingBox()
                is UiState.Error -> ErroBox(s.message)
                is UiState.Success -> {
                    if (s.data.isEmpty()) {
                        MensagemBox("Nenhuma locação ativa.\nToque em \"Nova locação\" para começar.")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(s.data, key = { it.locacao.id }) { item ->
                                LocacaoCard(item = item, onFinalizar = { vm.finalizar(item.locacao) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocacaoCard(item: LocacaoDetalhe, onFinalizar: () -> Unit) {
    val l = item.locacao
    val dias = Formatters.diasFaltantes(l.dataPrevista)
    val atrasada = dias < 0
    val textoDias = when {
        dias < 0 -> "ATRASADA há ${-dias} dia(s)"
        dias == 0L -> "Devolução hoje"
        else -> "Faltam $dias dia(s)"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (atrasada) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "${item.veiculo.marca} ${item.veiculo.modelo}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text("Placa: ${item.veiculo.placa}")
            Text("Cliente: ${item.cliente.nome}")
            Text("Telefone: ${item.cliente.telefone}")
            Text("Saída: ${Formatters.formatarData(l.dataSaida)}")
            Text("Entrega prevista: ${Formatters.formatarData(l.dataPrevista)}")
            Text(
                textoDias,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text("Total estimado: ${Formatters.moeda(l.valorTotal)}")
            TextButton(onClick = onFinalizar) { Text("Finalizar locação") }
        }
    }
}
