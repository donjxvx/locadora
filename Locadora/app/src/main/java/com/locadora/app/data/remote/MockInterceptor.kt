package com.locadora.app.data.remote

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * Simula um servidor REST: responde localmente às chamadas do Retrofit,
 * sem depender de uma API real.
 */
class MockInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val caminho = request.url.encodedPath

        val corpo = when {
            request.method == "GET" && caminho.endsWith("/veiculos") ->
                """[
                    {"marca":"Fiat","modelo":"Mobi","placa":"MOB-1A23","ano":2022,"valorDiaria":110.0},
                    {"marca":"Chevrolet","modelo":"Onix","placa":"ONX-2B34","ano":2023,"valorDiaria":150.0}
                ]"""
            request.method == "POST" && caminho.endsWith("/locacoes/sync") ->
                """{"sucesso":true,"mensagem":"Locações sincronizadas"}"""
            else -> "{}"
        }

        return Response.Builder()
            .code(200)
            .message("OK")
            .protocol(Protocol.HTTP_1_1)
            .request(request)
            .body(corpo.toResponseBody("application/json".toMediaType()))
            .build()
    }
}
