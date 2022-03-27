package com.zpw.myplayground.room

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import com.zpw.myplayground.R
import kotlinx.coroutines.*

class RoomActivity : AppCompatActivity() {
    private val TAG = RoomActivity::class.java.canonicalName

    lateinit var db: AppDatabase
    lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        GlobalScope.launch(Dispatchers.IO) {
            loadDatabase()
            var all = userDao.getAll()
            Log.d(TAG, "all is $all")

            val zpw = User(0, "pinwen", "zheng")
            val zpy = User(1, "pingyi", "zheng")
            val yym = User(2, "yamei", "yang")
            val zyl = User(3, "yuanlong", "zheng")
            userDao.insertAll(zpw, zpy, yym, zyl)

            all = userDao.getAll()
            Log.d(TAG, "all is $all")

            userDao.delete(zpw)
            all = userDao.getAll()
            Log.d(TAG, "all is $all")

            val user = userDao.findByName("pingyi", "zheng")
            Log.d(TAG, "user is $user")

            val users = userDao.loadAllByIds(intArrayOf(2, 3))
            Log.d(TAG, "users is $users")
        }
    }

    fun loadDatabase() {
        db = Room
            .databaseBuilder(applicationContext, AppDatabase::class.java, "room_database")
            .build()
        userDao = db.userDao()
    }
}