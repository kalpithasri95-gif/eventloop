package com.eventloop.exception;

public class ReservationConflictException extends Exception {
    public ReservationConflictException(String message) {
        super(message);
    }
}
