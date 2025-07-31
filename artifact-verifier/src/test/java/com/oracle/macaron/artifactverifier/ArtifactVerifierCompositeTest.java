/* Copyright (c) 2025, 2025, Oracle and/or its affiliates. */
/* Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/. */

package com.oracle.macaron.artifactverifier;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class ArtifactVerifierCompositeTest {

    @Test
    public void testArtifactVerifierComposite() throws Exception {
        ProvenanceLookup repoSkippedA = new ProvenanceLookup("https://example.com/repoSkippedA");
        ProvenanceLookup repoSkippedB = new ProvenanceLookup("https://example.com/repoSkippedB");
        ProvenanceLookup repoVerifiedA = new ProvenanceLookup("https://example.com/repoVerifiedA");
        ProvenanceLookup repoVerifiedB = new ProvenanceLookup("https://example.com/repoVerifiedB");
        ProvenanceLookup repoFailedA = new ProvenanceLookup("https://example.com/repoFailedA");
        ProvenanceLookup repoFailedB = new ProvenanceLookup("https://example.com/repoFailedB");


        Artifact artifact = new Artifact("somegroup", "someartifact", "1.0", NullableOrUnknown.getUnknown(), NullableOrUnknown.getUnknown());
        String hash = "8e15816427afc0f42388dda52e998a9aee6d7168eccbf7c22ffb6c1de729bb84";

        Supplier<String> hashSupplier = () -> hash;

        ArtifactVerifier artifactVerifier = new MockArtifactVerifier(
            Map.of(
                artifact, Map.of(
                    hash, Map.of(
                        repoSkippedA, new ArtifactVerificationResultWithMessage(ArtifactVerificationResult.SKIPPED, ""),
                        repoSkippedB, new ArtifactVerificationResultWithMessage(ArtifactVerificationResult.SKIPPED, ""),
                        repoVerifiedA, new ArtifactVerificationResultWithMessage(ArtifactVerificationResult.VERIFIED, ""),
                        repoVerifiedB, new ArtifactVerificationResultWithMessage(ArtifactVerificationResult.VERIFIED, ""),
                        repoFailedA, new ArtifactVerificationResultWithMessage(ArtifactVerificationResult.FAILED, ""),
                        repoFailedB, new ArtifactVerificationResultWithMessage(ArtifactVerificationResult.FAILED, "")
                    )
                )
            )
        );


        assertEquals(ArtifactVerificationResult.SKIPPED,
            new ArtifactVerifierComposite(List.of(repoSkippedA), ArtifactVerifierMode.NO_REQUIRE_VERIFY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.SKIPPED,
            new ArtifactVerifierComposite(List.of(repoSkippedA), ArtifactVerifierMode.REQUIRE_VERIFY_IF_ATTESTED_ANY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.SKIPPED,
            new ArtifactVerifierComposite(List.of(repoSkippedA), ArtifactVerifierMode.REQUIRE_VERIFY_IF_ATTESTED_FIRST, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.FAILED,
            new ArtifactVerifierComposite(List.of(repoSkippedA), ArtifactVerifierMode.REQUIRE_VERIFY_ANY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.FAILED,
            new ArtifactVerifierComposite(List.of(repoSkippedA), ArtifactVerifierMode.REQUIRE_VERIFY_FIRST, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());


        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoVerifiedA), ArtifactVerifierMode.NO_REQUIRE_VERIFY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoVerifiedA), ArtifactVerifierMode.REQUIRE_VERIFY_IF_ATTESTED_ANY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoVerifiedA), ArtifactVerifierMode.REQUIRE_VERIFY_IF_ATTESTED_FIRST, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoVerifiedA), ArtifactVerifierMode.REQUIRE_VERIFY_ANY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoVerifiedA), ArtifactVerifierMode.REQUIRE_VERIFY_FIRST, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());


        assertEquals(ArtifactVerificationResult.SKIPPED,
            new ArtifactVerifierComposite(List.of(repoFailedA), ArtifactVerifierMode.NO_REQUIRE_VERIFY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.FAILED,
            new ArtifactVerifierComposite(List.of(repoFailedA), ArtifactVerifierMode.REQUIRE_VERIFY_IF_ATTESTED_ANY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.FAILED,
            new ArtifactVerifierComposite(List.of(repoFailedA), ArtifactVerifierMode.REQUIRE_VERIFY_IF_ATTESTED_FIRST, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.FAILED,
            new ArtifactVerifierComposite(List.of(repoFailedA), ArtifactVerifierMode.REQUIRE_VERIFY_ANY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.FAILED,
            new ArtifactVerifierComposite(List.of(repoFailedA), ArtifactVerifierMode.REQUIRE_VERIFY_FIRST, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());


        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoSkippedA, repoVerifiedA), ArtifactVerifierMode.NO_REQUIRE_VERIFY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoSkippedA, repoVerifiedA), ArtifactVerifierMode.REQUIRE_VERIFY_IF_ATTESTED_ANY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoSkippedA, repoVerifiedA), ArtifactVerifierMode.REQUIRE_VERIFY_IF_ATTESTED_FIRST, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoSkippedA, repoVerifiedA), ArtifactVerifierMode.REQUIRE_VERIFY_ANY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoSkippedA, repoVerifiedA), ArtifactVerifierMode.REQUIRE_VERIFY_FIRST, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());


        assertEquals(ArtifactVerificationResult.SKIPPED,
            new ArtifactVerifierComposite(List.of(repoSkippedA, repoFailedA), ArtifactVerifierMode.NO_REQUIRE_VERIFY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.FAILED,
            new ArtifactVerifierComposite(List.of(repoSkippedA, repoFailedA), ArtifactVerifierMode.REQUIRE_VERIFY_IF_ATTESTED_ANY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.FAILED,
            new ArtifactVerifierComposite(List.of(repoSkippedA, repoFailedA), ArtifactVerifierMode.REQUIRE_VERIFY_IF_ATTESTED_FIRST, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.FAILED,
            new ArtifactVerifierComposite(List.of(repoSkippedA, repoFailedA), ArtifactVerifierMode.REQUIRE_VERIFY_ANY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.FAILED,
            new ArtifactVerifierComposite(List.of(repoSkippedA, repoFailedA), ArtifactVerifierMode.REQUIRE_VERIFY_FIRST, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());


        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoVerifiedA, repoFailedA), ArtifactVerifierMode.NO_REQUIRE_VERIFY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoVerifiedA, repoFailedA), ArtifactVerifierMode.REQUIRE_VERIFY_IF_ATTESTED_ANY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoVerifiedA, repoFailedA), ArtifactVerifierMode.REQUIRE_VERIFY_IF_ATTESTED_FIRST, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoVerifiedA, repoFailedA), ArtifactVerifierMode.REQUIRE_VERIFY_ANY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoVerifiedA, repoFailedA), ArtifactVerifierMode.REQUIRE_VERIFY_FIRST, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());


        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoFailedA, repoVerifiedA), ArtifactVerifierMode.NO_REQUIRE_VERIFY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoFailedA, repoVerifiedA), ArtifactVerifierMode.REQUIRE_VERIFY_IF_ATTESTED_ANY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.FAILED,
            new ArtifactVerifierComposite(List.of(repoFailedA, repoVerifiedA), ArtifactVerifierMode.REQUIRE_VERIFY_IF_ATTESTED_FIRST, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoFailedA, repoVerifiedA), ArtifactVerifierMode.REQUIRE_VERIFY_ANY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.FAILED,
            new ArtifactVerifierComposite(List.of(repoFailedA, repoVerifiedA), ArtifactVerifierMode.REQUIRE_VERIFY_FIRST, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());


        assertEquals(ArtifactVerificationResult.SKIPPED,
            new ArtifactVerifierComposite(List.of(repoFailedA, repoFailedB), ArtifactVerifierMode.NO_REQUIRE_VERIFY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.FAILED,
            new ArtifactVerifierComposite(List.of(repoFailedA, repoFailedB), ArtifactVerifierMode.REQUIRE_VERIFY_IF_ATTESTED_ANY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.FAILED,
            new ArtifactVerifierComposite(List.of(repoFailedA, repoFailedB), ArtifactVerifierMode.REQUIRE_VERIFY_IF_ATTESTED_FIRST, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.FAILED,
            new ArtifactVerifierComposite(List.of(repoFailedA, repoFailedB), ArtifactVerifierMode.REQUIRE_VERIFY_ANY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.FAILED,
            new ArtifactVerifierComposite(List.of(repoFailedA, repoFailedB), ArtifactVerifierMode.REQUIRE_VERIFY_FIRST, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());


        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoVerifiedA, repoVerifiedB), ArtifactVerifierMode.NO_REQUIRE_VERIFY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoVerifiedA, repoVerifiedB), ArtifactVerifierMode.REQUIRE_VERIFY_IF_ATTESTED_ANY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoVerifiedA, repoVerifiedB), ArtifactVerifierMode.REQUIRE_VERIFY_IF_ATTESTED_FIRST, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoVerifiedA, repoVerifiedB), ArtifactVerifierMode.REQUIRE_VERIFY_ANY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoVerifiedA, repoVerifiedB), ArtifactVerifierMode.REQUIRE_VERIFY_FIRST, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());


        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoVerifiedA, repoVerifiedB), ArtifactVerifierMode.NO_REQUIRE_VERIFY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoVerifiedA, repoVerifiedB), ArtifactVerifierMode.REQUIRE_VERIFY_IF_ATTESTED_ANY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoVerifiedA, repoVerifiedB), ArtifactVerifierMode.REQUIRE_VERIFY_IF_ATTESTED_FIRST, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoVerifiedA, repoVerifiedB), ArtifactVerifierMode.REQUIRE_VERIFY_ANY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoVerifiedA, repoVerifiedB), ArtifactVerifierMode.REQUIRE_VERIFY_FIRST, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());


        assertEquals(ArtifactVerificationResult.SKIPPED,
            new ArtifactVerifierComposite(List.of(repoSkippedA, repoSkippedB), ArtifactVerifierMode.NO_REQUIRE_VERIFY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.SKIPPED,
            new ArtifactVerifierComposite(List.of(repoSkippedA, repoSkippedB), ArtifactVerifierMode.REQUIRE_VERIFY_IF_ATTESTED_ANY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.SKIPPED,
            new ArtifactVerifierComposite(List.of(repoSkippedA, repoSkippedB), ArtifactVerifierMode.REQUIRE_VERIFY_IF_ATTESTED_FIRST, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.FAILED,
            new ArtifactVerifierComposite(List.of(repoSkippedA, repoSkippedB), ArtifactVerifierMode.REQUIRE_VERIFY_ANY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.FAILED,
            new ArtifactVerifierComposite(List.of(repoSkippedA, repoSkippedB), ArtifactVerifierMode.REQUIRE_VERIFY_FIRST, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());


        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoSkippedA, repoVerifiedA, repoFailedA), ArtifactVerifierMode.NO_REQUIRE_VERIFY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoSkippedA, repoVerifiedA, repoFailedA), ArtifactVerifierMode.REQUIRE_VERIFY_IF_ATTESTED_ANY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoSkippedA, repoVerifiedA, repoFailedA), ArtifactVerifierMode.REQUIRE_VERIFY_IF_ATTESTED_FIRST, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoSkippedA, repoVerifiedA, repoFailedA), ArtifactVerifierMode.REQUIRE_VERIFY_ANY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoSkippedA, repoVerifiedA, repoFailedA), ArtifactVerifierMode.REQUIRE_VERIFY_FIRST, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());


        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoSkippedA, repoFailedA, repoVerifiedA), ArtifactVerifierMode.NO_REQUIRE_VERIFY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoSkippedA, repoFailedA, repoVerifiedA), ArtifactVerifierMode.REQUIRE_VERIFY_IF_ATTESTED_ANY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.FAILED,
            new ArtifactVerifierComposite(List.of(repoSkippedA, repoFailedA, repoVerifiedA), ArtifactVerifierMode.REQUIRE_VERIFY_IF_ATTESTED_FIRST, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.VERIFIED,
            new ArtifactVerifierComposite(List.of(repoSkippedA, repoFailedA, repoVerifiedA), ArtifactVerifierMode.REQUIRE_VERIFY_ANY, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());

        assertEquals(ArtifactVerificationResult.FAILED,
            new ArtifactVerifierComposite(List.of(repoSkippedA, repoFailedA, repoVerifiedA), ArtifactVerifierMode.REQUIRE_VERIFY_FIRST, artifactVerifier).verifyArtifact(artifact, hashSupplier).result());


    }
}
