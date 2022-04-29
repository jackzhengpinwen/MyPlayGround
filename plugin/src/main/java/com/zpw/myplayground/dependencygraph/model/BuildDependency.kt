package com.zpw.myplayground.dependencygraph.model

interface BuildDependency {
    val name: String
}
data class BuildModuleDependency(
    val path: String,
) : BuildDependency {
    override val name: String get() = path
}

data class BuildArtifactDependency(
    val group: String,
    val artifact: String,
    val version: String,
) : BuildDependency {
    override val name: String get() = "$group:$artifact:$version"
}