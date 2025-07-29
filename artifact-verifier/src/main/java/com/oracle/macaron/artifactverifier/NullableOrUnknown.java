/* Copyright (c) 2025, 2025, Oracle and/or its affiliates. */
/* Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/. */

package com.oracle.macaron.artifactverifier;

import java.util.Objects;

public class NullableOrUnknown<T> {
    private final T value;
    private final boolean unknown;

    public NullableOrUnknown(T value) {
        this.value = value;
        this.unknown = false;
    }

    private NullableOrUnknown(boolean unknown) {
        this.value = null;
        this.unknown = unknown;
    }

    public static NullableOrUnknown getUnknown() {
        return new NullableOrUnknown(true);
    }

    public T getValue() {
        if (unknown) {
            throw new IllegalArgumentException("calling getValue on unknown value");
        }
        return value;
    }

    public boolean isUnknown() {
        return unknown;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.value);
        hash = 97 * hash + (this.unknown ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NullableOrUnknown<?> other = (NullableOrUnknown<?>) obj;
        if (this.unknown != other.unknown) {
            return false;
        }
        return Objects.equals(this.value, other.value);
    }
}
