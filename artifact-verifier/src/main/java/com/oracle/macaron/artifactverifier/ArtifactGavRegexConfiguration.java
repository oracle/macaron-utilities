/* Copyright (c) 2025, 2025, Oracle and/or its affiliates. */
/* Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/. */

package com.oracle.macaron.artifactverifier;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.fasterxml.jackson.databind.JsonNode;

public class ArtifactGavRegexConfiguration {
    private static Pattern getRegexField(JsonNode gavRegexJsonNode, String field) throws ParseException {
        JsonNode fieldNode = gavRegexJsonNode.get(field);
        if (fieldNode != null) {
            if (!fieldNode.isTextual()) {
                throw new ParseException("gav regex " + field + " is invalid");
            }
            String regex = fieldNode.asText();
            try {
                return Pattern.compile(regex);
            } catch (PatternSyntaxException e) {
                throw new ParseException("gav regex " + field + " is invalid regex");
            }
        }
        return null;
    }

    /**
     * Constructs an artifact regex matcher from json configuration. See readme for documentation on configuration format.
     */
    public static ArtifactGavRegex fromConfiguration(JsonNode gavRegexJsonNode) throws ParseException {
        if (!gavRegexJsonNode.isObject()) {
            throw new ParseException("gav regex is not an object");
        }
        Pattern groupIdRegex = getRegexField(gavRegexJsonNode, "group-id");
        Pattern artifactIdRegex = getRegexField(gavRegexJsonNode, "artifact-id");
        Pattern versionRegex = getRegexField(gavRegexJsonNode, "version");
        Pattern negativeGroupIdRegex = getRegexField(gavRegexJsonNode, "~group-id");
        Pattern negativeArtifactIdRegex = getRegexField(gavRegexJsonNode, "~artifact-id");
        Pattern negativeVersionRegex = getRegexField(gavRegexJsonNode, "~version");

        return new ArtifactGavRegex(groupIdRegex, artifactIdRegex, versionRegex, negativeGroupIdRegex, negativeArtifactIdRegex, negativeVersionRegex);
    }
}
