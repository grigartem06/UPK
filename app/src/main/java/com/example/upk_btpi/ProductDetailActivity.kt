package com.example.upk_btpi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.upk_btpi.Models.Product.ProductDto
import com.example.upk_btpi.Models.StatusProduct.StatusProductDto
import com.example.upk_btpi.Retrofit.AuthRepository
import com.example.upk_btpi.Retrofit.RetrofitClient
import com.example.upk_btpi.databinding.ActivityProductDetailBinding
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private val authRepository = AuthRepository()

    private var isEditMode = false
    private var userRole: String? = null
    private var currentProductId: String? = null
    private var oldProduct: ProductDto? = null

    // Для Spinner статусов
    private var statusProducts: List<StatusProductDto> = emptyList()
    private var selectedStatusId: String? = null

    // Для выбора изображения

    private var selectedImageUri: Uri? = null
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedImageUri = uri
        uri?.let {
            binding.imageViewProduct.setImageURI(it)
            binding.imageViewProduct.visibility = View.VISIBLE
        }
    }

    // Значение по умолчанию для StatusProductId
    private val defaultStatusId = "a3a89ae5-9acb-4fb5-9b88-e6b9ffa5994f"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Получение данных из SharedPreferences
        val productPrefs = getSharedPreferences("product_prefs", MODE_PRIVATE)
        val authPrefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)

        currentProductId = productPrefs.getString("selected_product_id", null)
        userRole = authPrefs.getString("user_role", null)

        if (currentProductId == null) {
            Toast.makeText(this, "⚠️ ID продукта не найден", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUIByRole()

        // Обработчики кнопок
        binding.buttonOrder.setOnClickListener { handleOrderButtonClick() }
        binding.buttonStopChanges.setOnClickListener { cancelEditing() }
        binding.buttonBack.setOnClickListener { finish() }

        // Обработчик кнопки изменения статуса
        binding.buttonChangeStatus.setOnClickListener {
            if (selectedStatusId != null) {
                changeProductStatus(selectedStatusId!!)
            } else {
                Toast.makeText(this, "⚠️ Выберите статус", Toast.LENGTH_SHORT).show()
            }
        }

        // Клик по изображению для выбора нового фото
        binding.imageViewProduct.setOnClickListener {
            if (isEditMode) {
                imagePickerLauncher.launch("image/*")
            }
        }

        // Загрузка данных продукта
        loadProductDetails(currentProductId!!)

        // Загрузка статусов (только для Admin/Manager)
        if (userRole == "Admin" || userRole == "Manager") {
            loadStatusProducts()
        }
    }

    // Загрузка списка статусов
    private fun loadStatusProducts() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getAllStatusProduct()

                if (response.isSuccessful && response.body() != null) {
                    statusProducts = response.body()!!.statusProducts
                    println("✅ Получено статусов: ${statusProducts.size}")
                    setupSpinner()
                } else {
                    println("❌ Ошибка загрузки статусов: ${response.code()}")
                }
            } catch (e: Exception) {
                println("❌ Исключение: ${e.message}")
            }
        }
    }

    // Настройка Spinner
    private fun setupSpinner() {
        if (statusProducts.isEmpty()) return

        val statusNames = statusProducts.map { it.statusName ?: "Без названия" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerStatus.adapter = adapter

        // Устанавливаем текущий статус
        oldProduct?.let { product ->
            val currentStatusIndex = statusProducts.indexOfFirst {
                it.id == product.statusProductId
            }
            if (currentStatusIndex >= 0) {
                binding.spinnerStatus.setSelection(currentStatusIndex)
                selectedStatusId = product.statusProductId
            }
        }

        // Обработчик выбора
        binding.spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedStatusId = statusProducts[position].id
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedStatusId = null
            }
        }
    }

    // Изменение статуса продукта
    private fun changeProductStatus(newStatusId: String) {
        lifecycleScope.launch {
            try {
                binding.buttonChangeStatus.isEnabled = false

                // ✅ ПРЕОБРАЗУЕМ ВСЕ ЗНАЧЕНИЯ В RequestBody
//                val idBody = (oldProduct?.id ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
//                val nameBody = (oldProduct?.productName ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
//                val infoBody = (oldProduct?.productInfo ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
//                val costBody = (oldProduct?.productCost ?: 0.0).toString().toRequestBody("text/plain".toMediaTypeOrNull())
//                val isProductBody = (oldProduct?.isProduct ?: true).toString().toRequestBody("text/plain".toMediaTypeOrNull())
//                val adressBody = (oldProduct?.adress ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
//                val ypkIdBody = (oldProduct?.ypkId ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
//                val statusProductIdBody = newStatusId.toRequestBody("text/plain".toMediaTypeOrNull())



//                if (response.isSuccessful) {
//                    Toast.makeText(this@ProductDetailActivity, "✅ Статус изменён", Toast.LENGTH_SHORT).show()
//                    oldProduct = oldProduct?.copy(statusProductId = newStatusId)
//                } else {
//                    val error = response.errorBody()?.string() ?: "Неизвестная ошибка"
//                    Toast.makeText(this@ProductDetailActivity, "❌ Ошибка: $error", Toast.LENGTH_SHORT).show()
//                }
            } catch (e: Exception) {
                Toast.makeText(this@ProductDetailActivity, "❌ ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.buttonChangeStatus.isEnabled = true
            }
        }
    }

    private fun setupUIByRole() {
        if (userRole == "DefaultUser") {
            binding.buttonOrder.text = "🛒 Заказать"
            binding.buttonStopChanges.visibility = View.GONE
            binding.layoutStatusChange.visibility = View.GONE
            setFieldsEditable(false)
        } else {
            binding.buttonOrder.text = "✏️ Изменить"
            binding.buttonStopChanges.visibility = View.GONE
            binding.layoutStatusChange.visibility = View.VISIBLE
            setFieldsEditable(false)
        }
    }

    private fun handleOrderButtonClick() {
        if (userRole == "DefaultUser") {
            val intent = Intent(this, NewOrderActivity::class.java)
            intent.putExtra("product_id", currentProductId)
            startActivity(intent)
            finish()
        } else {
            if (isEditMode) {
                saveProductChanges()
            } else {
                enableEditMode()
            }
        }
    }

    private fun enableEditMode() {
        isEditMode = true
        binding.buttonOrder.text = "💾 Сохранить"
        binding.buttonStopChanges.visibility = View.VISIBLE
        setFieldsEditable(true)
        Toast.makeText(this, "Режим редактирования включён", Toast.LENGTH_SHORT).show()
    }

    private fun cancelEditing() {
        isEditMode = false
        binding.buttonOrder.text = "✏️ Изменить"
        binding.buttonStopChanges.visibility = View.GONE
        setFieldsEditable(false)
        oldProduct?.let { displayProduct(it) }
        Toast.makeText(this, "Изменения отменены", Toast.LENGTH_SHORT).show()
    }

    private fun saveProductChanges() {
        val productName = binding.textViewName.text.toString().trim()
        if (productName.isEmpty()) {
            Toast.makeText(this, "⚠️ Введите название", Toast.LENGTH_SHORT).show()
            return
        }

        val productCost = binding.textViewCost.text.toString()
            .replace(" ₽", "")
            .replace(",", ".")
            .trim()
            .toDoubleOrNull()

        if (productCost == null || productCost <= 0) {
            Toast.makeText(this, "⚠️ Введите корректную цену", Toast.LENGTH_SHORT).show()
            return
        }

        val productInfo = binding.textViewInfo.text.toString()
        val address = binding.textViewAddress.text.toString()

        // Гарантируем, что StatusProductId не пустой
        val statusProductIdToSend = oldProduct?.statusProductId ?: defaultStatusId

        lifecycleScope.launch {
            try {
                binding.buttonOrder.isEnabled = false
                binding.buttonOrder.text = "Сохранение..."

                println("\n📤 ОТПРАВКА ОБНОВЛЕНИЯ ПРОДУКТА (multipart/form-data):")


                // Создаём Part для фото (если выбрано)
                val photoPart = selectedImageUri?.let { uri ->
                    try {
                        val inputStream = contentResolver.openInputStream(uri)
                        val tempFile = File(cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
                        tempFile.outputStream().use { output ->
                            inputStream?.copyTo(output)
                        }

                        val requestBody = tempFile.asRequestBody("image/jpeg".toMediaType())
                        MultipartBody.Part.createFormData("Photo", tempFile.name, requestBody)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }

                // ВЫЗОВ API
                val response = RetrofitClient.apiService.updateProduct(
                    id = (oldProduct?.id ?: "").toRequestBody("text/plain".toMediaType()),
                    productName = binding.textViewName.text.toString().trim().toRequestBody("text/plain".toMediaType()),
                    productInfo = binding.textViewInfo.text.toString().trim().toRequestBody("text/plain".toMediaType()),
                    productCost = (binding.textViewCost.text.toString().trim().toDoubleOrNull() ?: 0.0).toString().toRequestBody("text/plain".toMediaType()),
                    isProduct = (oldProduct?.isProduct ?: true).toString().toRequestBody("text/plain".toMediaType()),
                    adress = binding.textViewAddress.text.toString().trim().toRequestBody("text/plain".toMediaType()),
                    photo = photoPart,
                    ypkId = (oldProduct?.ypkId ?: "").toRequestBody("text/plain".toMediaType()),
                    //statusProductId = (oldProduct?.statusProductId ?: defaultStatusId).toRequestBody("text/plain".toMediaType())
                    statusProductId = "d0d14847-2493-4850-a26f-834a6e8a745c".toRequestBody("text/plain".toMediaType())
                )

                if (response.isSuccessful) {
                    Toast.makeText(this@ProductDetailActivity, "✅ Изменения сохранены", Toast.LENGTH_SHORT).show()

                    oldProduct = oldProduct?.copy(
                        productName = productName,
                        productCost = productCost,
                        productInfo = productInfo,
                        adress = address
                    )
                } else {
                    val error = response.errorBody()?.string() ?: "Неизвестная ошибка"

                    if (error.startsWith("<!DOCTYPE")) {
                        println("❌ СЕРВЕР ВЕРНУЛ HTML ВМЕСТО JSON!")
                        Toast.makeText(this@ProductDetailActivity, "❌ Ошибка сервера (HTML ответ)", Toast.LENGTH_LONG).show()
                    } else {
                        println("❌ ОШИБКА: $error")
                        Toast.makeText(this@ProductDetailActivity, "❌ Ошибка: $error", Toast.LENGTH_LONG).show()
                    }
                }

            } catch (e: Exception) {
                println("❌ ИСКЛЮЧЕНИЕ: ${e.message}")
                e.printStackTrace()
                Toast.makeText(this@ProductDetailActivity, "❌ ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                disableEditMode()
                binding.buttonOrder.isEnabled = true
            }
        }
    }

    private fun disableEditMode() {
        isEditMode = false
        binding.buttonOrder.text = "✏️ Изменить"
        binding.buttonStopChanges.visibility = View.GONE
        setFieldsEditable(false)
    }

    private fun setFieldsEditable(isEditable: Boolean) {
        binding.textViewName.apply {
            isFocusable = isEditable
            isFocusableInTouchMode = isEditable
            isClickable = isEditable
            isCursorVisible = isEditable
            if (!isEditable) clearFocus()
        }

        binding.textViewCost.apply {
            isFocusable = isEditable
            isFocusableInTouchMode = isEditable
            isClickable = isEditable
            isCursorVisible = isEditable
            if (!isEditable) clearFocus()
        }

        binding.textViewInfo.apply {
            isFocusable = isEditable
            isFocusableInTouchMode = isEditable
            isClickable = isEditable
            isCursorVisible = isEditable
            if (!isEditable) clearFocus()
        }

        binding.textViewAddress.apply {
            isFocusable = isEditable
            isFocusableInTouchMode = isEditable
            isClickable = isEditable
            isCursorVisible = isEditable
            if (!isEditable) clearFocus()
        }
    }

    private fun loadProductDetails(productId: String) {
        lifecycleScope.launch {
            val result = authRepository.getProductById(productId)

            result.onSuccess { product ->
                oldProduct = product
                displayProduct(product)

                // Если статусы уже загружены — обновляем Spinner
                if (statusProducts.isNotEmpty()) {
                    setupSpinner()
                }
            }

            result.onFailure {
                Toast.makeText(this@ProductDetailActivity, "❌ Ошибка получения данных", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayProduct(product: ProductDto) {
        binding.textViewName.setText(product.productName ?: "Без названия")

        val typeLabel = if (product.isProduct) "📦 Товар" else "🔧 Услуга"
        binding.textViewType.setText(typeLabel)

        binding.textViewCost.setText("${product.productCost} ₽")
        binding.textViewInfo.setText(product.productInfo ?: "Нет описания")
        binding.textViewAddress.setText(product.adress ?: "Адрес не указан")

        if (!product.photoPath.isNullOrEmpty()) {
            binding.imageViewProduct.visibility = View.VISIBLE
        } else {
            binding.imageViewProduct.visibility = View.GONE
        }
    }
}