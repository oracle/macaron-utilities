/* Copyright (c) 2025, 2025, Oracle and/or its affiliates. */
/* Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/. */

package com.oracle.macaron.artifactverifier;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.function.Supplier;

public class FileHashSupplier implements Supplier<String> {
    private final Path filepath;

    public FileHashSupplier(Path filepath) {
        this.filepath = filepath;
    }

    @Override
    public String get() {
        try {
            return HashUtil.getSHA256HashHexString(filepath);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Cannot compute file hash for " + filepath, e);
        }
    }

}
