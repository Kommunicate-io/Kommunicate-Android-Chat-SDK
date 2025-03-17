package io.kommunicate.devkit.listners;

import io.kommunicate.devkit.api.conversation.Message;
import io.kommunicate.devkit.exception.ApplozicException;

/**
 * Created by reytum on 27/11/17.
 */

public interface MediaDownloadProgressHandler {
    void onDownloadStarted();

    void onProgressUpdate(int percentage, ApplozicException e);

    void onCompleted(Message message, ApplozicException e);
}
