package com.zpw.myplayground.lrucache.module

import com.zpw.myplayground.lrucache.module.fetcher.DiskFetcher
import com.zpw.myplayground.lrucache.module.fetcher.Fetcher
import com.zpw.myplayground.lrucache.module.fetcher.NeverFetcher
import com.zpw.myplayground.lrucache.module.fetcher.SimpleFetcher
import com.zpw.myplayground.result.Result
import java.io.File

object Fuse {
    interface DataConvertible<T: Any> {
        fun convertFromData(byteArray: ByteArray): T
        fun convertToData(value: T): ByteArray
    }

    interface Cacheable {
        interface Put<T: Any> {
            fun put(fetcher: Fetcher<T>): Result<T, Exception>
        }

        interface Get<T : Any> {
            fun get(fetcher: Fetcher<T>): Result<T, Exception>
            fun getWithSource(fetcher: Fetcher<T>): Pair<Result<T, Exception>, Source>
        }

        fun remove(key: String, fromSource: Source = Source.MEM): Boolean

        fun removeAll()

        fun allKeys(): Set<String>

        fun hasKey(key: String): Boolean

        fun getTimestamp(key: String): Long?
    }
}

fun <T : Any> Cache<T>.get(file: File): Result<T, Exception> = get(DiskFetcher(file, this))

fun <T : Any> Cache<T>.getWithSource(file: File): Pair<Result<T, Exception>, Source> =
    getWithSource(DiskFetcher(file, this))

fun <T : Any> Cache<T>.put(file: File): Result<T, Exception> = put(DiskFetcher(file, this))

fun <T : Any> Cache<T>.get(key: String, defaultValue: (() -> T?)): Result<T, Exception> {
    val fetcher = SimpleFetcher(key, defaultValue)
    return get(fetcher)
}

fun <T : Any> Cache<T>.get(key: String): Result<T, Exception> = get(NeverFetcher(key))

fun <T : Any> Cache<T>.getWithSource(key: String, getValue: (() -> T?)): Pair<Result<T, Exception>, Source> {
    val fetcher = SimpleFetcher(key, getValue)
    return getWithSource(fetcher)
}

fun <T : Any> Cache<T>.getWithSource(key: String): Pair<Result<T, Exception>, Source> = getWithSource(
    NeverFetcher(key)
)

fun <T : Any> Cache<T>.put(key: String, putValue: T): Result<T, Exception> {
    val fetcher = SimpleFetcher(key, { putValue })
    return put(fetcher)
}