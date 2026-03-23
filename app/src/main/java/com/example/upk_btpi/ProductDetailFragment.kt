package com.example.upk_btpi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.upk_btpi.Models.Product.ProductDto
import com.example.upk_btpi.Retrofit.AuthRepository
import com.example.upk_btpi.databinding.FragmentProductDetailBinding
import kotlinx.coroutines.launch


class ProductDetailFragment : Fragment() {
    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    private val authRepository = AuthRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Получаем ID из аргументов
        val prefs = requireContext().getSharedPreferences("product_prefs", 0)
        val productId = prefs.getString("selected_product_id", null)

        if (productId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "⚠️ ID продукта не найден", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressedDispatcher.onBackPressed()
            return
        }

        // Загрузка данных
        loadProductDetails(productId)

         //Кнопка "Назад"
        binding.buttonBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Кнопка "Заказать"
//        binding.buttonOrder.setOnClickListener {
//            Toast.makeText(requireContext(), "Заказ оформляется...", Toast.LENGTH_SHORT).show()
//            // TODO: Переход на страницу заказа
//        }
    }

    private fun loadProductDetails(productId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = authRepository.getProductById(productId)

            result.onSuccess { product ->
                displayProduct(product)
            }

            result.onFailure { error ->
                println("❌ Ошибка: ${error.message}")
                Toast.makeText(requireContext(), "❌ ${error.message}", Toast.LENGTH_SHORT).show()
                binding.textViewName.text = "Не удалось загрузить"
            }
        }
    }

    private fun displayProduct(product: ProductDto) {
        binding.textViewName.text = product.productName ?: "Без названия"
        binding.textViewCost.text = "${product.productCost} ₽"
        binding.textViewInfo.text = product.productInfo ?: "Нет описания"
        binding.textViewRating.text = "⭐ ${product.raiting} / 5"
        binding.textViewAddress.text = "📍 ${product.adress ?: "Адрес не указан"}"

        // Тип: Товар или Услуга
        val typeLabel = if (product.isProduct) "📦 Товар" else "🔧 Услуга"
        binding.textViewType.text = typeLabel

        // Статус (если есть)
       // binding.textViewStatus.text = "Статус: ${product. ?: "Неизвестно"}"

        // Изображение
        if (!product.photoPath.isNullOrEmpty()) {
            binding.imageViewProduct.visibility = View.VISIBLE
            // Загрузка через Glide (если подключен)
            // Glide.with(this).load(product.photoPath).into(binding.imageViewProduct)
        } else {
            binding.imageViewProduct.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ✅ Фабричный метод для создания фрагмента с аргументами
    companion object {
        fun newInstance(productId: String): ProductDetailFragment {
            val fragment = ProductDetailFragment()
            val args = Bundle()
            args.putString("product_id", productId)
            fragment.arguments = args
            return fragment
        }
    }

}