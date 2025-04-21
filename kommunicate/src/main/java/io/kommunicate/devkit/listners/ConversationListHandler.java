package io.kommunicate.devkit.listners;

import android.content.Context;

import io.kommunicate.devkit.api.conversation.ConversationDetails;
import io.kommunicate.devkit.exception.ApplozicException;

import java.util.List;

public interface ConversationListHandler {
    void onResult(Context context, List<ConversationDetails> conversationList, ApplozicException e);
}
