package com.zpw.myplayground.dagger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.zpw.myplayground.dagger.component.DaggerCreatureComponent
import com.zpw.myplayground.dagger.data.Creature
import com.zpw.myplayground.dagger.module.CreatureModule
import javax.inject.Inject

class DaggerActivity : AppCompatActivity() {
    private val TAG = DaggerActivity::class.java.canonicalName

    @Inject
    lateinit var creature: Creature

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DaggerCreatureComponent.builder()
            .creatureModule(CreatureModule(CreatureModule.CAT))
            .build()
            .inject(this)

        Log.d(TAG, "onCreate: ${creature.shout()}")
    }
}