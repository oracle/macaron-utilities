/* Copyright (c) 2025, 2025, Oracle and/or its affiliates. */
/* Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/. */

package com.oracle.macaron.artifactverifier;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ArtifactUsageReporterComposite implements ArtifactUsageReporter {

    private final List<ArtifactUsageReporter> reporters;

    public ArtifactUsageReporterComposite(List<ArtifactUsageReporter> reporters) {
        this.reporters = List.copyOf(reporters);
    }

    @Override
    public Optional<ArtifactUsageAlert> checkArtifact(Artifact artifact) {
        Set<String> result = null;
        for (ArtifactUsageReporter reporter : reporters) {
            Optional<ArtifactUsageAlert> subResult = reporter.checkArtifact(artifact);
            if (subResult.isPresent()) {
                if (result == null) {
                    result = new LinkedHashSet<>();
                }
                result.addAll(subResult.get().alternatives());
            }
        }
        if (result != null) {
            return Optional.of(new ArtifactUsageAlert(result));
        }
        return Optional.empty();
    }

    @Override
    public boolean checkArtifactAnyVersion(String groupId, String artifactId) {
        for (ArtifactUsageReporter reporter : reporters) {
            if (reporter.checkArtifactAnyVersion(groupId, artifactId)) {
                return true;
            }
        }
        return false;
    }

}
