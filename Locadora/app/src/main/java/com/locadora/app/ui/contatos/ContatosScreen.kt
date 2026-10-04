package com.locadora.app.ui.contatos

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.locadora.app.domain.Contato
import com.locadora.app.ui.appViewModelFactory
import com.locadora.app.ui.common.ErroBox
import com.locadora.app.ui.common.LoadingBox
import com.locadora.app.ui.common.MensagemBox
import com.locadora.app.ui.common.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContatosScreen(
    onVoltar: () -> Unit,
    onSelecionado: (Contato) -> Unit,
    vm: ContatosViewModel = viewModel(factory = appViewModelFactory())
) {
    val context = LocalContext.current

    var permitido by rememberSaveable {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var negado by rememberSaveable { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedida ->
        permitido = concedida
        negado = !concedida
    }

    LaunchedEffect(Unit) {
        if (!permitido) launcher.launch(Manifest.permission.READ_CONTACTS)
    }
    LaunchedEffect(permitido) {
        vm.setPermissao(permitido)
    }

    val query by vm.query.collectAsStateWithLifecycle()
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Selecionar cliente") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (!permitido) {
                PermissaoNegada(
                    jaNegou = negado,
                    onTentarNovamente = { launcher.launch(Manifest.permission.READ_CONTACTS) },
                    onAbrirConfiguracoes = {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                        context.startActivity(intent)
                    }
                )
            } else {
                OutlinedTextField(
                    value = query,
                    onValueChange = vm::onQuery,
                    label = { Text("Buscar por nome") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                )
                Box(Modifier.fillMaxSize()) {
                    when (val s = state) {
                        is UiState.Loading -> LoadingBox()
                        is UiState.Error -> ErroBox(s.message)
                        is UiState.Success -> {
                            if (s.data.isEmpty()) {
                                MensagemBox("Nenhum contato encontrado.")
                            } else {
                                LazyColumn(Modifier.fillMaxSize()) {
                                    items(s.data, key = { it.id }) { contato ->
                                        ListItem(
                                            headlineContent = { Text(contato.nome) },
                                            supportingContent = { Text(contato.telefone) },
                                            modifier = Modifier.clickable { onSelecionado(contato) }
                                        )
                                        HorizontalDivider()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissaoNegada(
    jaNegou: Boolean,
    onTentarNovamente: () -> Unit,
    onAbrirConfiguracoes: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (jaNegou) {
                "Sem a permissão de contatos não é possível buscar clientes na sua agenda. " +
                    "Conceda a permissão para continuar."
            } else {
                "Precisamos da permissão de leitura de contatos para buscar o cliente da locação."
            },
            textAlign = TextAlign.Center
        )
        Button(onClick = onTentarNovamente, modifier = Modifier.padding(top = 16.dp)) {
            Text("Tentar novamente")
        }
        if (jaNegou) {
            OutlinedButton(onClick = onAbrirConfiguracoes, modifier = Modifier.padding(top = 8.dp)) {
                Text("Abrir configurações do app")
            }
        }
    }
}
