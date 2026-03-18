package com.example.upk_btpi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.upk_btpi.databinding.FragmentProfileBinding

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
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater,container,false)
        return binding.root
    }

     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //загрузка данные об аккаунте
        loadUserProfile()

        //Кнопка "редактировать"

    // Кнопка "Выход"


    }

    fun loadUserProfile(){
        val prefs = requireContext().getSharedPreferences("auth_prefs", 0)

        binding.textViewName.text = prefs.getString("user_name", "Не указано")
        binding.textViewPhone.text = prefs.getString("user_id", "Не указано")
        binding.textViewRole.text = prefs.getString("user_role", "Пользователь")
        binding.textViewInf.text = prefs.getString("user_info", "Нет информации")
    }


}