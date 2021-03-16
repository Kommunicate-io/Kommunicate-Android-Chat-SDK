package com.applozic.mobicomkit.api.authentication;

@SuppressWarnings("WeakerAccess")
public class DecodeException extends RuntimeException {

    DecodeException(String message) {
        super(message);
    }

    DecodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
