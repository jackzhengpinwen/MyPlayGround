package com.zpw.myplayground.lrucache.module.scenario

import com.zpw.myplayground.lrucache.module.Cache
import com.zpw.myplayground.lrucache.module.Source
import com.zpw.myplayground.lrucache.module.fetcher.Fetcher
import com.zpw.myplayground.lrucache.module.fetcher.NeverFetcher
import com.zpw.myplayground.lrucache.module.fetcher.SimpleFetcher
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import com.zpw.myplayground.result.Result
import java.util.*

class ExpirableCache<T : Any>(private val cache: Cache<T>) : Cache<T> by cache {

    fun get(
        fetcher: Fetcher<T>,
        timeLimit: Duration = Duration.INFINITE,
        useEntryEvenIfExpired: Boolean = false
    ): Result<T, Exception> = getWithSource(fetcher, timeLimit, useEntryEvenIfExpired).first

    fun getWithSource(
        fetcher: Fetcher<T>,
        timeLimit: Duration = Duration.INFINITE,
        useEntryEvenIfExpired: Boolean = false
    ): Pair<Result<T, Exception>, Source> {
        val key = fetcher.key
        val persistedTimestamp = getTimestamp(key)
        // no timestamp fetch, we need to just fetch the new data
        return if (persistedTimestamp == null) {
            put(fetcher) to Source.ORIGIN
        } else {
            val isExpired = hasExpired(persistedTimestamp, timeLimit)

            // if it is not expired yet or user wants to use it even it is already expired
            if (!isExpired || useEntryEvenIfExpired) {
                cache.getWithSource(fetcher)
            } else {
                // fetch the value from the fetcher and put back if success, if failure we will fallback to the cache
                putOrGetFromCacheIfFailure(fetcher)
            }
        }
    }

    private fun putOrGetFromCacheIfFailure(fetcher: Fetcher<T>): Pair<Result<T, Exception>, Source> {
        return when (val result = put(fetcher)) {
            is Result.Success -> result to Source.ORIGIN
            is Result.Failure -> {
                // fallback to cache
                cache.getWithSource(fetcher)
            }
        }
    }

    private fun hasExpired(persistedTimestamp: Long, timeLimit: Duration): Boolean {
        val now = System.currentTimeMillis()
        val durationSincePersisted = (now - persistedTimestamp).milliseconds
        return durationSincePersisted > timeLimit
    }
}

// region Value
fun <T : Any> ExpirableCache<T>.get(
    key: String,
    getValue: (() -> T),
    timeLimit: Duration = Duration.INFINITE,
    useEntryEvenIfExpired: Boolean = false
): Result<T, Exception> {
    val fetcher = SimpleFetcher(key, getValue)
    return get(fetcher, timeLimit, useEntryEvenIfExpired)
}

fun <T : Any> ExpirableCache<T>.get(
    key: String,
    timeLimit: Duration = Duration.INFINITE,
    useEntryEvenIfExpired: Boolean = false
): Result<T, Exception> = get(NeverFetcher(key), timeLimit, useEntryEvenIfExpired)

fun <T : Any> ExpirableCache<T>.getWithSource(
    key: String,
    getValue: (() -> T),
    timeLimit: Duration = Duration.INFINITE,
    useEntryEvenIfExpired: Boolean = false
): Pair<Result<T, Exception>, Source> {
    val fetcher = SimpleFetcher(key, getValue)
    return getWithSource(fetcher, timeLimit, useEntryEvenIfExpired)
}

fun <T : Any> ExpirableCache<T>.getWithSource(
    key: String,
    timeLimit: Duration = Duration.INFINITE,
    useEntryEvenIfExpired: Boolean = false
): Pair<Result<T, Exception>, Source> = getWithSource(NeverFetcher(key), timeLimit, useEntryEvenIfExpired)

// endregion
