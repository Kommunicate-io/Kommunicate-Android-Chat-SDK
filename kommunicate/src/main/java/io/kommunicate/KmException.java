package io.kommunicate;

import dev.kommunicate.devkit.exception.ApplozicException;

/**
 * Created by ashish on 07/05/18.
 */

public class KmException extends ApplozicException {
    public KmException(String message) {
        super(message);
    }
}
