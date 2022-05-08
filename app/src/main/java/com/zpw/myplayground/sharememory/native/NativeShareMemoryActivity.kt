package com.zpw.myplayground.sharememory.native

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.zpw.myplayground.ISharedMemNativeInterface
import com.zpw.myplayground.R
import com.zpw.myplayground.utils.Logger
import com.zpw.myplayground.utils.log

class NativeShareMemoryActivity : AppCompatActivity() {
    private val TAG = NativeShareMemoryActivity::class.java.canonicalName

    private val serverConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Logger.log(TAG, "serverConnection onServiceConnected")
            val nativeAshmemService = ISharedMemNativeInterface.Stub.asInterface(service)
            nativeAshmemService.openSharedMem("sh1", 1000, false)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Logger.log(TAG, "serverConnection onServiceDisconnected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_native_ashmem)
        Log.d(TAG, "onCreate: ")
        findViewById<Button>(R.id.bind_service).setOnClickListener {
            val intent = Intent("com.zpw.myplayground.sharememory.native.NativeAshmemService")
            intent.setClassName("com.zpw.myplayground", "com.zpw.myplayground.sharememory.native.NativeAshmemService")
            bindService(intent, serverConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private external fun setVal(pos: Int, `val`: Int): Int
    private external fun getVal(pos: Int): Int
    private external fun setMap(fd: Int, size: Int)
}