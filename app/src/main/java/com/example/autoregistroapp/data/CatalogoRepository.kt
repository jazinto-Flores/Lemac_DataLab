package com.example.autoregistroapp.data

import com.example.autoregistroapp.data.remote.ApiService
import com.example.autoregistroapp.data.remote.CatalogosResponse

/** Capa de datos: el ViewModel pide los catálogos aquí y no conoce los detalles de Retrofit. */
class CatalogoRepository(
    private val api: ApiService
) {
    suspend fun obtenerCatalogos(): CatalogosResponse = api.obtenerCatalogos()
}