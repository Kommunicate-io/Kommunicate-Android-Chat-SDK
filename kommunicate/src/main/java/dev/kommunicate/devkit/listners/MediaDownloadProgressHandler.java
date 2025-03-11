package dev.kommunicate.devkit.listners;

import dev.kommunicate.devkit.api.conversation.Message;
import dev.kommunicate.devkit.exception.ApplozicException;

/**
 * Created by reytum on 27/11/17.
 */

public interface MediaDownloadProgressHandler {
    void onDownloadStarted();

    void onProgressUpdate(int percentage, ApplozicException e);

    void onCompleted(Message message, ApplozicException e);
}
