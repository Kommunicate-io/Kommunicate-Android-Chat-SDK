package com.applozic.mobicomkit.exception;

/**
 * Created by user on 5/30/2015.
 */
public class UnAuthoriseException extends Exception {
    private String message;

    public UnAuthoriseException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}