allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

plugins {
    kotlin("multiplatform") apply false
    kotlin("plugin.compose") apply false
    kotlin("plugin.serialization") apply false
    kotlin("android") apply false
    id("com.android.application") apply false
    id("com.android.library") apply false
    id("org.jetbrains.compose") apply false
    alias(libs.plugins.build.config) apply false
    id("com.mikepenz.aboutlibraries.plugin") apply false
}
