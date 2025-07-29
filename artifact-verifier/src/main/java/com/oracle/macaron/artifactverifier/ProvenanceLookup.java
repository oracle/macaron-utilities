/* Copyright (c) 2025, 2025, Oracle and/or its affiliates. */
/* Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/. */

package com.oracle.macaron.artifactverifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProvenanceLookup {
    private final String mavenRepoBaseUrl;
    private final Map<String, List<JsonNode>> provenanceCache = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Base64.Decoder base64Decoder = Base64.getDecoder();

    public ProvenanceLookup(String mavenRepoBaseUrl) {
        this.mavenRepoBaseUrl = mavenRepoBaseUrl;
    }

    public List<JsonNode> lookupProvenance(String groupId, String artifactId, String version) throws URISyntaxException, IOException {
        String gav = groupId + ":" + artifactId + ":" + version;
        List<JsonNode>  provenances = provenanceCache.get(gav);
        if (provenances == null) {
            URI vsaUrl = new URI(mavenRepoBaseUrl + "/" + groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/vsa.intoto.jsonl");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(vsaUrl.toURL().openStream(), "UTF-8"))) {
                provenances = new ArrayList<>();
                for (String vsaContent : reader.lines().toList()) {
                    JsonNode provenanceRootNode = objectMapper.readTree(vsaContent);
                    String payload = provenanceRootNode.get("payload").asText();
                    String decodedPayload = new String(base64Decoder.decode(payload), "UTF-8");
                    JsonNode payloadRootNode = objectMapper.readTree(decodedPayload);
                    provenances.add(payloadRootNode);
                }
                provenanceCache.put(gav, provenances);
            }
        }
        return provenances;
    }

    public String getMavenRepoBaseUrl() {
        return this.mavenRepoBaseUrl;
    }

}
