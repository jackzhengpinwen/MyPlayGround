package com.zpw.myplayground.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zpw.myplayground.mvvm.utils.AppExecutors
import java.lang.RuntimeException
import java.lang.reflect.InvocationTargetException

internal class AppViewModelFactory : ViewModelProvider.Factory {
    // 创建 viewModel 实例
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(HvacRepository::class.java, AppExecutors::class.java)
            .newInstance(AppInjection.getHvacRepository(), AppExecutors.APP_EXECUTORS)
    }
}