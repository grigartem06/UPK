package com.example.upk_btpi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.upk_btpi.Models.Product.ProductDto
import com.example.upk_btpi.Retrofit.AuthRepository
import com.example.upk_btpi.databinding.ActivityProductDetailBinding
import kotlinx.coroutines.launch

class ProductDetailActivity : AppCompatActivity() {
    private lateinit var binding : ActivityProductDetailBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("product_prefs", 0)
        val productId = prefs.getString("selected_product_id", null)
        if(productId ==null) {
            Toast.makeText(this, "ошибка", Toast.LENGTH_SHORT)
            return
        }
        loadProductDetails(productId)

        binding.buttonOrder.setOnClickListener {
            val intent = Intent(this@ProductDetailActivity, NewOrderActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun loadProductDetails(productId: String) {
        lifecycleScope.launch {
            val result = authRepository.getProductById(productId)
            result.onSuccess {product -> displayProduct(product) }
            result.onFailure { Toast.makeText(this@ProductDetailActivity,"ошибка получения данных", Toast.LENGTH_SHORT)  }
        }
    }

    private fun displayProduct(product: ProductDto) {
        binding.textViewName.setText(product.productName)
        if(product.isProduct != true) {binding.textViewType.setText("услуга")}
        binding.textViewCost.setText(product.productCost.toString())
        binding.textViewRating.setText(product.raiting.toString())
        binding.textViewInfo.setText(product.productInfo.toString())
        binding.textViewAddress.setText(product.adress)
    }



}