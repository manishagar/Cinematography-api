package com.cinematography.core.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Date/time helpers shared by the services.
 * <p>
 * The {@code isValidDate*FormatString} methods validate against ISO formats (strict). Use
 * {@link #isValidLocalDate(String, String)} / {@link #isValidLocalDateTime(String, String)} to validate
 * against an explicit pattern instead.
 */
public class DateUtils {
    private static final Logger log = LoggerFactory.getLogger(DateUtils.class);

    private static final String PARSING_DATE_ERROR_MSG = "Error parsing date string: {} with pattern: {}";

    private DateUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Validates whether a given date-time string is an ISO local date-time.
     *
     * @param formatPattern The date format pattern, used for logging only.
     * @param date The date string to validate.
     * @return true if the date matches the format, false otherwise.
     */
    public static boolean isValidDateTimeFormatString(String formatPattern, String date) {
        try {
            LocalDateTime.parse(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return true;
        } catch (DateTimeParseException ex) {
            log.error(PARSING_DATE_ERROR_MSG, date, formatPattern, ex);
            return false;
        }
    }

    /**
     * Validates whether a given date string is an ISO local date.
     *
     * @param formatPattern The date format pattern, used for logging only.
     * @param date The date string to validate.
     * @return true if the date matches the format, false otherwise.
     */
    public static boolean isValidDateFormatString(String formatPattern, String date) {
        try {
            LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
            return true;
        } catch (DateTimeParseException ex) {
            log.error(PARSING_DATE_ERROR_MSG, date, formatPattern, ex);
            return false;
        }
    }

    /**
     * Validates whether a date string can be parsed as a {@link LocalDate} with the given pattern.
     *
     * @param formatPattern The date format pattern (e.g., "yyyy-MM-dd").
     * @param date The date string to validate.
     * @return true if the date matches the pattern, false otherwise.
     */
    public static boolean isValidLocalDate(String formatPattern, String date) {
        try {
            LocalDate.parse(date, DateTimeFormatter.ofPattern(formatPattern));
            return true;
        } catch (DateTimeParseException ex) {
            log.error("Invalid date format: {}. Expected format: {}", date, formatPattern, ex);
            return false;
        }
    }

    /**
     * Validates whether a date-time string can be parsed as a {@link LocalDateTime} with the given pattern.
     *
     * @param formatPattern The date-time format pattern (e.g., "yyyy-MM-dd HH:mm:ss").
     * @param date The date-time string to validate.
     * @return true if the value matches the pattern, false otherwise.
     */
    public static boolean isValidLocalDateTime(String formatPattern, String date) {
        try {
            LocalDateTime.parse(date, DateTimeFormatter.ofPattern(formatPattern));
            return true;
        } catch (DateTimeParseException ex) {
            log.error("Invalid date format: {}. Expected format: {}", date, formatPattern, ex);
            return false;
        }
    }

    /**
     * Formats a LocalDateTime object into a string based on the given format pattern.
     *
     * @param localDateTime The LocalDateTime object to format.
     * @param formatPattern The date format pattern (e.g., "yyyy-MM-dd'T'HH:mm:ss").
     * @return A formatted date string, or an empty Optional if the input is null or an error occurs.
     */
    public static Optional<String> formatToStringDateTime(LocalDateTime localDateTime, String formatPattern) {
        if (localDateTime == null) {
            return Optional.empty();
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);
            return Optional.of(localDateTime.format(formatter));
        } catch (IllegalArgumentException ex) {
            log.error("Error formatting LocalDateTime: {} with pattern: {}", localDateTime, formatPattern, ex);
            return Optional.empty();
        }
    }

    /**
     * Converts a date-time string into a LocalDateTime object based on the given format pattern.
     *
     * @param stringDateTime The date-time string to convert.
     * @param formatPattern The expected format pattern (e.g., "yyyy-MM-dd HH:mm:ss").
     * @return An Optional containing LocalDateTime if parsing is successful, otherwise empty.
     */
    public static Optional<LocalDateTime> formatToLocalDateTime(String stringDateTime, String formatPattern) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);
            return Optional.of(LocalDateTime.parse(stringDateTime, formatter));
        } catch (DateTimeParseException ex) {
            log.error(PARSING_DATE_ERROR_MSG, stringDateTime, formatPattern, ex);
            return Optional.empty();
        }
    }

    public static Optional<LocalDate> formatToLocalDate(String stringDate, String formatPattern) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);
            return Optional.of(LocalDate.parse(stringDate, formatter));
        } catch (DateTimeParseException ex) {
            log.error(PARSING_DATE_ERROR_MSG, stringDate, formatPattern, ex);
            return Optional.empty();
        }
    }

    /**
     * Converts a date-time into a LocalDateTime object based on the given format pattern.
     *
     * @param formatPattern The expected format pattern (e.g., "yyyy-MM-dd HH:mm:ss").
     * @return An Optional containing LocalDateTime if parsing is successful, otherwise empty.
     */
    public static Optional<LocalDateTime> formatLocalDateTime(LocalDateTime localDateTime, String formatPattern) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);
            return Optional.ofNullable(Optional.of(localDateTime.format(formatter)).map(dateStr -> LocalDateTime.parse(dateStr, formatter))
                    .orElse(null)
            );
        } catch (DateTimeParseException ex) {
            log.error(PARSING_DATE_ERROR_MSG, localDateTime, formatPattern, ex);
            return Optional.empty();
        }
    }

    /**
     * Converts a UTC datetime string to LocalDateTime using the provided format pattern.
     * Example input: "2025-10-03T23:00:00Z" or "2025-10-03 23:00:00" depending on pattern.
     *
     * @param utcDateTimeStr The UTC datetime string.
     * @param formatPattern The date-time format pattern (e.g., "yyyy-MM-dd'T'HH:mm:ss'Z'").
     * @return Optional containing LocalDateTime in UTC, or empty if parsing fails.
     */
    public static Optional<LocalDateTime> formatUtcToLocalDateTime(String utcDateTimeStr, String formatPattern) {
        if (!StringUtils.hasText(utcDateTimeStr)) {
            return Optional.empty();
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);

            // Remove Z if pattern does not include it, since LocalDateTime cannot store offset
            String cleanedDateTime = utcDateTimeStr.endsWith("Z") && !formatPattern.contains("X")
                    ? utcDateTimeStr.substring(0, utcDateTimeStr.length() - 1)
                    : utcDateTimeStr;

            return Optional.of(LocalDateTime.parse(cleanedDateTime, formatter));
        } catch (DateTimeParseException ex) {
            log.error(PARSING_DATE_ERROR_MSG, utcDateTimeStr, formatPattern, ex);
            return Optional.empty();
        }
    }

    /**
     * Parses a date string in "yyyy-MM-dd" format into a java.sql.Date.
     *
     * @param dateStr the input date string (expected format: yyyy-MM-dd)
     * @return parsed java.sql.Date, or null if the input is blank or invalid
     */
    public static java.sql.Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            log.warn("parseDate: Input date string is null or blank.");
            return null;
        }
        try {
            LocalDate localDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return java.sql.Date.valueOf(localDate);
        } catch (DateTimeParseException ex) {
            log.error("parseDate: Failed to parse date string '{}'. Expected format is yyyy-MM-dd.", dateStr, ex);
            return null;
        }
    }
}
