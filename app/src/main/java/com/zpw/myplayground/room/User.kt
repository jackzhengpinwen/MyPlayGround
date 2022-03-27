package com.zpw.myplayground.room

import androidx.room.*

@Entity(tableName = "user")
data class User(
    @PrimaryKey
    val uid: Int,
    @ColumnInfo(name = "first_name")
    val firstName: String,
    @ColumnInfo(name = "last_name")
    val lastName: String
)
