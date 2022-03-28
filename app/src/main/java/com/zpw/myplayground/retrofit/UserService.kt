package com.zpw.myplayground.retrofit

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface UserService {
    @GET("api/v1/users/{user_name}/info")
    suspend fun getUserInfo(
        @Path("user_name") userName: String,
        @Query("query") query: String
    ): Response<UserResponse>
}

data class UserResponse(
    val users: List<User>
)

data class User(
    val id: Int,
    val detail: String
)