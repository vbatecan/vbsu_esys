package com.group5.paul_esys.utils;

import java.util.Optional;
import java.util.regex.Pattern;

public final class FormValidationUtil {

    public static final long LARGE_NUMBER_LIMIT = 1_000_000L;

    private static final Pattern SIMPLE_EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PASSWORD_COMPLEXITY_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$"
    );

    private FormValidationUtil() {
    }

    public static String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static Optional<String> validateRequiredText(
            String fieldLabel,
            String value,
            int minLength,
            int maxLength,
            Pattern allowedCharactersPattern,
            String allowedCharactersDescription
    ) {
        return validateText(
                fieldLabel,
                value,
                true,
                minLength,
                maxLength,
                allowedCharactersPattern,
                allowedCharactersDescription
        );
    }

    public static Optional<String> validateOptionalText(
            String fieldLabel,
            String value,
            int minLength,
            int maxLength,
            Pattern allowedCharactersPattern,
            String allowedCharactersDescription
    ) {
        return validateText(
                fieldLabel,
                value,
                false,
                minLength,
                maxLength,
                allowedCharactersPattern,
                allowedCharactersDescription
        );
    }

    public static Optional<String> validateRequiredEmail(String fieldLabel, String value, int maxLength) {
        String normalized = normalizeOptionalText(value);
        if (normalized == null) {
            return Optional.of(fieldLabel + " is required.");
        }

        if (normalized.length() > maxLength) {
            return Optional.of(fieldLabel + " must not exceed " + maxLength + " characters.");
        }

        if (!SIMPLE_EMAIL_PATTERN.matcher(normalized).matches()) {
            return Optional.of(fieldLabel + " format is invalid.");
        }

        return Optional.empty();
    }

    public static Optional<String> validateRequiredDomainEmail(
            String fieldLabel,
            String value,
            int maxLength,
            String requiredDomain
    ) {
        Optional<String> emailValidation = validateRequiredEmail(fieldLabel, value, maxLength);
        if (emailValidation.isPresent()) {
            return emailValidation;
        }

        String normalized = normalizeOptionalText(value).toLowerCase();
        if (!normalized.endsWith("@" + requiredDomain.toLowerCase())) {
            return Optional.of(fieldLabel + " must use the @" + requiredDomain + " domain.");
        }

        return Optional.empty();
    }

    public static Optional<String> validateRequiredSelection(String fieldLabel, Object selectedValue) {
        if (selectedValue == null) {
            return Optional.of(fieldLabel + " is required.");
        }

        String normalized = selectedValue.toString().trim();
        if (normalized.isEmpty()) {
            return Optional.of(fieldLabel + " is required.");
        }

        return Optional.empty();
    }

    public static Optional<String> validateMappedSelection(
            String fieldLabel,
            Object selectedValue,
            java.util.Map<String, ?> availableValues
    ) {
        Optional<String> requiredSelection = validateRequiredSelection(fieldLabel, selectedValue);
        if (requiredSelection.isPresent()) {
            return requiredSelection;
        }

        String key = selectedValue.toString();
        if (!availableValues.containsKey(key)) {
            return Optional.of("Please select a valid " + fieldLabel.toLowerCase() + ".");
        }

        return Optional.empty();
    }

    public static Optional<String> validateDigits(String fieldLabel, String value, int exactLength) {
        String normalized = normalizeOptionalText(value);
        if (normalized == null) {
            return Optional.of(fieldLabel + " is required.");
        }

        if (!normalized.matches("\\d{" + exactLength + "}")) {
            return Optional.of(fieldLabel + " must be exactly " + exactLength + " digits.");
        }

        return Optional.empty();
    }

    public static Optional<String> validateNumberRange(String fieldLabel, Number value, long minInclusive, long maxInclusive) {
        if (value == null) {
            return Optional.of(fieldLabel + " is required.");
        }

        return validateNumberRange(fieldLabel, value.longValue(), minInclusive, maxInclusive);
    }

    public static Optional<String> validateIntegerTextRange(
            String fieldLabel,
            String value,
            int minInclusive,
            int maxInclusive,
            boolean allowBlank
    ) {
        String normalized = normalizeOptionalText(value);
        if (normalized == null) {
            return allowBlank ? Optional.empty() : Optional.of(fieldLabel + " is required.");
        }

        try {
            int parsed = Integer.parseInt(normalized);
            if (parsed < minInclusive) {
                return Optional.of(fieldLabel + " must not be less than " + minInclusive + ".");
            }

            if (parsed > maxInclusive) {
                return Optional.of(fieldLabel + " must not exceed " + maxInclusive + ".");
            }

            return Optional.empty();
        } catch (NumberFormatException ex) {
            return Optional.of(fieldLabel + " must be a whole number.");
        }
    }

    public static Optional<String> validatePassword(
            String fieldLabel,
            String value,
            boolean required,
            int minLength,
            int maxLength
    ) {
        String normalized = normalizeOptionalText(value);

        if (normalized == null) {
            if (required) {
                return Optional.of(fieldLabel + " is required.");
            }

            return Optional.empty();
        }

        if (normalized.length() < minLength) {
            return Optional.of(fieldLabel + " must be at least " + minLength + " characters.");
        }

        if (normalized.length() > maxLength) {
            return Optional.of(fieldLabel + " must not exceed " + maxLength + " characters.");
        }

        if (normalized.chars().anyMatch(Character::isWhitespace)) {
            return Optional.of(fieldLabel + " must not contain spaces.");
        }

        if (!PASSWORD_COMPLEXITY_PATTERN.matcher(normalized).matches()) {
            return Optional.of(
                    fieldLabel + " must include uppercase, lowercase, number, and special character."
            );
        }

        return Optional.empty();
    }

    public static Optional<String> validateNumberRange(String fieldLabel, long value, long minInclusive, long maxInclusive) {
        if (value < minInclusive) {
            return Optional.of(fieldLabel + " must not be less than " + minInclusive + ".");
        }

        if (value > maxInclusive) {
            return Optional.of(fieldLabel + " must not exceed " + maxInclusive + ".");
        }

        return Optional.empty();
    }

    private static Optional<String> validateText(
            String fieldLabel,
            String value,
            boolean required,
            int minLength,
            int maxLength,
            Pattern allowedCharactersPattern,
            String allowedCharactersDescription
    ) {
        String normalized = normalizeOptionalText(value);

        if (normalized == null) {
            if (required) {
                return Optional.of(fieldLabel + " is required.");
            }

            return Optional.empty();
        }

        if (normalized.length() < minLength) {
            return Optional.of(fieldLabel + " must be at least " + minLength + " characters.");
        }

        if (normalized.length() > maxLength) {
            return Optional.of(fieldLabel + " must not exceed " + maxLength + " characters.");
        }

        if (allowedCharactersPattern != null && !allowedCharactersPattern.matcher(normalized).matches()) {
            return Optional.of(fieldLabel + " contains invalid characters. Allowed: " + allowedCharactersDescription + ".");
        }

        return Optional.empty();
    }
}