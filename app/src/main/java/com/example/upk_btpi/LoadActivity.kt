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
import kotlinx.coroutines.launch

class LoadActivity : AppCompatActivity() {
    private val authRepository = AuthRepository()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_load)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val isSave = prefs.getBoolean("save", false)

        if(isSave){
            val phoneNumber = prefs.getString("user_phone", null)
            val password = prefs.getString("user_password", null)
            if(!phoneNumber.isNullOrEmpty() && !password.isNullOrEmpty()) {
                //сохранены
                lifecycleScope.launch {
                    val result = authRepository.login(phoneNumber, password)
                    result.onSuccess { response ->
                        val token = response.token ?: ""

                        if (token.isNotEmpty()) {
                            // ✅ 4️⃣ СОХРАНЯЕМ ТОКЕН
                            saveUserData(token, phoneNumber, response)
                            goToMainPage()
                        } else {
                            goToEntry()
                        }


                        //  Переход на главную страницу
                        val intent = Intent(this@LoadActivity, MainPage::class.java)
                        startActivity(intent)
                        finish()
                    }
                    result.onFailure { error ->
                        val errorMessage = error.message ?: "Неизвестная ошибка"
                        Toast.makeText(this@LoadActivity, "❌ $errorMessage",
                            Toast.LENGTH_LONG).show()
                    }
                }
            }else
            {
                goToEntry()
            }
        }
        else
        {
            //данные не сохранены
            startActivity(Intent(this, Entry::class.java))
            finish()
        }
    }

    // Переход на главную
    private fun goToMainPage() {
        val intent = Intent(this@LoadActivity, MainPage::class.java)
        startActivity(intent)
        finish()
    }

    // Переход на экран входа
    private fun goToEntry() {
        val intent = Intent(this@LoadActivity, Entry::class.java)
        startActivity(intent)
        finish()
    }


    private fun saveUserData(token: String, phoneNumber: String, response: AuthResponse) {
        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val claims = JwtDecoder.decode(token)

        // Отладка — выводим все claims
        claims.forEach { (key, value) -> println("$key = $value") }

        prefs.edit().apply {
            putString("auth_token", token)

            // Пробуем несколько вариантов имени claim
            putString("user_id",
                claims["nameid"]?.toString()
                    ?: claims["id"]?.toString()
                    ?: claims["userId"]?.toString()
                    ?: "")

            putString("user_name",
                claims["unique_name"]?.toString()
                    ?: response.fullName
                    ?: "Не указано")

            putString("user_phone", phoneNumber)
            putString("user_role", claims["role"]?.toString() ?: "Пользователь")
            putString("user_password", prefs.getString("user_password", null))
            putBoolean("save", prefs.getBoolean("save", false))

            apply()
        }

        println("💾 user_id сохранён: ${claims["nameid"] ?: claims["id"] ?: "не найден"}")
    }


}


