package versioncontrol

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.artifact.SingleArtifact
import org.gradle.api.Plugin
import org.gradle.api.Project

import org.gradle.kotlin.dsl.*
import java.io.File

class GitVersionPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        val gitVersionProvider = project.tasks.register<GitVersionTask>(
            "gitVersionProvider"
        ) {
            gitVersionOutputFile.set(File(
                project.buildDir,
                "intermediates/gitVersionProvider/output"
            ))
            outputs.upToDateWhen { false }
        }

        val androidComponents = project.extensions.getByType(
            AndroidComponentsExtension::class.java
        )

        androidComponents.onVariants { variant ->
            val manifestUpdater = project.tasks.register<ManifestTransformTask>(
                "${variant.name}ManifestUpdater",
            ) {
                gitInfoFile.set(
                    gitVersionProvider.flatMap(
                        GitVersionTask::gitVersionOutputFile
                    )
                )
            }
            variant.artifacts.use(manifestUpdater)
                .wiredWithFiles(
                    ManifestTransformTask::mergedManifest,
                    ManifestTransformTask::updatedManifest
                ).toTransform(SingleArtifact.MERGED_MANIFEST)

            project.tasks.register(
                 "${variant.name}Verifier",
                VerifyManifestTask::class.java
            ) {
                apkFolder.set(variant.artifacts.get(SingleArtifact.APK))
                builtArtifactsLoader.set(
                    variant.artifacts.getBuiltArtifactsLoader()
                )
            }
        }
    }

}