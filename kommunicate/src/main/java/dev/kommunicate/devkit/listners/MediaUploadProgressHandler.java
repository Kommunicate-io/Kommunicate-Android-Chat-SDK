package dev.kommunicate.devkit.listners;

import dev.kommunicate.devkit.api.conversation.Message;
import dev.kommunicate.devkit.exception.ApplozicException;

/**
 * Created by reytum on 27/11/17.
 */

public interface MediaUploadProgressHandler {
    void onUploadStarted(ApplozicException e, String oldMessageKey);

    void onProgressUpdate(int percentage, ApplozicException e, String oldMessageKey);

    void onCancelled(ApplozicException e, String oldMessageKey);

    void onCompleted(ApplozicException e, String oldMessageKey);

    void onSent(Message message, String oldMessageKey);
}
