package com.locadora.app.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

enum class StatusVeiculo { DISPONIVEL, ALUGADO, MANUTENCAO }

enum class StatusLocacao { ATIVA, FINALIZADA }

@Entity(
    tableName = "veiculos",
    indices = [Index(value = ["placa"], unique = true)]
)
data class Veiculo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val marca: String,
    val modelo: String,
    val placa: String,
    val ano: Int,
    val valorDiaria: Double,
    val status: StatusVeiculo = StatusVeiculo.DISPONIVEL
)

@Entity(tableName = "clientes_cache")
data class ClienteCache(
    @PrimaryKey val contactId: Long,
    val nome: String,
    val telefone: String
)

@Entity(
    tableName = "locacoes",
    foreignKeys = [
        ForeignKey(
            entity = Veiculo::class,
            parentColumns = ["id"],
            childColumns = ["veiculoId"]
        ),
        ForeignKey(
            entity = ClienteCache::class,
            parentColumns = ["contactId"],
            childColumns = ["clienteId"]
        )
    ],
    indices = [Index("veiculoId"), Index("clienteId")]
)
data class Locacao(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val veiculoId: Long,
    val clienteId: Long,
    /** Datas guardadas como epochDay (LocalDate.toEpochDay) */
    val dataSaida: Long,
    val dataPrevista: Long,
    val valorTotal: Double,
    val status: StatusLocacao = StatusLocacao.ATIVA
)

/** POJO com @Relation: locação + veículo + cliente */
data class LocacaoDetalhe(
    @Embedded val locacao: Locacao,
    @Relation(parentColumn = "veiculoId", entityColumn = "id")
    val veiculo: Veiculo,
    @Relation(parentColumn = "clienteId", entityColumn = "contactId")
    val cliente: ClienteCache
)
