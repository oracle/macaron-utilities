/* Copyright (c) 2025, 2025, Oracle and/or its affiliates. */
/* Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/. */

package com.oracle.macaron.artifactverifier.maven;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.maven.enforcer.rule.api.AbstractEnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerLogger;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.project.MavenProject;

import com.oracle.macaron.artifactverifier.ArtifactUsageAlert;
import com.oracle.macaron.artifactverifier.ArtifactUsageReporter;
import com.oracle.macaron.artifactverifier.ArtifactUsageReporterConfiguration;
import com.oracle.macaron.artifactverifier.ArtifactVerificationResultWithMessage;
import com.oracle.macaron.artifactverifier.ArtifactVerifierComposite;
import com.oracle.macaron.artifactverifier.ArtifactVerifierMode;
import com.oracle.macaron.artifactverifier.FileHashSupplier;
import com.oracle.macaron.artifactverifier.NullableOrUnknown;
import com.oracle.macaron.artifactverifier.ParseException;
import com.oracle.macaron.artifactverifier.ProvenanceLookupFactory;
import com.oracle.macaron.artifactverifier.maven.VerifyDependencies.ArtifactUsageAlertSeverityConfiguration;

@Named("verifyDependencies")
public class VerifyDependencies extends AbstractEnforcerRule {

    @Inject
    private MavenProject project;

    private List<String> verificationRepos = null;
    private ArtifactVerifierMode verifierMode = null;

    private String artifactUsageAlertConfigFilePath = null;
    private String artifactUsageAlertConfigUrl = null;
    private ArtifactUsageAlertSeverityConfiguration artifactUsageAlertSeverity = ArtifactUsageAlertSeverityConfiguration.WARN_EXACT_VERSION;

    public static enum ArtifactUsageAlertSeverityConfiguration {
        NONE,
        WARN_EXACT_VERSION,
        ERROR_EXACT_VERSION,
        WARN_ANY_VERSION,
        WARN_ANY_VERSION_ERROR_EXACT_VERSION,
        ERROR_ANY_VERSION;

        public boolean warnsOnExact() {
            return this == WARN_EXACT_VERSION || this == WARN_ANY_VERSION;
        }

        public boolean errorsOnExact() {
            return this == ERROR_EXACT_VERSION || this == ERROR_ANY_VERSION || this == WARN_ANY_VERSION_ERROR_EXACT_VERSION;
        }

        public boolean warnsOnInexact() {
            return this == WARN_ANY_VERSION || this == WARN_ANY_VERSION_ERROR_EXACT_VERSION;
        }

        public boolean errorsOnInexact() {
            return this == ERROR_ANY_VERSION;
        }

        public boolean hasInexact() {
            return this == WARN_ANY_VERSION || this == ERROR_ANY_VERSION || this == WARN_ANY_VERSION_ERROR_EXACT_VERSION;
        }

    }


    @Override
    public void execute() throws EnforcerRuleException {

        EnforcerLogger logger = getLog();

        ProvenanceLookupFactory provenanceLookupFactory = new ProvenanceLookupFactory();

        ArtifactVerifierComposite verifier = null;

        if (verificationRepos != null && !verificationRepos.isEmpty()) {
            verifier = new ArtifactVerifierComposite(verificationRepos, provenanceLookupFactory, verifierMode != null ? verifierMode : ArtifactVerifierMode.REQUIRE_VERIFY_IF_ATTESTED_ANY);
        }

        if (verifier != null) {
            Set<String> verified = new LinkedHashSet<>();
            Set<String> verificationSkipped = new LinkedHashSet<>();
            Set<String> verificationFailed = new LinkedHashSet<>();
            for (org.apache.maven.artifact.Artifact artifact : project.getArtifacts()) {
                if (artifact.getFile() == null) {
                    continue;
                }

                String gav = artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion();

                com.oracle.macaron.artifactverifier.Artifact artifactDetails = new com.oracle.macaron.artifactverifier.Artifact(
                    artifact.getGroupId(),
                    artifact.getArtifactId(),
                    artifact.getVersion(),
                    new NullableOrUnknown<>(artifact.getType()),
                    new NullableOrUnknown<>(artifact.getClassifier()));
                ArtifactVerificationResultWithMessage verificationResult = verifier.verifyArtifact(artifactDetails, new FileHashSupplier(artifact.getFile().toPath()));
                switch (verificationResult.result()) {
                    case VERIFIED:
                        verified.add(gav);
                        break;
                    case SKIPPED:
                        verificationSkipped.add(gav);
                        break;
                    case FAILED:
                        logger.error("    " + verificationResult.message());
                        logger.error("    Failed to verify VSA for " + artifact.getId());
                        verificationFailed.add(gav);
                        break;
                }
            }

            if (!verified.isEmpty()) {
                logger.info("Verified: " + verified);
            }
            if (!verificationSkipped.isEmpty()) {
                logger.info("Verification Skipped: " + verificationSkipped);
            }
            if (!verificationFailed.isEmpty()) {
                logger.error("Verification Failed: " + verificationFailed);
                throw new EnforcerRuleException("Couldn't verify dependencies");
            }
        } else {
            logger.info("No verifier configured, skipping verification");
        }

        if (artifactUsageAlertSeverity != ArtifactUsageAlertSeverityConfiguration.NONE) {

            ArtifactUsageReporter usageReporter = null;
            if (artifactUsageAlertConfigUrl != null) {
                try {
                    URI uri = new URI(artifactUsageAlertConfigUrl);
                    usageReporter = ArtifactUsageReporterConfiguration.fromConfiguration(uri.toURL());
                } catch (ParseException | IOException | URISyntaxException e) {
                    throw new EnforcerRuleException("Cannot read artifact usage alert config: " + e.getMessage(), e);
                }
            } else if (artifactUsageAlertConfigFilePath != null) {
                try {
                    Path path = Path.of(artifactUsageAlertConfigFilePath);
                    usageReporter = ArtifactUsageReporterConfiguration.fromConfiguration(path);
                } catch (ParseException | IOException e) {
                    throw new EnforcerRuleException("Cannot read artifact usage alert config: " + e.getMessage(), e);
                }
            }

            if (usageReporter != null) {
                boolean usageAlertErrorRaised = false;
                for (org.apache.maven.artifact.Artifact artifact : project.getArtifacts()) {
                    String gav = artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion();

                    com.oracle.macaron.artifactverifier.Artifact artifactDetails = new com.oracle.macaron.artifactverifier.Artifact(
                        artifact.getGroupId(),
                        artifact.getArtifactId(),
                        artifact.getVersion(),
                        new NullableOrUnknown<>(artifact.getType()),
                        new NullableOrUnknown<>(artifact.getClassifier()));

                    Optional<ArtifactUsageAlert> alert = usageReporter.checkArtifact(artifactDetails);
                    if (alert.isPresent()) {
                        if (artifactUsageAlertSeverity.errorsOnExact()) {
                            logger.error(getExactReportMessage(true, gav, alert.get().alternatives()));
                            usageAlertErrorRaised = true;
                        } else {
                            logger.warn(getExactReportMessage(false, gav, alert.get().alternatives()));
                        }
                    } else if (artifactUsageAlertSeverity.hasInexact() && usageReporter.checkArtifactAnyVersion(artifact.getGroupId(), artifact.getArtifactId())) {
                        if (artifactUsageAlertSeverity.errorsOnInexact()) {
                            logger.error(getInexactReportMessage(true, gav));
                            usageAlertErrorRaised = true;
                        } else {
                            logger.warn(getInexactReportMessage(false, gav));
                        }
                    }
                }

                if (usageAlertErrorRaised) {
                    throw new EnforcerRuleException("Using dependencies with configured usage alert");
                }
            }
        }
    }

    private String getExactReportMessage(boolean error, String gav, Set<String> correspondingGavs) {
        return (error ? "Error: " : "Warning: ") + "using dependency " + gav + " which is configured for usage alert" + (!correspondingGavs.isEmpty() ? (" (suggested alternatives: " + correspondingGavs + ")") : "");
    }

    private String getInexactReportMessage(boolean error, String gav) {
        return (error ? "Error: " : "Warning: ") + "using dependency " + gav + " for which any version is configured for usage alert";
    }

    @Override
    public String toString() {
        return String.format("VerifyDependencies");
    }
}
