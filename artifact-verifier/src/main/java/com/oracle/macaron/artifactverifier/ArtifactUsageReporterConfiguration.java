/* Copyright (c) 2025, 2025, Oracle and/or its affiliates. */
/* Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/. */

package com.oracle.macaron.artifactverifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ArtifactUsageReporterConfiguration {

    /**
     * Constructs an artifact usage reporter from json configuration. See readme for documentation on configuration format.
     */
    public static ArtifactUsageReporter fromConfiguration(JsonNode usageReporterJsonNode) throws ParseException {
        JsonNode typeNode = usageReporterJsonNode.get("type");
        if (typeNode == null || !typeNode.isTextual()) {
            throw new ParseException("Usage reporter has no type specified");
        }

        switch (typeNode.asText()) {
            case "report-gav-list": {
                JsonNode gavListNode = usageReporterJsonNode.get("gav-list");
                if (gavListNode == null || !gavListNode.isArray()) {
                    throw new ParseException("Usage reporter of type report-gav-list has no gav-list specified");
                }

                Set<String> gavs = new LinkedHashSet<>();

                for (JsonNode gavNode : gavListNode) {
                    if (!gavNode.isTextual()) {
                        throw new ParseException("Usage reporter of type report-gav-list has invalid gav");
                    }
                    gavs.add(gavNode.asText());

                }

                return new ArtifactUsageReporterByGavList(gavs);
            }
            case "report-gav-alternatives-map": {
                JsonNode gavMapNode = usageReporterJsonNode.get("gav-alternatives");
                if (gavMapNode == null || !gavMapNode.isObject()) {
                    throw new ParseException("Usage reporter of type report-gav-alternatives-map has no gav-alternatives specified");
                }

                Map<String, Set<String>> gavAlternatives = new LinkedHashMap<>();
                for (Map.Entry<String, JsonNode> field : gavMapNode.properties()) {
                    if (field.getValue() == null || !field.getValue().isArray()) {
                        throw new ParseException("Usage reporter of type report-gav-alternatives-map has invalid alternatives list");
                    }
                    Set<String> alternatives = new LinkedHashSet<>();
                    for (JsonNode gavNode : field.getValue()) {
                        if (!gavNode.isTextual()) {
                            throw new ParseException("Usage reporter of type report-gav-alternatives-map has invalid gav");
                        }
                        alternatives.add(gavNode.asText());
                    }
                    gavAlternatives.put(field.getKey(), alternatives);
                }

                return new ArtifactUsageReporterByGavAlternativesMap(gavAlternatives);

            }
            case "report-gav-regex": {
                JsonNode gavRegexNode = usageReporterJsonNode.get("gav-regex");
                if (gavRegexNode == null || !gavRegexNode.isObject()) {
                    throw new ParseException("Usage reporter of type report-gav-regex has no gav-regex specified");
                }

                ArtifactGavRegex gavRegex = ArtifactGavRegexConfiguration.fromConfiguration(gavRegexNode);
                return new ArtifactUsageReporterByGavRegex(gavRegex);
            }
            case "reporter-list": {
                JsonNode subUsageReportersNode = usageReporterJsonNode.get("usage-reporters");
                if (subUsageReportersNode == null || !subUsageReportersNode.isArray()) {
                    throw new ParseException("Usage reporter of type reporter-list has no reporters specified");
                }
                List<ArtifactUsageReporter> subUsageReporters = new ArrayList<>();
                for (JsonNode subVerifierNode : subUsageReportersNode) {
                    subUsageReporters.add(fromConfiguration(subVerifierNode));
                }

                return new ArtifactUsageReporterComposite(subUsageReporters);
            }
            default:
                throw new ParseException("Usage reporter has unknown type '" + typeNode.asText() + "'");
        }
    }

    public static ArtifactUsageReporter fromConfiguration(URL configFileUrl) throws IOException, ParseException {
        ObjectMapper objectMapper = new ObjectMapper();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(configFileUrl.openStream(), "UTF-8"))) {
            String configContent = reader.lines().collect(Collectors.joining("\n"));
            JsonNode configNode = objectMapper.readTree(configContent);
            return fromConfiguration(configNode);
        }
    }

    public static ArtifactUsageReporter fromConfiguration(Path configFilePath) throws IOException, ParseException {
        ObjectMapper objectMapper = new ObjectMapper();
        try (BufferedReader reader = Files.newBufferedReader(configFilePath)) {
            String configContent = reader.lines().collect(Collectors.joining("\n"));
            JsonNode configNode = objectMapper.readTree(configContent);
            return fromConfiguration(configNode);
        }
    }
}
