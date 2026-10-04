package com.locadora.app.ui.veiculos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.locadora.app.data.local.StatusVeiculo
import com.locadora.app.data.local.Veiculo
import com.locadora.app.data.repository.LocadoraRepository
import com.locadora.app.domain.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Year

data class VeiculoFormState(
    val marca: String = "",
    val modelo: String = "",
    val placa: String = "",
    val ano: String = "",
    val diaria: String = "",
    val erroMarca: String? = null,
    val erroModelo: String? = null,
    val erroPlaca: String? = null,
    val erroAno: String? = null,
    val erroDiaria: String? = null,
    val salvando: Boolean = false,
    val salvo: Boolean = false
)

class VeiculoFormViewModel(private val repository: LocadoraRepository) : ViewModel() {

    private val _state = MutableStateFlow(VeiculoFormState())
    val state: StateFlow<VeiculoFormState> = _state.asStateFlow()

    fun onMarca(v: String) = _state.update { it.copy(marca = v, erroMarca = null) }
    fun onModelo(v: String) = _state.update { it.copy(modelo = v, erroModelo = null) }
    fun onPlaca(v: String) = _state.update { it.copy(placa = v.uppercase(), erroPlaca = null) }
    fun onAno(v: String) = _state.update { it.copy(ano = v.filter { c -> c.isDigit() }, erroAno = null) }
    fun onDiaria(v: String) = _state.update { it.copy(diaria = v, erroDiaria = null) }

    fun salvar() {
        val s = _state.value
        val anoInt = s.ano.toIntOrNull()
        val anoMax = Year.now().value + 1

        val erroMarca = if (s.marca.isBlank()) "Campo obrigatório" else null
        val erroModelo = if (s.modelo.isBlank()) "Campo obrigatório" else null
        val erroPlaca = when {
            s.placa.isBlank() -> "Campo obrigatório"
            !Validators.placaValida(s.placa) -> "Formato inválido (AAA-1234 ou AAA1A23)"
            else -> null
        }
        val erroAno = when {
            s.ano.isBlank() -> "Campo obrigatório"
            anoInt == null || anoInt < 1950 || anoInt > anoMax -> "Ano inválido"
            else -> null
        }
        val erroDiaria = when {
            s.diaria.isBlank() -> "Campo obrigatório"
            !Validators.diariaValida(s.diaria) -> "Informe um valor numérico positivo"
            else -> null
        }

        if (listOf(erroMarca, erroModelo, erroPlaca, erroAno, erroDiaria).any { it != null }) {
            _state.update {
                it.copy(
                    erroMarca = erroMarca,
                    erroModelo = erroModelo,
                    erroPlaca = erroPlaca,
                    erroAno = erroAno,
                    erroDiaria = erroDiaria
                )
            }
            return
        }

        val veiculo = Veiculo(
            marca = s.marca.trim(),
            modelo = s.modelo.trim(),
            placa = s.placa.trim().uppercase(),
            ano = anoInt ?: return,
            valorDiaria = s.diaria.trim().replace(',', '.').toDouble(),
            status = StatusVeiculo.DISPONIVEL
        )

        viewModelScope.launch {
            _state.update { it.copy(salvando = true) }
            val inserido = repository.cadastrarVeiculo(veiculo)
            if (inserido) {
                _state.update { it.copy(salvando = false, salvo = true) }
            } else {
                _state.update { it.copy(salvando = false, erroPlaca = "Placa já cadastrada") }
            }
        }
    }
}
