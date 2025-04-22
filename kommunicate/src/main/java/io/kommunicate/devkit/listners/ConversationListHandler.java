package io.kommunicate.devkit.listners;

import android.content.Context;

import io.kommunicate.devkit.api.conversation.ConversationDetails;
import io.kommunicate.devkit.exception.KommunicateException;

import java.util.List;

public interface ConversationListHandler {
    void onResult(Context context, List<ConversationDetails> conversationList, KommunicateException e);
}
