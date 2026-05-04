package com.example.upk_btpi.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.upk_btpi.Models.Product.ProductDto
import com.example.upk_btpi.databinding.ItemProductBinding
import java.security.PrivateKey

class ProductAdapter(
    private var products:List<ProductDto>,
    private val onItemClick:(ProductDto) -> Unit
): RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

class ProductViewHolder(private val binding: ItemProductBinding)
    : RecyclerView.ViewHolder(binding.root) {
    fun bind(product: ProductDto, onClick: (ProductDto) -> Unit) {
        binding.textViewName.text = product.productName ?: "Без названия"
        binding.textViewCost.text = "${product.productCost} ₽"
        binding.textViewInfo.text = product.productInfo ?: "Нет описания"
        binding.textViewRaitind.text = product.ypkId

        // Загрузка изображения
        if (!product.photoPath.isNullOrEmpty())
        { binding.imageViewProduct.visibility = android.view.View.VISIBLE }
        else { binding.imageViewProduct.visibility = android.view.View.GONE }

        binding.root.setOnClickListener { onClick(product) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) { holder.bind(products[position], onItemClick) }

    fun updateProducts(newProducts: List<ProductDto>) {
        products = newProducts
        notifyDataSetChanged()
    }

    override fun getItemCount() = products.size
}