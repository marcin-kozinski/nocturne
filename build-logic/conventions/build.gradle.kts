plugins { `kotlin-dsl` }

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(libs.plugin.dependency.analysis)
    implementation(libs.plugin.android)
    implementation(libs.plugin.compose.compiler)
    implementation(libs.plugin.kotlin)
}
