package com.zpw.myplayground.sharememory

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.os.MemoryFile
import android.os.ParcelFileDescriptor
import android.util.Log
import android.widget.Button
import com.zpw.myplayground.IAshmemInterface
import com.zpw.myplayground.R
import com.zpw.myplayground.utils.Logger
import com.zpw.myplayground.utils.log
import java.io.FileDescriptor

class ShareMemoryActivity : AppCompatActivity() {
    private val TAG = ShareMemoryActivity::class.java.canonicalName

    private val serverConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Logger.log(TAG, "serverConnection onServiceConnected")
            val ashmemService = IAshmemInterface.Stub.asInterface(service)

            val inputStream = assets.open("large.jpg")
            val bytes = inputStream.readBytes()
            val memoryFile = MemoryFile("ashmem", bytes.size)
            memoryFile.writeBytes(bytes, 0, 0, bytes.size)
            val fd: FileDescriptor = ReflectUtils.invoke("android.os.MemoryFile", memoryFile, "getFileDescriptor") as FileDescriptor
            val pfd = ParcelFileDescriptor.dup(fd)

            ashmemService.serverToClient(pfd)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Logger.log(TAG, "serverConnection onServiceDisconnected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ashmem)
        Log.d(TAG, "onCreate: ")
        findViewById<Button>(R.id.bind_service).setOnClickListener {
            val intent = Intent("com.zpw.myplayground.sharememory.AshmemService")
            intent.setClassName("com.zpw.myplayground", "com.zpw.myplayground.sharememory.AshmemService")
            bindService(intent, serverConnection, Context.BIND_AUTO_CREATE)
        }
    }
}