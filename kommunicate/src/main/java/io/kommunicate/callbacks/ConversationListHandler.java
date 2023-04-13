package io.kommunicate.callbacks;

import android.content.Context;

import java.util.List;

import io.kommunicate.data.conversation.AlConversation;
import io.kommunicate.exception.KommunicateException;

public interface ConversationListHandler {
    void onResult(Context context, List<AlConversation> conversationList, KommunicateException e);
}
