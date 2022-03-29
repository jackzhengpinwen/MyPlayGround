mapOf(
    "AppCompileSdkVersion" to 31
).forEach { (name, version) ->
    project.extra.set(name, version)
}