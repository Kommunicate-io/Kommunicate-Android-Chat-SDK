package io.kommunicate.uiwidgets.conversation.richmessaging.callbacks;

import android.content.Context;

import io.kommunicate.data.conversation.Message;

import java.util.Map;

/**
 * Created by ashish on 05/03/18.
 */

public interface KmRichMessageListener {
    void onAction(Context context, String action, Message message, Object object, Map<String, Object> replyMetadata);
}