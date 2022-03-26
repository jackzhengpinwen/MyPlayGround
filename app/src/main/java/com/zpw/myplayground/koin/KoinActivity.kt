package com.zpw.myplayground.koin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.zpw.myplayground.R
import com.zpw.myplayground.data.Creature
import com.zpw.myplayground.koin.module.CAT
import com.zpw.myplayground.koin.module.creatureModule
import org.koin.android.ext.android.get
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.parametersOf

class KoinActivity : AppCompatActivity() {
    private val TAG = KoinActivity::class.java.canonicalName

    private lateinit var creature : Creature

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_koin)
        GlobalContext.startKoin {
            modules(creatureModule)
        }

        creature = get {
            parametersOf(CAT)
        }

        Log.d(TAG, "onCreate: ${creature.shout()}")

        findViewById<TextView>(R.id.jump_to_A).setOnClickListener {
            startActivity(Intent(this, KoinActivityA::class.java))
        }
        findViewById<TextView>(R.id.jump_to_B).setOnClickListener {
            startActivity(Intent(this, KoinActivityB::class.java))
        }
    }
}