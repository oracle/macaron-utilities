/* Copyright (c) 2025, 2025, Oracle and/or its affiliates. */
/* Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/. */

package com.oracle.macaron.artifactverifier;

import java.util.regex.Pattern;

/**
 * Matcher for Maven artifact GAVs (group id, artifact id, version) via regex patterns on individual components.
 * Can check both positive regexes (regex must match) and negative regexes (regex must not match).
 * Regexes for each component are optional, checking of null regexes will be skipped.
 */
public class ArtifactGavRegex {
    private final Pattern groupIdRegex;
    private final Pattern artifactIdRegex;
    private final Pattern versionRegex;
    private final Pattern negativeGroupIdRegex;
    private final Pattern negativeArtifactIdRegex;
    private final Pattern negativeVersionRegex;

    public ArtifactGavRegex(Pattern groupIdRegex, Pattern artifactIdRegex, Pattern versionRegex,
            Pattern negativeGroupIdRegex, Pattern negativeArtifactIdRegex, Pattern negativeVersionRegex) {
        this.groupIdRegex = groupIdRegex;
        this.artifactIdRegex = artifactIdRegex;
        this.versionRegex = versionRegex;
        this.negativeGroupIdRegex = negativeGroupIdRegex;
        this.negativeArtifactIdRegex = negativeArtifactIdRegex;
        this.negativeVersionRegex = negativeVersionRegex;
    }

    public boolean matches(String groupId, String artifactId, String version) {
        if (groupIdRegex != null && !groupIdRegex.matcher(groupId).matches()) {
            return false;
        }

        if (negativeGroupIdRegex != null && negativeGroupIdRegex.matcher(groupId).matches()) {
            return false;
        }

        if (artifactIdRegex != null && !artifactIdRegex.matcher(artifactId).matches()) {
            return false;
        }

        if (negativeArtifactIdRegex != null && negativeArtifactIdRegex.matcher(artifactId).matches()) {
            return false;
        }

        if (versionRegex != null && !versionRegex.matcher(version).matches()) {
            return false;
        }

        if (negativeVersionRegex != null && negativeVersionRegex.matcher(version).matches()) {
            return false;
        }

        return true;
    }

    public boolean matchesGroupAndArtifact(String groupId, String artifactId) {
        if (groupIdRegex != null && !groupIdRegex.matcher(groupId).matches()) {
            return false;
        }

        if (negativeGroupIdRegex != null && negativeGroupIdRegex.matcher(groupId).matches()) {
            return false;
        }

        if (artifactIdRegex != null && !artifactIdRegex.matcher(artifactId).matches()) {
            return false;
        }

        if (negativeArtifactIdRegex != null && negativeArtifactIdRegex.matcher(artifactId).matches()) {
            return false;
        }

        return true;
    }

}
