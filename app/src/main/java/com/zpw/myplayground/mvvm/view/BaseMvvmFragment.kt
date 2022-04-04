package com.zpw.myplayground.mvvm.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory
import com.zpw.myplayground.mvvm.viewmodel.BaseViewModel
import java.lang.reflect.ParameterizedType



abstract class BaseMvvmFragment<Vm: BaseViewModel<*>, V: ViewDataBinding>: BaseBindingFragment<V>() {
    protected lateinit var viewModel: Vm

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initViewModel()
        val view = super.onCreateView(inflater, container, savedInstanceState)
        initObservable(viewModel)
        if (getViewModelVariable() != 0) {
            binding.setVariable(getViewModelVariable(), viewModel)
        }
        return view
    }

    private fun initViewModel() {
        var modelClass: Class<Vm>
        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            modelClass = type.actualTypeArguments[0] as Class<Vm>
        } else {
            modelClass = BaseViewModel::class.java as Class<Vm>
        }
        val `object`: Any = getViewModelOrFactory()
        if (`object` is ViewModel) {
            viewModel = `object` as Vm
        } else if (`object` is ViewModelProvider.Factory) {
            viewModel =
                ViewModelProvider(this, (`object` as ViewModelProvider.Factory)!!)[modelClass]
        } else {
            viewModel = ViewModelProvider(this, NewInstanceFactory())[modelClass]
        }
    }

    protected abstract fun getViewModelOrFactory(): Any

    protected abstract fun initObservable(viewModel: Vm)

    protected abstract fun getViewModelVariable(): Int

    protected abstract fun loadData(viewModel: Vm)
}