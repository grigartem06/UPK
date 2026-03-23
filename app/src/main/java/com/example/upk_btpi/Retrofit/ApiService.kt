package com.example.upk_btpi.Retrofit

import com.example.upk_btpi.Models.Auth.AuthResponse
import com.example.upk_btpi.Models.Feedback.FeedbackDto
import com.example.upk_btpi.Models.Feedback.FeedbackResponse
import com.example.upk_btpi.Models.Feedback.NewFeedbackDto
import com.example.upk_btpi.Models.Feedback.NewFeedbackResponse
import com.example.upk_btpi.Models.LoginDto
import com.example.upk_btpi.Models.Order.CreateOrderDto
import com.example.upk_btpi.Models.Order.OrdersResponse
import com.example.upk_btpi.Models.Order.OrderDto
import com.example.upk_btpi.Models.Order.UpdateOrderDto
import com.example.upk_btpi.Models.Product.CreateProductDto
import com.example.upk_btpi.Models.Product.ProductDto
import com.example.upk_btpi.Models.Product.ProductsResponse
import com.example.upk_btpi.Models.RegistrationDto
import com.example.upk_btpi.Models.Role.RoleDto
import com.example.upk_btpi.Models.Role.RolesResponse
import com.example.upk_btpi.Models.StatusOrder.StatusOrderResponse
import com.example.upk_btpi.Models.StatusProduct.StatusProductResponse
import com.example.upk_btpi.Models.User.UpdateUserDto
import com.example.upk_btpi.Models.User.UpdateUserForAdminDto
import com.example.upk_btpi.Models.User.UserResponse
import com.example.upk_btpi.Models.User.CreateUserDto
import com.example.upk_btpi.Models.User.UserDto
import com.example.upk_btpi.Models.Ypk.CreateYpkDto
import com.example.upk_btpi.Models.Ypk.UpdateYpkDto
import com.example.upk_btpi.Models.Ypk.YpkResponse
import com.example.upk_btpi.Models.Ypk.YpksDto
import okhttp3.Request
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    //Auth
    @POST("api/Auth/register")
    suspend fun register(@Body request: RegistrationDto): Response<AuthResponse>
    @POST("api/Auth/login")
    suspend fun login(@Body request: LoginDto): Response<AuthResponse>

    //FeedBack
    @GET("api/Feedback/All")
    suspend fun getAllFeedback() : Response<FeedbackResponse>

    @GET("api/Feedback/{id}")
    suspend fun getFeedbackById(@Path("id") feedbackId: String) : Response<FeedbackDto>

    @DELETE("api/Feedback/{id}")
    suspend fun deleteFeedbackById(@Path("id") feedbackId: String)

    @POST("api/Feedback")
    suspend fun addNewFeedback(@Body request: NewFeedbackDto) : Response <NewFeedbackResponse>

    @Multipart
    @PUT("api/Feedback")
    suspend fun updateFeedback(
        @Part("Id")id: RequestBody,
        @Part("FeedbackName") feedbackName: RequestBody,
        @Part("Raiting") rating: RequestBody
    ): Response<Unit>


    //Order
    @GET("api/Order/All")
    suspend fun getAllOrders() : Response<OrdersResponse>

    @GET("api/Order/{id}")
    suspend fun getOrderById(@Path ("id") orderId: String ) : Response<OrderDto>

    @DELETE("api/Order/{id}")
    suspend fun deleteOrderById(@Path("id") orderId: String)

    @POST("api/Order")
    suspend fun addNewOrder(@Body request: CreateOrderDto) : Response<Unit>

    @PUT("api/Order")
    suspend fun updateOrder(@Body request: UpdateOrderDto) : Response<Unit>

    //Product
    @GET("api/Product/All")
    suspend fun getAllProducts() : Response<ProductsResponse>

    @GET("api/Product/All/created")
    suspend fun getAllEdetingProducts() : Response<ProductsResponse>

    @GET("api/Product/{id}")
    suspend fun getProductById(@Path ("id") productId: String) : Response<ProductDto>

    @DELETE("api/Product/{id}")
    suspend fun deleteProductById(@Path("id") productId: String)

    @POST("api/Product/{id}")
    suspend fun addNewProduct(@Body request: CreateProductDto) : Response<Unit>


    //Role
    @GET("api/Role/All")
    suspend fun getAllRoles() : Response<RolesResponse>

    @GET("api/Role/{id}")
    suspend fun getRoleById(@Path("id") roleId: String) : Response<RoleDto>

    // StatusProduct
    @GET("api/StatusProduct/All")
    suspend fun getAllStatusProduct(): Response<StatusProductResponse>

    //StatusOrder
    @GET("api/StatusOrder/All")
    suspend fun getAllStatusOrder(): Response<StatusOrderResponse>

    //User
    @GET("api/User/All")
    suspend fun getUserAll() : Response<UserResponse>
    @GET("api/User/{id}")
    suspend fun  getUserByID(@Path("id") userId: String) : Response<UserDto>
    @DELETE("api/User/{id}")
    suspend fun deleteUserByID(@Path( "id") userId: String)
    @POST("api/User")
    suspend fun createUser(@Body request: CreateUserDto) : Response<Unit>
    @PUT("api/User")
    suspend fun  updateUser(@Body request: UpdateUserDto)
    @PUT("/api/user/admin")
    suspend fun updateUserForAdmin(@Body request: UpdateUserForAdminDto)

    //Ypk
    @GET("api/Ypk/All")
    suspend fun getAllYpk() : Response<YpkResponse>
    @GET("api/Ypk/{id}")
    suspend fun getYpkById(@Path ("id") ypkId: String) : Response<YpksDto>
    @DELETE("api/Ypk/{id}")
    suspend fun  deleteYpkById(@Path("id") ypkId: String)
    @POST("api/Ypk")
    suspend fun createNewYpk(@Body request: CreateYpkDto) : Response<Unit>
    @PUT("api/Ypk")
    suspend fun updateYpk(@Body request: UpdateYpkDto)


}