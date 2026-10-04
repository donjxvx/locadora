package com.locadora.app

import android.content.Context
import androidx.room.Room
import com.locadora.app.data.local.AppDatabase
import com.locadora.app.data.remote.LocadoraApi
import com.locadora.app.data.remote.MockInterceptor
import com.locadora.app.data.repository.ContatosRepository
import com.locadora.app.data.repository.LocadoraRepository
import com.locadora.app.domain.AbrirLocacaoUseCase
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Injeção de dependência manual (sem Hilt, para manter o projeto simples). */
class AppContainer(context: Context) {

    private val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "locadora.db"
    ).build()

    private val api: LocadoraApi = Retrofit.Builder()
        .baseUrl("https://api.locadora.exemplo/")
        .client(OkHttpClient.Builder().addInterceptor(MockInterceptor()).build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(LocadoraApi::class.java)

    val repository = LocadoraRepository(database, api)
    val contatosRepository = ContatosRepository(context.applicationContext.contentResolver)
    val abrirLocacao = AbrirLocacaoUseCase(repository)
}
