/* Copyright (c) 2025, 2025, Oracle and/or its affiliates. */
/* Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/. */

package com.oracle.macaron.artifactverifier;

import java.util.Optional;

public interface ArtifactUsageReporter {

    /**
     * Checks whether usage of the given artifact should produce an alert.
     * If so, returns an alert that optionally contains a list of alternative artifact suggestions.
     * Otherwise, returns empty.
     */
    public Optional<ArtifactUsageAlert> checkArtifact(Artifact artifact);

    /**
     * Checks whether usage of the artifact with the given group id and artifact id should produce an alert (without checking version).
     */
    public boolean checkArtifactAnyVersion(String groupId, String artifactId);
}
