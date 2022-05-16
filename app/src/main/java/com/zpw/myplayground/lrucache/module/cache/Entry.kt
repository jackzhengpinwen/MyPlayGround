package com.zpw.myplayground.lrucache.module.cache

import kotlinx.serialization.Serializable

@Serializable
data class Entry<T: Any>(val key: String, val data: T, val timeStamp: Long)
