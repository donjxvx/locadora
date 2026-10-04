package com.locadora.app.domain

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object Formatters {
    private val dataFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val localeBr: Locale = Locale.forLanguageTag("pt-BR")

    fun formatarData(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).format(dataFormatter)

    fun formatarData(data: LocalDate): String = data.format(dataFormatter)

    fun moeda(valor: Double): String = NumberFormat.getCurrencyInstance(localeBr).format(valor)

    /** Dias que faltam para a devolução (negativo = em atraso). */
    fun diasFaltantes(dataPrevistaEpochDay: Long, hoje: LocalDate = LocalDate.now()): Long =
        dataPrevistaEpochDay - hoje.toEpochDay()
}
