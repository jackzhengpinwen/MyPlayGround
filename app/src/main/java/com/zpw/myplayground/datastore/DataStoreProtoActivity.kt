package com.zpw.myplayground.datastore

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.zpw.myplayground.R
import com.zpw.myplayground.datastore.userSettings.ThemeSettings
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class DataStoreProtoActivity : AppCompatActivity() {
    val TAG = DataStoreProtoActivity::class.java.canonicalName

    lateinit var settingsManager: UserSettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datastore_proto)

        settingsManager = UserSettingsManager(this)

        val lightThemeBtn = findViewById<MaterialRadioButton>(R.id.lightRadioBtn)
        val darkThemeBtn = findViewById<MaterialRadioButton>(R.id.darkRadioBtn)
        val autoThemeBtn = findViewById<MaterialRadioButton>(R.id.autoRadioBtn)
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        val notificationsSwitch = findViewById<SwitchMaterial>(R.id.notificationSwitch)

        lifecycleScope.launch {
            settingsManager.themeMode.collect {
                setThemeMode(it)
                when(it){
                    ThemeSettings.Light -> lightThemeBtn.isChecked = true
                    ThemeSettings.Dark -> darkThemeBtn.isChecked = true
                    else -> autoThemeBtn.isChecked = true
                }
            }
        }

        lifecycleScope.launch {
            settingsManager.notifications.collect {
                notificationsSwitch.isChecked = it
                Toast.makeText(this@DataStoreProtoActivity, "Notifications enabled: $it", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            lifecycleScope.launch {
                val mode = when(checkedId){
                    R.id.lightRadioBtn -> ThemeSettings.Light
                    R.id.darkRadioBtn -> ThemeSettings.Dark
                    else -> ThemeSettings.Auto
                }
                lifecycleScope.launch {
                    settingsManager.setThemeMode(mode)
                }
            }
        }

        // Update the datastore value when the switch button state changes
        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                settingsManager.setNotificationSettings(isChecked)
            }
        }
    }

    private fun setThemeMode(theme: ThemeSettings) {
        val mode = when(theme) {
            ThemeSettings.Light -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeSettings.Dark -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}