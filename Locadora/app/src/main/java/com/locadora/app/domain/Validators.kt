package com.locadora.app.domain

object Validators {
    // Padrão antigo (AAA-1234) ou Mercosul (AAA1A23)
    private val PLACA_ANTIGA = Regex("^[A-Z]{3}-?[0-9]{4}$")
    private val PLACA_MERCOSUL = Regex("^[A-Z]{3}[0-9][A-Z][0-9]{2}$")

    fun placaValida(placa: String): Boolean {
        val p = placa.trim().uppercase()
        return PLACA_ANTIGA.matches(p) || PLACA_MERCOSUL.matches(p)
    }

    fun diariaValida(texto: String): Boolean {
        val valor = texto.trim().replace(',', '.').toDoubleOrNull()
        return valor != null && valor > 0.0
    }
}
