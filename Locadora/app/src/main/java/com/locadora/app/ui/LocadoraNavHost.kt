package com.locadora.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.locadora.app.domain.Contato
import com.locadora.app.ui.contatos.ContatosScreen
import com.locadora.app.ui.dashboard.DashboardScreen
import com.locadora.app.ui.locacao.NovaLocacaoScreen
import com.locadora.app.ui.veiculos.VeiculoFormScreen
import com.locadora.app.ui.veiculos.VeiculosScreen

object Routes {
    const val DASHBOARD = "dashboard"
    const val VEICULOS = "veiculos"
    const val VEICULO_FORM = "veiculo_form"
    const val NOVA_LOCACAO = "nova_locacao"
    const val CONTATOS = "contatos"

    // Chaves do resultado devolvido pela tela de contatos (SavedStateHandle)
    const val KEY_CONTATO_ID = "contato_id"
    const val KEY_CONTATO_NOME = "contato_nome"
    const val KEY_CONTATO_TELEFONE = "contato_telefone"
}

@Composable
fun LocadoraNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.DASHBOARD) {

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onNovaLocacao = { navController.navigate(Routes.NOVA_LOCACAO) },
                onFrota = { navController.navigate(Routes.VEICULOS) }
            )
        }

        composable(Routes.VEICULOS) {
            VeiculosScreen(
                onVoltar = { navController.popBackStack() },
                onNovoVeiculo = { navController.navigate(Routes.VEICULO_FORM) }
            )
        }

        composable(Routes.VEICULO_FORM) {
            VeiculoFormScreen(onVoltar = { navController.popBackStack() })
        }

        composable(Routes.NOVA_LOCACAO) { entry ->
            val handle = entry.savedStateHandle
            val id by handle.getStateFlow<Long?>(Routes.KEY_CONTATO_ID, null)
                .collectAsStateWithLifecycle()
            val nome by handle.getStateFlow<String?>(Routes.KEY_CONTATO_NOME, null)
                .collectAsStateWithLifecycle()
            val telefone by handle.getStateFlow<String?>(Routes.KEY_CONTATO_TELEFONE, null)
                .collectAsStateWithLifecycle()

            val idAtual = id
            val nomeAtual = nome
            val telefoneAtual = telefone
            val contato: Contato? =
                if (idAtual != null && nomeAtual != null && telefoneAtual != null) {
                    Contato(idAtual, nomeAtual, telefoneAtual)
                } else {
                    null
                }

            NovaLocacaoScreen(
                contatoSelecionado = contato,
                onSelecionarContato = { navController.navigate(Routes.CONTATOS) },
                onVoltar = { navController.popBackStack() },
                onConcluido = { navController.popBackStack(Routes.DASHBOARD, false) }
            )
        }

        composable(Routes.CONTATOS) {
            ContatosScreen(
                onVoltar = { navController.popBackStack() },
                onSelecionado = { contato ->
                    val anterior = navController.previousBackStackEntry?.savedStateHandle
                    anterior?.set(Routes.KEY_CONTATO_ID, contato.id)
                    anterior?.set(Routes.KEY_CONTATO_NOME, contato.nome)
                    anterior?.set(Routes.KEY_CONTATO_TELEFONE, contato.telefone)
                    navController.popBackStack()
                }
            )
        }
    }
}
