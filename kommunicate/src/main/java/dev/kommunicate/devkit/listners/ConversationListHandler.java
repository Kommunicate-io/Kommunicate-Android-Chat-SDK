package dev.kommunicate.devkit.listners;

import android.content.Context;

import dev.kommunicate.devkit.api.conversation.AlConversation;
import dev.kommunicate.devkit.exception.ApplozicException;

import java.util.List;

public interface ConversationListHandler {
    void onResult(Context context, List<AlConversation> conversationList, ApplozicException e);
}
