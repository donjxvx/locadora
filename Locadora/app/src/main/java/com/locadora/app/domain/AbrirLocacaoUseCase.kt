package com.locadora.app.domain

import com.locadora.app.data.local.ClienteCache
import com.locadora.app.data.local.Locacao
import com.locadora.app.data.local.StatusVeiculo
import com.locadora.app.data.local.Veiculo
import com.locadora.app.data.repository.LocadoraRepository
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class AbrirLocacaoUseCase(private val repository: LocadoraRepository) {

    suspend operator fun invoke(
        veiculo: Veiculo,
        contato: Contato,
        saida: LocalDate,
        prevista: LocalDate
    ): Result<Unit> = runCatching {
        require(veiculo.status == StatusVeiculo.DISPONIVEL) { "Veículo indisponível" }
        val dias = ChronoUnit.DAYS.between(saida, prevista)
        require(dias > 0) { "A data de entrega deve ser posterior à data de saída" }

        repository.abrirLocacao(
            cliente = ClienteCache(
                contactId = contato.id,
                nome = contato.nome,
                telefone = contato.telefone
            ),
            locacao = Locacao(
                veiculoId = veiculo.id,
                clienteId = contato.id,
                dataSaida = saida.toEpochDay(),
                dataPrevista = prevista.toEpochDay(),
                valorTotal = dias * veiculo.valorDiaria
            )
        )
    }
}
