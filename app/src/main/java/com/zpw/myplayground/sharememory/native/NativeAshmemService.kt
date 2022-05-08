package com.zpw.myplayground.sharememory.native

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.ParcelFileDescriptor
import com.zpw.myplayground.ISharedMemNativeInterface
import com.zpw.myplayground.utils.Logger
import com.zpw.myplayground.utils.log

class NativeAshmemService: Service() {
    private val TAG = NativeAshmemService::class.java.canonicalName

    init {
        System.load("entrypoint")
    }

    private val memAreas = HashMap<String, Int>()

    override fun onBind(intent: Intent?): IBinder? {
        Logger.log(TAG, "NativeAshmemService onBind")
        return stub
    }

    private val stub: ISharedMemNativeInterface.Stub = object : ISharedMemNativeInterface.Stub() {
        override fun openSharedMem(name: String?, size: Int, create: Boolean): ParcelFileDescriptor {
            Logger.log(TAG, "NativeAshmemService openSharedMem")
            val fd: Int = OpenSharedMem(name!!, size, create)
            return ParcelFileDescriptor.fromFd(fd)
        }

    }

    fun OpenSharedMem(name: String, size: Int, create: Boolean): Int {
        var i: Int? = memAreas[name]
        if (create && i != null) {
            return -1
        }
        if(i == null) {
            i = getFD(name, size)
            memAreas[name] = i
        }
        return i
    }

    private external fun getFD(name: String, size: Int): Int
    private external fun setVal(fd: Int, pos: Int, `val`: Int): Int
    private external fun getVal(fd: Int, pos: Int): Int
}