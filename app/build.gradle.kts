import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import com.google.protobuf.gradle.generateProtoTasks

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.google.protobuf") version "0.8.18"
//    id("jacoco")
    id("com.zpw.myplugin") version "1.0.0-SNAPSHOT"
}

//apply("../jacoco.gradle")
apply("../version.gradle.kts")

android {
    compileSdk = extra.get("AppCompileSdkVersion") as Int

    defaultConfig {
        applicationId = "com.zpw.myplayground"
        minSdk = ConfigData.minSdkVersion
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"

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
    testOptions{
        unitTests.apply {
            isReturnDefaultValues = true
        }
    }
    buildFeatures {
        dataBinding = true
    }
    packagingOptions {
        resources.pickFirsts.add("META-INF/AL2.0")
        resources.pickFirsts.add("META-INF/LGPL2.1")
    }
}

dependencies {

    implementation(libs.corektx)
    implementation(Deps.appCompat)
    implementation("androidx.activity:activity-ktx:1.4.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")

    // test
    testImplementation("junit:junit:4.+")
    testImplementation("org.robolectric:robolectric:4.2")
    testImplementation("androidx.test:core:1.1.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("org.mockito:mockito-core:2.28.2")
    androidTestImplementation("org.mockito:mockito-android:2.24.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.0")
    implementation("androidx.arch.core:core-testing:2.0.1")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.9.0")

    // dagger
    implementation("com.google.dagger:dagger:2.41")
    kapt("com.google.dagger:dagger-compiler:2.41")

    // koin
    implementation("io.insert-koin:koin-android:3.1.5")

    // coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")

    // room
    implementation("androidx.room:room-runtime:2.4.2")
    kapt("androidx.room:room-compiler:2.4.2")

    // datastore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.datastore:datastore:1.0.0")
    implementation("com.google.protobuf:protobuf-javalite:3.10.0")

    // retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.6.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")
    implementation("io.github.rburgst:okhttp-digest:2.6")
    implementation("com.jakewharton.timber:timber:5.0.1")

    // workmanager
    implementation("androidx.work:work-runtime-ktx:2.7.1")

    // glide
    implementation("com.github.bumptech.glide:glide:4.12.0")

    // jacoco
    implementation("org.jacoco:org.jacoco.report:0.8.4")

    // circleimageview
    implementation("de.hdodenhof:circleimageview:3.1.0")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.10.0"
    }

    generateProtoTasks {
        all().forEach { task ->
            task.plugins{
                create("java") {
                    option("lite")
                }
            }
        }
    }
}

afterEvaluate {
    logger.log(org.gradle.api.logging.LogLevel.DEBUG, "zpw$$ app afterEvaluate")
}