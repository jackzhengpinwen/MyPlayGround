plugins {
    id("com.android.library")
    kotlin("android")
    `maven-publish`
//    `kotlin-dsl`
//    id("com.zpw.myplugin") version "1.0.0-SNAPSHOT"
}

android {
    compileSdk = 31

    defaultConfig {
        minSdk = 21
        targetSdk = 31

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    api("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.0")
    implementation("com.zpw.library2:library2:1.0")
}

// Log timings per task.
class TimingsListener: TaskExecutionListener, BuildListener {
    override fun beforeExecute(task: Task) {
    }

    override fun afterExecute(task: Task, state: TaskState) {
        task.project.logger.log(LogLevel.WARN,"task ---- ${task.path}")
    }

    override fun settingsEvaluated(settings: Settings) {
    }

    override fun projectsLoaded(gradle: Gradle) {
    }

    override fun projectsEvaluated(gradle: Gradle) {
    }

    override fun buildFinished(result: BuildResult) {
    }
}

//gradle.addListener(TimingsListener())

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.zpw.library1"
            artifactId = "library1"
            version = "1.0"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}

//tasks.withType<GenerateMavenPom>().configureEach {
//    destination = layout.buildDirectory.file("poms/pom.xml").get().asFile
//}

//androidComponents {
//    onVariants { variant->
//        afterEvaluate {
//            project.tasks.named("bundle${variant.name.capitalize()}Aar").configure {
//                val t = this as com.android.build.gradle.tasks.BundleAar
//                t.from(layout.buildDirectory.file("poms/pom.xml").get().asFile)
//            }
//        }
//    }
//}