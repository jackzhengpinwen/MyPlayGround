package com.zpw.myplayground.helpers

import com.zpw.myplayground.constants.GeneralConstants.PLUGIN_NAME
import com.zpw.myplayground.constants.Miscellaneous.FALSE as BOOLEAN_FALSE
import com.zpw.myplayground.constants.Miscellaneous.IGNORE
import com.zpw.myplayground.constants.Miscellaneous.PACKAGE_SEPARATOR
import com.zpw.myplayground.constants.Miscellaneous.TRUE as BOOLEAN_TRUE
import com.zpw.myplayground.constants.ModelConstants.METADATA_NAME_ATTRIBUTE_ADDRESSABLE_VALUE
import com.zpw.myplayground.extensions.QuadrantConfigurationExtension
import com.zpw.myplayground.model.manifest.Activity
import com.zpw.myplayground.model.manifest.MetaData
import com.zpw.myplayground.model.module.FilteredModule
import com.zpw.myplayground.model.module.ParsedManifest
import com.zpw.myplayground.model.module.ParsedModule


class ActivityFilteringHelper(
    private val configurationExtension: QuadrantConfigurationExtension
) {

    fun filter(parsedModules: List<ParsedModule>) =
        parsedModules
            .mapNotNull { it.applyFilter() }

    private fun ParsedModule.applyFilter() =
        manifestList
            .generateFullyQualifiedClassNames()
            .filterClassNames()
            .takeIf { it.isNotEmpty() }
            ?.let { classNames ->
                FilteredModule(
                    name = name,
                    filteredClassNameList = classNames
                )
            }

    private fun List<ParsedManifest>.generateFullyQualifiedClassNames() = map { manifest ->
        val activityList = manifest.application.activityList.map { activity ->
            val className = createFullyQualifiedClassName(manifest.packageName, activity.className)
            Activity(className, activity.metaDataList)
        }.toMutableList()
        val application = manifest.application.copy(activityList = activityList)
        ParsedManifest(manifest.path, application, manifest.packageName)
    }

    private fun createFullyQualifiedClassName(packageName: String, className: String): String {
        return if (className.startsWith(PACKAGE_SEPARATOR)) packageName + className else className
    }

    private fun List<ParsedManifest>.filterClassNames() =
        mutableListOf<String>().apply {
            forEachActivity { className, isAddressable ->
                if (isAddressable) {
                    add(className)
                }
            }
        }

    private fun List<ParsedManifest>.forEachActivity(block: (String, Boolean) -> Unit) =
        map { it.application }
            .flatMap { it.activityList }
            .filterNot { it.metaDataList.hasIgnoreValue() }
            .groupBy { it.className }
            .forEach {
                block(
                    it.key,
                    isActivityAddressable(
                        activityAddressability = it.value.toActivityAddressability(),
                        applicationAddressability = toApplicationAddressability()
                    )
                )
            }

    private fun isActivityAddressable(
        activityAddressability: Addressability,
        applicationAddressability: Addressability
    ) =
        when (activityAddressability) {
            Addressability.TRUE -> true
            Addressability.FALSE -> false
            Addressability.UNDEFINED -> {
                when (applicationAddressability) {
                    Addressability.TRUE -> true
                    Addressability.FALSE -> false
                    Addressability.UNDEFINED -> configurationExtension.generateByDefault
                }
            }
        }

    private fun List<Activity>.toActivityAddressability() =
        flatMap { it.metaDataList }
            .toAddressability()

    private fun List<ParsedManifest>.toApplicationAddressability() =
        map { it.application }
            .flatMap { it.metaDataList }
            .toAddressability()

    private fun List<MetaData>.toAddressability() =
        when (firstOrNull { it.name == METADATA_NAME_ATTRIBUTE_ADDRESSABLE_VALUE }?.value) {
            BOOLEAN_TRUE -> Addressability.TRUE
            BOOLEAN_FALSE -> Addressability.FALSE
            else -> Addressability.UNDEFINED
        }

    private fun List<MetaData>.hasIgnoreValue() =
        find { it.name == PLUGIN_NAME.toLowerCase() && it.value == IGNORE } != null

    private enum class Addressability { TRUE, FALSE, UNDEFINED }
}
