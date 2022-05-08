package com.zpw.myplayground.sharememory

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.ParcelFileDescriptor
import com.zpw.myplayground.IAshmemInterface
import com.zpw.myplayground.utils.Logger
import com.zpw.myplayground.utils.log
import java.io.FileInputStream

class AshmemService: Service() {
    private val TAG = AshmemService::class.java.canonicalName

    override fun onBind(intent: Intent?): IBinder? {
        Logger.log(TAG, "AshmemService onBind")
        return stub
    }

    private val stub: IAshmemInterface.Stub = object : IAshmemInterface.Stub() {
        override fun serverToClient(pfd: ParcelFileDescriptor?) {
            Logger.log(TAG, "AshmemService serverToClient")
            val fileDescriptor = pfd?.fileDescriptor
            val fis = FileInputStream(fileDescriptor)
            val bytes = fis.readBytes()
            Logger.log(TAG, "AshmemService serverToClient bytes is ${bytes.size}")
        }
    }
}