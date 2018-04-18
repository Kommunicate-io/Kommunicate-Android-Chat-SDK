package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging;

import android.content.Context;

import com.applozic.mobicomkit.api.conversation.Message;

/**
 * Created by ashish on 05/03/18.
 */

public interface ALRichMessageListener {
    void onAction(Context context, String action, Message message, Object object);
}
