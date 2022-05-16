package com.zpw.myplayground.lrucache.module

import com.zpw.myplayground.lrucache.module.cache.DiskCache
import com.zpw.myplayground.lrucache.module.cache.MemCache
import com.zpw.myplayground.lrucache.module.cache.Persistence

class Config<T: Any>(
    val cacheDir: String,
    val name: String = DEFAULT_NAME,
    val convertible: Fuse.DataConvertible<T>,
    var diskCapacity: Long = 1024 * 1024 * 20,
    var memCache: Persistence<Any> = defaultMemoryCache(),
    var diskCache: Persistence<ByteArray> = defaultDiskCache(cacheDir, name, diskCapacity)
) {
    companion object {
        const val DEFAULT_NAME = "com.zpw.playground"
    }

    var transformer: ((key: String, value: T) -> T) = { _, value -> value }
}

internal fun defaultMemoryCache(minimalSize: Int = 128): Persistence<Any> = MemCache(minimalSize)

internal fun defaultDiskCache(cacheDir: String, name: String, diskCapacity: Long): Persistence<ByteArray> =
    DiskCache.open(cacheDir, name, diskCapacity)