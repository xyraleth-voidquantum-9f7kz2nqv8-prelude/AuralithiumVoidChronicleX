import org.jetbrains.dokka.gradle.engine.parameters.KotlinPlatform
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.io.File

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.dokka)
}

val javaTarget = JvmTarget.fromTarget(libs.versions.jvmTarget.get())

fun getGitCommitHash(): String {
    return try {
        val head = file("${project.rootDir}/.git/HEAD")
        if (!head.exists()) return "nogit"

        val ref = head.readText().trim()
        val hash = if (ref.startsWith("ref:")) {
            val refFile = file("${project.rootDir}/.git/${ref.substringAfter("ref:").trim()}")
            if (refFile.exists()) refFile.readText().trim() else "nogit"
        } else ref

        hash.take(7)
    } catch (_: Throwable) {
        "nogit"
    }
}

android {
    namespace = "com.lagradost.cloudstream3"
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

        buildConfigField("long", "BUILD_DATE", System.currentTimeMillis().toString())
        buildConfigField("String", "APP_VERSION", "\"$versionName\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // ================================
    // 🔐 SIGNING CONFIG (SAFE VERSION)
    // ================================
    val keystoreFile = file("keystore.jks")
    val hasKeystore = keystoreFile.exists()

    signingConfigs {
        if (hasKeystore) {
            create("release") {
                storeFile = keystoreFile
                storePassword = System.getenv("KEY_STORE_PASSWORD") ?: "161105"
                keyAlias = System.getenv("ALIAS") ?: "cloudplay"
                keyPassword = System.getenv("KEY_PASSWORD") ?: "161105"
                storeType = "PKCS12"
            }
        }
    }

    buildTypes {
        release {
            if (hasKeystore) {
                signingConfig = signingConfigs.getByName("release")
            } else {
                println("⚠️ Release signing DISABLED (keystore.jks not found)")
            }

            isDebuggable = false
            isMinifyEnabled = false
            isShrinkResources = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    flavorDimensions += "state"
    productFlavors {
        create("stable") {
            dimension = "state"
            resValue("bool", "is_prerelease", "false")
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.toVersion(javaTarget.target)
        targetCompatibility = JavaVersion.toVersion(javaTarget.target)
    }

    java {
        toolchain {
            languageVersion.set(
                JavaLanguageVersion.of(libs.versions.jdkToolchain.get())
            )
        }
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
        disable += "MissingTranslation"
    }

    buildFeatures {
        buildConfig = true
        resValues = true
        viewBinding = true
    }
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.json)

    androidTestImplementation(libs.core)
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

    implementation(libs.jsoup)
    implementation(libs.rhino)
    implementation(libs.quickjs)
    implementation(libs.palette.ktx)

    coreLibraryDesugaring(libs.desugar.jdk.libs.nio)

    implementation(project(":library"))
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(javaTarget)
        jvmDefault.set(JvmDefaultMode.ENABLE)
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
