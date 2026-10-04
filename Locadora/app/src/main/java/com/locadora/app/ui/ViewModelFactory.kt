package com.locadora.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.locadora.app.AppContainer
import com.locadora.app.LocadoraApp
import com.locadora.app.ui.contatos.ContatosViewModel
import com.locadora.app.ui.dashboard.DashboardViewModel
import com.locadora.app.ui.locacao.NovaLocacaoViewModel
import com.locadora.app.ui.veiculos.VeiculoFormViewModel
import com.locadora.app.ui.veiculos.VeiculosViewModel

class ViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val vm: ViewModel = when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
                DashboardViewModel(container.repository)
            modelClass.isAssignableFrom(VeiculosViewModel::class.java) ->
                VeiculosViewModel(container.repository)
            modelClass.isAssignableFrom(VeiculoFormViewModel::class.java) ->
                VeiculoFormViewModel(container.repository)
            modelClass.isAssignableFrom(ContatosViewModel::class.java) ->
                ContatosViewModel(container.contatosRepository)
            modelClass.isAssignableFrom(NovaLocacaoViewModel::class.java) ->
                NovaLocacaoViewModel(container.repository, container.abrirLocacao)
            else -> throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
        }
        return vm as T
    }
}

@Composable
fun appViewModelFactory(): ViewModelFactory {
    val app = LocalContext.current.applicationContext as LocadoraApp
    return remember { ViewModelFactory(app.container) }
}
