package com.applozic.mobicomkit.listners;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.exception.ApplozicException;

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
