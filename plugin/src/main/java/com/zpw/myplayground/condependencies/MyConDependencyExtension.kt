package com.zpw.myplayground.condependencies

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import javax.inject.Inject

open class MyConDependencyExtension @Inject constructor(objects: ObjectFactory) {
    val conDependencies: MapProperty<String, String> = objects.mapProperty(
        String::class.java,
        String::class.java
    )
}