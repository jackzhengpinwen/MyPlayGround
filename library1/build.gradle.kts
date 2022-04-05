plugins {
    id("com.android.library")
    kotlin("android")
    id("com.zpw.myplugin") version "1.0.0-SNAPSHOT"
}

android {
    compileSdk = 31

    defaultConfig {
        minSdk = 21
        targetSdk = 31

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
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

//    implementation 'androidx.core:core-ktx:1.7.0'
    api("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
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