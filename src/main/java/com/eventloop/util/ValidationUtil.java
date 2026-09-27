package com.eventloop.util;

import com.eventloop.exception.ValidationException;
import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern RESOURCE_ID_PATTERN = Pattern.compile("^EL-[A-Z]{3}-\\d{3,}$");

    public static void requireNonEmpty(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty.");
        }
    }

    public static void validatePositive(int value, String fieldName) throws ValidationException {
        if (value <= 0) {
            throw new ValidationException(fieldName + " must be greater than zero.");
        }
    }

    public static void validateNonNegative(double value, String fieldName) throws ValidationException {
        if (value < 0) {
            throw new ValidationException(fieldName + " cannot be negative.");
        }
    }

    public static void validateConditionRating(int rating) throws ValidationException {
        if (rating < 1 || rating > 5) {
            throw new ValidationException("Condition rating must be between 1 and 5.");
        }
    }

    public static void validateEmail(String email) throws ValidationException {
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Please enter a valid email address.");
        }
    }

    public static void validateResourceIdFormat(String resourceId) throws ValidationException {
        if (resourceId == null || !RESOURCE_ID_PATTERN.matcher(resourceId.trim().toUpperCase()).matches()) {
            throw new ValidationException("Resource ID must follow pattern 'EL-XXX-001' (e.g., EL-EQP-001, EL-AUD-001).");
        }
    }

    public static void validateTimeOrder(String startTime, String endTime) throws ValidationException {
        if (startTime != null && endTime != null && !startTime.isEmpty() && !endTime.isEmpty()) {
            if (startTime.compareTo(endTime) >= 0) {
                throw new ValidationException("End time must be after start time.");
            }
        }
    }
}
