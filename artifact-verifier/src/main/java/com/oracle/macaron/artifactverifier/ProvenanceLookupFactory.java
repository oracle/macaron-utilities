/* Copyright (c) 2025, 2025, Oracle and/or its affiliates. */
/* Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/. */

package com.oracle.macaron.artifactverifier;

import java.util.HashMap;
import java.util.Map;

public class ProvenanceLookupFactory {
    private final Map<String, ProvenanceLookup> provenanceLookups;

    public ProvenanceLookupFactory() {
        this.provenanceLookups = new HashMap<>();
    }

    public ProvenanceLookup getProvenanceLookup(String mavenRepoBaseUrl) {
        return provenanceLookups.computeIfAbsent(mavenRepoBaseUrl, ProvenanceLookup::new);
    }
}
