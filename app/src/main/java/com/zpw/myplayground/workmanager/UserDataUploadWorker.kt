package com.zpw.myplayground.workmanager

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters

class UserDataUploadWorker(context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters) {
    val TAG = UserDataUploadWorker::class.java.canonicalName

    override fun doWork(): Result {
        val userData = inputData.getString("user_data")
        val isSuccess = uploadUserData(userData)
        val outputData = Data.Builder()
            .putBoolean("is_success", isSuccess)
            .build()
        return Result.success(outputData)
    }

    private fun uploadUserData(userData: String?): Boolean {
        Log.d(TAG, "uploadUserData userData is $userData")
        return true
    }
}