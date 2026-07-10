// Copyright 2012-2026 (C) Butterfly Network, Inc.

package com.butterflynetwork.sdk.imaging

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.security.MessageDigest
import java.util.Base64
import java.util.zip.ZipFile

abstract class InstallButterflyImagingSdkTask : DefaultTask() {

    @get:Input
    abstract val sdkVersion: Property<String>

    @get:Input
    abstract val sdkCheckSum: Property<String>

    @get:Input
    abstract val sdkZipUrl: Property<String>

    @TaskAction
    fun install() {
        val user = System.getenv("BFLY_SDK_USER_NAME")
        val token = System.getenv("BFLY_SDK_ACCESS_TOKEN")
        if (user.isNullOrBlank() || token.isNullOrBlank()) {
            throw GradleException(
                "Set BFLY_SDK_USER_NAME and BFLY_SDK_ACCESS_TOKEN environment variables to download Butterfly Imaging SDK."
            )
        }

        val tempZip = File.createTempFile("butterfly-sdk-${sdkVersion.get()}", ".zip")
        try {
            logger.lifecycle("Set up ButterflyImagingSDK-${sdkVersion.get()}...")
            downloadSDK(user, token, toZip = tempZip)
            verifyChecksum(file = tempZip)
            publishToMavenLocal(zipFile = tempZip)
            logger.lifecycle("ButterflyImagingSDK-${sdkVersion.get()} set up successfully.")
        } finally {
            tempZip.delete()
        }
    }

    /**
     * Download the Butterfly Imaging SDK Zip file from the given URL and save it to the given file.
     */
    private fun downloadSDK(user: String, token: String, toZip: File) {
        val auth = Base64.getEncoder()
            .encodeToString("$user:$token".toByteArray(Charsets.UTF_8))

        val connection = URI(sdkZipUrl.get()).toURL().openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 30_000
            connection.readTimeout = 60_000
            connection.setRequestProperty("Authorization", "Basic $auth")
            connection.connect()

            val status = connection.responseCode
            if (status !in 200..299) {
                throw GradleException(
                    "Failed to download Butterfly Imaging SDK (HTTP $status ${connection.responseMessage})."
                )
            }

            connection.inputStream.use { input ->
                toZip.outputStream().buffered().use { output ->
                    input.copyTo(output)
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    /**
     * Verify the checksum of the downloaded Butterfly Imaging SDK Zip file.
     */
    private fun verifyChecksum(file: File) {
        val expected = sdkCheckSum.get()
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered().use { input ->
            val buffer = ByteArray(8192)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        val actual = digest.digest().joinToString("") { "%02x".format(it) }

        if (!actual.equals(expected, ignoreCase = true)) {
            throw GradleException("Butterfly Imaging SDK checksum mismatch. Set up fails.")
        }
    }

    /**
     * Publish the downloaded Butterfly Imaging SDK to the local Maven repository.
     */
    private fun publishToMavenLocal(zipFile: File) {
        val localMaven = File(System.getProperty("user.home"), ".m2/repository")
        ZipFile(zipFile).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                val out = localMaven.resolve(entry.name)
                if (entry.isDirectory) {
                    out.mkdirs()
                } else {
                    out.parentFile.mkdirs()
                    zip.getInputStream(entry).use { input ->
                        out.outputStream().buffered().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        }
    }
}
