package nocturne

import com.android.build.api.variant.HostTestBuilder

plugins {
    id("com.autonomousapps.dependency-analysis")
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    compileSdk { version = release(36) }
    defaultConfig {
        minSdk = 35
        targetSdk = 36

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures { compose = true }
}

androidComponents {
    beforeVariants { variantBuilder ->
        @Suppress("UnstableApiUsage")
        variantBuilder.hostTests[HostTestBuilder.UNIT_TEST_TYPE]?.enable =
            (variantBuilder.name == "debug")
    }
}

kotlin {
    compilerOptions { allWarningsAsErrors = true }
    jvmToolchain(21)
}

repositories {
    google()
    mavenCentral()
}
