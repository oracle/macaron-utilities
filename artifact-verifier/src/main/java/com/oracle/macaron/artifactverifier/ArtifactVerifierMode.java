/* Copyright (c) 2025, 2025, Oracle and/or its affiliates. */
/* Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/. */

package com.oracle.macaron.artifactverifier;

public enum ArtifactVerifierMode {
    NO_REQUIRE_VERIFY,
    REQUIRE_VERIFY_IF_ATTESTED_ANY,
    REQUIRE_VERIFY_IF_ATTESTED_FIRST,
    REQUIRE_VERIFY_ANY,
    REQUIRE_VERIFY_FIRST
}
