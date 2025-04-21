package io.kommunicate;

import io.kommunicate.devkit.exception.KommunicateException;

/**
 * Created by ashish on 07/05/18.
 */

public class KmException extends KommunicateException {
    public KmException(String message) {
        super(message);
    }
}
