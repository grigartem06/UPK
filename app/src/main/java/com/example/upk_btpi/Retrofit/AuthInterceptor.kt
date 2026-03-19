package com.example.upk_btpi.Retrofit

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Получаем токен из SharedPreferences
        // Используем контекст приложения через OkHttpClient
        val token = getToken()

        val newRequest = if (!token.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(newRequest)
    }

    companion object {
        private var prefs: SharedPreferences? = null

        fun init(context: Context) {
            prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        }

        private fun getToken(): String? {
            return prefs?.getString("auth_token", null)
        }
    }
}