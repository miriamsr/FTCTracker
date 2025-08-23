plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
}

group = "me.zharel.ftctracker"
application {
    mainClass.set("me.zharel.ftctracker.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}

val generateVersionFile by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/version")
    val versionFile = outputDir.map { it.file("AppVersion.kt") }

    val versionProvider = provider {
        project.findProperty("version") as String? ?: "unknown"
    }

    inputs.property("version", versionProvider)
    outputs.file(versionFile)

    doLast {
        val version = versionProvider.get()
        versionFile.get().asFile.writeText("""
            package me.zharel.ftctracker

            object AppVersion {
                const val VERSION = "$version"
            }
        """.trimIndent())
    }
}

sourceSets["main"].java.srcDir(layout.buildDirectory.dir("generated/version"))
tasks.named("compileKotlin").configure {
    dependsOn(generateVersionFile)
}
