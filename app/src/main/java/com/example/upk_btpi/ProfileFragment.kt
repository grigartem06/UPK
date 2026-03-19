package com.example.upk_btpi

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.upk_btpi.Retrofit.RetrofitClient
import com.example.upk_btpi.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters

    private  var _binding: FragmentProfileBinding? = null
    private  val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = FragmentProfileBinding.inflate(inflater,container,false)
        return binding.root
    }

     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //загрузка данные об аккаунте
        loadUserProfile()

        //Кнопка "редактировать"

    // Кнопка "Выход"
    binding.buttonLogOut.setOnClickListener {
        val prefs = requireContext().getSharedPreferences("auth_prefs", 0)
        prefs.edit().clear().apply()

        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun loadUserProfile() {
        val prefs = requireContext().getSharedPreferences("auth_prefs", 0)
        val nowUserID = prefs.getString("user_id", null)


        val token = prefs.getString("auth_token", null)
        Toast.makeText(requireContext(), token.toString(), Toast.LENGTH_LONG).show()
        Toast.makeText(requireContext(), nowUserID.toString(), Toast.LENGTH_LONG).show()

        if (nowUserID.isNullOrEmpty()) {
            Toast.makeText(requireContext(),"⚠️ user_id не найден в SharedPreferences", Toast.LENGTH_LONG)
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getUserByID(nowUserID)

                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!

                    // Обновляем UI
                    binding.textViewName.text = user.fullName ?: "Не указано"
                    binding.textViewPhone.text = user.phoneNumber ?: "Не указано"
                    binding.textViewRole.text = user.role?.roleName ?: "Пользователь"
                    binding.textViewInf.text = user.userInfo ?: "Нет информации"

                } else {
                    val error = response.errorBody()?.string() ?: "Неизвестная ошибка"
                    Toast.makeText(requireContext(), "Ошибка: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}