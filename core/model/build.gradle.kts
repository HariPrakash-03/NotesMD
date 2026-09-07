plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.notesmd.core.model"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }}

dependencies {
    implementation(libs.androidx.core.ktx)
}








