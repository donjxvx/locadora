package com.locadora.app.ui.veiculos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.locadora.app.data.local.StatusVeiculo
import com.locadora.app.data.local.Veiculo
import com.locadora.app.data.repository.LocadoraRepository
import com.locadora.app.ui.common.UiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VeiculosViewModel(private val repository: LocadoraRepository) : ViewModel() {

    val state: StateFlow<UiState<List<Veiculo>>> = repository.veiculos()
        .map<List<Veiculo>, UiState<List<Veiculo>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Erro ao carregar veículos")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    /** Alterna entre DISPONIVEL e MANUTENCAO (veículo alugado não pode ser alterado). */
    fun alternarManutencao(veiculo: Veiculo) {
        val novo = when (veiculo.status) {
            StatusVeiculo.DISPONIVEL -> StatusVeiculo.MANUTENCAO
            StatusVeiculo.MANUTENCAO -> StatusVeiculo.DISPONIVEL
            StatusVeiculo.ALUGADO -> return
        }
        viewModelScope.launch { repository.alterarStatusVeiculo(veiculo.id, novo) }
    }
}
