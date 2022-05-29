package com.zpw.myplayground.focus

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject
import org.gradle.kotlin.dsl.property

private const val DEFAULT_FOCUS_FILENAME = ".focus"
private const val DEFAULT_ALL_SETTINGS_FILENAME = "settings-all.gradle"

public abstract class MyFocusExtension @Inject constructor(objects: ObjectFactory) {
    public val focusFileName: Property<String> = objects.property<String>().convention(DEFAULT_FOCUS_FILENAME)
    public val allSettingsFileName: Property<String> = objects.property<String>().convention(DEFAULT_ALL_SETTINGS_FILENAME)
}