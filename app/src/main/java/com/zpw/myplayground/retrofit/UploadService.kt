package com.zpw.myplayground.retrofit

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadService {
    @Multipart
    @POST("api/v1/upload")
    suspend fun postUpload(
        @Part file: MultipartBody.Part
    ): Response<Void>
}