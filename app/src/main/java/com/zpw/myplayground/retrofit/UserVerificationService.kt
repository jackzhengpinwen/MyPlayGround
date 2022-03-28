package com.zpw.myplayground.retrofit

import retrofit2.Response
import retrofit2.http.GET

interface UserVerificationService {
    @GET("api/v1/user/verification")
    suspend fun getUserVerification(): Response<Void>
}