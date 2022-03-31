package com.zpw.myplayground.processors

import com.zpw.myplayground.helpers.*
import com.zpw.myplayground.model.module.RawModule

class GenerateActivityClassNameConstantProcessor(
    private val manifestParsingHelper: ManifestParsingHelper,
    private val manifestVerificationHelper: ManifestVerificationHelper,
    private val activityFilteringHelper: ActivityFilteringHelper,
    private val constantFileDeterminationHelper: ConstantFileDeterminationHelper,
    private val constantGenerationHelper: ConstantGenerationHelper
) {

    fun process(rawModules: List<RawModule>) {
        System.out.println("GenerateActivityClassNameConstantProcessor is process")
        val parsedModules = manifestParsingHelper.parse(rawModules)
        parsedModules.forEach {
            System.out.println("$it")
        }
        manifestVerificationHelper.verify(parsedModules)
        val filteredModules = activityFilteringHelper.filter(parsedModules)
        filteredModules.forEach {
            System.out.println("$it")
        }
        val filesToBeGenerated = constantFileDeterminationHelper.determine(filteredModules)
        filesToBeGenerated.forEach {
            System.out.println("$it")
        }
        constantGenerationHelper.generate(filesToBeGenerated)
    }
}
