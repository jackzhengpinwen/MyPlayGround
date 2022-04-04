package com.zpw.myplayground.mvvm.view

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import java.lang.RuntimeException

abstract class BaseBindingActivity<V: ViewDataBinding>: BaseActivity() {
    protected lateinit var binding: V

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (getLayoutId() == 0) {
            throw RuntimeException("getLayout() must be not null")
        }
        binding = DataBindingUtil.setContentView(this, getLayoutId())
        binding.setLifecycleOwner(this)
        binding.executePendingBindings()
        initView()
    }

    @LayoutRes
    protected abstract fun getLayoutId(): Int

    protected abstract fun initView()
}