package com.zpw.myplayground.lrucache.module.fetcher

import com.zpw.myplayground.result.Result

interface Fetcher<out T: Any> {
    val key: String
    fun fetch(): Result<T, Exception>
    fun cancle() {

    }
}

class NotFoundException(key: String) : RuntimeException("Value with key: $key is not found in cache")

internal class SimpleFetcher<out T: Any>(override val key: String, private val getValue: () -> T?):
    Fetcher<T> {
    override fun fetch(): Result<T, Exception> =
        if (getValue() == null) Result.failure(RuntimeException("Fetch with Key: $key is failure"))
        else Result.of(getValue)
}

internal class NeverFetcher<out T: Any>(override val key: String): Fetcher<T> {
    override fun fetch(): Result<T, Exception> = Result.failure(NotFoundException(key))

}