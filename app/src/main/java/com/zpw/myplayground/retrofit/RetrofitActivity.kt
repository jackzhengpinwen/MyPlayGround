package com.zpw.myplayground.retrofit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.zpw.myplayground.R
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import timber.log.Timber
import java.io.*

class RetrofitActivity : AppCompatActivity() {
    val TAG = RetrofitActivity::class.java.canonicalName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val response = NetHelper.getDownloadService().downloadFile("https://html5demos.com/assets/dizzy.mp4")
            if(response.isSuccessful){
                val inputStream = response.body()?.byteStream() ?: return@launch
                val file = File(this@RetrofitActivity.applicationContext.getExternalFilesDir(null), "dizzy.mp4")
                DataInputStream(inputStream).use { dataInputStream: DataInputStream ->
                    FileOutputStream(file).use { fileOutputStream: FileOutputStream ->
                        DataOutputStream(BufferedOutputStream(fileOutputStream)).use { dataOutputStream ->
                            val byte = ByteArray(4096)
                            var readByte: Int
                            while (-1 != dataInputStream.read(byte).also { readByte = it }) {
                                dataOutputStream.write(byte, 0, readByte)
                            }
                            dataOutputStream.flush()
                        }
                        fileOutputStream.flush()
                    }
                    dataInputStream.close()
                }
                inputStream.close()
            }
        }

        lifecycleScope.launch {
            val file = File(this@RetrofitActivity.getExternalFilesDir(null), "result.log")
            val requestBody = file.asRequestBody(MultipartBody.FORM)
            val multiPartBody = MultipartBody.Builder("--*****")
                .addFormDataPart("file", file.name, requestBody)
                .build()
            NetHelper.getUploadService().postUpload(multiPartBody.part(0))
        }
    }
}