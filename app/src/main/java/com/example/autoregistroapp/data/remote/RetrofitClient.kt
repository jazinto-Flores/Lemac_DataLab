package com.example.autoregistroapp.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Configura Retrofit una sola vez: Base URL, cliente HTTP (con la API simulada) y conversor JSON. */
object RetrofitClient {

    // La Base URL de Retrofit debe terminar en "/"
    private const val BASE_URL = "https://api.lemacdatalab.test/"

    val api: ApiService by lazy {
        val cliente = OkHttpClient.Builder()
            .addInterceptor(MockApiInterceptor())
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}