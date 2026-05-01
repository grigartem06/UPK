package com.example.upk_btpi

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.animateDecay
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.upk_btpi.Models.Role.RoleDto
import com.example.upk_btpi.Models.User.UpdateUserDto
import com.example.upk_btpi.Models.User.UpdateUserForAdminDto
import com.example.upk_btpi.Models.User.UserDto
import com.example.upk_btpi.Models.Ypk.YpksDto
import com.example.upk_btpi.Retrofit.AuthRepository
import com.example.upk_btpi.Retrofit.RetrofitClient
import com.example.upk_btpi.databinding.ActivityUserDetailBinding
import kotlinx.coroutines.launch

class user_detail_Activity : AppCompatActivity() {
    private lateinit var binding: ActivityUserDetailBinding
    private lateinit var oldUser: UserDto
    private  var selectedUserId: String?=null
    private val authRepository = AuthRepository()
    private var EditMode: Boolean = false
    private var listOfRoles:List<RoleDto> = emptyList()
    private var listOfYpk:  List<YpksDto> = emptyList()
    private var selectedRole: String?=null
    private var selectedYpk: String?=null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUserDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //получение данных из SharedPreferences
        val userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        selectedUserId = userPrefs.getString("selected_user_id", null)

        //проверка на получение данных
        if(selectedUserId.isNullOrEmpty()) {finish();return}

        getInfAboutUser(selectedUserId.toString())

        binding.buttonBack.setOnClickListener { back()}
        binding.buttonEdit.setOnClickListener { edit()}
        binding.buttonSave.setOnClickListener { save()}
        binding.buttonDelete.setOnClickListener { delete() }




    }

    private fun getInfAboutUser(userId: String) {
        //получаем данные из апи
        lifecycleScope.launch {
            var result = authRepository.getUserByID(userId)
            result.onSuccess {user->oldUser= user; displayUserInf(user)  }
            result.onFailure { Toast.makeText(this@user_detail_Activity, "❌ Ошибка получения данных", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun displayUserInf(user: UserDto) {
        if(oldUser.role.roleName!="Admin") {binding.buttonDelete.visibility= View.VISIBLE}
        if (EditMode) {
            // ✅ В режиме редактирования заполняем EditText
            binding.editTextTextName.setText(user.fullname ?: "")
            binding.editTextPhone.setText(user.phoneNumber ?: "")
            binding.editTextTextInfo.setText(user.userInfo ?: "")
        } else {
            // ✅ В режиме просмотра показываем TextView
            binding.textViewName.text = user.fullname ?: "Без имени"
            binding.textViewPhoneNumber.text = user.phoneNumber ?: "Нет телефона"
            binding.textViewInfo.text = user.userInfo ?: "Нет информации"
            binding.textViewIsActive.text = if (user.isActive) "✅ Активный" else "❌ Не активен"

            // ✅ ИСПРАВЛЕНО: безопасный доступ к nullable ypk
            binding.textViewYpk.text = user.ypk?.ypkName ?: "Не назначен"

            // ✅ ИСПРАВЛЕНО: безопасный доступ к nullable role
            binding.textViewRole.text = user.role?.roleName ?: "Не назначена"
        }
    }

    private fun edit() {
        EditMode= true
        // отображаем поля ввода
        binding.editTextTextName.visibility = View.VISIBLE
        binding.editTextPhone.visibility = View.VISIBLE
        binding.editTextTextInfo.visibility = View.VISIBLE
        binding.spinnerYpk.visibility= View.VISIBLE
        binding.spinnerRole.visibility= View.VISIBLE

        loadSpiners()

        //скрываем поля вывода
        binding.textViewName.visibility = View.GONE
        binding.textViewPhoneNumber.visibility = View.GONE
        binding.textViewInfo.visibility = View.GONE
        binding.textViewYpk.visibility = View.GONE
        binding.textViewRole.visibility = View.GONE

        //выводим данные
        displayUserInf(oldUser)

        //скрываем кнопки
        binding.buttonEdit.visibility = View.GONE
        //раскрываем кнопки
        binding.buttonSave.visibility = View.VISIBLE

        binding.buttonBack.text = "отменить"
    }

    private fun save() {
        lifecycleScope.launch {
            try {
                val fullname = binding.editTextTextName.text.toString().trim()
                val phoneNumber = binding.editTextPhone.text.toString().trim()
                val userInfo = binding.editTextTextInfo.text.toString().trim()

                if (fullname.isEmpty()) {
                    Toast.makeText(this@user_detail_Activity, "⚠️ Введите имя", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                if (phoneNumber.isEmpty()) {
                    Toast.makeText(this@user_detail_Activity, "⚠️ Введите телефон", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // ✅ Проверяем, является ли выбранная роль Manager
                val isManagerRole = selectedRole == "47140546-4f69-41a4-b5ec-8a071db6d66b"

                // ✅ Если роль не Manager, ypkId = null
                val ypkIdValue = if (isManagerRole) {
                    selectedYpk ?: oldUser.ypk?.id
                } else {
                    null  // Для всех остальных ролей ypkId = null
                }

                val request = UpdateUserForAdminDto(
                    id = oldUser.id,
                    fullname = fullname,
                    phoneNumber = phoneNumber,
                    roleId = selectedRole ?: oldUser.role?.id ?: "",
                    userInfo = if (userInfo.isEmpty()) oldUser.userInfo else userInfo,
                    ypkId = ypkIdValue  // ✅ Используем вычисленное значение
                )

                val response = RetrofitClient.apiService.updateUserForAdmin(request)

                if (response.isSuccessful) {
                    Toast.makeText(this@user_detail_Activity, "✅ Изменения сохранены", Toast.LENGTH_SHORT).show()
                } else {
                    val error = response.errorBody()?.string() ?: "Неизвестная ошибка"
                    println("❌ ОШИБКА: ${response.code()} - $error")
                    Toast.makeText(this@user_detail_Activity, "❌ Ошибка: $error", Toast.LENGTH_LONG).show()
                    return@launch
                }

            } catch (e: Exception) {
                println("❌ ИСКЛЮЧЕНИЕ: ${e.message}")
                e.printStackTrace()
                Toast.makeText(this@user_detail_Activity, "❌ ${e.message}", Toast.LENGTH_SHORT).show()
                return@launch
            }

            EditMode = false
            displayUserInf(oldUser)

            // Возвращаем UI в исходное состояние
            binding.editTextTextName.visibility = View.GONE
            binding.editTextPhone.visibility = View.GONE
            binding.editTextTextInfo.visibility = View.GONE
            binding.spinnerYpk.visibility = View.GONE
            binding.spinnerRole.visibility = View.GONE

            binding.textViewName.visibility = View.VISIBLE
            binding.textViewPhoneNumber.visibility = View.VISIBLE
            binding.textViewInfo.visibility = View.VISIBLE
            binding.textViewYpk.visibility = View.VISIBLE
            binding.textViewRole.visibility = View.VISIBLE

            binding.buttonEdit.visibility = View.VISIBLE
            binding.buttonSave.visibility = View.GONE
            binding.buttonBack.text = "← Назад"
        }
    }
//    private fun save() {
//        lifecycleScope.launch {
//            try {
//                // Валидация данных
//                val fullname = binding.editTextTextName.text.toString().trim()
//                val phoneNumber = binding.editTextPhone.text.toString().trim()
//                val userInfo = binding.editTextTextInfo.text.toString().trim()
//
//                if (fullname.isEmpty()) {
//                    Toast.makeText(this@user_detail_Activity, "⚠️ Введите имя", Toast.LENGTH_SHORT).show()
//                    return@launch
//                }
//
//                if (phoneNumber.isEmpty()) {
//                    Toast.makeText(this@user_detail_Activity, "⚠️ Введите телефон", Toast.LENGTH_SHORT).show()
//                    return@launch
//                }
//
//                val request = UpdateUserForAdminDto(
//                    id = oldUser.id,
//                    fullname = fullname,
//                    phoneNumber = phoneNumber,
//                    roleId = selectedRole?: oldUser?.role?.id?: "",
//                    userInfo = if (userInfo.isEmpty()) oldUser.userInfo else userInfo,
//                    ypkId = selectedYpk?: oldUser?.ypk?.id ?: null
//                )
//
//                val response = RetrofitClient.apiService.updateUserForAdmin(request)
//
//                if (response.isSuccessful) {
//                    Toast.makeText(this@user_detail_Activity, "✅ Изменения сохранены", Toast.LENGTH_SHORT).show() }
//                else {
//                    val error = response.errorBody()?.string() ?: "Неизвестная ошибка"
//                    println("❌ ОШИБКА: ${response.code()} - $error")
//                    Toast.makeText(this@user_detail_Activity, "❌ Ошибка: $error", Toast.LENGTH_LONG).show()
//                    return@launch
//                }
//
//            } catch (e: Exception) {
//                println("❌ ИСКЛЮЧЕНИЕ: ${e.message}")
//                e.printStackTrace()
//                Toast.makeText(this@user_detail_Activity, "❌ ${e.message}", Toast.LENGTH_SHORT).show()
//                return@launch
//            }
//
//            // ✅ Только после успешного сохранения:
//            EditMode = false
//            displayUserInf(oldUser)  // ✅ Показываем обновлённые данные
//
//            // ✅ Возвращаем UI в исходное состояние
//            binding.editTextTextName.visibility = View.GONE
//            binding.editTextPhone.visibility = View.GONE
//            binding.editTextTextInfo.visibility = View.GONE
//
//            binding.textViewName.visibility = View.VISIBLE
//            binding.textViewPhoneNumber.visibility = View.VISIBLE
//            binding.textViewInfo.visibility = View.VISIBLE
//
//            binding.buttonEdit.visibility = View.VISIBLE
//            binding.buttonSave.visibility = View.GONE
//            binding.buttonBack.text = "← Назад"
//        }
//    }

    private fun back() {
        if(EditMode){EditMode= false; displayUserInf(oldUser)}
        else{finish()}
    }

    private fun loadSpiners(){
        lifecycleScope.launch {
            try {
                var responseYpk = RetrofitClient.apiService.getAllYpk()
                var responseRoles = RetrofitClient.apiService.getAllRoles()

                if(responseRoles.isSuccessful && responseRoles.body()!=null) {listOfRoles=responseRoles.body()!!.roles; setupRoleSpinner()}
                else{}

                if(responseYpk.isSuccessful && responseYpk.body()!=null) {listOfYpk=responseYpk.body()!!.ypks; setupYpkSpinner()}
                else{}

            }catch (e: Exception) {}
        }
    }

    private fun setupYpkSpinner(){
        if(listOfYpk.isEmpty()) return
        val ypkNames = listOfYpk.map { it.ypkName?:"без названия" }
        val adapter = ArrayAdapter(this,android.R.layout.simple_spinner_item, ypkNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerYpk.adapter = adapter

        //устанавливаем дефолт значение
        oldUser?.let { user ->
            val currentYpkIndex = listOfYpk.indexOfFirst { it.ypkName == user.ypk.ypkName }
            if (currentYpkIndex >= 0) {
                binding.spinnerYpk.setSelection(currentYpkIndex)
                selectedYpk = user.ypk.ypkName
            }

            //обработчик выбора
            binding.spinnerYpk.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long)
                { selectedYpk = listOfYpk[position].id }
                override fun onNothingSelected(parent: AdapterView<*>?) { selectedYpk= null }
            }
        }
    }
//    private fun setupRoleSpinner(){
//        if(listOfRoles.isEmpty()) return
//
//        val roleName =listOfRoles.map { it.roleName?:"без названия" }
//        val adapter = ArrayAdapter(this,android.R.layout.simple_spinner_item, roleName)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//
//        binding.spinnerRole.adapter = adapter
//
//        //устанавливаем дефолт значение
//        oldUser?.let { user->
//            val currentRoleIndex = listOfRoles.indexOfFirst { it.roleName == user.role.roleName }
//            if(currentRoleIndex>=0) {binding.spinnerRole.setSelection(currentRoleIndex); selectedRole = user.role.roleName}
//        }
//
//        //обработчик выбора
//        binding.spinnerRole.onItemSelectedListener= object : AdapterView.OnItemSelectedListener{
//            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long)
//            { selectedRole = listOfRoles[position].id }
//
//            override fun onNothingSelected(parent: AdapterView<*>?) { selectedRole= null }
//        }
//    }

    private fun setupRoleSpinner(){
        if(listOfRoles.isEmpty()) return

        val roleName = listOfRoles.map { it.roleName ?: "без названия" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roleName)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerRole.adapter = adapter

        // Устанавливаем дефолт значение
        val currentUserRoleId = oldUser.role?.id
        if (currentUserRoleId != null) {
            val currentRoleIndex = listOfRoles.indexOfFirst { it.id == currentUserRoleId }
            if (currentRoleIndex >= 0) {
                binding.spinnerRole.setSelection(currentRoleIndex)
                selectedRole = currentUserRoleId
                // ✅ Проверяем видимость YPK при загрузке
                setupYpkVisibility(currentUserRoleId)
            }
        }

        // Обработчик выбора
        binding.spinnerRole.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedRole = listOfRoles[position].id
                // ✅ Обновляем видимость YPK при изменении роли
                setupYpkVisibility(selectedRole!!)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedRole = null
            }
        }
    }

    private fun setupYpkVisibility(roleId: String) {
        // ID роли Manager из ваших логов: 47140546-4f69-41a4-b5ec-8a071db6d66b
        val isManager = roleId == "47140546-4f69-41a4-b5ec-8a071db6d66b"

        if (isManager) {
            // Показываем спиннер YPK для менеджеров
            binding.spinnerYpk.visibility = View.VISIBLE
            binding.textViewYpk?.visibility = View.VISIBLE // если есть label
        } else {
            // Скрываем спиннер YPK для других ролей
            binding.spinnerYpk.visibility = View.GONE
            binding.textViewYpk?.visibility = View.GONE
            selectedYpk = null // Сбрасываем выбор
        }
    }

    private fun delete() {
        AlertDialog.Builder(this)
            .setTitle("Удаление упк")
            .setMessage("Вы уверены, что хотите удалить этого пользователя? Это действие нельзя отменить.")
            .setPositiveButton("Удалить") { _, _ -> performDeleteUpk(selectedUserId!!) }
            .setNegativeButton("Отмена", null).show()
    }

    private fun performDeleteUpk(selectedUpkId: String) {
        lifecycleScope.launch {
            try {
                println("🗑️ Удаление пользователя: $selectedUpkId")
                val response = RetrofitClient.apiService.deleteUserByID(selectedUpkId)
                println("📥 Ответ сервера: ${response.code()}")
                if (response.isSuccessful) {
                    println("✅ пользователь удалён")
                    Toast.makeText(this@user_detail_Activity, "✅ пользователь удалён", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val error = response.errorBody()?.string() ?: "Неизвестная ошибка"
                    println("❌ ОШИБКА: ${response.code()} - $error")
                    Toast.makeText(this@user_detail_Activity, "❌ Ошибка: $error", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                println("❌ ИСКЛЮЧЕНИЕ: ${e.message}")
                e.printStackTrace()
                Toast.makeText(this@user_detail_Activity, "❌ ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}