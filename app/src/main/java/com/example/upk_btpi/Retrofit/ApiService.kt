package com.example.upk_btpi.Retrofit

import com.example.upk_btpi.Models.AuthResponse
import com.example.upk_btpi.Models.LoginDto
import com.example.upk_btpi.Models.RegistrationDto
import com.example.upk_btpi.Models.UpdateUserDto
import com.example.upk_btpi.Models.UpdateUserForAdminDto
import com.example.upk_btpi.Models.UserResponse
import com.example.upk_btpi.Models.postUser
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi

interface ApiService {

    //Auth
    @POST("api/Auth/register")
    suspend fun register(@Body request: RegistrationDto): Response<AuthResponse>
    @POST("api/Auth/login")
    suspend fun login(@Body request: LoginDto): Response<AuthResponse>

    //FeedBack
    //Order
    //Product
    //Role
    //StatusOrder
    //StatusProduct
    //User
    @GET("api/User/All")
    suspend fun getUserAll() : Response<UserResponse>

    @GET("api/User/{id}")
    suspend fun  getUserByID(@Path("id") userId: String) : Response<UserResponse>

    @DELETE("api/User/{id}")
    suspend fun deleteUserByID(@Path( "id") userId: String)

    @POST("api/User")
    suspend fun createUser(@Body request: postUser) : Response<AuthResponse>

    @PUT("api/User")
    suspend fun updateUser(@Body request: UpdateUserDto)

    @PUT("/api/user/admin")
    suspend fun updateUserForAdmin(@Body request: UpdateUserForAdminDto)



    //Ypk


}