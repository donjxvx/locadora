package com.locadora.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.locadora.app.data.local.Locacao
import com.locadora.app.data.local.LocacaoDetalhe
import com.locadora.app.data.repository.LocadoraRepository
import com.locadora.app.ui.common.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(private val repository: LocadoraRepository) : ViewModel() {

    val state: StateFlow<UiState<List<LocacaoDetalhe>>> = repository.locacoesAtivas()
        .map<List<LocacaoDetalhe>, UiState<List<LocacaoDetalhe>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Erro ao carregar locações")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    private val _sincronizando = MutableStateFlow(false)
    val sincronizando: StateFlow<Boolean> = _sincronizando.asStateFlow()

    private val _mensagem = MutableStateFlow<String?>(null)
    val mensagem: StateFlow<String?> = _mensagem.asStateFlow()

    fun sincronizar() {
        viewModelScope.launch {
            _sincronizando.value = true
            try {
                val novos = repository.sincronizar()
                _mensagem.value = "Sincronização concluída ($novos veículo(s) recebido(s))"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensagem.value = "Falha na sincronização: ${e.message ?: "erro desconhecido"}"
            } finally {
                _sincronizando.value = false
            }
        }
    }

    fun finalizar(locacao: Locacao) {
        viewModelScope.launch { repository.finalizarLocacao(locacao) }
    }

    fun consumirMensagem() {
        _mensagem.value = null
    }
}
