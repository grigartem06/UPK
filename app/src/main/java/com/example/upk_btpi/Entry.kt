package com.example.upk_btpi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.upk_btpi.Models.AuthResponse
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
            val phoneNumber = binding.editTextText2.text.toString()
            val password = binding.editTextTextPassword3.text.toString()

            if(!phoneNumber.isEmpty() && !password.isEmpty()) {
                LogIn(phoneNumber, password)
            }

        }

    }

    //api запрос
    private fun  LogIn(phoneNumber: String ,password: String) {
        binding.buttonInput.isEnabled = false
        binding.buttonInput.text = "вход в аккаунт"

        lifecycleScope.launch {
            val result = authRepository.login(phoneNumber, password)

            result.onSuccess { response ->
                val token = response.token ?:""

                if(token.isNotEmpty()){
                    val claims = JwtDecoder.decode(token)

                    // Извлекаем нужные поля
                    val userId = claims["nameid"]?.toString()?.toIntOrNull()
                    val userRole = claims["role"]?.toString()
                    val userName = claims["name"]?.toString() ?: claims["fullName"]?.toString()
                    val exp = claims["exp"]?.toString()?.toLongOrNull()

                    // Сохраняем дополнительные данные в SharedPreferences
                    val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
                    prefs.edit().apply {
                        if (userId != null) putInt("user_id", userId)
                        if (userRole != null) putString("user_role", userRole)
                        if (userName != null) putString("user_name", userName)
                        if (exp != null) putLong("token_exp", exp)
                        apply()
                    }
                }



                 // ✅ Переход на главную страницу
                val intent = Intent(this@Entry, MainPage::class.java)
                startActivity(intent)
                finish()
            }

                result.onFailure { error ->
                    val errorMessage = error.message ?: "Неизвестная ошибка"

                    // Показываем ошибку в поле телефона
                    binding.editTextText2.error = errorMessage
                    binding.editTextText2.requestFocus()

                    Toast.makeText(this@Entry, "❌ $errorMessage", Toast.LENGTH_LONG).show()

                    binding.buttonInput.isEnabled = true
                    binding.buttonInput.text = "Войти"
                }


        }


    }

    private  fun saveUserData(response: AuthResponse,phoneNumber: String) {
        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        //prefs.edit().putString("auth_token",token).apply()
        prefs.edit().apply(){
         putString("auth_token", response.token.toString())
        }
    }

}