package com.resilichain.api.domain;

import java.math.BigDecimal;

/** Constructor/setter guard clauses shared by domain entities to enforce invariants consistently. */
final class Guard {

    private Guard() {
    }

    static String notBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    static <T> T notNull(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        return value;
    }

    static double inRange(double value, double min, double max, String fieldName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(fieldName + " must be between " + min + " and " + max);
        }
        return value;
    }

    static int positive(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
        return value;
    }

    static int nonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
        return value;
    }

    static BigDecimal nonNegative(BigDecimal value, String fieldName) {
        notNull(value, fieldName);
        if (value.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
        return value;
    }
}
