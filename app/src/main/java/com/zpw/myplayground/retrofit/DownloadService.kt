package com.zpw.myplayground.retrofit

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface DownloadService {
    @GET
    suspend fun downloadFile(@Url fileUrl: String): Response<ResponseBody>
}