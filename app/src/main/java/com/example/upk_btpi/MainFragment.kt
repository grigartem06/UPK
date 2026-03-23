package com.example.upk_btpi

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater,container, false)
        return binding.root
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
    }

    private enum class FilterType { ALL, PRODUCTS, SERVICES }
    private fun filterProducts(type: FilterType) {
        val filteredList = when (type) {
            FilterType.ALL -> allProducts
            FilterType.PRODUCTS -> allProducts.filter { it.isProduct == true }
            FilterType.SERVICES -> allProducts.filter { it.isProduct == false }
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(requireContext(), "📭 Список пуст", Toast.LENGTH_SHORT).show()
        }

        if (productAdapter == null) {
            productAdapter = ProductAdapter(filteredList) { product ->
                onProductClick(product)
            }
            binding.recyclerViewProducts.adapter = productAdapter
        } else {
            productAdapter?.updateProducts(filteredList)
        }
    }

    private fun  loadProducts(){
        viewLifecycleOwner.lifecycleScope.launch {
            val result = authRepository.getAllProducts()

            result.onSuccess { response ->
                allProducts = response.products
                filterProducts(FilterType.ALL)
            }
            result.onFailure { error->
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onProductClick(product: ProductDto) {
        val prefs = requireContext().getSharedPreferences("product_prefs", 0)
        prefs.edit().apply(){
            putString("selected_product_id", product.id)
            apply()
        }
        val detailFragment = ProductDetailFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment,detailFragment)
            .addToBackStack(null)
            .commit()

    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}