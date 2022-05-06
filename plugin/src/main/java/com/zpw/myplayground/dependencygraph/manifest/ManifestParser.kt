package com.zpw.myplayground.dependencygraph.manifest

import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File

internal class ManifestParser {

  class ParseResult(
    val packageName: String,
    val components: Map<String, Set<String>>
  )

  /**
   * The purpose of [allComponents] is to assist in the migration from the old to the new model.
   */
  fun parse(manifest: File, allComponents: Boolean = false): ParseResult {
    val document = buildDocument(manifest)

    val packageName = packageName(document)
    val application = application(document)

    val services = application?.componentNames(Manifest.Component.SERVICE, packageName) ?: emptySet()
    val providers = application?.componentNames(Manifest.Component.PROVIDER, packageName) ?: emptySet()
    val activities = application?.componentNames(Manifest.Component.ACTIVITY, packageName) ?: emptySet()
    val receivers = application?.componentNames(Manifest.Component.RECEIVER, packageName) ?: emptySet()

    val componentsMapping = mutableMapOf<String, Set<String>>()

    // "service" is enough to catch LeakCanary, and "provider" makes sense in principle. Trying not to be too aggressive.
    if (services.isNotEmpty()) componentsMapping[Manifest.Component.SERVICE.mapKey] = services
    if (providers.isNotEmpty()) componentsMapping[Manifest.Component.PROVIDER.mapKey] = providers

    if (allComponents) {
      if (activities.isNotEmpty()) componentsMapping[Manifest.Component.ACTIVITY.mapKey] = activities
      if (receivers.isNotEmpty()) componentsMapping[Manifest.Component.RECEIVER.mapKey] = receivers
    }

    return ParseResult(
      packageName = packageName,
      components = componentsMapping
    )
  }

  private fun application(document: Document): Element? {
    val elements = document.getElementsByTagName("application")
    return if (elements.length > 0) {
      elements.item(0) as Element
    } else {
      null
    }
  }

  private fun packageName(document: Document): String {
    return document.getElementsByTagName("manifest").item(0)
      .attributes
      .getNamedItem("package")
      .nodeValue
  }

  private fun Element.componentNames(
    component: Manifest.Component,
    packageName: String
  ): Set<String> {
    return getElementsByTagName(component.tagName)
      .mapToSet {
        it.attributes.getNamedItem(component.attrName).nodeValue.withPackageName(
          packageName
        )
      }
  }

  private fun String.withPackageName(packageName: String): String {
    return if (startsWith(".")) {
      // item name is relative, so prefix with the package name
      "$packageName$this"
    } else {
      // item name is absolute, so use it as-is
      this
    }
  }
}

internal inline fun <T, R> Iterable<T>.mapToSet(transform: (T) -> R): Set<R> {
    return mapTo(LinkedHashSet(collectionSizeOrDefault(10)), transform)
}

internal fun <T> Iterable<T>.collectionSizeOrDefault(default: Int): Int =
    if (this is Collection<*>) this.size
    else default

/** Metadata from an Android manifest. */
data class Manifest(
    /** The package name per `<manifest package="...">`. */
    val packageName: String,
    /** A map of component type to components. */
    val componentMap: Map<String, Set<String>>
) {

    internal enum class Component(val tagName: String, val mapKey: String) {
        ACTIVITY("activity", "activities"),
        SERVICE("service", "services"),
        RECEIVER("receiver", "receivers"),
        PROVIDER("provider", "providers");

        val attrName = "android:name"
    }
}
