package org.schabi.newpipe.extractor.services.bitchute;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public final class BitChuteParsingHelper {
    private BitChuteParsingHelper() {
    }

    public static long parseDuration(final Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (!(value instanceof String)) {
            return 0;
        }
        final String[] parts = ((String) value).trim().split(":");
        long seconds = 0;
        for (final String part : parts) {
            try {
                seconds = seconds * 60 + Long.parseLong(part);
            } catch (final NumberFormatException ignored) {
                return 0;
            }
        }
        return seconds;
    }

    public static DateWrapper parseDate(final String value) throws ParsingException {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return new DateWrapper(OffsetDateTime.parse(value));
        } catch (final Exception ignored) {
            try {
                return new DateWrapper(OffsetDateTime.ofInstant(
                        Instant.parse(value), ZoneOffset.UTC));
            } catch (final Exception e) {
                throw new ParsingException("Could not parse BitChute date " + value, e);
            }
        }
    }

    public static long parseCount(final Object value, final long defaultValue) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value == null) {
            return defaultValue;
        }
        final String text = value.toString().trim().toUpperCase().replace(",", "");
        try {
            if (text.endsWith("K")) {
                return Math.round(Double.parseDouble(text.substring(0, text.length() - 1)) * 1_000);
            }
            if (text.endsWith("M")) {
                return Math.round(Double.parseDouble(text.substring(0, text.length() - 1))
                        * 1_000_000);
            }
            return Long.parseLong(text);
        } catch (final NumberFormatException ignored) {
            return defaultValue;
        }
    }
}
