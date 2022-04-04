package com.zpw.myplayground.unittest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.zpw.myplayground.R

class LoginActivity : AppCompatActivity() {
    private val TAG = LoginActivity::class.java.canonicalName

    private val loginViewModel: LoginViewModel = ViewModelProvider
        .AndroidViewModelFactory
        .getInstance(application)
        .create(LoginViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ${loginViewModel.isLoading}")
    }
}