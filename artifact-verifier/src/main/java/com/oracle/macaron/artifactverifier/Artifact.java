/* Copyright (c) 2025, 2025, Oracle and/or its affiliates. */
/* Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/. */

package com.oracle.macaron.artifactverifier;

/**
 * Represents a Maven ecosystem artifact by its coordinates (group id, artifact id, version, can also specify a precise type and classifier if desired).
 */
public record Artifact(String groupId, String artifactId, String version, NullableOrUnknown<String> type, NullableOrUnknown<String> classifier) {
}
