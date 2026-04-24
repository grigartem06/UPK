package com.example.upk_btpi

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.upk_btpi.Models.Feedback.FeedbackDto
import com.example.upk_btpi.Models.Feedback.NewFeedbackDto
import com.example.upk_btpi.Retrofit.AuthRepository
import com.example.upk_btpi.Retrofit.RetrofitClient
import com.example.upk_btpi.databinding.ActivityFeedbackDetailBinding
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

private lateinit var binding: ActivityFeedbackDetailBinding
private var EditMode: Boolean?=null
private  var selectedFeedback: String?=null
private var userRole: String? = null
private val authRepository = AuthRepository()
class feedback_detail_Activity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        binding = ActivityFeedbackDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Получение данных из SharedPreferences
        val feedbackPrefs = getSharedPreferences("feedback_prefs", MODE_PRIVATE)
        selectedFeedback = feedbackPrefs.getString("selected_feedback_id", null)

        val authPrefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        userRole = authPrefs.getString("user_role", null)

        if(selectedFeedback.isNullOrEmpty()) {
            Toast.makeText(this, "⚠️ ID отзыва не найден", Toast.LENGTH_SHORT).show()
            finish()
            return }

        if(selectedFeedback == "new_feedback"){
            newFeedback()
        }else{
            //открывает доступ к "изменению"
            setupUIByRole()
            //загрузка данных
            loadInfoAboutFeedback(selectedFeedback.toString())
            //обработка кнопок
            binding.buttonEdit.setOnClickListener {
                EditMode=true
                //скрываем поля вывода
                binding.textViewComment.visibility = View.GONE
                binding.textViewRaitind.visibility = View.GONE

                //делаем видимыми поля ввода
                binding.editTextTextComment.visibility = View.VISIBLE
                binding.editTextTextRaitind.visibility = View.VISIBLE

                binding.buttonSave.visibility= View.VISIBLE
                binding.buttonEdit.visibility= View.GONE

                binding.buttonSave.setOnClickListener {saveUpdate(selectedFeedback.toString())}
            }
        }







        binding.buttonBack.setOnClickListener {  finish()}

    }

    private fun setupUIByRole() { if(userRole=="Admin"){ binding.buttonEdit.visibility = View.VISIBLE } }

    private fun loadInfoAboutFeedback(feedbackId: String) {
            lifecycleScope.launch {
                val result = authRepository.getFeedbackById(feedbackId)
                result.onSuccess { feedback -> displayFeedback(feedback) }
                result.onFailure { Toast.makeText(this@feedback_detail_Activity, "❌ Ошибка получения данных", Toast.LENGTH_SHORT).show() } }
    }

    private fun displayFeedback(feedback: FeedbackDto) {
        binding.textViewComment.setText(feedback.comment)
        binding.textViewRaitind.setText(feedback.raiting.toString())
        binding.textViewUserName.setText(feedback.user.fullname)
    }

    private fun saveUpdate(feedbackId: String) {
        lifecycleScope.launch {
            try {
                // ✅ Получаем текущие данные отзыва
                val result = authRepository.getFeedbackById(feedbackId)

                result.onSuccess { feedback ->
                    //Преобразуем значения в RequestBody
                    val idBody = (feedback.id ?: "").toRequestBody("text/plain".toMediaType())
                    val commentBody = binding.editTextTextComment.text.toString().trim().toRequestBody("text/plain".toMediaType())
                    val ratingValue = binding.editTextTextRaitind.text.toString().trim().toIntOrNull() ?: 0
                    val ratingBody = ratingValue.toString().toRequestBody("text/plain".toMediaType())

                    //ВЫЗОВ API
                    val response = RetrofitClient.apiService.updateFeedback(id = idBody, feedbackName = commentBody, rating = ratingBody)

                    if (response.isSuccessful) { Toast.makeText(this@feedback_detail_Activity, "✅ Успешно", Toast.LENGTH_SHORT).show() }
                    else {
                        val error = response.errorBody()?.string() ?: "Неизвестная ошибка"
                        println("❌ ОШИБКА: ${response.code()} - $error")
                        Toast.makeText(this@feedback_detail_Activity, "❌ Ошибка: $error", Toast.LENGTH_LONG).show() }
                }
                result.onFailure { Toast.makeText(this@feedback_detail_Activity, "❌ Ошибка получения данных", Toast.LENGTH_SHORT).show() }

            } catch (e: Exception) {
                println("❌ ИСКЛЮЧЕНИЕ: ${e.message}")
                e.printStackTrace()
                Toast.makeText(this@feedback_detail_Activity, "❌ ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun newFeedback(){
        //отображаем поля ввода  и скрываем вывода
        binding.editTextTextComment.visibility = View.VISIBLE
        binding.editTextTextRaitind.visibility = View.VISIBLE

        binding.textViewComment.visibility= View.GONE
        binding.textViewRaitind.visibility= View.GONE

        binding.buttonSave.visibility = View.VISIBLE

        binding.buttonSave.setOnClickListener {
            lifecycleScope.launch {
                try {
                    var result = authRepository.addNewFeedback(
                        binding.editTextTextComment.text.toString(),
                        binding.editTextTextRaitind.text.toString().trim().toInt(),
                        imageUri = null,
                        this@feedback_detail_Activity
                    )

                    result.onSuccess { feedbackId ->
                        Toast.makeText(this@feedback_detail_Activity, "✅ Отзыв отправлен!", Toast.LENGTH_SHORT).show()
                        println("ID отзыва: $feedbackId")
                        finish()
                    }

                    result.onFailure { error ->
                        Toast.makeText(this@feedback_detail_Activity, "❌ ${error.message}", Toast.LENGTH_SHORT).show()
                    }

                }catch (e: Exception) {}
            }
        }
    }





}