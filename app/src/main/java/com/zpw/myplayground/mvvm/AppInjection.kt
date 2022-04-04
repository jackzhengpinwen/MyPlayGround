package com.zpw.myplayground.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.fwk.sdk.hvac.HvacManager

object AppInjection {

    private val viewModelFactory: AppViewModelFactory = AppViewModelFactory()

    fun <T : ViewModel> getViewModel(store: ViewModelStoreOwner, clazz: Class<T>): T {
        return ViewModelProvider(store, viewModelFactory).get(clazz)
    }

    internal fun getViewModelFactory(): AppViewModelFactory {
        return viewModelFactory
    }

    /**
     * 受保护的权限,除了ViewModel，其它模块不应该需要Model层的实例
     *
     * @return [HvacRepository]
     */
    fun getHvacRepository(): HvacRepository {
        return HvacRepository(getHvacManager())
    }

    fun getHvacManager(): HvacManager {
        return HvacManager.getInstance()
    }
}