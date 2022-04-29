package com.zpw.myplayground.dependencygraph.model

class PomProject {
    var modelVersion: String = ""
    var groupId: String = ""
    var artifactId: String = ""
    var version: String = ""
    var packaging: String = ""
    var dependencies: PomDependencies = PomDependencies()
}

class PomDependencies {
    var dependency: MutableList<PomDependency> = mutableListOf()
}

class PomDependency {
    var groupId: String = ""
    var artifactId: String = ""
    var version: String = ""
    var scope: String = ""
}