plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    application
}

application {
    mainClass.set("com.iodigital.lokex.MainKt")
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.cli)
            implementation(libs.ktor.core)
            implementation(project(":lokex-core"))
        }
    }
}

// Wire the application plugin to use the KMP JVM jar and its runtime classpath
// so the generated launcher script has the correct classpath entries.
val jvmJar by tasks.getting(Jar::class)
val jvmRuntimeClasspath by configurations.getting

tasks.named<CreateStartScripts>("startScripts") {
    classpath = files(jvmJar.archiveFile) + jvmRuntimeClasspath
}

distributions {
    main {
        distributionBaseName.set("lokex")
        contents {
            into("") {
                from(jvmJar)
                from("src/lokex")
            }
            into("lib/") {
                from(jvmJar)
                val main by kotlin.jvm().compilations.getting
                from(main.runtimeDependencyFiles)
            }
            exclude("**/lokalise-exporter")
            exclude("**/lokalise-exporter.bat")
        }
    }
}

tasks.withType<Jar> {
    doFirst {
        manifest {
            val main by kotlin.jvm().compilations.getting
            attributes(
                "Main-Class" to "com.iodigital.lokex.MainKt",
                "Class-Path" to main.runtimeDependencyFiles.files.joinToString(" ") { "lib/" + it.name }
            )
        }
    }
}