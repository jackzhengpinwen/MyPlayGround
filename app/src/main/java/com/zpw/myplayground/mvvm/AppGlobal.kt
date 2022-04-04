package com.zpw.myplayground.mvvm

import android.app.Application
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import java.lang.IllegalArgumentException
import java.lang.reflect.InvocationTargetException

object AppGlobal {
    private val TAG = AppGlobal::class.java.simpleName
    const val CLASS_FOR_NAME = "android.app.ActivityThread"
    const val CURRENT_APPLICATION = "currentApplication"
    const val GET_INITIAL_APPLICATION = "getInitialApplication"

    fun getApplication(): Application {
        var application: Application? = null
        try {
            val atClass = Class.forName(CLASS_FOR_NAME)
            val method = atClass.getDeclaredMethod(CURRENT_APPLICATION)
            method.isAccessible = true
            application = method.invoke(null) as Application
        } catch (exception: IllegalAccessException) {
            Log.e(TAG, "exception:$exception")
        } catch (exception: IllegalArgumentException) {
            Log.e(TAG, "exception:$exception")
        } catch (exception: InvocationTargetException) {
            Log.e(TAG, "exception:$exception")
        } catch (exception: NoSuchMethodException) {
            Log.e(TAG, "exception:$exception")
        } catch (exception: SecurityException) {
            Log.e(TAG, "exception:$exception")
        } catch (exception: ClassNotFoundException) {
            Log.e(TAG, "exception:$exception")
        }
        if (application != null) {
            return application
        }
        try {
            val atClass = Class.forName(CLASS_FOR_NAME)
            val method = atClass.getDeclaredMethod(GET_INITIAL_APPLICATION)
            method.isAccessible = true
            application = method.invoke(null) as Application
        } catch (exception: IllegalAccessException) {
            Log.e(TAG, "exception:$exception")
        } catch (exception: IllegalArgumentException) {
            Log.e(TAG, "exception:$exception")
        } catch (exception: InvocationTargetException) {
            Log.e(TAG, "exception:$exception")
        } catch (exception: NoSuchMethodException) {
            Log.e(TAG, "exception:$exception")
        } catch (exception: SecurityException) {
            Log.e(TAG, "exception:$exception")
        } catch (exception: ClassNotFoundException) {
            Log.e(TAG, "exception:$exception")
        }
        return application!!
    }

    fun getResource(): Resources {
        return getApplication().resources
    }

    fun getString(@StringRes resId: Int): String {
        return getApplication().resources.getString(resId)
    }

    fun getDimension(@DimenRes resId: Int): Int {
        return getApplication().resources.getDimensionPixelOffset(resId)
    }

    fun getDrawable(@DrawableRes resId: Int): Drawable? {
        return ResourcesCompat.getDrawable(getResource(), resId, null)
    }

    fun getColor(@ColorRes resId: Int): Int {
        return ResourcesCompat.getColor(getResource(), resId, null)
    }
}