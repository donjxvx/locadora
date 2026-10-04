package com.locadora.app.ui.contatos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.locadora.app.data.repository.ContatosRepository
import com.locadora.app.domain.Contato
import com.locadora.app.ui.common.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class ContatosViewModel(private val repository: ContatosRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _permissao = MutableStateFlow(false)

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val state: StateFlow<UiState<List<Contato>>> =
        combine(_query, _permissao) { q, p -> Pair(q, p) }
            .debounce(200)
            .flatMapLatest { par ->
                if (!par.second) {
                    flowOf<UiState<List<Contato>>>(UiState.Loading)
                } else {
                    flow<UiState<List<Contato>>> { emit(buscarSeguro(par.first)) }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    fun onQuery(texto: String) {
        _query.value = texto
    }

    fun setPermissao(concedida: Boolean) {
        _permissao.value = concedida
    }

    private suspend fun buscarSeguro(filtro: String): UiState<List<Contato>> =
        try {
            UiState.Success(repository.buscar(filtro))
        } catch (e: CancellationException) {
            throw e
        } catch (e: SecurityException) {
            UiState.Error("Sem permissão para ler os contatos")
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Erro ao ler contatos")
        }
}
