package com.zpw.myplayground.datastore

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import androidx.appcompat.widget.AppCompatEditText
import com.zpw.myplayground.R
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class DataStorePreferenceActivity : AppCompatActivity() {
    val TAG = DataStorePreferenceActivity::class.java.canonicalName

    lateinit var textInput: AppCompatEditText
    lateinit var increaseBtn: Button
    lateinit var decreaseBtn: Button
    lateinit var setValueBtn: Button

    lateinit var counterManager: CounterDataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datastore_prefrence)

        textInput = findViewById(R.id.textInput)
        increaseBtn = findViewById(R.id.increaseBtn)
        decreaseBtn = findViewById(R.id.decreaseBtn)
        setValueBtn = findViewById(R.id.setBtn)

        counterManager = CounterDataStoreManager(this)

        lifecycleScope.launch {
            counterManager.counter.collect { it ->
                textInput.setText(it.toString())
            }
        }

        // Increment the counter
        increaseBtn.setOnClickListener {
            lifecycleScope.launch {
                counterManager.incrementCounter()
            }
        }

        // Decrement the counter
        decreaseBtn.setOnClickListener {
            lifecycleScope.launch {
                counterManager.decrementCounter()
            }
        }

        // Set the current value of the counter
        setValueBtn.setOnClickListener {
            lifecycleScope.launch {
                counterManager.setCounter(textInput.text.toString().toInt())
            }
        }
    }
}