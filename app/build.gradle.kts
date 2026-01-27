import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.jetbrains.dokka.gradle.engine.parameters.KotlinPlatform
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.dokka)
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
    } catch (_: Throwable) { "" }
}.take(7)

android {
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.cloudplay.app"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 74
        versionName = "1.6.0"

        resValue("string", "commit_hash", getGitCommitHash())
        resValue("bool", "is_prerelease", "false")
        resValue("string", "app_name", "CloudPlay")
        resValue("color", "blackBoarder", "#FF000000")

        // Placeholder untuk AndroidManifest.xml
        manifestPlaceholders["target_sdk_version"] = libs.versions.targetSdk.get()

        buildConfigField("long", "BUILD_DATE", "${System.currentTimeMillis()}")
        buildConfigField("String", "APP_VERSION", "\"$versionName\"")
        buildConfigField(
            "String",
            "SIMKL_CLIENT_ID",
            "\"db13c9a72e036f717c3a85b13cdeb31fa884c8f4991e43695f7b6477374e35b8\""
        )
        buildConfigField(
            "String",
            "SIMKL_CLIENT_SECRET",
            "\"d8cf8e1b79bae9b2f77f0347d6384a62f1a8d802abdd73d9aa52bf6a848532ba\""
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val envKeystorePath = System.getenv("KEYSTORE_PATH")
            storeFile = if (envKeystorePath != null) file(envKeystorePath) else file("keystore.jks")
            storePassword = System.getenv("KEY_STORE_PASSWORD") ?: "161105"
            keyAlias = System.getenv("ALIAS") ?: "cloudplay_new" // alias baru
            keyPassword = System.getenv("KEY_PASSWORD") ?: "161105"
            storeType = "PKCS12"
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isDebuggable = false
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    flavorDimensions.add("state")
    productFlavors {
        create("stable") {
            dimension = "state"
            resValue("bool", "is_prerelease", "false")
            manifestPlaceholders["target_sdk_version"] = libs.versions.targetSdk.get()
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.toVersion(javaTarget.target)
        targetCompatibility = JavaVersion.toVersion(javaTarget.target)
    }

    java {
        toolchain { languageVersion.set(JavaLanguageVersion.of(libs.versions.jdkToolchain.get())) }
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
        disable.add("MissingTranslation")
    }

    buildFeatures {
        buildConfig = true
        resValues = true
        viewBinding = true
    }

    namespace = "com.lagradost.cloudstream3"
}

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

    implementation(project(":library") {
        val isDebug = gradle.startParameter.taskRequests.any { task ->
            task.args.any { arg -> arg.contains("debug", true) }
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
            documentedVisibilities(VisibilityModifier.Public, VisibilityModifier.Protected)
        }
    }
}
