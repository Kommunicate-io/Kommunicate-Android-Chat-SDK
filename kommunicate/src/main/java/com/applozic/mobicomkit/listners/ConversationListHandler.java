package com.applozic.mobicomkit.listners;

import android.content.Context;

import com.applozic.mobicomkit.api.conversation.AlConversation;
import com.applozic.mobicomkit.exception.ApplozicException;

import java.util.List;

public interface ConversationListHandler {
    void onResult(Context context, List<AlConversation> conversationList, ApplozicException e);
}
