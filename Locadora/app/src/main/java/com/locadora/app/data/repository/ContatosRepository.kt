package com.locadora.app.data.repository

import android.content.ContentResolver
import android.provider.ContactsContract
import com.locadora.app.domain.Contato
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Lê os contatos do aparelho via ContentResolver (ContactsContract). */
class ContatosRepository(private val resolver: ContentResolver) {

    suspend fun buscar(filtro: String): List<Contato> = withContext(Dispatchers.IO) {
        val phone = ContactsContract.CommonDataKinds.Phone
        val projecao = arrayOf(phone.CONTACT_ID, phone.DISPLAY_NAME, phone.NUMBER)
        val texto = filtro.trim()
        val selecao = if (texto.isEmpty()) null else "${phone.DISPLAY_NAME} LIKE ?"
        val args = if (texto.isEmpty()) null else arrayOf("%$texto%")
        val ordem = "${phone.DISPLAY_NAME} ASC"

        val lista = mutableListOf<Contato>()
        resolver.query(phone.CONTENT_URI, projecao, selecao, args, ordem)?.use { cursor ->
            val iId = cursor.getColumnIndexOrThrow(phone.CONTACT_ID)
            val iNome = cursor.getColumnIndexOrThrow(phone.DISPLAY_NAME)
            val iNumero = cursor.getColumnIndexOrThrow(phone.NUMBER)
            while (cursor.moveToNext()) {
                lista.add(
                    Contato(
                        id = cursor.getLong(iId),
                        nome = cursor.getString(iNome) ?: "",
                        telefone = cursor.getString(iNumero) ?: ""
                    )
                )
            }
        }
        lista.distinctBy { it.id }
    }
}
