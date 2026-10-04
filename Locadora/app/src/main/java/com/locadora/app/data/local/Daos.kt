package com.locadora.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface VeiculoDao {
    @Query("SELECT * FROM veiculos ORDER BY marca, modelo")
    fun todos(): Flow<List<Veiculo>>

    @Query("SELECT * FROM veiculos WHERE status = :status ORDER BY marca, modelo")
    fun porStatus(status: StatusVeiculo): Flow<List<Veiculo>>

    /** Retorna -1 quando a placa já existe (índice único + IGNORE). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserir(veiculo: Veiculo): Long

    @Query("UPDATE veiculos SET status = :status WHERE id = :id")
    suspend fun atualizarStatus(id: Long, status: StatusVeiculo)
}

@Dao
abstract class LocacaoDao {

    @Transaction
    @Query("SELECT * FROM locacoes WHERE status = 'ATIVA' ORDER BY dataPrevista ASC")
    abstract fun ativas(): Flow<List<LocacaoDetalhe>>

    @Query("SELECT * FROM locacoes")
    abstract suspend fun listarTodas(): List<Locacao>

    @Upsert
    abstract suspend fun salvarCliente(cliente: ClienteCache)

    @Insert
    abstract suspend fun inserir(locacao: Locacao): Long

    @Query("UPDATE locacoes SET status = :status WHERE id = :id")
    abstract suspend fun atualizarStatus(id: Long, status: StatusLocacao)

    @Query("UPDATE veiculos SET status = :status WHERE id = :id")
    abstract suspend fun atualizarStatusVeiculo(id: Long, status: StatusVeiculo)

    /** RF04.5: salva cliente, locação ATIVA e marca o veículo como ALUGADO, tudo atomicamente. */
    @Transaction
    open suspend fun abrir(cliente: ClienteCache, locacao: Locacao): Long {
        salvarCliente(cliente)
        val id = inserir(locacao.copy(status = StatusLocacao.ATIVA))
        atualizarStatusVeiculo(locacao.veiculoId, StatusVeiculo.ALUGADO)
        return id
    }

    @Transaction
    open suspend fun finalizar(locacao: Locacao) {
        atualizarStatus(locacao.id, StatusLocacao.FINALIZADA)
        atualizarStatusVeiculo(locacao.veiculoId, StatusVeiculo.DISPONIVEL)
    }
}
