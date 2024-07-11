package com.applozic.mobicomkit.listners;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.exception.ApplozicException;

/**
 * Created by reytum on 27/11/17.
 */

public interface MediaDownloadProgressHandler {
    void onDownloadStarted();

    void onProgressUpdate(int percentage, ApplozicException e);

    void onCompleted(Message message, ApplozicException e);
}
