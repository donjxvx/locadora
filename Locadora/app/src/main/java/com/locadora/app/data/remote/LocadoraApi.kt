package com.locadora.app.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class VeiculoDto(
    val marca: String,
    val modelo: String,
    val placa: String,
    val ano: Int,
    val valorDiaria: Double
)

data class LocacaoDto(
    val id: Long,
    val veiculoId: Long,
    val clienteId: Long,
    val dataSaida: Long,
    val dataPrevista: Long,
    val valorTotal: Double,
    val status: String
)

data class SyncResponse(
    val sucesso: Boolean,
    val mensagem: String
)

interface LocadoraApi {
    @GET("veiculos")
    suspend fun buscarVeiculos(): List<VeiculoDto>

    @POST("locacoes/sync")
    suspend fun sincronizarLocacoes(@Body locacoes: List<LocacaoDto>): SyncResponse
}
