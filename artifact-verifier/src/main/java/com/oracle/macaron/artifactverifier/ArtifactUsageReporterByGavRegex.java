/* Copyright (c) 2025, 2025, Oracle and/or its affiliates. */
/* Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/. */

package com.oracle.macaron.artifactverifier;

import java.util.Optional;
import java.util.Set;

public class ArtifactUsageReporterByGavRegex implements ArtifactUsageReporter {

    private final ArtifactGavRegex gavRegex;

    public ArtifactUsageReporterByGavRegex(ArtifactGavRegex gavRegex) {
        this.gavRegex = gavRegex;
    }

    @Override
    public Optional<ArtifactUsageAlert> checkArtifact(Artifact artifact) {
        if (gavRegex.matches(artifact.groupId(), artifact.artifactId(), artifact.version())) {
            return Optional.of(new ArtifactUsageAlert(Set.of()));
        }
        return Optional.empty();
    }

    @Override
    public boolean checkArtifactAnyVersion(String groupId, String artifactId) {
        return gavRegex.matchesGroupAndArtifact(groupId, artifactId);
    }
}
