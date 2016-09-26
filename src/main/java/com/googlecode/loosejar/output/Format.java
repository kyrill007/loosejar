package com.googlecode.loosejar.output;

public enum Format {
    CSV, VERBAL;

    public static Format fromString(String formatName) {
        for (Format format : Format.values()) {
            if (format.name().equalsIgnoreCase(formatName)) {
                return format;
            }
        }
        return null;
    }
}
