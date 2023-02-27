package io.kommunicate.exception;

/**
 * Created by reytum on 27/11/17.
 */

public class KommunicateException extends Exception {
    private String message;

    public KommunicateException(String message) {
        super(message);
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
