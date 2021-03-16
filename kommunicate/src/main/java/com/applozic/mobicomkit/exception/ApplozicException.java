package com.applozic.mobicomkit.exception;

/**
 * Created by reytum on 27/11/17.
 */

public class ApplozicException extends Exception {
    private String message;

    public ApplozicException(String message) {
        super(message);
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
