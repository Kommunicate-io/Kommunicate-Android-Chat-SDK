package io.kommunicate.ui.conversation.activity;

import android.content.Intent;

import io.kommunicate.devkit.api.conversation.Message;
import io.kommunicate.ui.conversation.fragment.ConversationFragment;

/**
 * Created by User on 23-05-2015.
 */
public interface MobiComKitActivityInterface {

    int REQUEST_CODE_FULL_SCREEN_ACTION = 301;
    int INSTRUCTION_DELAY = 5000;

    void onQuickConversationFragmentItemClick(Message message, Integer conversationId, String searchString);

    void startContactActivityForResult();

    void addFragment(ConversationFragment conversationFragment);

    void updateLatestMessage(Message message, String number);

    void removeConversation(Message message, String number);

    void startActivityForResult(Intent intent, int code);

    void showErrorMessageView(String errorMessage);

    void retry();

    int getRetryCount();

}
