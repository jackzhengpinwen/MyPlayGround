package com.zpw.myplayground.koin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.zpw.myplayground.data.Creature
import com.zpw.myplayground.data.Dog
import com.zpw.myplayground.koin.module.CAT
import com.zpw.myplayground.koin.module.creatureModule
import org.koin.android.ext.android.get
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.ScopeActivity
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

class KoinActivityA : ScopeActivity() {
    private val TAG = KoinActivityA::class.java.canonicalName

    private val dog : Dog by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ourSession = getKoin().createScope("ourSessionA", named("DogSession"))
        scope.linkTo(ourSession)
        Log.d(TAG, "onCreate: $dog")
    }
}