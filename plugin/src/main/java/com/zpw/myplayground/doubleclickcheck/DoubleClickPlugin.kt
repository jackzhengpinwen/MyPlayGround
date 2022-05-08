package com.zpw.myplayground.doubleclickcheck

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppExtension
import com.zpw.myplayground.logger
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

class DoubleClickPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        logger.log("DoubleClickPlugin apply")
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            logger.log("variant is ${variant.name}")
            variant.instrumentation.transformClassesWith(
                DoubleCheckClassVisitorFactory::class.java,
                InstrumentationScope.ALL
            ){}
            variant.instrumentation.setAsmFramesComputationMode(FramesComputationMode.COPY_FRAMES)
        }
    }
}