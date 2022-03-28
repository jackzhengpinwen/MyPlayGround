package com.zpw.myplayground

import com.google.common.truth.Truth
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.zpw.myplayground.unittest.LoginAPIService
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class LoginAPIServiceTest {
    @Mock
    lateinit var mockWebServer: MockWebServer
    @Mock
    lateinit var apiService: LoginAPIService
    lateinit var gson: Gson

    @Before
    fun setup() {
        gson = GsonBuilder().create()
        mockWebServer = MockWebServer()
        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LoginAPIService::class.java)
    }

    @After
    fun deconstruct() {
        mockWebServer.shutdown()
    }

    @Test
    fun validateUserData_return_success() {
        runBlocking {
            val mockResponse = MockResponse()
            mockWebServer.enqueue(
                mockResponse.setBody("{ }")
            )
            val response = apiService.validateUserData("", "")
            val request = mockWebServer.takeRequest()

            Truth.assertThat(request.path).isEqualTo(LoginAPIService.VALIDATE_USER_PATH)
            Truth.assertThat(response).isNotNull()
        }
    }
}