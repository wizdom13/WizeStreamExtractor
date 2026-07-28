package org.schabi.newpipe.extractor.services.rumble;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RumbleParsingHelper {
    private static final Pattern COUNT_PATTERN =
            Pattern.compile("([\\d.,]+)\\s*([KMB]?)", Pattern.CASE_INSENSITIVE);

    private RumbleParsingHelper() {
    }

    public static long parseCount(final String value, final long fallback) {
        if (value == null) {
            return fallback;
        }
        final Matcher matcher = COUNT_PATTERN.matcher(value.replace("\u00a0", " "));
        if (!matcher.find()) {
            return fallback;
        }
        try {
            final double number = Double.parseDouble(
                    matcher.group(1).replace(",", ""));
            final String suffix = matcher.group(2).toUpperCase(Locale.ROOT);
            final double multiplier;
            switch (suffix) {
                case "K":
                    multiplier = 1_000D;
                    break;
                case "M":
                    multiplier = 1_000_000D;
                    break;
                case "B":
                    multiplier = 1_000_000_000D;
                    break;
                default:
                    multiplier = 1D;
                    break;
            }
            return (long) (number * multiplier);
        } catch (final NumberFormatException e) {
            return fallback;
        }
    }

    public static long parseDuration(final String value) {
        if (value == null || value.isEmpty()) {
            return -1;
        }
        final String[] pieces = value.trim().split(":");
        long seconds = 0;
        try {
            for (final String piece : pieces) {
                seconds = seconds * 60 + Long.parseLong(piece.trim());
            }
            return seconds;
        } catch (final NumberFormatException e) {
            return -1;
        }
    }

    public static DateWrapper parseDate(final String value) throws ParsingException {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return new DateWrapper(OffsetDateTime.parse(value));
        } catch (final DateTimeParseException e) {
            throw new ParsingException("Could not parse Rumble date " + value, e);
        }
    }
}
