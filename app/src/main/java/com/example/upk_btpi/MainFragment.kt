package com.example.upk_btpi

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.upk_btpi.Adapters.ProductAdapter
import com.example.upk_btpi.Models.Product.ProductDto
import com.example.upk_btpi.Retrofit.AuthRepository
import com.example.upk_btpi.databinding.FragmentMainBinding
import kotlinx.coroutines.launch


class MainFragment : Fragment() {
    private  var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private val authRepository = AuthRepository()
    private var productAdapter: ProductAdapter? = null
    private var allProducts : List<ProductDto> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMainBinding.inflate(inflater,container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // Простая проверка: фрагмент добавлен и binding существует
        if (isAdded && _binding != null) {
            loadProducts()
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerViewProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        loadProducts()

        binding.buttonCheckAll.setOnClickListener {filterProducts(FilterType.ALL) }
        binding.buttonCheckOrders.setOnClickListener {filterProducts(FilterType.PRODUCTS) }
        binding.buttonCheckServices.setOnClickListener {filterProducts(FilterType.SERVICES) }
        binding.buttonSearch.setOnClickListener {
            val query = binding.editTextText3.text.toString()
            Toast.makeText(requireContext(), "Поиск: $query", Toast.LENGTH_SHORT).show()
        }
        binding.checkBox.setOnClickListener {loadProducts()  }
        binding.floatingActionButton.setOnClickListener { addNewProduct() }
    }

    private enum class FilterType { ALL, PRODUCTS, SERVICES }
    private fun filterProducts(type: FilterType) {
        val filteredList = when (type) {
            FilterType.ALL -> allProducts
            FilterType.PRODUCTS -> allProducts.filter { it.isProduct == true }
            FilterType.SERVICES -> allProducts.filter { it.isProduct == false }
        }

        if (filteredList.isEmpty()) { Toast.makeText(requireContext(), "📭 Список пуст", Toast.LENGTH_SHORT).show() }

        productAdapter = ProductAdapter(filteredList) { product -> onProductClick(product) }
        binding.recyclerViewProducts.adapter = productAdapter
    }



    private fun  loadProducts(){
        val authPrefs =requireContext().getSharedPreferences("auth_prefs", MODE_PRIVATE)
        var userRole = authPrefs.getString("user_role", null)

        viewLifecycleOwner.lifecycleScope.launch {
            if(userRole == "DefaultUser") {
                val result = authRepository.getAllProducts()
                result.onSuccess { response ->
                    allProducts = response.products
                    filterProducts(FilterType.ALL)
                }
                result.onFailure { error-> Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show() }
            }
            else {
                if (binding.checkBox.isChecked) {
                    val result = authRepository.getAllEdetingProducts()
                    result.onSuccess { response -> allProducts = response.products
                        filterProducts(FilterType.ALL) }

                    result.onFailure { error-> Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show() }
                }
                else {
                    val result = authRepository.getAllProducts()
                    result.onSuccess { response -> allProducts = response.products
                        filterProducts(FilterType.ALL) }

                    result.onFailure { error-> Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show() }
                }
            }
        }
    }

    private fun onProductClick(product: ProductDto) {
        val prefs = requireContext().getSharedPreferences("product_prefs", 0)
        prefs.edit().apply(){ putString("selected_product_id", product.id);apply() }

        //переход на другой activity
        val intent = Intent(requireContext(), ProductDetailActivity::class.java)
        startActivity(intent)
    }

    private fun addNewProduct(){
        val prefs = requireContext().getSharedPreferences("product_prefs", 0)
        prefs.edit().apply(){ putString("selected_product_id", "add_new_product");apply() }

        //переход на другой activity
        val intent = Intent(requireContext(), ProductDetailActivity::class.java)
        startActivity(intent)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}