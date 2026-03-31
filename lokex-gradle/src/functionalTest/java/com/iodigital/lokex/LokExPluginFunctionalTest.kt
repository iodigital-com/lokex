package com.iodigital.lokex

import org.gradle.testkit.runner.GradleRunner
import org.junit.Test
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files

class LokExPluginFunctionalTest {

    @Test
    @Throws(IOException::class)
    fun canRunTask() {
        // Setup the test build
        val projectDir = File("build/functionalTest")
        setupTestProject(projectDir)

        // Run the build
        GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withArguments("exportLokalise")
            .withProjectDir(projectDir)
            .build()

        // Verify that the export produced a valid, non-empty XML file
        val output = File(projectDir, "strings.xml")
        assert(output.exists()) { "Expected strings.xml to be created" }
        val content = output.readText()
        assert(content.isNotBlank()) { "Expected strings.xml to have content" }
        assert(content.contains("<?xml") || content.contains("<resources")) {
            "Expected strings.xml to contain valid XML"
        }
    }

    private fun setupTestProject(projectDir: File) {
        val configFile = File(projectDir, "config.json")
        val token = File("../lokex-cli/.lokalisetoken").readText().trim()
        Files.createDirectories(projectDir.toPath())
        writeString(File(projectDir, "settings.gradle"), "")
        writeString(
            configFile, """
            {
                "exports": [
                    {
                        "language": "en",
                        "platform":  "android",
                        "exportEmptyAs": "base",
                        "projectId": "41526497605e2bb0170e72.28979333",
                        "templatePath": "strings.xml.lokex",
                        "destinationPath": "strings.xml"
                    }
                ]
            }
        """.trimIndent()
        )
        writeString(
            File(projectDir, "build.gradle"),
            """
                plugins {
                    id('com.iodigital.lokex')
                }
                
                lokex {
                    lokaliseToken = "$token"
                    configFile = file("${configFile.absolutePath}")
                }
            """.trimIndent()
        )

        File("src/functionalTest/strings.xml.lokex")
            .copyTo(File(projectDir, "strings.xml.lokex"), overwrite = true)

    }

    @Throws(IOException::class)
    private fun writeString(file: File, string: String) {
        FileWriter(file).use { writer ->
            writer.write(string)
        }
    }
}
