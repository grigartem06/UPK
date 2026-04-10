package com.example.upk_btpi

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.saveable.autoSaver
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.upk_btpi.Adapters.UserAdapter
import com.example.upk_btpi.Models.User.UpdateUserDto
import com.example.upk_btpi.Models.User.UserDto
import com.example.upk_btpi.Retrofit.AuthRepository
import com.example.upk_btpi.Retrofit.RetrofitClient
import com.example.upk_btpi.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch
import okhttp3.Response
import kotlin.io.path.Path
import kotlin.uuid.ExperimentalUuidApi

class ProfileFragment : Fragment() {
    private  var _binding: FragmentProfileBinding? = null
    private val authRepository = AuthRepository()
    var isEditMode = false
    var oldUser = UserDto(
        id = "id",
        fullname = null,
        hashPassword = null,
        phoneNumber = null,
        userInfo = null,
        isActive = true
    )
    private  val binding get() = _binding!!
    var nowUserID: String ?= null
    var role: String ?= null
    var token: String ?= null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = FragmentProfileBinding.inflate(inflater,container,false)
        return binding.root
    }

     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

         val prefs = requireContext().getSharedPreferences("auth_prefs", 0)
         nowUserID = prefs.getString("user_id", null)
         role = prefs.getString("user_role", null)
         token = prefs.getString("auth_token", null)

         //загрузка данные об аккаунте
        loadUserProfile()

         //загрузка списка пользователей
         if(role =="Admin") {

         }

        // Кнопка "Выход"
        binding.buttonLogOut.setOnClickListener {
            if(!isEditMode)
            {
                //"выйти из аккаунта
                val prefs = requireContext().getSharedPreferences("auth_prefs", 0)
                prefs.edit().clear().apply()

                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()

            }
            else{ //"отмена"
                findNavController().apply { popBackStack()
                navigate(R.id.profileFragment)}
            }

        }

         //кнопка редактирования
         binding.buttonEdit.setOnClickListener {
             if(isEditMode ) //отключаем редактирование
             {
                 isEditMode = false
                 binding.buttonEdit.text = "изменить"
                 binding.buttonLogOut.text = "выйти"
                 val prefs = requireContext().getSharedPreferences("auth_prefs", 0)
                 val roleName = prefs.getString("user_role", null)

                 lifecycleScope.launch {
                     val rolesResponse = RetrofitClient.apiService.getAllRoles()
                     if(!rolesResponse.isSuccessful || rolesResponse.body() == null) {
                     }
                     val roles = rolesResponse.body()!!.roles
                     val role = roles.find {
                         it.roleName?.equals(roleName, ignoreCase = true) == true
                     }
                     val roleid = role?.id

                     //сохранение изменений
                     val request = UpdateUserDto(
                         id = oldUser.id,
                         fullname = binding.editTextTextName.text.toString(),
                         userInfo = binding.editTextTextInf.text.toString(),
                         phoneNumber = binding.editTextTextPhone.text.toString(),
                         roleId = roleid.toString(),
                         isActive = true
                     )
                     RetrofitClient.apiService.updateUser(request)

                 }
             }
             else // включаем редактирование
             {
                 isEditMode = true
                 binding.buttonEdit.text = "сохранить изменения"
                 binding.buttonLogOut.text = "отмена"

             }
             changeOfAccess(isEditMode)
         }
    }

    fun changeOfAccess(isEditMode: Boolean){
        //разрешено редактирование на момент нажатия
        binding.editTextTextName.apply {
            isFocusable = isEditMode
            isFocusableInTouchMode = isEditMode
            isClickable = isEditMode
            isCursorVisible = isEditMode
        }
        binding.editTextTextPhone.apply {
            isFocusable = isEditMode
            isFocusableInTouchMode = isEditMode
            isClickable = isEditMode
            isCursorVisible = isEditMode
        }
        binding.editTextTextInf.apply {
            isFocusable = isEditMode
            isFocusableInTouchMode = isEditMode
            isClickable = isEditMode
            isCursorVisible = isEditMode
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun loadUserProfile() {
        if (nowUserID.isNullOrEmpty()) {
            Toast.makeText(requireContext(),"⚠️user_id не найден в SharedPreferences", Toast.LENGTH_LONG)
            return
        }
        else { loadUserInf() }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

     fun loadUserInf() {
         viewLifecycleOwner.lifecycleScope.launch {
             try {
                 val response = RetrofitClient.apiService.getUserByID(nowUserID.toString())
                 if (response.isSuccessful && response.body() != null) {
                     val user = response.body()!!
                     // Обновляем UI
                     binding.editTextTextName.setText(user.fullname)
                     binding.editTextTextPhone.setText(user.phoneNumber)
                     binding.editTextTextRole.setText(role.toString())

                     if (user.userInfo != null) { binding.editTextTextInf.setText(user.userInfo) }
                     oldUser = user

                 } else {
                     val error = response.errorBody()?.string() ?: "Неизвестная ошибка"
                     Toast.makeText(requireContext(), "Ошибка: ${error}", Toast.LENGTH_SHORT).show()
                 }
             } catch (e: Exception) {
                 e.printStackTrace()
                 Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
             }

             if(role == "Admin") {
                loadAllUser()
             }
             else { binding.recyclerViewUsers.visibility = View.GONE}
         }
    }

    fun loadAllUser(){
        viewLifecycleOwner.lifecycleScope.launch {
            val result =authRepository.getALLUsers()
            result.onSuccess {response ->
                val users= response.users
                if(users.isEmpty()) { Toast.makeText(requireContext(),"пользователей нет", Toast.LENGTH_SHORT).show() }
                else
                {
                    binding.recyclerViewUsers.apply {
                        visibility = View.VISIBLE
                        layoutManager = LinearLayoutManager(requireContext())
                        setHasFixedSize(true)
                        adapter = UserAdapter(users) {user ->
                            Toast.makeText(requireContext(), "Выбран: ${user.fullname}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            result.onFailure {
                super.onDestroyView()
                _binding= null
            }
        }
    }



}