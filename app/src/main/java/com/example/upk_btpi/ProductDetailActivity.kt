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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.upk_btpi.Models.Product.ProductDto
import com.example.upk_btpi.Models.StatusProduct.StatusProductDto
import com.example.upk_btpi.Models.Ypk.YpksDto
import com.example.upk_btpi.Retrofit.AuthRepository
import com.example.upk_btpi.Retrofit.RetrofitClient
import com.example.upk_btpi.databinding.ActivityProductDetailBinding
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private val authRepository = AuthRepository()

    private var currentProductId: String? = null
    private var oldProduct: ProductDto? = null
    private var isEditMode: Boolean = false

    // Данные для spinner'ов
    private var ypksList: List<YpksDto> = emptyList()
    private var statusProductsList: List<StatusProductDto> = emptyList()
    private var selectedYpkId: String? = null
    private var selectedStatusProductId: String? = null

    // Для выбора фото
    private var selectedImageUri: Uri? = null
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedImageUri = uri
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Получаем ID продукта из SharedPreferences
        val productPrefs = getSharedPreferences("product_prefs", MODE_PRIVATE)
        currentProductId = productPrefs.getString("selected_product_id", null)

        if (currentProductId.isNullOrEmpty()) {
            Toast.makeText(this, "⚠️ ID продукта не найден", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Загружаем данные для spinner'ов
        loadYpks()
        loadStatusProducts()

        // Проверяем: добавление нового продукта или редактирование
        if (currentProductId == "add_new_product") { setupUIForNewProduct() }
        else { loadProductInfo(currentProductId!!) }

        val authPrefs =getSharedPreferences("auth_prefs", MODE_PRIVATE)
        var userRole = authPrefs.getString("user_role", null)

        if(userRole == "Admin") {binding.buttonDelete.visibility = View.VISIBLE}

        // УСТАНАВЛИВАЕМ ОБРАБОТЧИКИ ДЛЯ ВСЕХ КНОПОК
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Кнопка "Изменить" - включаем режим редактирования
        binding.buttonEdit.setOnClickListener { enableEditMode() }

        // Кнопка "Сохранить" - сохраняем изменения
        binding.buttonSave.setOnClickListener {
            if (currentProductId == "add_new_product") { addNewProduct() }
            else { updateProduct() }
        }

        // Кнопка "Заказать" - оформляем заказ
        binding.buttonOrder.setOnClickListener { placeOrder() }

        // Кнопка "Назад"
        binding.buttonBack.setOnClickListener { finish() }

        // Выбор фото
        binding.imageViewPhoto.setOnClickListener { imagePickerLauncher.launch("image/*") }

        // кнопка "удалить"
        binding.buttonDelete.setOnClickListener { delete() }
    }

    private fun loadYpks() {
        lifecycleScope.launch {
            val result = authRepository.getAllUpks()
            result.onSuccess { response ->
                ypksList = response.ypks
                setupYpkSpinner()
            }
            result.onFailure {
                Toast.makeText(this@ProductDetailActivity, "❌ Ошибка загрузки УПК", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadStatusProducts() {
        lifecycleScope.launch {
            val result = authRepository.getAllStatusProducts()
            result.onSuccess { response ->
                statusProductsList = response.statusProducts
                setupStatusProductSpinner()
            }
            result.onFailure {
                Toast.makeText(this@ProductDetailActivity, "❌ Ошибка загрузки статусов", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupYpkSpinner() {
        val ypkNames = ypksList.map { it.ypkName }.toMutableList()
        ypkNames.add(0, "Выберите УПК")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ypkNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerYpk.adapter = adapter

        binding.spinnerYpk.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    selectedYpkId = ypksList[position - 1].id
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupStatusProductSpinner() {
        val statusNames = statusProductsList.map { it.statusName }.toMutableList()
        statusNames.add(0, "Выберите статус")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatus.adapter = adapter

        binding.spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    selectedStatusProductId = statusProductsList[position - 1].id
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadProductInfo(productId: String) {
        lifecycleScope.launch {
            val result = authRepository.getProductById(productId)
            result.onSuccess { product ->
                oldProduct = product
                displayProductInfo(product)
            }
            result.onFailure {
                Toast.makeText(this@ProductDetailActivity, "❌ Ошибка загрузки продукта", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayProductInfo(product: ProductDto) {
        // Показываем TextView, скрываем EditText
        binding.textViewProductName.visibility = View.VISIBLE
        binding.editTextProductName.visibility = View.GONE
        binding.textViewCost.visibility = View.VISIBLE
        binding.editTextCost.visibility = View.GONE
        binding.textViewProductInfo.visibility = View.VISIBLE
        binding.editTextInfo.visibility = View.GONE
        binding.textViewProductOrService.visibility = View.VISIBLE
        binding.checkBoxIsProduct.visibility = View.GONE
        binding.textViewAdress.visibility = View.VISIBLE
        binding.editTextAdress.visibility = View.GONE
        binding.spinnerYpk.visibility = View.VISIBLE
        binding.spinnerStatus.visibility = View.VISIBLE
        binding.buttonEdit.visibility = View.VISIBLE
        binding.buttonSave.visibility = View.GONE

        // Заполняем TextView данными
        binding.textViewProductName.text = product.productName
        binding.textViewCost.text = "${product.productCost} ₽"
        binding.textViewProductInfo.text = product.productInfo ?: "Нет описания"
        binding.textViewProductOrService.text = if (product.isProduct) "Товар" else "Услуга"
        binding.textViewAdress.text = product.adress ?: "Нет адреса"

        // Выбираем УПК в spinner
        val ypkPosition = ypksList.indexOfFirst { it.id == product.ypkId }
        if (ypkPosition >= 0) {
            binding.spinnerYpk.setSelection(ypkPosition + 1)
            selectedYpkId = product.ypkId
        }

        // Выбираем статус в spinner
        if (!product.statusProductId.isNullOrEmpty()) {
            val statusPosition = statusProductsList.indexOfFirst { it.id == product.statusProductId }
            if (statusPosition >= 0) {
                binding.spinnerStatus.setSelection(statusPosition + 1)
                selectedStatusProductId = product.statusProductId
            }
        }
    }

    private fun setupUIForNewProduct() {
        binding.textViewProductName.visibility = View.GONE
        binding.editTextProductName.visibility = View.VISIBLE
        binding.textViewCost.visibility = View.GONE
        binding.editTextCost.visibility = View.VISIBLE
        binding.textViewProductInfo.visibility = View.GONE
        binding.editTextInfo.visibility = View.VISIBLE
        binding.textViewProductOrService.visibility = View.GONE
        binding.checkBoxIsProduct.visibility = View.VISIBLE
        binding.textViewAdress.visibility = View.GONE
        binding.editTextAdress.visibility = View.VISIBLE
        binding.spinnerYpk.visibility = View.VISIBLE
        binding.spinnerStatus.visibility = View.VISIBLE
        binding.buttonEdit.visibility = View.GONE
        binding.buttonSave.visibility = View.VISIBLE
        binding.buttonSave.text = "Добавить"
    }

    private fun enableEditMode() {
        if (oldProduct == null) return

        // Скрываем TextView
        binding.textViewProductName.visibility = View.GONE
        binding.textViewCost.visibility = View.GONE
        binding.textViewProductInfo.visibility = View.GONE
        binding.textViewProductOrService.visibility = View.GONE
        binding.textViewAdress.visibility = View.GONE

        // Показываем EditText
        binding.editTextProductName.visibility = View.VISIBLE
        binding.editTextCost.visibility = View.VISIBLE
        binding.editTextInfo.visibility = View.VISIBLE
        binding.checkBoxIsProduct.visibility = View.VISIBLE
        binding.editTextAdress.visibility = View.VISIBLE

        // Заполняем EditText данными
        binding.editTextProductName.setText(oldProduct!!.productName)
        binding.editTextCost.setText(oldProduct!!.productCost.toString())
        binding.editTextInfo.setText(oldProduct!!.productInfo ?: "")
        binding.checkBoxIsProduct.isChecked = oldProduct!!.isProduct
        binding.editTextAdress.setText(oldProduct!!.adress ?: "")

        // Показываем/скрываем кнопки
        binding.buttonEdit.visibility = View.GONE
        binding.buttonSave.visibility = View.VISIBLE
        binding.buttonSave.text = "Сохранить"
    }

    // ==================== ОФОРМЛЕНИЕ ЗАКАЗА ====================
    private fun placeOrder() {
        val prefs = getSharedPreferences("product_prefs", 0)
        prefs.edit().apply(){ putString("selected_product_id", currentProductId);apply() }

        //переход на страницу
        val intent  = Intent(this@ProductDetailActivity, NewOrderActivity::class.java)
        startActivity(intent)
    }

    // ==================== ДОБАВЛЕНИЕ НОВОГО ПРОДУКТА ====================
    private fun addNewProduct() {
        val productName = binding.editTextProductName.text.toString().trim()
        val productCost = binding.editTextCost.text.toString()
            .replace(" ₽", "")
            .replace(",", ".")
            .trim()
            .toDoubleOrNull()
        val productInfo = binding.editTextInfo.text.toString().trim()
        val isProduct = binding.checkBoxIsProduct.isChecked
        val address = binding.editTextAdress.text.toString().trim()

        if (productName.isEmpty()) {
            Toast.makeText(this, "⚠️ Введите название продукта", Toast.LENGTH_SHORT).show()
            return
        }
        if (productCost == null || productCost <= 0) {
            Toast.makeText(this, "⚠️ Введите корректную цену", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedYpkId.isNullOrEmpty()) {
            Toast.makeText(this, "⚠️ Выберите УПК", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedStatusProductId.isNullOrEmpty()) {
            Toast.makeText(this, "⚠️ Выберите статус", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                binding.buttonSave.isEnabled = false
                binding.buttonSave.text = "Добавление..."

                val nameBody = productName.toRequestBody("text/plain".toMediaType())
                val infoBody = productInfo.toRequestBody("text/plain".toMediaType())
                val costBody = productCost.toString().toRequestBody("text/plain".toMediaType())
                val isProductBody = isProduct.toString().toRequestBody("text/plain".toMediaType())
                val adressBody = address.toRequestBody("text/plain".toMediaType())
                val ypkIdBody = selectedYpkId!!.toRequestBody("text/plain".toMediaType())
                val statusProductIdBody = selectedStatusProductId!!.toRequestBody("text/plain".toMediaType())

                val photoPart = selectedImageUri?.let { uri ->
                    try {
                        val inputStream = contentResolver.openInputStream(uri)
                        val tempFile = File.createTempFile("product_image", ".jpg", cacheDir)
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

                println("\n📤 ОТПРАВКА НОВОГО ПРОДУКТА:")
                println("   ProductName: $productName")
                println("   ProductCost: $productCost")
                println("   YpkId: $selectedYpkId")
                println("   StatusProductId: $selectedStatusProductId")
                println("   Photo: ${if (photoPart != null) "Есть" else "Нет"}\n")

                val response = RetrofitClient.apiService.createProduct(
                    productName = nameBody,
                    productInfo = infoBody,
                    productCost = costBody,
                    isProduct = isProductBody,
                    adres = adressBody,
                    ypkId = ypkIdBody,
                    statusProductId = statusProductIdBody,
                    photo = photoPart
                )

                println("📥 ОТВЕТ СЕРВЕРА:")
                println("   Code: ${response.code()}")
                println("   Message: ${response.message()}")
                println("   Successful: ${response.isSuccessful}\n")

                if (response.isSuccessful) {
                    Toast.makeText(this@ProductDetailActivity, "✅ Продукт создан", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val error = response.errorBody()?.string() ?: "Неизвестная ошибка"
                    println("❌ ОШИБКА: $error")
                    Toast.makeText(this@ProductDetailActivity, "❌ Ошибка: $error", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                println("❌ ИСКЛЮЧЕНИЕ: ${e.message}")
                e.printStackTrace()
                Toast.makeText(this@ProductDetailActivity, "❌ ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.buttonSave.isEnabled = true
                binding.buttonSave.text = "Добавить"
            }
        }
    }

    // ==================== ОБНОВЛЕНИЕ СУЩЕСТВУЮЩЕГО ПРОДУКТА ====================
    private fun updateProduct() {
        if (oldProduct == null) return

        val productName = binding.editTextProductName.text.toString().trim()
        val productCost = binding.editTextCost.text.toString()
            .replace(" ₽", "")
            .replace(",", ".")
            .trim()
            .toDoubleOrNull()
        val productInfo = binding.editTextInfo.text.toString().trim()
        val isProduct = binding.checkBoxIsProduct.isChecked
        val address = binding.editTextAdress.text.toString().trim()

        if (productName.isEmpty()) {
            Toast.makeText(this, "⚠️ Введите название продукта", Toast.LENGTH_SHORT).show()
            return
        }
        if (productCost == null || productCost <= 0) {
            Toast.makeText(this, "⚠️ Введите корректную цену", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedYpkId.isNullOrEmpty()) {
            Toast.makeText(this, "⚠️ Выберите УПК", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedStatusProductId.isNullOrEmpty()) {
            Toast.makeText(this, "⚠️ Выберите статус", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                binding.buttonSave.isEnabled = false
                binding.buttonSave.text = "Сохранение..."

                val idBody = oldProduct!!.id.toRequestBody("text/plain".toMediaType())
                val nameBody = productName.toRequestBody("text/plain".toMediaType())
                val infoBody = productInfo.toRequestBody("text/plain".toMediaType())
                val costBody = productCost.toString().toRequestBody("text/plain".toMediaType())
                val isProductBody = isProduct.toString().toRequestBody("text/plain".toMediaType())
                val adressBody = address.toRequestBody("text/plain".toMediaType())
                val ypkIdBody = selectedYpkId!!.toRequestBody("text/plain".toMediaType())
                val statusProductIdBody = selectedStatusProductId!!.toRequestBody("text/plain".toMediaType())

                val photoPart = selectedImageUri?.let { uri ->
                    try {
                        val inputStream = contentResolver.openInputStream(uri)
                        val tempFile = File.createTempFile("product_image", ".jpg", cacheDir)
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

                println("\n📤 ОТПРАВКА ОБНОВЛЕНИЯ ПРОДУКТА:")
                println("   ID: ${oldProduct!!.id}")
                println("   ProductName: $productName")
                println("   ProductCost: $productCost")
                println("   YpkId: $selectedYpkId")
                println("   StatusProductId: $selectedStatusProductId")
                println("   Photo: ${if (photoPart != null) "Есть" else "Нет"}\n")
                println("   adres : $adressBody")

                val response = RetrofitClient.apiService.updateProduct(
                    id = idBody,
                    productName = nameBody,
                    productInfo = infoBody,
                    productCost = costBody,
                    isProduct = isProductBody,
                    adres = adressBody,
                    ypkId = ypkIdBody,
                    statusProductId = statusProductIdBody,
                    photo = photoPart
                )

                println("📥 ОТВЕТ СЕРВЕРА:")
                println("   Code: ${response.code()}")
                println("   Message: ${response.message()}")
                println("   Successful: ${response.isSuccessful}\n")

                if (response.isSuccessful) {
                    Toast.makeText(this@ProductDetailActivity, "✅ Изменения сохранены", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val error = response.errorBody()?.string() ?: "Неизвестная ошибка"
                    println("❌ ОШИБКА: $error")
                    Toast.makeText(this@ProductDetailActivity, "❌ Ошибка: $error", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                println("❌ ИСКЛЮЧЕНИЕ: ${e.message}")
                e.printStackTrace()
                Toast.makeText(this@ProductDetailActivity, "❌ ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.buttonSave.isEnabled = true
                binding.buttonSave.text = "Сохранить"
            }
        }
    }


    private fun delete() {
        AlertDialog.Builder(this)
            .setTitle("Удаление упк")
            .setMessage("Вы уверены, что хотите удалить этот упк? Это действие нельзя отменить.")
            .setPositiveButton("Удалить") { _, _ -> performDeleteUpk(currentProductId!!) }
            .setNegativeButton("Отмена", null).show()
    }

    private fun performDeleteUpk(selectedUpkId: String) {
        lifecycleScope.launch {
            try {
                println("🗑️ Удаление продукта: $selectedUpkId")

                val response = RetrofitClient.apiService.deleteProductById(selectedUpkId)

                println("📥 Ответ сервера: ${response.code()}")

                if (response.isSuccessful) {
                    println("✅ заказ продукт")
                    Toast.makeText(this@ProductDetailActivity, "✅ упк продукт", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val error = response.errorBody()?.string() ?: "Неизвестная ошибка"
                    println("❌ ОШИБКА: ${response.code()} - $error")
                    Toast.makeText(this@ProductDetailActivity, "❌ Ошибка: $error", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                println("❌ ИСКЛЮЧЕНИЕ: ${e.message}")
                e.printStackTrace()
                Toast.makeText(this@ProductDetailActivity, "❌ ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}