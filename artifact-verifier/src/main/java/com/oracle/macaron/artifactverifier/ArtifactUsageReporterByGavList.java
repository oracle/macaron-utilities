/* Copyright (c) 2025, 2025, Oracle and/or its affiliates. */
/* Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/. */

package com.oracle.macaron.artifactverifier;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public class ArtifactUsageReporterByGavList implements ArtifactUsageReporter {

    private final Set<String> gavSet;
    private final Set<String> gaSet;

    public ArtifactUsageReporterByGavList(Set<String> gavSet) {
        this.gavSet = Set.copyOf(gavSet);
        this.gaSet = new LinkedHashSet<>();
        for (String gav : gavSet) {
            String[] split = gav.split(":");
            if (split.length >= 2) {
                this.gaSet.add(split[0] + ":" + split[1]);
            }
        }
    }

    @Override
    public Optional<ArtifactUsageAlert> checkArtifact(Artifact artifact) {
        String gav = artifact.groupId() + ":" + artifact.artifactId() + ":" + artifact.version();
        if (gavSet.contains(gav)) {
            return Optional.of(new ArtifactUsageAlert(Set.of()));
        }
        return Optional.empty();
    }

    @Override
    public boolean checkArtifactAnyVersion(String groupId, String artifactId) {
        String ga = groupId + ":" + artifactId;
        return gaSet.contains(ga);
    }
}
