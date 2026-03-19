package com.example.upk_btpi.Retrofit

import android.content.Context
import androidx.annotation.DisplayContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://btpit-ypk-api.somee.com/"

    val apiService: ApiService by lazy {

        // Логгер для отладки запросов/ответов
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Интерцептор для добавления токена авторизации
        val authInterceptor = AuthInterceptor()

        // Создаём OkHttpClient с интерцепторами
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)      // Логи запросов
            .addInterceptor(authInterceptor)         // Токен авторизации
            .connectTimeout(30, TimeUnit.SECONDS)    // Таймаут подключения
            .readTimeout(30, TimeUnit.SECONDS)       // Таймаут чтения
            .writeTimeout(30, TimeUnit.SECONDS)      // Таймаут записи
            .build()

        // Создаём Retrofit
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())  // Парсинг JSON
            .build()
            .create(ApiService::class.java)
    }





}