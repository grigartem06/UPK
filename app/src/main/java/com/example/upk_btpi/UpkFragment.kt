package com.example.upk_btpi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.collection.emptyLongSet
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.upk_btpi.Adapters.UpkAdapter
import com.example.upk_btpi.Retrofit.AuthRepository
import com.example.upk_btpi.databinding.FragmentUpkBinding
import kotlinx.coroutines.launch

class UpkFragment : Fragment() {
  private var _binding: FragmentUpkBinding?= null
    private val binding get() = _binding!!
    private val authRepository = AuthRepository()
    private var upkAdapter: UpkAdapter?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUpkBinding.inflate(inflater, container, false)
        return binding.root
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding.recyclerViewUpk.apply {layoutManager = LinearLayoutManager(requireContext())
//        setHasFixedSize(true)}
//
//        loadListOfUpk()
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ✅ 3️⃣ Настройка RecyclerView (ТОЛЬКО здесь, не в onCreate!)
        binding.recyclerViewUpk.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
        // ✅ 4️⃣ Загрузка данных
        loadListOfUpk()
    }


    fun loadListOfUpk() {
        lifecycleScope.launch {
            val result = authRepository.getAllUpks()
            result.onSuccess {
                response -> val upks = response.ypks
                if(upks.isEmpty()) {Toast.makeText(requireContext(),"упк нет", Toast.LENGTH_SHORT).show()}
                else {
                    binding.recyclerViewUpk.apply { layoutManager = LinearLayoutManager(requireContext())
                    setHasFixedSize(true)
                    adapter = UpkAdapter(upks) {upk ->
                        Toast.makeText(requireContext(), "Выбран: ${upk.ypkName}", Toast.LENGTH_SHORT).show() }
                    }
                }
            }

            result.onFailure { super.onDestroyView()
                _binding = null }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}