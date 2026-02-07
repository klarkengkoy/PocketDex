package com.samidevstudio.pocketdex.data

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

/**
 * RetrofitClient is a Singleton object (only one exists).
 * It configures and provides our network service.
 */
object RetrofitClient {
    private const val BASE_URL = "https://pokeapi.co/api/v2/"

    // We configure Json to be "lenient" so it doesn't crash if the API
    // sends extra data we didn't define in our models.
    private val json = Json {
        ignoreUnknownKeys = true
    }

    // This is the core Retrofit engine setup.
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    // This is what the rest of the app will use to get data.
    // 'by lazy' ensures it's only initialized when first called.
    val pokeApiService: PokeApiService by lazy {
        retrofit.create(PokeApiService::class.java)
    }
}