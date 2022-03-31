package com.zpw.myplayground.helpers

import com.zpw.myplayground.constants.GeneralConstants.PLUGIN_NAME
import com.zpw.myplayground.constants.Miscellaneous.EMPTY_SEPARATOR
import com.zpw.myplayground.constants.Miscellaneous.PACKAGE_SEPARATOR
import com.zpw.myplayground.extensions.QuadrantConfigurationExtension
import com.zpw.myplayground.model.generation.ConstantToBeGenerated
import com.zpw.myplayground.model.generation.FileToBeGenerated
import com.zpw.myplayground.model.module.FilteredModule


class ConstantFileDeterminationHelper(
    private val configurationExtension: QuadrantConfigurationExtension
) {

    fun determine(filteredModules: List<FilteredModule>) = with(filteredModules) {
        if (configurationExtension.perModule) {
            toMultipleFiles()
        } else {
            toSingleFile()
        }
    }

    private fun List<FilteredModule>.toMultipleFiles() =
        map { module ->
            FileToBeGenerated(
                name = module.name.capitalize(),
                constantList = module
                    .filteredClassNameList
                    .map { it.toConstant() }
            )
        }

    private fun List<FilteredModule>.toSingleFile() =
        flatMap { it.filteredClassNameList }
            .takeIf { it.isNotEmpty() }
            ?.map { it.toConstant() }
            ?.let { constantList ->
                listOf(
                    FileToBeGenerated(
                        name = SINGLE_FILE_NAME,
                        constantList = constantList
                    )
                )
            } ?: emptyList()

    private fun String.toConstant() = ConstantToBeGenerated(
        name = formatConstantName(),
        value = this
    )

    private fun String.formatConstantName() =
        split(PACKAGE_SEPARATOR)
            .last()
            .mapIndexed { index, letter -> formatLetter(index, letter) }
            .joinToString(EMPTY_SEPARATOR)

    private fun formatLetter(index: Int, letter: Char) =
        if (letter.isUpperCase() && index != 0) "_$letter" else letter.toUpperCase().toString()

    companion object {

        private const val SINGLE_FILE_NAME = "${PLUGIN_NAME}Constants"
    }
}
