package com.example.upk_btpi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.upk_btpi.Models.Auth.AuthResponse
import com.example.upk_btpi.Retrofit.AuthRepository
import com.example.upk_btpi.Utils.JwtDecoder
import com.example.upk_btpi.databinding.ActivityEntryBinding
import com.example.upk_btpi.databinding.ActivitySmsBinding
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import kotlin.toString

class Entry : AppCompatActivity() {

    private lateinit var binding: ActivityEntryBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonGoToRegistrtion.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.buttonInput.setOnClickListener {
            val phoneNumber = binding.editTextText2.text.toString().trim()
            val password = binding.editTextTextPassword3.text.toString()
            if(!phoneNumber.isEmpty() && !password.isEmpty()) {
                LogIn(phoneNumber, password, binding.switch1.isChecked)
            }
        }
    }

    //api запрос
    private fun  LogIn(phoneNumber: String ,password: String, rememberMe: Boolean) {
        binding.buttonInput.isEnabled = false
        binding.buttonInput.text = "вход в аккаунт"

        lifecycleScope.launch {
            val result = authRepository.login(phoneNumber, password)
            result.onSuccess { response ->
                val token = response.token ?: ""
                if (token.isNotEmpty()) {
                    saveUserData(token, phoneNumber, password, response, rememberMe)


                    //  Переход на главную страницу
                    val intent = Intent(this@Entry, MainPage::class.java)
                    startActivity(intent)
                    finish()
                } else { Toast.makeText(this@Entry, "⚠️ Токен не получен", Toast.LENGTH_SHORT).show() }
            }
                result.onFailure { error ->
                    val errorMessage = error.message ?: "Неизвестная ошибка"
                    // Показываем ошибку в поле телефона
                    binding.editTextText2.error = errorMessage
                    binding.editTextText2.requestFocus()
                    Toast.makeText(this@Entry, "❌ $errorMessage",
                        Toast.LENGTH_LONG).show()
                    binding.buttonInput.isEnabled = true
                    binding.buttonInput.text = "Войти"
                }
        }

    }

    private  fun saveUserData(token: String, phoneNumber: String,password: String, response: AuthResponse, rememberMe: Boolean) {
        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val claims = JwtDecoder.decode(token)

        prefs.edit().apply {
            putString("auth_token", token)
            putString("user_id", claims["nameid"]?.toString() ?: "")
            putString("user_name", claims["unique_name"]?.toString() ?: response.fullName ?: "Не указано")
            putString("user_phone", phoneNumber)
            putString("user_role", claims["role"]?.toString() ?: "Пользователь")

            // Пароль ТОЛЬКО если "Запомнить меня"
            if (rememberMe) {
                putString("user_password", password)
                putBoolean("save", true)
            } else {
                remove("user_password")  // ✅ Удаляем старый пароль
                putBoolean("save", false)
            }
            apply()
        }


        // Декодируем токен и сохраняем данные
//        if (token.isNotEmpty()) {
//
//            prefs.edit().apply {
//                putString("user_id", claims["nameid"]?.toString() ?: "")
//                putString("user_name", claims["unique_name"]?.toString() ?: response.fullName ?: "Не указано")
//                putString("user_phone", phoneNumber)
//                putString("user_role", claims["role"]?.toString() ?: "Пользователь")
//                putString("user_password", binding.editTextTextPassword3.text.toString().trim())
//
//                if(binding.switch1.isChecked == true)
//                {
//                    prefs.edit().apply{putBoolean("save", true)}
//                }
//                else{   prefs.edit().apply{putBoolean("save", false)    }}
//
//                apply()
//            }
//        }
    }

}