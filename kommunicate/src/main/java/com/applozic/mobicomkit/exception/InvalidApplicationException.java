package com.applozic.mobicomkit.exception;

/**
 * Created by user on 5/30/2015.
 */
public class InvalidApplicationException extends Exception {
    private String message;

    public InvalidApplicationException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}