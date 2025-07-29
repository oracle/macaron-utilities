/* Copyright (c) 2025, 2025, Oracle and/or its affiliates. */
/* Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/. */

package com.oracle.macaron.artifactverifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ArtifactVerifierComposite {

    private List<ProvenanceLookup> verificationRepos;
    private ArtifactVerifierMode mode;
    private ArtifactVerifier artifactVerifier;

    public ArtifactVerifierComposite(List<ProvenanceLookup> verificationRepos, ArtifactVerifierMode mode) {
        this.verificationRepos = List.copyOf(verificationRepos);
        this.mode = mode;
        this.artifactVerifier = new ArtifactVerifier();
    }

    public ArtifactVerifierComposite(List<String> verificationRepoBaseUrls, ProvenanceLookupFactory provenanceLookupFactory, ArtifactVerifierMode mode) {
        this(verificationRepoBaseUrls.stream().map(baseUrl -> provenanceLookupFactory.getProvenanceLookup(baseUrl)).collect(Collectors.toCollection(ArrayList::new)), mode);
    }

    public ArtifactVerifierComposite(List<ProvenanceLookup> verificationRepos, ArtifactVerifierMode mode, ArtifactVerifier artifactVerifier) {
        this.verificationRepos = List.copyOf(verificationRepos);
        this.mode = mode;
        this.artifactVerifier = artifactVerifier;
    }

    public ArtifactVerifierComposite(List<String> verificationRepoBaseUrls, ProvenanceLookupFactory provenanceLookupFactory, ArtifactVerifierMode mode, ArtifactVerifier artifactVerifier) {
        this(verificationRepoBaseUrls.stream().map(baseUrl -> provenanceLookupFactory.getProvenanceLookup(baseUrl)).collect(Collectors.toCollection(ArrayList::new)), mode, artifactVerifier);
    }

    public ArtifactVerificationResultWithMessage verifyArtifact(Artifact artifact, Supplier<String> artifactFileHashSupplier) {
        List<String> failingRepos = new ArrayList<>();
        for (ProvenanceLookup repo : verificationRepos) {
            ArtifactVerificationResultWithMessage subResult = artifactVerifier.verifyArtifact(artifact, artifactFileHashSupplier, repo);
            switch (subResult.result()) {
                case VERIFIED -> {
                    return subResult;
                }
                case FAILED -> {
                    if (mode == ArtifactVerifierMode.REQUIRE_VERIFY_IF_ATTESTED_FIRST || mode == ArtifactVerifierMode.REQUIRE_VERIFY_FIRST) {
                        return subResult;
                    }
                    failingRepos.add(repo.getMavenRepoBaseUrl());
                }
                default -> {}
            }
        }

        if (mode == ArtifactVerifierMode.REQUIRE_VERIFY_ANY || mode == ArtifactVerifierMode.REQUIRE_VERIFY_FIRST) {
            return new ArtifactVerificationResultWithMessage(ArtifactVerificationResult.FAILED, "No attestation found");
        }

        if (!failingRepos.isEmpty()) {
            if (mode == ArtifactVerifierMode.REQUIRE_VERIFY_IF_ATTESTED_ANY) {
                return new ArtifactVerificationResultWithMessage(ArtifactVerificationResult.FAILED, "Non-matching artifact found on " + failingRepos);
            }
            return new ArtifactVerificationResultWithMessage(ArtifactVerificationResult.SKIPPED, "Non-matching artifact found on " + failingRepos);
        }

        return new ArtifactVerificationResultWithMessage(ArtifactVerificationResult.SKIPPED, "No attestation found");

    }
}
