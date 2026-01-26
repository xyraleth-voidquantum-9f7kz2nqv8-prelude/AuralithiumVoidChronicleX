import org.jetbrains.dokka.gradle.engine.parameters.KotlinPlatform
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.util.Base64
import java.io.File

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin.android)
}

val javaTarget = JvmTarget.fromTarget(libs.versions.jvmTarget.get())

fun getGitCommitHash(): String {
    return try {
        val headFile = file("${project.rootDir}/.git/HEAD")
        if (headFile.exists()) {
            val headContent = headFile.readText().trim()
            if (headContent.startsWith("ref:")) {
                val refPath = headContent.substring(5).trim()
                val commitFile = file("${project.rootDir}/.git/$refPath")
                if (commitFile.exists()) commitFile.readText().trim() else ""
            } else headContent
        } else ""
    } catch (_: Throwable) {
        ""
    }.take(7)
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.cloudplay.app"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 100
        versionName = "1.6.0"

        manifestPlaceholders["target_sdk_version"] = libs.versions.targetSdk.get()
        resValue("string", "app_name", "PlayCloud")
        resValue("color", "blackBoarder", "#FF000000")

        buildConfigField("long", "BUILD_DATE", "${System.currentTimeMillis()}")
        buildConfigField("String", "APP_VERSION", "\"$versionName\"")
        buildConfigField("String", "SIMKL_CLIENT_ID", "\"${System.getenv("SIMKL_CLIENT_ID") ?: ""}\"")
        buildConfigField("String", "SIMKL_CLIENT_SECRET", "\"${System.getenv("SIMKL_CLIENT_SECRET") ?: ""}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // ABI splits
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a")
            isUniversalApk = true // penting: universal APK untuk TV
        }
    }

    signingConfigs {
        create("release") {
            val signingKeyBase64 = System.getenv("SIGNING_KEY")
            val keystoreFile = file("${buildDir}/release.keystore")
            if (!keystoreFile.exists() && !signingKeyBase64.isNullOrBlank()) {
                keystoreFile.parentFile.mkdirs()
                keystoreFile.writeBytes(Base64.getDecoder().decode(signingKeyBase64))
            }
            storeFile = keystoreFile
            keyAlias = System.getenv("ALIAS") ?: "playcloud25"
            storePassword = System.getenv("KEY_STORE_PASSWORD") ?: "161105"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "161105"
        }
    }

    buildTypes {
        release {
            isDebuggable = false
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ""
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    flavorDimensions += "state"
    productFlavors {
        create("stable") {
            dimension = "state"
        }
        create("prerelease") {
            dimension = "state"
            signingConfig = signingConfigs.getByName("release")
            versionNameSuffix = "-PRE"
            versionCode = (System.currentTimeMillis() / 60000).toInt()
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.toVersion(javaTarget.target)
        targetCompatibility = JavaVersion.toVersion(javaTarget.target)
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(libs.versions.jdkToolchain.get()))
        }
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
        disable.add("MissingTranslation")
    }

    buildFeatures {
        viewBinding = true
        dataBinding = false
        buildConfig = true
        resValues = true
    }

    namespace = "com.lagradost.cloudstream3"
}

// Dependencies (tetap sama seperti sebelumnya)
dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.json)
    androidTestImplementation(libs.core)
    implementation(libs.junit.ktx)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.core.ktx)
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.fragment.ktx)
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.navigation)
    implementation(libs.preference.ktx)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.bundles.coil)
    implementation(libs.bundles.media3)
    implementation(libs.video)
    implementation(libs.bundles.nextlib)
    implementation(libs.colorpicker)
    implementation(libs.newpipeextractor)
    implementation(libs.juniversalchardet)
    implementation(libs.shimmer)
    implementation(libs.palette.ktx)
    implementation(libs.tvprovider)
    implementation(libs.overlappingpanels)
    implementation(libs.biometric)
    implementation(libs.previewseekbar.media3)
    implementation(libs.qrcode.kotlin)
    implementation(libs.jsoup)
    implementation(libs.rhino)
    implementation(libs.quickjs)
    implementation(libs.fuzzywuzzy)
    implementation(libs.safefile)
    coreLibraryDesugaring(libs.desugar.jdk.libs.nio)
    implementation(libs.conscrypt.android)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.torrentserver)
    implementation(libs.work.runtime.ktx)
    implementation(libs.nicehttp)
    implementation("io.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:6.0.7")
    implementation(project(":library") {
        val isDebug = gradle.startParameter.taskRequests.any { task ->
            task.args.any { arg -> arg.contains("debug", ignoreCase = true) }
        }
        this.extra.set("isDebug", isDebug)
    })
}

tasks.withType<KotlinJvmCompile> {
    compilerOptions {
        jvmTarget.set(javaTarget)
        jvmDefault.set(JvmDefaultMode.ENABLE)
        optIn.add("com.lagradost.cloudstream3.Prerelease")
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
}

dokka {
    moduleName = "App"
    dokkaSourceSets {
        main {
            analysisPlatform = KotlinPlatform.JVM
            documentedVisibilities(
                VisibilityModifier.Public,
                VisibilityModifier.Protected
            )
        }
    }
}

// **Task build universal APK untuk TV**
tasks.register<Zip>("universalApk") {
    group = "build"
    description = "Build universal APK for Android TV"

    val releaseApkDir = layout.buildDirectory.dir("outputs/apk/release")
    val stableRelease = releaseApkDir.map { it.asFile.resolve("stable/release") }

    dependsOn("assembleStableRelease")

    from(stableRelease) {
        include("*.apk")
    }

    archiveBaseName.set("PlayCloud-TV")
    archiveExtension.set("apk")
    destinationDirectory.set(layout.buildDirectory.dir("universal-apk"))
}