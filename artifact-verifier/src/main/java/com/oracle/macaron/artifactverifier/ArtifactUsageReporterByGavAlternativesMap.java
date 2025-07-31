/* Copyright (c) 2025, 2025, Oracle and/or its affiliates. */
/* Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/. */

package com.oracle.macaron.artifactverifier;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ArtifactUsageReporterByGavAlternativesMap implements ArtifactUsageReporter {

    private final Map<String, Set<String>> gavAlternativesMap;
    private final Set<String> gaSet;

    public ArtifactUsageReporterByGavAlternativesMap(Map<String, Set<String>> gavAlternativesMap) {
        this.gavAlternativesMap = Map.copyOf(gavAlternativesMap);
        this.gaSet = new LinkedHashSet<>();
        for (Map.Entry<String, Set<String>> gavEntry : gavAlternativesMap.entrySet()) {
            String[] split = gavEntry.getKey().split(":");
            if (split.length >= 2) {
                this.gaSet.add(split[0] + ":" + split[1]);
            }
        }
    }

    @Override
    public Optional<ArtifactUsageAlert> checkArtifact(Artifact artifact) {
        String gav = artifact.groupId() + ":" + artifact.artifactId() + ":" + artifact.version();
        Set<String> alternativeGavs = gavAlternativesMap.get(gav);
        if (alternativeGavs != null) {
            return Optional.of(new ArtifactUsageAlert(alternativeGavs));
        }
        return Optional.empty();
    }

    @Override
    public boolean checkArtifactAnyVersion(String groupId, String artifactId) {
        String ga = groupId + ":" + artifactId;
        return gaSet.contains(ga);
    }

}
