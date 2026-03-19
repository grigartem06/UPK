package com.example.upk_btpi.Retrofit

import com.example.upk_btpi.Models.AuthResponse
import com.example.upk_btpi.Models.LoginDto
import com.example.upk_btpi.Models.RegistrationDto
import com.example.upk_btpi.Models.UserResponse
import retrofit2.Response
import java.io.IOException

class AuthRepository {
    suspend fun register(fullName: String, phoneNumber: String, password: String): Result<AuthResponse> {
        return try {
            val request = RegistrationDto(fullName, phoneNumber, password)
            val response = RetrofitClient.apiService.register(request)

            if (response.isSuccessful) {
                val body = response.body()

                if (body != null) {
                    Result.success(body)
                } else {
                    Result.success(AuthResponse(null, "Регистрация успешна",
                        null))
                }
            } else {
                // Сервер вернул ошибку
                val errorBody = try {
                    response.errorBody()?.string()
                } catch (e: Exception) {
                    "Не удалось прочитать ошибку"
                }
                println("❌ ОШИБКА СЕРВЕРА:")
                println("   Код: ${response.code()}")
                println("   Тело ошибки: $errorBody")

                Result.failure(Exception("Ошибка ${response.code()}: $errorBody"))
            }

        } catch (e: IOException) {
            println("❌ ОШИБКА СЕТИ:")
            println("   ${e.message}")
            e.printStackTrace()
            Result.failure(Exception(e.message))
        } catch (e: Exception) {
            println("❌ НЕИЗВЕСТНАЯ ОШИБКА:")
            println("   ${e.message}")
            e.printStackTrace()
            Result.failure(Exception("Ошибка: ${e.message ?: "Неизвестная ошибка"}"))
        }
    }
    suspend fun login(phoneNumber: String, password: String): Result<AuthResponse> {
        return try {
            val request = LoginDto(phoneNumber, password)
            val response = RetrofitClient.apiService.login(request)

            println("📤 ВХОД: phoneNumber=$phoneNumber")
            println("📥 ОТВЕТ: код=${response.code()}, токен=${response.body()?.token?.take(20)}...")

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val error = response.errorBody()?.string() ?: "Неизвестная ошибка"
                Result.failure(Exception("Ошибка ${response.code()}: $error"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Нет соединения с сервером"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    private  fun  handleResponse(response: Response<AuthResponse>): Result<AuthResponse> {
        return if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            Result.success(body)
        } else {
            val errorBody = try {
                response.errorBody()?.string()
            }catch (e: Exception) {
                "Не удалось прочитать ошибку"
            }
            Result.failure(Exception("Ошибка ${response.code()}: $errorBody"))
        }
    }

    suspend fun getUserByID(userId: String): Result<UserResponse> {
        return try {
            val response = RetrofitClient.apiService.getUserByID(userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val error = response.errorBody()?.string() ?: "Неизвестная ошибка"
                Result.failure(Exception("Ошибка ${response.code()}: $error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }






}