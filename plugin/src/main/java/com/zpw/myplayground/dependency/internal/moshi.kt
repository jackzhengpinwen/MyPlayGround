package com.zpw.myplayground.dependency.internal

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.Types.newParameterizedType
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

val MOSHI: Moshi by lazy {
    Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(TypeAdapters())
        .build()
}

inline fun <reified T> getJsonAdapter(withNulls: Boolean = false): JsonAdapter<T> {
    val adapter = MOSHI.adapter(T::class.java)
    return if (withNulls) {
        adapter.serializeNulls()
    } else {
        adapter
    }
}

inline fun <reified T> getJsonListAdapter(withNulls: Boolean = false): JsonAdapter<List<T>> {
    val type = newParameterizedType(List::class.java, T::class.java)
    val adapter = MOSHI.adapter<List<T>>(type)
    return if (withNulls) {
        adapter.serializeNulls()
    } else {
        adapter
    }
}

inline fun <reified T> getJsonSetAdapter(withNulls: Boolean = false): JsonAdapter<Set<T>> {
    val type = newParameterizedType(Set::class.java, T::class.java)
    val adapter = MOSHI.adapter<Set<T>>(type)
    return if (withNulls) {
        adapter.serializeNulls()
    } else {
        adapter
    }
}

inline fun <reified K, reified V> getJsonMapAdapter(withNulls: Boolean = false): JsonAdapter<Map<K, V>> {
    val type = newParameterizedType(Map::class.java, K::class.java, V::class.java)
    val adapter = MOSHI.adapter<Map<K, V>>(type)
    return if (withNulls) {
        adapter.serializeNulls()
    } else {
        adapter
    }
}

inline fun <reified T> String.fromJson(): T {
    return getJsonAdapter<T>().fromJson(this)!!
}

inline fun <reified T> T.toJson(withNulls: Boolean = false): String {
    return getJsonAdapter<T>(withNulls).toJson(this)
}

inline fun <reified T> String.fromJsonList(withNulls: Boolean = false): List<T> {
    return getJsonListAdapter<T>(withNulls).fromJson(this)!!
}

inline fun <reified K, reified V> String.fromJsonMapList(): Map<K, List<V>> {
    val listType = newParameterizedType(List::class.java, V::class.java)
    val mapType = newParameterizedType(Map::class.java, K::class.java, listType)
    val adapter = MOSHI.adapter<Map<K, List<V>>>(mapType)

    return adapter.fromJson(this)!!
}

inline fun <reified T> List<T>.toPrettyString(withNulls: Boolean = false): String {
    return getJsonListAdapter<T>(withNulls).indent("  ").toJson(this)
}

inline fun <reified T> Set<T>.toPrettyString(withNulls: Boolean = false): String {
    return getJsonSetAdapter<T>(withNulls).indent("  ").toJson(this)
}

inline fun <reified K, reified V> Map<K, V>.toPrettyString(withNulls: Boolean = false): String {
    return getJsonMapAdapter<K, V>(withNulls).indent("  ").toJson(this)
}

@Suppress("unused", "HasPlatformType")
internal class TypeAdapters {

    @ToJson fun fileToJson(file: File) = file.absolutePath
    @FromJson fun fileFromJson(absolutePath: String) = File(absolutePath)
}
