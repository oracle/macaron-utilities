/* Copyright (c) 2025, 2025, Oracle and/or its affiliates. */
/* Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/. */

package com.oracle.macaron.artifactverifier;

import java.util.Set;

public record ArtifactUsageAlert(Set<String> alternatives) {
    public ArtifactUsageAlert {
        alternatives = Set.copyOf(alternatives);
    }
}
