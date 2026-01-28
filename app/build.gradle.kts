import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.jetbrains.dokka.gradle.engine.parameters.KotlinPlatform
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.util.Base64

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin.android)
}

val javaTarget = JvmTarget.fromTarget(libs.versions.jvmTarget.get())

fun getGitCommitHash(): String {
    return try {
        val headFile = file("${project.rootDir}/.git/HEAD")
        if (!headFile.exists()) return ""
        val head = headFile.readText().trim()
        if (head.startsWith("ref:")) {
            val ref = head.substringAfter("ref:").trim()
            file("${project.rootDir}/.git/$ref").readText().trim()
        } else head
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

        versionCode = 74
        versionName = "1.6.0"

        manifestPlaceholders["target_sdk_version"] = targetSdk as Any

        resValue("string", "app_name", "PlayCloud")
        resValue("color", "blackBoarder", "#FF000000")
        resValue("string", "commit_hash", getGitCommitHash())

        buildConfigField("long", "BUILD_DATE", System.currentTimeMillis().toString())
        buildConfigField("String", "APP_VERSION", "\"$versionName\"")

        buildConfigField(
            "String",
            "SIMKL_CLIENT_ID",
            "\"${System.getenv("SIMKL_CLIENT_ID") ?: ""}\""
        )
        buildConfigField(
            "String",
            "SIMKL_CLIENT_SECRET",
            "\"${System.getenv("SIMKL_CLIENT_SECRET") ?: ""}\""
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a")
            isUniversalApk = true
        }
    }

    signingConfigs {
        create("release") {
            val base64Key = System.getenv("SIGNING_KEY") ?: return@create
            val file = layout.buildDirectory.file("release.jks").get().asFile

            if (!file.exists()) {
                file.parentFile.mkdirs()
                file.writeBytes(Base64.getDecoder().decode(base64Key))
            }

            storeFile = file
            storePassword = System.getenv("KEY_STORE_PASSWORD")
            keyAlias = System.getenv("ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            isDebuggable = true
        }
    }

    flavorDimensions += "state"
    productFlavors {
        create("stable") { dimension = "state" }
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

    buildFeatures {
        viewBinding = true
        buildConfig = true
        resValues = true
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
        disable.add("MissingTranslation")
    }

    namespace = "com.lagradost.cloudstream3"
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.activity.ktx)
    implementation(libs.fragment.ktx)
    implementation(libs.material)
    implementation(libs.constraintlayout)

    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.navigation)
    implementation(libs.bundles.coil)
    implementation(libs.bundles.media3)

    implementation(libs.preference.ktx)
    implementation(libs.video)
    implementation(libs.bundles.nextlib)

    implementation(libs.jsoup)
    implementation(libs.rhino)
    implementation(libs.quickjs)
    implementation(libs.work.runtime.ktx)
    implementation(libs.biometric)
    implementation(libs.previewseekbar.media3)

    coreLibraryDesugaring(libs.desugar.jdk.libs.nio)

    implementation(libs.conscrypt.android)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.torrentserver)
    implementation(libs.nicehttp)

    implementation(project(":library"))
}

tasks.withType<KotlinJvmCompile> {
    compilerOptions {
        jvmTarget.set(javaTarget)
        jvmDefault.set(JvmDefaultMode.ENABLE)
        optIn.add("com.lagradost.cloudstream3.Prerelease")
    }
}
