package com.zpw.myplayground.mvvm.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.zpw.myplayground.utils.Logger
import com.zpw.myplayground.utils.TAG_FWK
import com.zpw.myplayground.utils.log

abstract class BaseFragment: Fragment() {
    private val TAG: String = TAG_FWK + javaClass.simpleName

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Logger.log(TAG, "[onActivityCreated]")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.log(TAG, "[onCreate]")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Logger.log(TAG, "[onCreateView]")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Logger.log(TAG, "[onAttach]")
    }

    override fun onStart() {
        super.onStart()
        Logger.log(TAG, "[onStart]")
    }

    override fun onResume() {
        super.onResume()
        Logger.log(TAG, "[onResume]")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Logger.log(TAG, "[onActivityResult]")
    }

    override fun onPause() {
        super.onPause()
        Logger.log(TAG, "[onPause]")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Logger.log(TAG, "[onSaveInstanceState]")
    }

    override fun onDetach() {
        super.onDetach()
        Logger.log(TAG, "[onDetach]")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Logger.log(TAG, "[onDestroyView]")
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.log(TAG, "[onDestroy]")
    }
}