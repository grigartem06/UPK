package com.example.upk_btpi.Retrofit

import com.example.upk_btpi.Models.AuthResponse
import com.example.upk_btpi.Models.RegistrationDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/api/Auth/register")
    suspend fun register(@Body request: RegistrationDto): Response<AuthResponse>
}