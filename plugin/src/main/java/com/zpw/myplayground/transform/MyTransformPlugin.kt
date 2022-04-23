package com.zpw.myplayground.transform

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.zpw.myplayground.logger
import com.zpw.myplayground.transform.internal.log.LogcatClassVisitorFactory
import org.gradle.api.Plugin
import org.gradle.api.Project

class MyTransformPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        logger.log("MyTransformPlugin apply")

        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)

        androidComponents.onVariants { variant ->
            logger.log("variant is ${variant.name}")
            variant.instrumentation.transformClassesWith(
                LogcatClassVisitorFactory::class.java,
                InstrumentationScope.ALL) {
            }
            variant.instrumentation.setAsmFramesComputationMode(FramesComputationMode.COPY_FRAMES)
        }
    }

}