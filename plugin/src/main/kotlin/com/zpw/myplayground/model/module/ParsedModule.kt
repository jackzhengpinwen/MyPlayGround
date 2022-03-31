package com.zpw.myplayground.model.module

import com.zpw.myplayground.model.manifest.Application

data class ParsedModule(
    val name: String,
    val manifestList: List<ParsedManifest>
)

data class ParsedManifest(
    val path: String,
    val application: Application,
    val packageName: String = ""
)
