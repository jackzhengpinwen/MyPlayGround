plugins {
    kotlin("jvm")
    kotlin("kapt")
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    kapt("com.google.auto.service:auto-service:1.0")
    implementation("com.google.auto.service:auto-service:1.0")
    implementation(kotlin("bom"))
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("com.github.spullara.mustache.java:compiler:0.9.10")
    implementation("org.apache.velocity.tools:velocity-tools-generic:3.1")
}