/* Copyright (c) 2025, 2025, Oracle and/or its affiliates. */
/* Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/. */

package com.oracle.macaron.artifactverifier;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ArtifactGavRegexTest {

    @Test
    public void testGavRegexGroupOnly() throws Exception {
        ArtifactGavRegex gavRegex = new ArtifactGavRegex(Pattern.compile("io\\.micronaut.*"), null, null, null, null, null);
        assertTrue(gavRegex.matches("io.micronaut", "micronaut-core", "4.6.4-oracle-00001"));
        assertTrue(gavRegex.matches("io.micronaut.abc", "micronaut-abc", "4.6.6-oracle-00001"));
        assertFalse(gavRegex.matches("io.notmicronaut", "micronaut-core", "4.6.4-oracle-00001"));
    }

    @Test
    public void testGavRegexArtifactOnly() throws Exception {
        ArtifactGavRegex gavRegex = new ArtifactGavRegex(null, Pattern.compile(".*-core"), null, null, null, null);
        assertTrue(gavRegex.matches("io.micronaut", "micronaut-core", "4.6.4-oracle-00001"));
        assertTrue(gavRegex.matches("io.notmicronaut", "notmicronaut-core", "4.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.micronaut", "micronaut-something", "4.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.micronaut", "micronaut-core-something", "4.6.4-oracle-00001"));
    }

    @Test
    public void testGavRegexVersionOnly() throws Exception {
        ArtifactGavRegex gavRegex = new ArtifactGavRegex(null, null, Pattern.compile(".*-oracle-[0-9]+"), null, null, null);
        assertTrue(gavRegex.matches("io.micronaut", "micronaut-core", "4.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.micronaut", "micronaut-core", "4.6.4"));
    }

    @Test
    public void testGavRegexCombinedPositive() throws Exception {
        ArtifactGavRegex gavRegex = new ArtifactGavRegex(Pattern.compile("io\\.micronaut.*"), Pattern.compile(".*-core"), Pattern.compile(".*-oracle-[0-9]+"), null, null, null);
        assertTrue(gavRegex.matches("io.micronaut", "micronaut-core", "4.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.micronaut.abc", "micronaut-abc", "4.6.6-oracle-00001"));
        assertFalse(gavRegex.matches("io.notmicronaut", "micronaut-core", "4.6.4-oracle-00001"));
        assertTrue(gavRegex.matches("io.micronaut", "micronaut-core", "4.6.7-oracle-00001"));
        assertFalse(gavRegex.matches("io.notmicronaut", "notmicronaut-core", "4.6.4-oracle-00001"));
        assertTrue(gavRegex.matches("io.micronaut", "notmicronaut-core", "4.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.micronaut", "micronaut-something", "4.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.micronaut", "micronaut-core-something", "4.6.4-oracle-00001"));
        assertTrue(gavRegex.matches("io.micronaut.abc", "micronaut-abc-core", "4.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.micronaut", "micronaut-core", "4.6.4"));
    }

    @Test
    public void testGavRegexNegativeGroupOnly() throws Exception {
        ArtifactGavRegex gavRegex = new ArtifactGavRegex(null, null, null, Pattern.compile("io\\.micronaut.*"), null, null);
        assertFalse(gavRegex.matches("io.micronaut", "micronaut-core", "4.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.micronaut.abc", "micronaut-abc", "4.6.6-oracle-00001"));
        assertTrue(gavRegex.matches("io.notmicronaut", "micronaut-core", "4.6.4-oracle-00001"));
    }

    @Test
    public void testGavRegexNegativeArtifactOnly() throws Exception {
        ArtifactGavRegex gavRegex = new ArtifactGavRegex(null, null, null, null, Pattern.compile(".*-core"), null);
        assertFalse(gavRegex.matches("io.micronaut", "micronaut-core", "4.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.notmicronaut", "notmicronaut-core", "4.6.4-oracle-00001"));
        assertTrue(gavRegex.matches("io.micronaut", "micronaut-something", "4.6.4-oracle-00001"));
        assertTrue(gavRegex.matches("io.micronaut", "micronaut-core-something", "4.6.4-oracle-00001"));
    }

    @Test
    public void testGavRegexNegativeVersionOnly() throws Exception {
        ArtifactGavRegex gavRegex = new ArtifactGavRegex(null, null, null, null, null, Pattern.compile(".*-oracle-[0-9]+"));
        assertFalse(gavRegex.matches("io.micronaut", "micronaut-core", "4.6.4-oracle-00001"));
        assertTrue(gavRegex.matches("io.micronaut", "micronaut-core", "4.6.4"));
    }

    @Test
    public void testGavRegexCombinedNegative() throws Exception {
        ArtifactGavRegex gavRegex = new ArtifactGavRegex(null, null, null, Pattern.compile("io\\.micronaut.*"), Pattern.compile(".*-core"), Pattern.compile(".*-oracle-[0-9]+"));
        assertFalse(gavRegex.matches("io.micronaut", "micronaut-core", "4.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.micronaut", "micronaut-core", "4.6.4"));
        assertFalse(gavRegex.matches("io.micronaut", "micronaut-abc", "4.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.notmicronaut", "micronaut-core", "4.6.4-oracle-00001"));
        assertTrue(gavRegex.matches("io.notmicronaut", "micronaut-abc", "4.6.4"));
    }

    @Test
    public void testGavRegexCombinedPositiveNegative() throws Exception {
        ArtifactGavRegex gavRegex = new ArtifactGavRegex(Pattern.compile("io\\..*"), Pattern.compile("micronaut-.*"), Pattern.compile("4\\..*"),
            Pattern.compile("io\\.micronaut.*"), Pattern.compile(".*-core"), Pattern.compile(".*-oracle-[0-9]+"));
        assertFalse(gavRegex.matches("io.micronaut", "abc-core", "5.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("abc", "abc-core", "5.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.abc", "abc-core", "5.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.micronaut", "abc", "5.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.micronaut", "micronaut-abc", "5.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.micronaut", "abc-core", "5.6.4"));
        assertFalse(gavRegex.matches("io.micronaut", "abc-core", "4.6.4"));
        assertFalse(gavRegex.matches("io.abc", "micronaut-abc", "5.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.abc", "abc-core", "4.6.4"));
        assertFalse(gavRegex.matches("io.micronaut", "micronaut-abc", "4.6.4"));
        assertTrue(gavRegex.matches("io.abc", "micronaut-abc", "4.6.4"));
    }

    @Test
    public void testGavRegexConfigGroupOnly() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ArtifactGavRegex gavRegex = ArtifactGavRegexConfiguration.fromConfiguration(objectMapper.readTree("""
        {
            "group-id": "io\\\\.micronaut.*"
        }
        """));
        assertTrue(gavRegex.matches("io.micronaut", "micronaut-core", "4.6.4-oracle-00001"));
        assertTrue(gavRegex.matches("io.micronaut.abc", "micronaut-abc", "4.6.6-oracle-00001"));
        assertFalse(gavRegex.matches("io.notmicronaut", "micronaut-core", "4.6.4-oracle-00001"));
    }

    @Test
    public void testGavRegexConfigArtifactOnly() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ArtifactGavRegex gavRegex = ArtifactGavRegexConfiguration.fromConfiguration(objectMapper.readTree("""
        {
            "artifact-id": ".*-core"
        }
        """));
        assertTrue(gavRegex.matches("io.micronaut", "micronaut-core", "4.6.4-oracle-00001"));
        assertTrue(gavRegex.matches("io.notmicronaut", "notmicronaut-core", "4.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.micronaut", "micronaut-something", "4.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.micronaut", "micronaut-core-something", "4.6.4-oracle-00001"));
    }

    @Test
    public void testGavRegexConfigVersionOnly() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ArtifactGavRegex gavRegex = ArtifactGavRegexConfiguration.fromConfiguration(objectMapper.readTree("""
        {
            "version": ".*-oracle-[0-9]+"
        }
        """));
        assertTrue(gavRegex.matches("io.micronaut", "micronaut-core", "4.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.micronaut", "micronaut-core", "4.6.4"));
    }

    @Test
    public void testGavRegexConfigCombinedPositive() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ArtifactGavRegex gavRegex = ArtifactGavRegexConfiguration.fromConfiguration(objectMapper.readTree("""
        {
            "group-id": "io\\\\.micronaut.*",
            "artifact-id": ".*-core",
            "version": ".*-oracle-[0-9]+"
        }
        """));
        assertTrue(gavRegex.matches("io.micronaut", "micronaut-core", "4.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.micronaut.abc", "micronaut-abc", "4.6.6-oracle-00001"));
        assertFalse(gavRegex.matches("io.notmicronaut", "micronaut-core", "4.6.4-oracle-00001"));
        assertTrue(gavRegex.matches("io.micronaut", "micronaut-core", "4.6.7-oracle-00001"));
        assertFalse(gavRegex.matches("io.notmicronaut", "notmicronaut-core", "4.6.4-oracle-00001"));
        assertTrue(gavRegex.matches("io.micronaut", "notmicronaut-core", "4.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.micronaut", "micronaut-something", "4.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.micronaut", "micronaut-core-something", "4.6.4-oracle-00001"));
        assertTrue(gavRegex.matches("io.micronaut.abc", "micronaut-abc-core", "4.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.micronaut", "micronaut-core", "4.6.4"));
    }

    @Test
    public void testGavRegexConfigNegativeGroupOnly() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ArtifactGavRegex gavRegex = ArtifactGavRegexConfiguration.fromConfiguration(objectMapper.readTree("""
        {
            "~group-id": "io\\\\.micronaut.*"
        }
        """));
        assertFalse(gavRegex.matches("io.micronaut", "micronaut-core", "4.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.micronaut.abc", "micronaut-abc", "4.6.6-oracle-00001"));
        assertTrue(gavRegex.matches("io.notmicronaut", "micronaut-core", "4.6.4-oracle-00001"));
    }

    @Test
    public void testGavRegexConfigNegativeArtifactOnly() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ArtifactGavRegex gavRegex = ArtifactGavRegexConfiguration.fromConfiguration(objectMapper.readTree("""
        {
            "~artifact-id": ".*-core"
        }
        """));
        assertFalse(gavRegex.matches("io.micronaut", "micronaut-core", "4.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.notmicronaut", "notmicronaut-core", "4.6.4-oracle-00001"));
        assertTrue(gavRegex.matches("io.micronaut", "micronaut-something", "4.6.4-oracle-00001"));
        assertTrue(gavRegex.matches("io.micronaut", "micronaut-core-something", "4.6.4-oracle-00001"));
    }

    @Test
    public void testGavRegexConfigNegativeVersionOnly() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ArtifactGavRegex gavRegex = ArtifactGavRegexConfiguration.fromConfiguration(objectMapper.readTree("""
        {
            "~version": ".*-oracle-[0-9]+"
        }
        """));
        assertFalse(gavRegex.matches("io.micronaut", "micronaut-core", "4.6.4-oracle-00001"));
        assertTrue(gavRegex.matches("io.micronaut", "micronaut-core", "4.6.4"));
    }

    @Test
    public void testGavRegexConfigCombinedNegative() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ArtifactGavRegex gavRegex = ArtifactGavRegexConfiguration.fromConfiguration(objectMapper.readTree("""
        {
            "~group-id": "io\\\\.micronaut.*",
            "~artifact-id": ".*-core",
            "~version": ".*-oracle-[0-9]+"
        }
        """));
        assertFalse(gavRegex.matches("io.micronaut", "micronaut-core", "4.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.micronaut", "micronaut-core", "4.6.4"));
        assertFalse(gavRegex.matches("io.micronaut", "micronaut-abc", "4.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.notmicronaut", "micronaut-core", "4.6.4-oracle-00001"));
        assertTrue(gavRegex.matches("io.notmicronaut", "micronaut-abc", "4.6.4"));
    }

    @Test
    public void testGavRegexConfigCombinedPositiveNegative() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ArtifactGavRegex gavRegex = ArtifactGavRegexConfiguration.fromConfiguration(objectMapper.readTree("""
        {
            "group-id": "io\\\\..*",
            "artifact-id": "micronaut-.*",
            "version": "4\\\\..*",
            "~group-id": "io\\\\.micronaut.*",
            "~artifact-id": ".*-core",
            "~version": ".*-oracle-[0-9]+"
        }
        """));
        assertFalse(gavRegex.matches("io.micronaut", "abc-core", "5.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("abc", "abc-core", "5.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.abc", "abc-core", "5.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.micronaut", "abc", "5.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.micronaut", "micronaut-abc", "5.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.micronaut", "abc-core", "5.6.4"));
        assertFalse(gavRegex.matches("io.micronaut", "abc-core", "4.6.4"));
        assertFalse(gavRegex.matches("io.abc", "micronaut-abc", "5.6.4-oracle-00001"));
        assertFalse(gavRegex.matches("io.abc", "abc-core", "4.6.4"));
        assertFalse(gavRegex.matches("io.micronaut", "micronaut-abc", "4.6.4"));
        assertTrue(gavRegex.matches("io.abc", "micronaut-abc", "4.6.4"));
    }
}
