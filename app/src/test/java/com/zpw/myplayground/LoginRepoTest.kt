package com.zpw.myplayground

import com.google.common.truth.Truth
import com.zpw.myplayground.unittest.LoginRepo
import com.zpw.myplayground.unittest.LoginResponse
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito

@RunWith(JUnit4::class)
class LoginRepoTest {
    private lateinit var repoInstance: LoginRepo

    @Before
    fun init() {
        repoInstance = Mockito.mock(LoginRepo::class.java)
    }

    @Test
    fun validateLogin_isSuccess() {
        runBlocking {
            Mockito.`when`(repoInstance.validateLoginDetails("test@test.com","123")).thenReturn(
                LoginResponse()
            )
        }
        runBlocking {
            Truth.assertThat(repoInstance.validateLoginDetails("test@test.com","123"))
                .isEqualTo(LoginResponse())
        }
    }

    @Test
    fun validateLogin_isFailed() {
        runBlocking {
            Mockito.`when`(repoInstance.validateLoginDetails("test@test.com","12")).thenReturn(null)
        }
        runBlocking {
            Truth.assertThat(repoInstance.validateLoginDetails("test@test.com","12")).isEqualTo(null)
        }
    }
}