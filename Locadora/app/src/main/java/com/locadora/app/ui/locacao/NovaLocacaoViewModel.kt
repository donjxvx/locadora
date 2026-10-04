package com.locadora.app.ui.locacao

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.locadora.app.data.local.Veiculo
import com.locadora.app.data.repository.LocadoraRepository
import com.locadora.app.domain.AbrirLocacaoUseCase
import com.locadora.app.domain.Contato
import com.locadora.app.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class NovaLocacaoState(
    val veiculo: Veiculo? = null,
    val cliente: Contato? = null,
    val saida: LocalDate? = LocalDate.now(),
    val prevista: LocalDate? = null,
    val salvando: Boolean = false,
    val salvo: Boolean = false,
    val erro: String? = null
) {
    val dias: Long?
        get() {
            val s = saida
            val p = prevista
            return if (s != null && p != null) ChronoUnit.DAYS.between(s, p) else null
        }

    val periodoValido: Boolean get() = (dias ?: 0L) > 0L

    /** RF04.4: Dias x Valor da diária */
    val total: Double?
        get() {
            val d = dias
            val v = veiculo
            return if (d != null && d > 0L && v != null) d * v.valorDiaria else null
        }

    val podeConfirmar: Boolean
        get() = veiculo != null && cliente != null && periodoValido && !salvando
}

class NovaLocacaoViewModel(
    repository: LocadoraRepository,
    private val abrirLocacao: AbrirLocacaoUseCase
) : ViewModel() {

    /** RF04.1: somente veículos DISPONÍVEIS */
    val veiculos: StateFlow<UiState<List<Veiculo>>> = repository.veiculosDisponiveis()
        .map<List<Veiculo>, UiState<List<Veiculo>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Erro ao carregar veículos")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    private val _state = MutableStateFlow(NovaLocacaoState())
    val state: StateFlow<NovaLocacaoState> = _state.asStateFlow()

    fun selecionarVeiculo(veiculo: Veiculo) = _state.update { it.copy(veiculo = veiculo, erro = null) }

    fun setCliente(contato: Contato) = _state.update { it.copy(cliente = contato, erro = null) }

    fun setSaida(data: LocalDate) = _state.update { it.copy(saida = data, erro = null) }

    fun setPrevista(data: LocalDate) = _state.update { it.copy(prevista = data, erro = null) }

    fun confirmar() {
        val s = _state.value
        val veiculo = s.veiculo
        val cliente = s.cliente
        val saida = s.saida
        val prevista = s.prevista
        if (veiculo == null || cliente == null || saida == null || prevista == null) {
            _state.update { it.copy(erro = "Preencha veículo, cliente e datas") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(salvando = true, erro = null) }
            abrirLocacao(veiculo, cliente, saida, prevista)
                .onSuccess { _state.update { it.copy(salvando = false, salvo = true) } }
                .onFailure { e ->
                    _state.update {
                        it.copy(salvando = false, erro = e.message ?: "Erro ao salvar locação")
                    }
                }
        }
    }
}
