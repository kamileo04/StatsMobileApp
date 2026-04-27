import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

// Wczytaj local.properties (gitignorowany, zawiera hasła testowe)
val localProps = Properties().also { props ->
    val f = rootProject.file("local.properties")
    if (f.exists()) props.load(f.inputStream())
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    js {
        browser()
        binaries.executable()
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.android)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        val jsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            // Serialization
            implementation(libs.kotlinx.serialization.json)
            // Settings
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.no.arg)
            // Charts
            implementation("io.github.koalaplot:koalaplot-core:0.6.1")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

android {
    namespace = "org.example.project"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.example.project"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}

// Przekaż dane z local.properties do JVM testów jako System.getProperty(...)
afterEvaluate {
    // --- Testy jednostkowe: wyklucz klasy *IntegrationTest ---
    tasks.named<Test>("testDebugUnitTest") {
        systemProperty("api.base.url", localProps.getProperty("api.base.url", "https://api.serkad.ovh"))
        systemProperty("api.password", localProps.getProperty("api.password", ""))
        excludes.add("**/*IntegrationTest*")
        reports.html.outputLocation.set(layout.buildDirectory.dir("reports/tests/unitTest"))
    }

    // --- Testy integracyjne: osobny task, osobny katalog raportów ---
    val integrationTest by tasks.registering(Test::class) {
        group = "verification"
        description = "Uruchamia testy integracyjne uderzające w prawdziwe API."

        // Użyj tych samych plików .class co testDebugUnitTest
        val unitTestTask = tasks.named<Test>("testDebugUnitTest").get()
        testClassesDirs = unitTestTask.testClassesDirs
        classpath = unitTestTask.classpath

        systemProperty("api.base.url", localProps.getProperty("api.base.url", "https://api.serkad.ovh"))
        systemProperty("api.password", localProps.getProperty("api.password", ""))

        // Uruchom TYLKO klasy *IntegrationTest
        includes.add("**/*IntegrationTest*")

        // Osobny katalog raportów – nie nadpisuje unitTest/index.html
        reports.html.outputLocation.set(layout.buildDirectory.dir("reports/tests/integrationTest"))
        reports.junitXml.outputLocation.set(layout.buildDirectory.dir("reports/tests/integrationTest/xml"))
    }
}

