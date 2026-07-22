import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("com.android.library")
    kotlin("android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("maven-publish")
}

android {
    namespace = "com.msaitodev.core.cloudsync"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = JavaVersion.VERSION_17.toString() }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                groupId = "com.msaitodev.core"
                artifactId = "core-cloud-sync"
                version = "1.1.2"
                from(components["release"])
            }
        }
    }
}

dependencies {
    // core-common 1.0.1 を参照
    implementation("com.msaitodev.core:core-common:1.0.1")
    implementation("com.msaitodev.quiz:quiz-core-domain:1.1.1")

    // Firebase
    val fbBom = platform("com.google.firebase:firebase-bom:32.7.4")
    implementation(fbBom)
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-config-ktx")

    // Hilt (KSP)
    val hiltVersion = "2.55"
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-compiler:$hiltVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
}
