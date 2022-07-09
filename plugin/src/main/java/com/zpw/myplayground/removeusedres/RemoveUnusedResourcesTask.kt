package com.zpw.myplayground.removeusedres

import com.zpw.myplayground.removeusedres.extensions.containsInDescendants
import com.zpw.myplayground.removeusedres.extensions.getAttributeText
import com.zpw.myplayground.removeusedres.extensions.getAttributeValue
import com.zpw.myplayground.removeusedres.extensions.getElements
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.StringWriter
import javax.xml.namespace.QName
import javax.xml.parsers.DocumentBuilderFactory

abstract class RemoveUnusedResourcesTask: DefaultTask() {

    @TaskAction
    fun taskAction() {
        val lintResultFile = project.file("/Users/zpw/GithubProjects/MyPlayGround/app/build/reports/lint-results-debug.xml")
        if (lintResultFile.exists()) {
            /**
                <issues format="6" by="lint 7.1.0">
                    <issue
                        id="UnusedResources"
                        severity="Warning"
                        message="The resource `R.color.unused_color` appears to be unused"
                        category="Performance"
                        priority="3"
                        summary="Unused resources"
                        explanation="Unused resources make applications larger and slow down builds.&#xA;&#xA;The unused resource check can ignore tests. If you want to include resources that are only referenced from tests, consider packaging them in a test source set instead.&#xA;&#xA;You can include test sources in the unused resource check by setting the system property lint.unused-resources.include-tests=true, and to exclude them (usually for performance reasons), use lint.unused-resources.exclude-tests=true."
                        errorLine1="    &lt;color name=&quot;unused_color&quot;>#FFFFFF&lt;/color>"
                        errorLine2="           ~~~~~~~~~~~~~~~~~~~">
                        <location
                            file="/Users/zpw/GithubProjects/android-remove-unused-resources-plugin/sample/src/main/res/values/colors.xml"
                            line="9"
                            column="12"/>
                    </issue>
                </issues>
             */
            val lintResultDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(lintResultFile).documentElement
            if (lintResultDocument.tagName != "issues") {
                logger.error("root tag is not \"issues\": ${lintResultDocument.tagName}")
            }
            var lintResultUnusedResourcesIssueCount = 0
            lintResultDocument.getElements("issue")
                .filter { it.getAttributeText("id") == "UnusedResources" } // id="UnusedResources"
                .forEach { issue ->
                    lintResultUnusedResourcesIssueCount++
                    val message = issue.getAttributeText("message")
                        ?: error("message attribute is missing: $issue")
                    logger.warn("message is $message")
                    // message is The resource `R.string.navigation_drawer_close` appears to be unused
                    val matchedResource = Regex(
                        "^The resource `(R\\.([^.]+)\\.([^.]+))` appears to be unused$"
                    ).matchEntire(message) ?: error("unknown message: $message")
                    val (_, resourceName, resourceType, resourceId) = matchedResource.groupValues
                    // resourceName is R.string.navigation_drawer_close, resourceType is string, resourceId is navigation_drawer_close
                    logger.warn("resourceName is $resourceName, resourceType is $resourceType, resourceId is $resourceId")
                    val location = issue.getElements("location").first()
                    val originalTargetFile = File(location.attributes.getNamedItem("file").nodeValue)
                    if (!originalTargetFile.isAbsolute) {
                        error("target file is relative path: $originalTargetFile")
                    }
                    // originalTargetFile = /Users/zpw/GithubProjects/MyPlayGround/app/src/main/res/values/strings.xml
                    logger.warn("originalTargetFile = ${originalTargetFile.absolutePath}")
                    if (listOf(project.rootProject).union(project.subprojects).all {
                        // check rootProject first, then check subprojects
                        // because rootProject is most likely top of all project's directory.
                        // any subproject placed in outside of rootProject is rare situation.
                        !it.projectDir.containsInDescendants(originalTargetFile)
                        }) {
                        logger.warn("skip: target file is outside of all project's directory: $originalTargetFile")
                        return@forEach
                    }
                    val resourceDirectory = originalTargetFile.parentFile.parentFile
                    // resourceDirectory = /Users/zpw/GithubProjects/MyPlayGround/app/src/main/res
                    logger.warn("resourceDirectory = ${resourceDirectory.absolutePath}")
                    val isValuesResource = Regex(
                        "values(-.+)?"
                    ).matches(originalTargetFile.parentFile.name)
                    val directoryName = if (isValuesResource) "values" else resourceType
                    // directoryName = values
                    logger.warn("directoryName = $directoryName")
                    val targetDirectories = resourceDirectory.listFiles()?.filter {
                        Regex(
                            "$directoryName(-.+)?"
                        ).matches(it.name)
                    } ?: emptyList()
                    targetDirectories.forEach {
                        logger.warn("directory is ${it.absolutePath}")
                    }
                    var targetFiles = targetDirectories.flatMap { directory ->
                        directory.listFiles()?.filter {
                            if (isValuesResource) {
                                it.name.endsWith(".xml")
                            } else {
                                (Regex("\\.9$").replace(it.nameWithoutExtension, "") == resourceId)
                            }
                        } ?: emptyList()
                    }
                    targetFiles.forEach {
                        logger.warn("targetFile is ${it.absolutePath}")
                    }
                    targetFiles.forEach { targetFile ->
                        if ((originalTargetFile == targetFile) && !targetFile.exists()) {
                            logger.warn("target file is not exist: $targetFile")
                            return@forEach
                        }
                        if (isValuesResource) {
                            // remove resource element
                            val tagNames = when (resourceType) {
                                "array" -> listOf("array", "integer-array", "string-array")
                                else -> listOf(resourceType)
                            }
                            logger.warn("tagNames is $tagNames")
                            var skipOverride = false
                            var remainResources = false
                            val converter = XmlConverter { startElementEvent ->
                                if (startElementEvent.level == 1) {
                                    // only check root <resources>'s child elements
                                    val target = startElementEvent.event.asStartElement()
                                    val tagName = target.name.toString()
                                    val attribute = target.getAttributeValue("name")
                                    logger.warn("tagName is $tagName, attribute is $attribute")
                                    val delete = if (
                                        tagName in tagNames &&
                                        attribute?.replace(".", "_") == resourceId
                                    ) {
                                        val overrideName = QName(
                                            "http://schemas.android.com/tools",
                                            "override"
                                        )
                                        val override =
                                            (target.getAttributeValue(overrideName) == "true")
                                        if (override) {
                                            skipOverride = true
                                        }
                                        !override
                                    } else false
                                    if (!delete) {
                                        remainResources = true
                                    }
                                    delete
                                } else false
                            }
                            val output = StringWriter()
                            val result = converter.convert(targetFile.inputStream(), output)
                            when {
                                skipOverride -> {
                                    logger.lifecycle("skip because it has tools:override: $resourceName in $targetFile")
                                }
                                result.removed.isNotEmpty() -> {
                                    logger.lifecycle("delete resource element: $resourceName in $targetFile")
                                }
                                (originalTargetFile == targetFile) -> {
                                    logger.warn("resource not found: $resourceName in $targetFile")
                                }
                            }

                            if (remainResources) {
                                targetFile.writeText(output.toString())
                            } else {
                                // delete empty resource file
                                logger.lifecycle("delete resource file because of empty: $targetFile")
                                targetFile.delete()
                            }
                        } else {
                            // delete resource file
                            // target: R.animator, R.anim, R.color, R.drawable, R.mipmap,
                            // R.layout, R.menu, R.raw, R.xml, R.font
                            logger.lifecycle("delete resource file: $targetFile")
                            targetFile.delete()
                        }
                    }
                }
        }
    }
}