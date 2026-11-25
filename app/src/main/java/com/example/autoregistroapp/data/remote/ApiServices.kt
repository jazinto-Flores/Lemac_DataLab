package com.example.autoregistroapp.data.remote

import retrofit2.http.GET

/** Contrato de la API: Retrofit genera la implementación a partir de estas anotaciones. */
interface ApiService {

    // Base URL + "catalogos" = https://api.lemacdatalab.test/catalogos
    @GET("catalogos")
    suspend fun obtenerCatalogos(): CatalogosResponse
}