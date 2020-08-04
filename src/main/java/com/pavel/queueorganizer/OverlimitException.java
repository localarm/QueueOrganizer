package com.pavel.queueorganizer;

public class OverlimitException extends Exception {

    public OverlimitException(String message) {
        super(message);
    }
}
