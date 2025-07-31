/* Copyright (c) 2025, 2025, Oracle and/or its affiliates. */
/* Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/. */

package com.oracle.macaron.artifactverifier.gradle;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.logging.Logger;
import org.gradle.internal.component.external.model.DefaultModuleComponentIdentifier;

import com.oracle.macaron.artifactverifier.Artifact;
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


public class VerifyDependenciesPlugin implements Plugin<Project> {

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

    public static class VerifyDependenciesPluginExtension {
        private List<String> verificationRepos;
        private ArtifactVerifierMode verifierMode;

        private String artifactUsageAlertConfigFilePath;
        private String artifactUsageAlertConfigUrl;
        private ArtifactUsageAlertSeverityConfiguration artifactUsageAlertSeverity = ArtifactUsageAlertSeverityConfiguration.WARN_EXACT_VERSION;

        public void setArtifactUsageAlertSeverity(ArtifactUsageAlertSeverityConfiguration mode) {
            this.artifactUsageAlertSeverity = mode;
        }

        public ArtifactUsageAlertSeverityConfiguration getArtifactUsageAlertSeverity() {
            return this.artifactUsageAlertSeverity;
        }

        public List<String> getVerificationRepos() {
            return verificationRepos;
        }

        public void setVerificationRepos(List<String> verificationRepos) {
            this.verificationRepos = List.copyOf(verificationRepos);
        }

        public ArtifactVerifierMode getVerifierMode() {
            return verifierMode;
        }

        public void setVerifierMode(ArtifactVerifierMode verifierMode) {
            this.verifierMode = verifierMode;
        }

        public String getArtifactUsageAlertConfigFilePath() {
            return artifactUsageAlertConfigFilePath;
        }

        public void setArtifactUsageAlertConfigFilePath(String artifactUsageAlertConfigFilePath) {
            this.artifactUsageAlertConfigFilePath = artifactUsageAlertConfigFilePath;
        }

        public String getArtifactUsageAlertConfigUrl() {
            return artifactUsageAlertConfigUrl;
        }

        public void setArtifactUsageAlertConfigUrl(String artifactUsageAlertConfigUrl) {
            this.artifactUsageAlertConfigUrl = artifactUsageAlertConfigUrl;
        }
    }

    @Override
    public void apply(Project project) {
        VerifyDependenciesPluginExtension extension = project.getExtensions().create("verifydependencies", VerifyDependenciesPluginExtension.class);

        project.afterEvaluate(p -> {
            Logger logger = p.getLogger();

            ProvenanceLookupFactory provenanceLookupFactory = new ProvenanceLookupFactory();

            ArtifactVerifierComposite verifier = null;

            if (extension.getVerificationRepos() != null && !extension.getVerificationRepos().isEmpty()) {
                verifier = new ArtifactVerifierComposite(extension.getVerificationRepos(), provenanceLookupFactory, extension.getVerifierMode() != null ? extension.getVerifierMode() : ArtifactVerifierMode.REQUIRE_VERIFY_IF_ATTESTED_ANY);
            }

            if (verifier != null) {
                Set<String> verified = new LinkedHashSet<>();
                Set<String> verificationSkipped = new LinkedHashSet<>();
                Set<String> verificationFailed = new LinkedHashSet<>();

                for (Configuration config : p.getConfigurations()) {
                    if (config.isCanBeResolved()) {
                        for (ResolvedArtifactResult artifact : config.getIncoming().getArtifacts()) {
                            if (artifact.getId().getComponentIdentifier() instanceof DefaultModuleComponentIdentifier id) {
                                String gav = id.getGroup() + ":" + id.getModule() + ":" + id.getVersion();

                                Artifact artifactDetails = new com.oracle.macaron.artifactverifier.Artifact(
                                    id.getGroup(),
                                    id.getModule(),
                                    id.getVersion(),
                                    NullableOrUnknown.getUnknown(),
                                    NullableOrUnknown.getUnknown());

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
                        }
                    }
                }

                if (!verified.isEmpty()) {
                    logger.lifecycle("Verified: " + verified);
                }
                if (!verificationSkipped.isEmpty()) {
                    logger.lifecycle("Verification Skipped: " + verificationSkipped);
                }
                if (!verificationFailed.isEmpty()) {
                    logger.lifecycle("Verification Failed: " + verificationFailed);
                    throw new GradleException("Couldn't verify dependencies");
                }
            }

            if (extension.getArtifactUsageAlertSeverity() != ArtifactUsageAlertSeverityConfiguration.NONE) {

                ArtifactUsageReporter usageReporter = null;
                if (extension.getArtifactUsageAlertConfigUrl() != null) {
                    try {
                        URI uri = new URI(extension.getArtifactUsageAlertConfigUrl());
                        usageReporter = ArtifactUsageReporterConfiguration.fromConfiguration(uri.toURL());
                    } catch (ParseException | IOException | URISyntaxException e) {
                        throw new GradleException("Cannot read artifact usage alert config: " + e.getMessage(), e);
                    }
                } else if (extension.getArtifactUsageAlertConfigFilePath() != null) {
                    try {
                        Path path = Path.of(extension.getArtifactUsageAlertConfigFilePath());
                        usageReporter = ArtifactUsageReporterConfiguration.fromConfiguration(path);
                    } catch (ParseException | IOException e) {
                        throw new GradleException("Cannot read artifact usage alert config: " + e.getMessage(), e);
                    }
                }

                if (usageReporter != null) {
                    Set<Artifact> seenArtifacts = new HashSet<>();
                    boolean usageAlertErrorRaised = false;
                    for (Configuration config : p.getConfigurations()) {
                        if (config.isCanBeResolved()) {
                            for (ResolvedArtifactResult artifact : config.getIncoming().getArtifacts()) {
                                if (artifact.getId().getComponentIdentifier() instanceof DefaultModuleComponentIdentifier id) {
                                    String gav = id.getGroup() + ":" + id.getModule() + ":" + id.getVersion();

                                    Artifact artifactDetails = new com.oracle.macaron.artifactverifier.Artifact(
                                        id.getGroup(),
                                        id.getModule(),
                                        id.getVersion(),
                                        NullableOrUnknown.getUnknown(),
                                        NullableOrUnknown.getUnknown());

                                    if (seenArtifacts.contains(artifactDetails)) {
                                        continue;
                                    }

                                    seenArtifacts.add(artifactDetails);

                                    Optional<ArtifactUsageAlert> alert = usageReporter.checkArtifact(artifactDetails);
                                    if (alert.isPresent()) {
                                        if (extension.getArtifactUsageAlertSeverity().errorsOnExact()) {
                                            logger.error(getExactReportMessage(true, gav, alert.get().alternatives()));
                                            usageAlertErrorRaised = true;
                                        } else {
                                            logger.warn(getExactReportMessage(false, gav, alert.get().alternatives()));
                                        }
                                    } else if (extension.getArtifactUsageAlertSeverity().hasInexact() && usageReporter.checkArtifactAnyVersion(id.getGroup(), id.getModule())) {
                                        if (extension.getArtifactUsageAlertSeverity().errorsOnInexact()) {
                                            logger.error(getInexactReportMessage(true, gav));
                                            usageAlertErrorRaised = true;
                                        } else {
                                            logger.warn(getInexactReportMessage(false, gav));
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (usageAlertErrorRaised) {
                        throw new GradleException("Using dependencies with configured usage alert");
                    }
                }
            }
        });
    }

    private String getExactReportMessage(boolean error, String gav, Set<String> correspondingGavs) {
        return (error ? "Error: " : "Warning: ") + "using dependency " + gav + " which is configured for usage alert" + (!correspondingGavs.isEmpty() ? (" (suggested alternatives: " + correspondingGavs + ")") : "");
    }

    private String getInexactReportMessage(boolean error, String gav) {
        return (error ? "Error: " : "Warning: ") + "using dependency " + gav + " for which any version is configured for usage alert";
    }
}
