package com.example.upk_btpi

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.upk_btpi.Models.Product.ProductDto
import com.example.upk_btpi.Models.StatusProduct.StatusProductDto  // ✅ Исправлено: с большой буквы
import com.example.upk_btpi.Models.StatusProduct.StatusProductResponse
import com.example.upk_btpi.Retrofit.AuthRepository
import com.example.upk_btpi.Retrofit.RetrofitClient
import com.example.upk_btpi.databinding.ActivityProductDetailBinding
import kotlinx.coroutines.launch

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private val authRepository = AuthRepository()

    private var isEditMode = false
    private var userRole: String? = null
    private var currentProductId: String? = null
    private var oldProduct: ProductDto? = null

    // ✅ Для Spinner статусов
    private var statusProducts: List<StatusProductDto> = emptyList()
    private var selectedStatusId: String? = null

    // ✅ Значение по умолчанию для StatusProductId
    private val defaultStatusId = "a3a89ae5-9acb-4fb5-9b88-e6b9ffa5994f"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        // ✅ Сначала загружаем продукт, потом статусы (чтобы установить текущий статус в Spinner)
        loadProductDetails(currentProductId!!)

        if (userRole == "Admin" || userRole == "Manager") {
            loadStatusProducts()
        }
    }

    // ✅ Загрузка списка статусов
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

    // ✅ Настройка Spinner
    private fun setupSpinner() {
        if (statusProducts.isEmpty()) return

        val statusNames = statusProducts.map { it.statusName ?: "Без названия" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerStatus.adapter = adapter

        // ✅ Устанавливаем текущий статус (только если oldProduct уже загружен)
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

    // ✅ Изменение статуса продукта
    private fun changeProductStatus(newStatusId: String) {
        lifecycleScope.launch {
            try {
                binding.buttonChangeStatus.isEnabled = false

                val response = RetrofitClient.apiService.updateProduct(
                    id = oldProduct?.id ?: "",
                    productName = oldProduct?.productName ?: "",
                    productInfo = oldProduct?.productInfo ?: "",
                    productCost = oldProduct?.productCost ?: 0.0,
                    isProduct = oldProduct?.isProduct ?: true,
                    adress = oldProduct?.adress ?: "",
                    ypkId = oldProduct?.ypkId ?: "",
                    statusProductId = newStatusId  // ✅ Новый статус из Spinner
                )

                if (response.isSuccessful) {
                    Toast.makeText(this@ProductDetailActivity, "✅ Статус изменён", Toast.LENGTH_SHORT).show()
                    oldProduct = oldProduct?.copy(statusProductId = newStatusId)
                } else {
                    val error = response.errorBody()?.string() ?: "Неизвестная ошибка"
                    Toast.makeText(this@ProductDetailActivity, "❌ Ошибка: $error", Toast.LENGTH_SHORT).show()
                }
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
        // Валидация
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

        // ✅ ГАРАНТИРУЕМ, ЧТО StatusProductId НЕ ПУСТОЙ
        val statusProductIdToSend = oldProduct?.statusProductId
            ?: "a3a89ae5-9acb-4fb5-9b88-e6b9ffa5994f"  // ✅ Значение по умолчанию

        lifecycleScope.launch {
            try {
                binding.buttonOrder.isEnabled = false
                binding.buttonOrder.text = "Сохранение..."

                println("\n📤 ОТПРАВКА ОБНОВЛЕНИЯ ПРОДУКТА:")
                println("   ID: ${oldProduct?.id}")
                println("   ProductName: $productName")
                println("   ProductCost: $productCost")
                println("   StatusProductId: $statusProductIdToSend\n")

                // ✅ ВЫЗОВ API С ПРОВЕРКОЙ ОТВЕТА
                val response = RetrofitClient.apiService.updateProduct(
                    id = oldProduct?.id ?: "",
                    productName = productName,
                    productInfo = productInfo,
                    productCost = productCost,
                    isProduct = oldProduct?.isProduct ?: true,
                    adress = address,
                    ypkId = oldProduct?.ypkId ?: "",
                    statusProductId = statusProductIdToSend  // ✅ НЕ ПУСТОЙ!
                )

                println("📥 ОТВЕТ СЕРВЕРА:")
                println("   Code: ${response.code()}")
                println("   Successful: ${response.isSuccessful}\n")

                if (response.isSuccessful) {
                    Toast.makeText(this@ProductDetailActivity, "✅ Изменения сохранены", Toast.LENGTH_SHORT).show()

                    // ✅ Обновляем локальный объект
                    oldProduct = oldProduct?.copy(
                        productName = productName,
                        productCost = productCost,
                        productInfo = productInfo,
                        adress = address
                    )
                } else {
                    val error = response.errorBody()?.string() ?: "Неизвестная ошибка"
                    println("❌ ОШИБКА: $error\n")
                    Toast.makeText(this@ProductDetailActivity, "❌ Ошибка: $error", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                println("❌ ИСКЛЮЧЕНИЕ: ${e.message}\n")
                e.printStackTrace()
                Toast.makeText(this@ProductDetailActivity, "❌ ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                // Возвращаем интерфейс в исходное состояние
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

                // ✅ Если статусы уже загружены — обновляем Spinner
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