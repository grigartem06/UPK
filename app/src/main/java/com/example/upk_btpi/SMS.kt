package com.example.upk_btpi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.upk_btpi.databinding.ActivitySmsBinding



class SMS : AppCompatActivity() {
    private  lateinit var  binding: ActivitySmsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmsBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val user = intent.getSerializableExtra("newuser") as?  User //объект нового пользователя

        // Защита от отсутствия данных
//        if (user == null) {
//            Toast.makeText(this, "Данные пользователя не найдены", Toast.LENGTH_SHORT).show()
//            finish()
//            return
//        }
        //получаем данные о номере
        //binding.textView4.text = "Номер: ${user.PhoneNumber}"

        //отправляем на номер смс


        //кнопка "ошиблись номером"
        binding.button5.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            //переход на страницу регистрации
            startActivity(intent)
            finish()
        }


        //кнопка  "подтвердить"
        binding.button6.setOnClickListener {
            val code = binding.editTextNumber.text.toString();
            //проверка правильности кода из смс
            if(CheckCode(code)){
                //AddNewUser(user)
            }
            else Toast.makeText(this, "Не правильный код", Toast.LENGTH_SHORT).show()
        }
    }

    fun CheckCode(code: String) : Boolean{
        var isCheck = false
        //если коды совпадают, то isCheck true
        if (code != null) {
            isCheck = true
        }
        return  isCheck
    }

//    fun AddNewUser(user: User){
//        //отправляем данные нового пользователя в api
//
//    }




}