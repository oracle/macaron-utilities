/* Copyright (c) 2025, 2025, Oracle and/or its affiliates. */
/* Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/. */

package com.oracle.macaron.artifactverifier;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;

public class ArtifactVerifier {
    public ArtifactVerificationResultWithMessage verifyArtifact(Artifact artifact, Supplier<String> artifactFileHashSupplier, ProvenanceLookup provenanceLookup) {
        try {
            List<JsonNode> provenances;
            try {
                provenances = provenanceLookup.lookupProvenance(artifact.groupId(), artifact.artifactId(), artifact.version());
            } catch (FileNotFoundException e) {
                return new ArtifactVerificationResultWithMessage(ArtifactVerificationResult.SKIPPED, "No published provenance found");
            }

            for (JsonNode provenance : provenances) {
                Set<JsonNode> potentialMatchingSubjects = new LinkedHashSet<>();
                JsonNode subjectNode = provenance.get("subject");
                for (JsonNode subject : subjectNode) {
                    PackageURL subjectPurl = new PackageURL(subject.get("uri").asText());
                    if (subjectPurl.getType().equals("maven") && subjectPurl.getNamespace().equals(artifact.groupId()) &&
                            subjectPurl.getName().equals(artifact.artifactId()) &&
                            subjectPurl.getVersion().equals(artifact.version())) {
                        if (!artifact.type().isUnknown() && !Objects.equals(subjectPurl.getQualifiers().get("type"), artifact.type().getValue())) {
                            continue;
                        }
                        if (!artifact.classifier().isUnknown() && !Objects.equals(subjectPurl.getQualifiers().get("classifier"), artifact.classifier().getValue())) {
                            continue;
                        }
                        potentialMatchingSubjects.add(subject);
                    }
                }

                if (!potentialMatchingSubjects.isEmpty()) {
                    for (JsonNode subject : potentialMatchingSubjects) {
                        String subjectSHA256Digest = subject.get("digest").get("sha256").asText();
                        String artifactSHA256Digest = artifactFileHashSupplier.get();
                        if (MessageDigest.isEqual(artifactSHA256Digest.getBytes("UTF-8"), subjectSHA256Digest.getBytes("UTF-8"))) {
                            return new ArtifactVerificationResultWithMessage(ArtifactVerificationResult.VERIFIED, "");
                        }
                    }
                    return new ArtifactVerificationResultWithMessage(ArtifactVerificationResult.FAILED, "Subject hash does not match");
                }

            }
        } catch (MalformedPackageURLException | IOException | URISyntaxException e) {
            return new ArtifactVerificationResultWithMessage(ArtifactVerificationResult.FAILED, e.getMessage());
        }
        return new ArtifactVerificationResultWithMessage(ArtifactVerificationResult.FAILED, "No matching subject found");
    }
}
