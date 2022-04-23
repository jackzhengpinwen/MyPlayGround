package com.zpw.myplayground.bindview

import android.app.Activity
import android.view.View
import androidx.fragment.app.Fragment

fun <V: View> Activity.bindView(id: Int): Lazy<V?> = lazy {
    viewFindId(id)?.saveAs<V>()
}

val viewFindId: Activity.(Int) -> View?
    get() = { findViewById(it) }

fun <V: View> Fragment.bindView(id: Int): Lazy<V?> = lazy {
    frgViewFindId(id)?.saveAs<V>()
}

val frgViewFindId: Fragment.(Int) -> View?
    get() = { view?.findViewById(it) }

fun <T> Any.saveAs(): T? {
    return this as? T
}