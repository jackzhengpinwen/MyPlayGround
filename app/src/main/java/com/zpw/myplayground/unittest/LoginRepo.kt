package com.zpw.myplayground.unittest

interface LoginRepo {
    fun validateLoginDetails(username: String, pass: String): LoginResponse?
}