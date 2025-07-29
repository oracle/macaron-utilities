/* Copyright (c) 2025, 2025, Oracle and/or its affiliates. */
/* Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/. */

package com.oracle.macaron.artifactverifier.gradle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class VerifyDependenciesPluginFunctionalTest {

    private File setupBasicProjectCommon(String testName) throws IOException {
        File projectDir = new File("build/functionalTest/" + testName);
        Files.createDirectories(projectDir.toPath());
        Path srcDir = projectDir.toPath().resolve("src").resolve("main").resolve("java");
        Files.createDirectories(srcDir);
        writeString(srcDir.resolve("HelloWorld.java").toFile(),
            """
            public class HelloWorld {
                public static void main(String[] args) {
                    System.out.println(\"Hello World\");
                }
            }
            """);
        writeString(new File(projectDir, "settings.gradle"), "");
        writeString(new File(projectDir, "gradle.properties"),
            """
            systemProp.http.proxyHost=au-proxy.au.oracle.com
            systemProp.http.proxyPort=80
            systemProp.https.proxyHost=au-proxy.au.oracle.com
            systemProp.https.proxyPort=80
            """);

        writeString(new File(projectDir, "test-artifact-usage-alert-config.json"),
            """
            {
                "type": "report-gav-alternatives-map",
                "gav-alternatives": {
                    "io.micronaut:micronaut-inject:3.8.5": ["io.micronaut:micronaut-inject:3.8.5-oracle-00001"],
                    "io.micronaut:micronaut-core:3.8.5": ["io.micronaut:micronaut-core:3.8.5-oracle-00001"],
                    "io.micronaut:micronaut-inject:4.6.8": ["io.micronaut:micronaut-inject:4.6.8-oracle-00001"],
                    "io.micronaut:micronaut-core:4.6.8": ["io.micronaut:micronaut-core:4.6.8-oracle-00001"],
                    "io.micronaut.security:micronaut-security-annotations:4.6.10": ["io.micronaut.security:micronaut-security-annotation:4.6.10-oracle-00001"]
                }
            }
            """);
        return projectDir;
    }

    @Test void testVerification() throws IOException {
        // Setup the test build
        File projectDir = setupBasicProjectCommon("testVerification");
        writeString(new File(projectDir, "build.gradle"),
            """
            plugins {
            id('application')
            id('com.oracle.macaron.artifactverifier.gradle.verifydependencies')
            }
            repositories {
            mavenCentral()
            maven {
                url = uri('https://maven.oracle.com/public')
            }
            }
            dependencies {
            implementation 'io.micronaut:micronaut-inject:4.6.8-oracle-00001'
            }
            application {
            mainClass = 'HelloWorld'
            }


            verifydependencies {
            verificationRepos = ["https://maven.oracle.com/public"]
            verifierMode = "REQUIRE_VERIFY_IF_ATTESTED_ANY"
            }
            """);

        // Run the build
        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments("build");
        runner.withProjectDir(projectDir);
        BuildResult result = runner.build();

        Assertions.assertTrue(result.getOutput().contains("Verified: [io.micronaut:micronaut-inject:4.6.8-oracle-00001, io.micronaut:micronaut-core:4.6.8-oracle-00001]"));
        Assertions.assertFalse(result.getOutput().contains("Verification Failed"));
    }

    @Test void testWithoutRelevantArtifacts() throws IOException {
        // Setup the test build
        File projectDir = setupBasicProjectCommon("testWithoutRelevantArtifacts");
        writeString(new File(projectDir, "build.gradle"),
            """
            plugins {
            id('application')
            id('com.oracle.macaron.artifactverifier.gradle.verifydependencies')
            }
            repositories {
            mavenCentral()
            maven {
                url = uri('https://maven.oracle.com/public')
            }
            }
            dependencies {
            implementation 'com.fasterxml.jackson.core:jackson-databind:2.18.2'
            }
            application {
            mainClass = 'HelloWorld'
            }

            verifydependencies {
            verificationRepos = ["https://maven.oracle.com/public"]
            }
            """);

        // Run the build
        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments("build");
        runner.withProjectDir(projectDir);
        BuildResult result = runner.build();

        Assertions.assertFalse(result.getOutput().contains("Verified: ["));
        Assertions.assertFalse(result.getOutput().contains("Verification Failed"));
    }

    @Test void testDepsWithoutVSA() throws IOException {
        // Setup the test build
        File projectDir = setupBasicProjectCommon("testDepsWithoutVSA");
        writeString(new File(projectDir, "build.gradle"),
            """
            plugins {
            id('application')
            id('com.oracle.macaron.artifactverifier.gradle.verifydependencies')
            }
            repositories {
            mavenCentral()
            maven {
                url = uri('https://maven.oracle.com/public')
            }
            }
            dependencies {
            implementation 'io.micronaut:micronaut-inject:3.8.5-oracle-00001'
            }
            application {
            mainClass = 'HelloWorld'
            }

            verifydependencies {
            verificationRepos = ["https://maven.oracle.com/public"]
            }
            """);

        // Run the build
        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments("build");
        runner.withProjectDir(projectDir);
        BuildResult result = runner.build();

        Assertions.assertTrue(result.getOutput().contains("Verification Skipped: [io.micronaut:micronaut-inject:3.8.5-oracle-00001, io.micronaut:micronaut-core:3.8.5-oracle-00001, org.slf4j:slf4j-api:1.7.36, javax.annotation:javax.annotation-api:1.3.2, jakarta.inject:jakarta.inject-api:2.0.1, jakarta.annotation:jakarta.annotation-api:2.1.1, org.yaml:snakeyaml:1.33]"));
        Assertions.assertFalse(result.getOutput().contains("Verification Failed"));
    }

    @Test void testReportCorrespondingWarnExact() throws IOException {
        // Setup the test build
        File projectDir = setupBasicProjectCommon("testReportCorrespondingWarnExact");
        writeString(new File(projectDir, "build.gradle"),
            """
            plugins {
            id('application')
            id('com.oracle.macaron.artifactverifier.gradle.verifydependencies')
            }
            repositories {
            mavenCentral()
            maven {
                url = uri('https://maven.oracle.com/public')
            }
            }
            dependencies {
            implementation 'io.micronaut:micronaut-inject:4.6.8'
            implementation 'io.micronaut.security:micronaut-security-annotations:3.8.4'
            }
            application {
            mainClass = 'HelloWorld'
            }

            verifydependencies {
            artifactUsageAlertConfigFilePath = project.projectDir.getAbsolutePath() + "/test-artifact-usage-alert-config.json"
            artifactUsageAlertSeverity = "WARN_EXACT_VERSION"
            }
            """);

        // Run the build
        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments("build");
        runner.withProjectDir(projectDir);
        BuildResult result = runner.build();

        Assertions.assertTrue(result.getOutput().contains("Warning: using dependency io.micronaut:micronaut-inject:4.6.8 which is configured for usage alert (suggested alternatives: [io.micronaut:micronaut-inject:4.6.8-oracle-00001])"));
        Assertions.assertTrue(result.getOutput().contains("Warning: using dependency io.micronaut:micronaut-core:4.6.8 which is configured for usage alert (suggested alternatives: [io.micronaut:micronaut-core:4.6.8-oracle-00001])"));
        Assertions.assertFalse(result.getOutput().contains("Error: using dependency io.micronaut:micronaut-inject"));
        Assertions.assertFalse(result.getOutput().contains("Error: using dependency io.micronaut:micronaut-core"));
        Assertions.assertFalse(result.getOutput().contains("Error: using dependency io.micronaut:micronaut-security"));
        Assertions.assertFalse(result.getOutput().contains("Warning: using dependency io.micronaut:micronaut-security"));
    }

    @Test void testReportCorrespondingErrorExact() throws IOException {
        // Setup the test build
        File projectDir = setupBasicProjectCommon("testReportCorrespondingErrorExact");
        writeString(new File(projectDir, "build.gradle"),
            """
            plugins {
            id('application')
            id('com.oracle.macaron.artifactverifier.gradle.verifydependencies')
            }
            repositories {
            mavenCentral()
            maven {
                url = uri('https://maven.oracle.com/public')
            }
            }
            dependencies {
            implementation 'io.micronaut:micronaut-inject:4.6.8'
            implementation 'io.micronaut.security:micronaut-security-annotations:3.8.4'
            }
            application {
            mainClass = 'HelloWorld'
            }

            verifydependencies {
            artifactUsageAlertConfigFilePath = project.projectDir.getAbsolutePath() + "/test-artifact-usage-alert-config.json"
            artifactUsageAlertSeverity = "ERROR_EXACT_VERSION"
            }
            """);

        // Run the build
        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments("build");
        runner.withProjectDir(projectDir);
        BuildResult result = runner.buildAndFail();

        Assertions.assertTrue(result.getOutput().contains("Error: using dependency io.micronaut:micronaut-inject:4.6.8 which is configured for usage alert (suggested alternatives: [io.micronaut:micronaut-inject:4.6.8-oracle-00001])"));
        Assertions.assertTrue(result.getOutput().contains("Error: using dependency io.micronaut:micronaut-core:4.6.8 which is configured for usage alert (suggested alternatives: [io.micronaut:micronaut-core:4.6.8-oracle-00001])"));
        Assertions.assertFalse(result.getOutput().contains("Warning: using dependency io.micronaut:micronaut-inject"));
        Assertions.assertFalse(result.getOutput().contains("Warning: using dependency io.micronaut:micronaut-core"));
        Assertions.assertFalse(result.getOutput().contains("Error: using dependency io.micronaut:micronaut-security"));
        Assertions.assertFalse(result.getOutput().contains("Warning: using dependency io.micronaut:micronaut-security"));
    }

    @Test void testReportCorrespondingWarnAny() throws IOException {
        // Setup the test build
        File projectDir = setupBasicProjectCommon("testReportCorrespondingWarnAny");
        writeString(new File(projectDir, "build.gradle"),
            """
            plugins {
            id('application')
            id('com.oracle.macaron.artifactverifier.gradle.verifydependencies')
            }
            repositories {
            mavenCentral()
            maven {
                url = uri('https://maven.oracle.com/public')
            }
            }
            dependencies {
            implementation 'io.micronaut:micronaut-inject:4.6.8'
            implementation 'io.micronaut.security:micronaut-security-annotations:3.8.4'
            }
            application {
            mainClass = 'HelloWorld'
            }

            verifydependencies {
            artifactUsageAlertConfigFilePath = project.projectDir.getAbsolutePath() + "/test-artifact-usage-alert-config.json"
            artifactUsageAlertSeverity = "WARN_ANY_VERSION"
            }
            """);

        // Run the build
        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments("build");
        runner.withProjectDir(projectDir);
        BuildResult result = runner.build();

        Assertions.assertTrue(result.getOutput().contains("Warning: using dependency io.micronaut:micronaut-inject:4.6.8 which is configured for usage alert (suggested alternatives: [io.micronaut:micronaut-inject:4.6.8-oracle-00001])"));
        Assertions.assertTrue(result.getOutput().contains("Warning: using dependency io.micronaut:micronaut-core:4.6.8 which is configured for usage alert (suggested alternatives: [io.micronaut:micronaut-core:4.6.8-oracle-00001])"));
        Assertions.assertTrue(result.getOutput().contains("Warning: using dependency io.micronaut.security:micronaut-security-annotations:3.8.4 for which any version is configured for usage alert"));
        Assertions.assertFalse(result.getOutput().contains("Error: using dependency io.micronaut:micronaut-inject"));
        Assertions.assertFalse(result.getOutput().contains("Error: using dependency io.micronaut:micronaut-core"));
        Assertions.assertFalse(result.getOutput().contains("Error: using dependency io.micronaut:micronaut-security"));
    }

    @Test void testReportCorrespondingErrorAny() throws IOException {
        // Setup the test build
        File projectDir = setupBasicProjectCommon("testReportCorrespondingErrorAny");
        writeString(new File(projectDir, "build.gradle"),
            """
            plugins {
            id('application')
            id('com.oracle.macaron.artifactverifier.gradle.verifydependencies')
            }
            repositories {
            mavenCentral()
            maven {
                url = uri('https://maven.oracle.com/public')
            }
            }
            dependencies {
            implementation 'io.micronaut:micronaut-inject:4.6.8'
            implementation 'io.micronaut.security:micronaut-security-annotations:3.8.4'
            }
            application {
            mainClass = 'HelloWorld'
            }

            verifydependencies {
            artifactUsageAlertConfigFilePath = project.projectDir.getAbsolutePath() + "/test-artifact-usage-alert-config.json"
            artifactUsageAlertSeverity = "ERROR_ANY_VERSION"
            }
            """);

        // Run the build
        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments("build");
        runner.withProjectDir(projectDir);
        BuildResult result = runner.buildAndFail();

        Assertions.assertTrue(result.getOutput().contains("Error: using dependency io.micronaut:micronaut-inject:4.6.8 which is configured for usage alert (suggested alternatives: [io.micronaut:micronaut-inject:4.6.8-oracle-00001])"));
        Assertions.assertTrue(result.getOutput().contains("Error: using dependency io.micronaut:micronaut-core:4.6.8 which is configured for usage alert (suggested alternatives: [io.micronaut:micronaut-core:4.6.8-oracle-00001])"));
        Assertions.assertTrue(result.getOutput().contains("Error: using dependency io.micronaut.security:micronaut-security-annotations:3.8.4 for which any version is configured for usage alert"));
        Assertions.assertFalse(result.getOutput().contains("Warning: using dependency io.micronaut:micronaut-inject"));
        Assertions.assertFalse(result.getOutput().contains("Warning: using dependency io.micronaut:micronaut-core"));
        Assertions.assertFalse(result.getOutput().contains("Warning: using dependency io.micronaut:micronaut-security"));
    }

    @Test void testReportCorrespondingWarnAnyErrorExact() throws IOException {
        // Setup the test build
        File projectDir = setupBasicProjectCommon("testReportCorrespondingWarnAnyErrorExact");
        writeString(new File(projectDir, "build.gradle"),
            """
            plugins {
            id('application')
            id('com.oracle.macaron.artifactverifier.gradle.verifydependencies')
            }
            repositories {
            mavenCentral()
            maven {
                url = uri('https://maven.oracle.com/public')
            }
            }
            dependencies {
            implementation 'io.micronaut:micronaut-inject:4.6.8'
            implementation 'io.micronaut.security:micronaut-security-annotations:3.8.4'
            }
            application {
            mainClass = 'HelloWorld'
            }

            verifydependencies {
            artifactUsageAlertConfigFilePath = project.projectDir.getAbsolutePath() + "/test-artifact-usage-alert-config.json"
            artifactUsageAlertSeverity = "WARN_ANY_VERSION_ERROR_EXACT_VERSION"
            reportArtifactWithCorrespondingOracleVersionAvailable = "WARN_ANY_ERROR_EXACT"
            }
            """);

        // Run the build
        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments("build");
        runner.withProjectDir(projectDir);
        BuildResult result = runner.buildAndFail();

        Assertions.assertTrue(result.getOutput().contains("Error: using dependency io.micronaut:micronaut-inject:4.6.8 which is configured for usage alert (suggested alternatives: [io.micronaut:micronaut-inject:4.6.8-oracle-00001])"));
        Assertions.assertTrue(result.getOutput().contains("Error: using dependency io.micronaut:micronaut-core:4.6.8 which is configured for usage alert (suggested alternatives: [io.micronaut:micronaut-core:4.6.8-oracle-00001])"));
        Assertions.assertTrue(result.getOutput().contains("Warning: using dependency io.micronaut.security:micronaut-security-annotations:3.8.4 for which any version is configured for usage alert"));
        Assertions.assertFalse(result.getOutput().contains("Warning: using dependency io.micronaut:micronaut-inject"));
        Assertions.assertFalse(result.getOutput().contains("Warning: using dependency io.micronaut:micronaut-core"));
    }

    private void writeString(File file, String string) throws IOException {
        try (Writer writer = new FileWriter(file, Charset.forName("UTF-8"))) {
            writer.write(string);
        }
    }
}
