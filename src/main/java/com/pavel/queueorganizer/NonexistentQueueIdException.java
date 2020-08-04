package com.pavel.queueorganizer;

public class NonexistentQueueIdException extends Exception{

    public NonexistentQueueIdException(String message) {
        super(message);
    }
}
