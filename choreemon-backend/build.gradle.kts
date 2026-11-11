plugins {
    kotlin("jvm") version "2.2.20"
    id("io.ktor.plugin") version "3.0.3"
}

group = "com.choreemon"
version = "1.0.0"

application {
    mainClass.set("io.ktor.server.cio.EngineMain")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${System.getenv("ENV") != "azure"}")
}

repositories {
    mavenCentral()
}

val ktor_version = "3.0.3"
val exposed_version = "0.55.0"
val flyway_version = "10.17.3"
val logback_version = "1.5.12"

dependencies {
    // ── Ktor server ──────────────────────────────────────────────────────────
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-cio-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-host-common-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-cors-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-jackson-jvm:$ktor_version")

    // ── Ktor client (FCM HTTP calls) ─────────────────────────────────────────
    implementation("io.ktor:ktor-client-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-cio-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation-jvm:$ktor_version")

    // ── Jackson (JSON, no annotation pollution on domain models) ─────────────
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2")

    // ── Database ─────────────────────────────────────────────────────────────
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
    // Azure SQL / local SQL Server — same driver for both
    implementation("com.microsoft.sqlserver:mssql-jdbc:12.8.1.jre11")
    implementation("com.zaxxer:HikariCP:5.1.0")

    // ── Migrations ───────────────────────────────────────────────────────────
    implementation("org.flywaydb:flyway-core:$flyway_version")
    implementation("org.flywaydb:flyway-sqlserver:$flyway_version")

    // ── Auth ─────────────────────────────────────────────────────────────────
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("at.favre.lib:bcrypt:0.10.2")

    // ── Logging ──────────────────────────────────────────────────────────────
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // ── Testing ──────────────────────────────────────────────────────────────
    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktor_version")
    testImplementation(kotlin("test"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

tasks.shadowJar {
    manifest { attributes(mapOf("Main-Class" to application.mainClass.get())) }
    mergeServiceFiles()
    archiveClassifier.set("")
}

tasks.build { dependsOn(tasks.shadowJar) }
