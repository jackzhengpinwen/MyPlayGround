package com.zpw.myplayground.lrucache.module.fetcher

import com.zpw.myplayground.lrucache.module.Fuse
import com.zpw.myplayground.result.Result
import com.zpw.myplayground.result.map
import java.io.File

class DiskFetcher<T: Any>(private val file: File, private val convertible: Fuse.DataConvertible<T>): Fetcher<T>,
    Fuse.DataConvertible<T> by convertible {
    override val key: String
        get() = file.path

    private var cancelled: Boolean = false

    override fun fetch(): Result<T, Exception> {
        val readFileResult = Result.of<ByteArray, Exception> { file.readBytes() }
        if (cancelled) return Result.failure(RuntimeException("Fetch got cancelled"))
        return readFileResult.map { convertFromData(it) }
    }

    override fun cancle() {
        cancelled = true
    }
}