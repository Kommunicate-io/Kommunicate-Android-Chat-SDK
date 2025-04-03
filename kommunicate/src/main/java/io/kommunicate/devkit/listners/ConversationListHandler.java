package io.kommunicate.devkit.listners;

import android.content.Context;

import io.kommunicate.devkit.api.conversation.AlConversation;
import io.kommunicate.devkit.exception.KommunicateException;

import java.util.List;

public interface ConversationListHandler {
    void onResult(Context context, List<AlConversation> conversationList, KommunicateException e);
}
