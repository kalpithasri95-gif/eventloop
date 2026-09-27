package com.eventloop.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtil {
    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static String today() {
        return LocalDate.now().format(DATE_FMT);
    }

    public static String now() {
        return LocalDateTime.now().format(DATETIME_FMT);
    }

    public static String formatDateTime(LocalDateTime dt) {
        if (dt == null) return "";
        return dt.format(DATETIME_FMT);
    }

    public static LocalDateTime parseDateTime(String dtStr) {
        if (dtStr == null || dtStr.trim().isEmpty()) return null;
        try {
            return LocalDateTime.parse(dtStr.trim(), DATETIME_FMT);
        } catch (DateTimeParseException e) {
            try {
                // Try date-only and append start of day
                LocalDate d = LocalDate.parse(dtStr.trim(), DATE_FMT);
                return d.atStartOfDay();
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
    }

    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr.trim(), DATE_FMT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static boolean isOverlap(String startAStr, String endAStr, String startBStr, String endBStr) {
        LocalDateTime startA = parseDateTime(startAStr);
        LocalDateTime endA = parseDateTime(endAStr);
        LocalDateTime startB = parseDateTime(startBStr);
        LocalDateTime endB = parseDateTime(endBStr);

        if (startA == null || endA == null || startB == null || endB == null) {
            return false;
        }

        // Two intervals [startA, endA] and [startB, endB] overlap if:
        // startA < endB && endA > startB
        return startA.isBefore(endB) && endA.isAfter(startB);
    }

    public static boolean isExpired(String nextVerificationDate) {
        if (nextVerificationDate == null || nextVerificationDate.trim().isEmpty()) {
            return true;
        }
        LocalDate nextDate = parseDate(nextVerificationDate);
        if (nextDate == null) return true;
        return LocalDate.now().isAfter(nextDate);
    }

    public static boolean isOverdue(String deadlineStr) {
        if (deadlineStr == null || deadlineStr.trim().isEmpty()) return false;
        LocalDateTime deadline = parseDateTime(deadlineStr);
        if (deadline == null) return false;
        return LocalDateTime.now().isAfter(deadline);
    }
}
