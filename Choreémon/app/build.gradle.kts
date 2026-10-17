plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.choreemon.app"
    compileSdk = 36
    buildToolsVersion = "36.1.0"
    defaultConfig {
        applicationId = "com.choreemon.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures { compose = true; buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    testOptions { unitTests.isReturnDefaultValues = true }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}

kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.04.01"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.04.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// Java on Windows can misread UTF-8 paths in Gradle's worker @argument file.
// Keep the project where the user put it; mirror only test runtime outputs to
// an ASCII cache before starting the JVM. Normal source/build paths stay intact.
tasks.withType<Test>().configureEach {
    if (System.getProperty("os.name").startsWith("Windows") && rootDir.path.any { it.code > 127 }) {
        doFirst {
            val cache = File(System.getenv("LOCALAPPDATA"), "Choreemon/test-runtime/${rootDir.path.hashCode().toUInt()}/$name")
            val resolved = classpath.files.filter { it.exists() }.map { source ->
                if (source.absolutePath.all { it.code < 128 }) source else {
                    val target = File(cache, source.absolutePath.hashCode().toUInt().toString())
                    if (source.isDirectory) {
                        project.sync { from(source); into(target) }
                        target
                    } else {
                        target.mkdirs()
                        File(target, source.name).also { source.copyTo(it, overwrite = true) }
                    }
                }
            }
            classpath = files(resolved)
        }
    }
}
