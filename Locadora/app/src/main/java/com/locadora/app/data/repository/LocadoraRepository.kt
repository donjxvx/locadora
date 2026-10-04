package com.locadora.app.data.repository

import com.locadora.app.data.local.AppDatabase
import com.locadora.app.data.local.ClienteCache
import com.locadora.app.data.local.Locacao
import com.locadora.app.data.local.LocacaoDetalhe
import com.locadora.app.data.local.StatusVeiculo
import com.locadora.app.data.local.Veiculo
import com.locadora.app.data.remote.LocacaoDto
import com.locadora.app.data.remote.LocadoraApi
import kotlinx.coroutines.flow.Flow

class LocadoraRepository(
    db: AppDatabase,
    private val api: LocadoraApi
) {
    private val veiculoDao = db.veiculoDao()
    private val locacaoDao = db.locacaoDao()

    fun veiculos(): Flow<List<Veiculo>> = veiculoDao.todos()

    fun veiculosDisponiveis(): Flow<List<Veiculo>> =
        veiculoDao.porStatus(StatusVeiculo.DISPONIVEL)

    /** @return false se a placa já estiver cadastrada */
    suspend fun cadastrarVeiculo(veiculo: Veiculo): Boolean =
        veiculoDao.inserir(veiculo) != -1L

    suspend fun alterarStatusVeiculo(id: Long, status: StatusVeiculo) =
        veiculoDao.atualizarStatus(id, status)

    fun locacoesAtivas(): Flow<List<LocacaoDetalhe>> = locacaoDao.ativas()

    suspend fun abrirLocacao(cliente: ClienteCache, locacao: Locacao) {
        locacaoDao.abrir(cliente, locacao)
    }

    suspend fun finalizarLocacao(locacao: Locacao) {
        locacaoDao.finalizar(locacao)
    }

    /** Baixa veículos do servidor (mock) e envia as locações locais. */
    suspend fun sincronizar(): Int {
        val remotos = api.buscarVeiculos()
        remotos.forEach {
            veiculoDao.inserir(
                Veiculo(
                    marca = it.marca,
                    modelo = it.modelo,
                    placa = it.placa,
                    ano = it.ano,
                    valorDiaria = it.valorDiaria
                )
            )
        }
        val locais = locacaoDao.listarTodas().map {
            LocacaoDto(
                id = it.id,
                veiculoId = it.veiculoId,
                clienteId = it.clienteId,
                dataSaida = it.dataSaida,
                dataPrevista = it.dataPrevista,
                valorTotal = it.valorTotal,
                status = it.status.name
            )
        }
        api.sincronizarLocacoes(locais)
        return remotos.size
    }
}
