package com.example.upk_btpi

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.upk_btpi.Adapters.OrderADapter
import com.example.upk_btpi.Models.Order.OrderDto
import com.example.upk_btpi.Retrofit.AuthRepository
import com.example.upk_btpi.databinding.FragmentOrdersBinding
<<<<<<< HEAD
import com.example.upk_btpi.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch


class OrdersFragment : Fragment() {

   private var _binding : FragmentOrdersBinding? = null
    private val binding get() = _binding!!
    private val authRepository = AuthRepository()
    private var orderAdapter: OrderADapter ?= null
=======
import kotlinx.coroutines.launch

class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!
    private val authRepository = AuthRepository()
    private var orderAdapter: OrderADapter? = null
>>>>>>> origin/Визуал

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
<<<<<<< HEAD
    ): View{
        _binding = FragmentOrdersBinding.inflate(inflater,container,false)
=======
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
>>>>>>> origin/Визуал
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val authPrefs = requireContext().getSharedPreferences("auth_prefs", 0)
        val role = authPrefs.getString("user_role", null)

<<<<<<< HEAD
        binding.ListOfOrders.apply { layoutManager = LinearLayoutManager(requireContext())
        setHasFixedSize(true)}

        if(role.isNullOrEmpty()) {
            Toast.makeText(requireContext(),"ошибка входа", Toast.LENGTH_SHORT).show()
=======
        binding.ListOfOrders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        if (role.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "ошибка входа", Toast.LENGTH_SHORT).show()
>>>>>>> origin/Визуал
            return
        }
        loadOrders(role)
    }

<<<<<<< HEAD
    fun loadOrders(role:String) {
        lifecycleScope.launch {
            val result = authRepository.getOrdersForRoles(role)
            if(result != null) {
                result.onSuccess {
                    response->
                    val orders = response.orders
                    if(orders.isEmpty()) {

                        _binding?.ListOfOrders?.visibility = View.GONE
                        _binding?.textView10?.visibility = View.VISIBLE
                        _binding?.textView10?.text = "Заказов нет"
                    }
                    else {
                        if(orderAdapter == null) {
                            orderAdapter = OrderADapter(orders) {order -> onOrderClick(order)}
                            binding.ListOfOrders.adapter = orderAdapter
                        }else  { orderAdapter?.updateOrders(orders) }
                    }
                }
                result.onFailure { error -> Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show() }
=======
    fun loadOrders(role: String) {
        lifecycleScope.launch {
            val result = authRepository.getOrdersForRoles(role)
            if (result != null) {
                result.onSuccess { response ->
                    val orders = response.orders
                    val binding = _binding ?: return@onSuccess
                    if (orders.isEmpty()) {
                        binding.ListOfOrders.visibility = View.GONE
                        binding.emptyOrdersMessage.visibility = View.VISIBLE
                        binding.emptyOrdersMessage.text = "Заказов нет"
                    } else {
                        binding.ListOfOrders.visibility = View.VISIBLE
                        binding.emptyOrdersMessage.visibility = View.GONE
                        if (orderAdapter == null) {
                            orderAdapter = OrderADapter(orders) { order -> onOrderClick(order) }
                            binding.ListOfOrders.adapter = orderAdapter
                        } else {
                            orderAdapter?.updateOrders(orders)
                        }
                    }
                }
                result.onFailure { error ->
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show()
                }
>>>>>>> origin/Визуал
            }
        }
    }

<<<<<<< HEAD
    private fun onOrderClick(order: OrderDto)  {
        val prefs = requireContext().getSharedPreferences("order_prefs", 0)
        prefs.edit().apply(){
=======
    private fun onOrderClick(order: OrderDto) {
        val prefs = requireContext().getSharedPreferences("order_prefs", 0)
        prefs.edit().apply {
>>>>>>> origin/Визуал
            putString("selected_orderId", order.id)
            apply()
        }

        //переход на страницу
<<<<<<< HEAD
        val intent  = Intent(requireContext(), order_detail_Activity::class.java)
        startActivity(intent)
    }
=======
        val intent = Intent(requireContext(), order_detail_Activity::class.java)
        startActivity(intent)
    }

>>>>>>> origin/Визуал
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
<<<<<<< HEAD









=======
>>>>>>> origin/Визуал
}