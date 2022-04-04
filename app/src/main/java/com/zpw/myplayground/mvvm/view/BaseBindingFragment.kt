package com.zpw.myplayground.mvvm.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.zpw.myplayground.utils.Logger
import com.zpw.myplayground.utils.TAG_FWK
import com.zpw.myplayground.utils.log

abstract class BaseBindingFragment<V: ViewDataBinding>: BaseFragment() {
    private val TAG: String = TAG_FWK + javaClass.simpleName

    protected lateinit var binding: V

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Logger.log(TAG, "[BaseBindingFragment]")
        if (getLayoutId() == 0) {
            throw RuntimeException("getLayout() must be not null!")
        }
        binding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)!!
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        initView()
        return binding.root
    }

    protected abstract fun getLayoutId(): Int

    protected abstract fun initView()
}