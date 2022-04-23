package com.zpw.myplayground.retrofit

import com.burgstaller.okhttp.AuthenticationCacheInterceptor
import com.burgstaller.okhttp.CachingAuthenticatorDecorator
import com.burgstaller.okhttp.digest.CachingAuthenticator
import com.burgstaller.okhttp.digest.Credentials
import com.burgstaller.okhttp.digest.DigestAuthenticator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

object NetHelper {

    fun getUserService(): UserService {
        val digestAuthenticator = DigestAuthenticator(Credentials("UserName", "Password"))
        val authCache: Map<String, CachingAuthenticator> = ConcurrentHashMap()

        val logging = HttpLoggingInterceptor {
            Timber.tag("OkHttp").d(it)
        }
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC)

        val client = OkHttpClient.Builder()
            .authenticator(CachingAuthenticatorDecorator(digestAuthenticator, authCache))
            .addInterceptor(AuthenticationCacheInterceptor(authCache))
            .addInterceptor(logging)
            .build()

        val userService = Retrofit.Builder()
            .baseUrl("https://qiita.com")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(UserService::class.java)

        return userService
    }

    fun getDownloadService(): DownloadService {
        val logging = HttpLoggingInterceptor {
            Timber.tag("OkHttp").d(it)
        }
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val downloadService = Retrofit.Builder()
            .baseUrl("https://qiita.com")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(DownloadService::class.java)

        return downloadService
    }

    fun getUploadService(): UploadService {
        val logging = HttpLoggingInterceptor {
            Timber.tag("OkHttp").d(it)
        }
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC)

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val uploadService = Retrofit.Builder()
            .baseUrl("https://qiita.com")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(UploadService::class.java)

        return uploadService
    }
}