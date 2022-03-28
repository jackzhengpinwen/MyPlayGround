package com.zpw.myplayground.workmanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.zpw.myplayground.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class WorkmanagerActivity : AppCompatActivity() {
    val TAG = WorkmanagerActivity::class.java.canonicalName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workmanager)

        val uploadDataConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputData = Data.Builder()
            .putString("user_data", "zpw")
            .build()

        val uploadWorkRequest = OneTimeWorkRequestBuilder<UserDataUploadWorker>()
            .setConstraints(uploadDataConstraints)
            .setInputData(inputData)
            .build()
        WorkManager.getInstance(this).enqueue(uploadWorkRequest)

        val periodicWorkRequest = PeriodicWorkRequestBuilder<UserDataUploadWorker>(1, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueue(periodicWorkRequest)

        lifecycleScope.launch(Dispatchers.IO) {
            delay(3000)
            val workInfo = WorkManager.getInstance(this@WorkmanagerActivity)
                .getWorkInfoById(uploadWorkRequest.id)
                .get()
            val wasSuccess = workInfo.outputData.getBoolean("is_success", false)
            Log.d(TAG, "Was work successful? delay - $wasSuccess")
        }

        WorkManager.getInstance(this)
            .getWorkInfoByIdLiveData(uploadWorkRequest.id)
            .observe(this, Observer {
                val wasSuccess = if(it != null && it.state == WorkInfo.State.SUCCEEDED) {
                    it.outputData.getBoolean("is_success", false)
                } else {
                    false
                }
                Log.d(TAG, "Was work successful? observe - $wasSuccess")
            })
    }
}