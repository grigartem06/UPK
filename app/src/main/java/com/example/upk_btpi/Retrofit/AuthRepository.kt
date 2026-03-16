package com.example.upk_btpi.Retrofit

import com.example.upk_btpi.Models.AuthResponse
import com.example.upk_btpi.Models.RegistrationDto
import retrofit2.Response
import java.io.IOException

class AuthRepository {
    suspend fun register(
        fullName: String,
        phoneNumber: String,
        password: String
    ): Result<AuthResponse> {
        return try {
            val request = RegistrationDto(fullName, phoneNumber, password)

            println("📤 ОТПРАВКА ЗАПРОСА:")
            println("   URL: ${RetrofitClient::class.java.getDeclaredField("BASE_URL").get(null)}api/Auth/register")
            println("   Данные: fullName=$fullName, phoneNumber=$phoneNumber, password=***")

            val response = RetrofitClient.apiService.register(request)

            println("📥 ПОЛУЧЕН ОТВЕТ:")
            println("   Код: ${response.code()}")
            println("   Успех: ${response.isSuccessful}")
            println("   Message: ${response.message()}")

            if (response.isSuccessful) {
                val body = response.body()
                println("   Body: $body")

                if (body != null) {
                    Result.success(body)
                } else {
                    // Сервер вернул 200 OK, но без тела
                    println("⚠️ Сервер вернул пустой ответ с кодом 200")
                    Result.success(AuthResponse(null, "Регистрация успешна", null))
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
            Result.failure(Exception("Нет соединения с сервером. Проверьте:\n1. Запущен ли сервер на порту 7001\n2. Правильный ли URL (http://10.0.2.2:7001)"))
        } catch (e: Exception) {
            println("❌ НЕИЗВЕСТНАЯ ОШИБКА:")
            println("   ${e.message}")
            e.printStackTrace()
            Result.failure(Exception("Ошибка: ${e.message ?: "Неизвестная ошибка"}"))
        }
    }

}