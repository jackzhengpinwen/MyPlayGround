package com.zpw.myplayground.dependencygraph.model

class Manifest {
    var packageName: String = ""
    var versionCode: String = ""
    var versionName: String = ""
    var usesSdk: UsesSdk = UsesSdk()
    var usePermission: MutableList<UsePermission> = mutableListOf()
    var usesFeature: MutableList<UsesFeature> = mutableListOf()
    var application: Application = Application()
}

class UsesSdk {
    var minSdkVersion = ""
    var targetSdkVersion = ""
}

class UsesFeature {
    var name: String = ""
}

class UsePermission {
    var name: String = ""
}

class Application {
    var activityList: MutableList<Activity> = mutableListOf()
    var serviceList: MutableList<Service> = mutableListOf()
    var providerList: MutableList<Provider> = mutableListOf()
    var receiverList: MutableList<Receiver> = mutableListOf()
}

class Activity {
    var name: String = ""
    var expoeted: Boolean = false
}

class Service {
    var name: String = ""
    var expoeted: Boolean = false
}

class Provider {
    var name: String = ""
    var expoeted: Boolean = false
}

class Receiver {
    var name: String = ""
    var expoeted: Boolean = false
}