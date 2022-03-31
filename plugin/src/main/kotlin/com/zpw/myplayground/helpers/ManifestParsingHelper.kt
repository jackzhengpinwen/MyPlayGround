package com.zpw.myplayground.helpers

import com.zpw.myplayground.model.manifest.Manifest
import com.zpw.myplayground.model.module.ParsedManifest
import com.zpw.myplayground.model.module.ParsedModule
import com.zpw.myplayground.model.module.RawModule
import java.io.File
import javax.xml.bind.JAXBContext

class ManifestParsingHelper {

    private val jaxbUnMarshaller = JAXBContext.newInstance(Manifest::class.java).createUnmarshaller()

    fun parse(rawModules: List<RawModule>) =
        rawModules
            .map { it.parse() }

    private fun RawModule.parse() = ParsedModule(
        name = name,
        manifestList = manifestFiles.toManifestList()
    )

    private fun List<File>.toManifestList() = this
        .map { it.toManifest() }

    private fun File.toManifest() =
        jaxbUnMarshaller
            .unmarshal(this)
            .let { it as Manifest }
            .let {
                ParsedManifest(
                    path = absolutePath,
                    application = it.application,
                    packageName = it.packageName
                )
            }
}
