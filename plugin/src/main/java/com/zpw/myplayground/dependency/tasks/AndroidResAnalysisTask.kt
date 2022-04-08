@file:Suppress("UnstableApiUsage")

package com.zpw.myplayground.dependency.tasks

import com.zpw.myplayground.dependency.internal.Dependency
import com.zpw.myplayground.dependency.internal.Res
import com.zpw.myplayground.dependency.internal.toJson
import com.zpw.myplayground.log
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Takes as input two types of artifacts:
 * 1. "android-symbol-with-package-name", which resolve to files with names like "package-aware-r.txt"; and
 * 2. "android-manifest", which resolve to AndroidManifest.xml files from upstream (depending) Android libraries.
 *
 * From these inputs we compute the _import statement_ for resources contributed by Android libraries. We then parse the
 * third input, viz., the set of source files of the current module/project, looking for these imports. This produces
 * the only output, which is the set of [Dependency]s that contribute _used_ Android resources.
 *
 * An important caveat to this approach is that it will not capture resources which are used from a merged resource
 * file. That is, if you import a resource from your own package namespace (`my.package.R`), then this algorithm will
 * not detect that.
 *
 * nb: this task can't use Workers (I think), because its main inputs are [ArtifactCollection]s, and they are not
 * serializable.
 */
@CacheableTask
abstract class AndroidResAnalysisTask : DefaultTask() {

    private lateinit var resources: ArtifactCollection

    fun setResources(resources: ArtifactCollection) {
        this.resources = resources
    }

    /**
     * This is the "official" input for wiring task dependencies correctly, but is otherwise
     * unused.
     */
    @PathSensitive(PathSensitivity.RELATIVE)
    @InputFiles
    fun getResourceArtifactFiles(): FileCollection {
        return resources.artifactFiles
    }

    private lateinit var manifests: ArtifactCollection

    fun setAndroidManifests(manifests: ArtifactCollection) {
        this.manifests = manifests
    }

    /**
     * This is the "official" input for wiring task dependencies correctly, but is otherwise
     * unused.
     */
    @PathSensitive(PathSensitivity.RELATIVE)
    @InputFiles
    fun getManifestArtifactFiles(): FileCollection {
        return manifests.artifactFiles
    }

    /**
     * Source code. Parsed for import statements.
     */
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    abstract val javaAndKotlinSourceFiles: ConfigurableFileCollection

    @get:OutputFile
    abstract val usedAndroidResDependencies: RegularFileProperty

    @TaskAction
    fun action() {
        logger.log("AndroidResAnalysisTask action")
        val outputFile = usedAndroidResDependencies.get().asFile
        outputFile.delete()

        val manifestCandidates = manifests.mapNotNull {
            try {
                Res(
                    componentIdentifier = it.id.componentIdentifier,
                    import = extractResImportFromAndroidManifestFile(it.file)
                )
            } catch (e: GradleException) {
                null
            }
        }
//        manifestCandidates.forEach {
//            logger.log("manifestCandidates is ${it}")
//        }

        val resourceCandidates = resources.mapNotNull { rar ->
            try {
                extractResImportFromResFile(rar.file)?.let {
                    Res(componentIdentifier = rar.id.componentIdentifier, import = it)
                }
            } catch (e: GradleException) {
                null
            }
        }
//        resourceCandidates.forEach {
//            logger.log("resourceCandidates is ${it}")
//        }

        val allCandidates = (manifestCandidates + resourceCandidates).toSet()
//        allCandidates.forEach {
//            logger.log("allCandidates is ${it}")
//        }

        val usedResources = mutableSetOf<Dependency>()
        javaAndKotlinSourceFiles.map {
            it.readLines()
        }.forEach { lines ->
            allCandidates.forEach { res ->
                lines.find { line -> line.startsWith("import ${res.import}") }?.let {
                    usedResources.add(res.dependency)
                }
            }
        }
//        usedResources.forEach {
//            logger.log("usedResources is ${it}")
//        }

        outputFile.writeText(usedResources.toJson())
    }

    private fun extractResImportFromResFile(resFile: File): String? {
        val pn = resFile.useLines { it.firstOrNull() } ?: return null
        return "$pn.R"
    }

    private fun extractResImportFromAndroidManifestFile(manifest: File): String {
        val document = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(manifest)
        document.documentElement.normalize()

        val pn = document.getElementsByTagName("manifest").item(0)
            .attributes
            .getNamedItem("package")
            .nodeValue

        return "$pn.R"
    }
}
