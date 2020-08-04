package com.pavel.queueorganizer;

public class RepeatedGetInTheQueueException extends Exception {
    public RepeatedGetInTheQueueException() {
    }

    public RepeatedGetInTheQueueException(String message) {
        super(message);
    }
}
