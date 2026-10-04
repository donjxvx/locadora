package com.locadora.app.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun veiculoStatusParaString(status: StatusVeiculo): String = status.name

    @TypeConverter
    fun stringParaVeiculoStatus(valor: String): StatusVeiculo = StatusVeiculo.valueOf(valor)

    @TypeConverter
    fun locacaoStatusParaString(status: StatusLocacao): String = status.name

    @TypeConverter
    fun stringParaLocacaoStatus(valor: String): StatusLocacao = StatusLocacao.valueOf(valor)
}
