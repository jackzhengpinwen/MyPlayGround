package com.zpw.myplayground.mvvm

import com.zpw.myplayground.R
import com.zpw.myplayground.databinding.ActivityMvvmBinding
import com.zpw.myplayground.mvvm.view.BaseMvvmActivity


class MVVMActivity: BaseMvvmActivity<HvacViewModel, ActivityMvvmBinding>() {
    override fun getLayoutId(): Int {
        return R.layout.activity_mvvm
    }

    override fun initView() {

    }

    override fun getViewModelOrFactory(): Any {
        return AppInjection.getViewModelFactory()
    }

    override fun initObservable(viewModel: HvacViewModel) {

    }

    override fun getViewModelVariable(): Int {
        return 0
    }

    override fun loadData(viewModel: HvacViewModel) {

    }
}