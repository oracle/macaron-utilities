/* Copyright (c) 2025, 2025, Oracle and/or its affiliates. */
/* Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/. */

package com.oracle.macaron.artifactverifier;

import java.util.Map;
import java.util.function.Supplier;

public class MockArtifactVerifier extends ArtifactVerifier {

    Map<Artifact, Map<String, Map<ProvenanceLookup, ArtifactVerificationResultWithMessage>>> predefinedResults;

    public MockArtifactVerifier(Map<Artifact, Map<String, Map<ProvenanceLookup, ArtifactVerificationResultWithMessage>>> predefinedResults) {
        this.predefinedResults = predefinedResults;
    }

    @Override
    public ArtifactVerificationResultWithMessage verifyArtifact(Artifact artifact, Supplier<String> artifactFileHashSupplier, ProvenanceLookup provenanceLookup) {
        String hash = artifactFileHashSupplier.get();
        Map<String, Map<ProvenanceLookup, ArtifactVerificationResultWithMessage>> predefinedResultsForArtifact = predefinedResults.get(artifact);
        if (predefinedResultsForArtifact == null) {
            throw new RuntimeException("No predefined result for " + artifact);
        }

        Map<ProvenanceLookup, ArtifactVerificationResultWithMessage> predefinedResultsForArtifactHash = predefinedResultsForArtifact.get(hash);
        if (predefinedResultsForArtifactHash == null) {
            throw new RuntimeException("No predefined result for " + artifact + " " + hash);
        }

        ArtifactVerificationResultWithMessage result = predefinedResultsForArtifactHash.get(provenanceLookup);
        if (result == null) {
            throw new RuntimeException("No predefined result for " + artifact + " " + hash + " " + provenanceLookup.getMavenRepoBaseUrl());
        }

        return result;

    }


}
