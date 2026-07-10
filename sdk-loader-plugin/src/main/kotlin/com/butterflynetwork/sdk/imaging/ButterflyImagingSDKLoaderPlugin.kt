// Copyright 2012-2026 (C) Butterfly Network, Inc.

package com.butterflynetwork.sdk.imaging

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import java.io.File

abstract class ButterflyImagingSDKLoaderPlugin : Plugin<Project> {

    // The group ID, artifact ID, version name and checksum of the Butterfly Imaging SDK to download.
    private val imagingSdkGroupId = "com.butterflynetwork.sdk"
    private val imagingSdkArtifactId = "imaging"
    private val imagingSdkVersion: String = SdkInfo.VERSION
    private val imagingSdkCheckSum: String = SdkInfo.CHECKSUM

    // URL to download the Butterfly Imaging SDK Zip file.
    private val imagingSdkZipUrl: String = SdkInfo.URL

    override fun apply(project: Project) {
        val install = project.tasks.register<InstallButterflyImagingSdkTask>("installButterflyImagingSdk") {
            group = "butterfly"
            description = "Download and install the Butterfly Imaging SDK."
            sdkVersion.set(imagingSdkVersion)
            sdkCheckSum.set(imagingSdkCheckSum)
            sdkZipUrl.set(imagingSdkZipUrl)
            onlyIf { !sdkExists() }
        }

        // Wire install before any Android build.
        project.tasks.matching { it.name == "preBuild" }.configureEach {
            dependsOn(install)
        }

        // Declare dependency.
        project.afterEvaluate {
            project.dependencies.add(
                "implementation",
                "$imagingSdkGroupId:$imagingSdkArtifactId:$imagingSdkVersion"
            )
        }
    }

    /**
     * Check if SDK exists in local maven repository.
     */
    private fun sdkExists(): Boolean {
        val artifactDir =
            File(System.getProperty("user.home"), ".m2/repository")
                .resolve(imagingSdkGroupId.replace('.', '/'))
                .resolve(imagingSdkArtifactId)
                .resolve(imagingSdkVersion)
        return artifactDir.resolve("$imagingSdkArtifactId-$imagingSdkVersion.aar").exists() &&
                artifactDir.resolve("$imagingSdkArtifactId-$imagingSdkVersion.pom").exists()
    }
}
