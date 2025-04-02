package io.kommunicate.ui.conversation.richmessaging.types;

import android.content.Context;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;

import io.kommunicate.devkit.api.conversation.Message;
import io.kommunicate.ui.CustomizationSettings;
import io.kommunicate.ui.conversation.richmessaging.KmRichMessage;
import io.kommunicate.ui.conversation.richmessaging.adapters.KmCardRMAdapter;
import io.kommunicate.ui.conversation.richmessaging.adapters.KmRichMessageAdapterFactory;
import io.kommunicate.ui.conversation.richmessaging.callbacks.KmRichMessageListener;
import io.kommunicate.ui.kommunicate.utils.KmThemeHelper;

public class CardTypeKmRichMessage extends KmRichMessage {

    public CardTypeKmRichMessage(Context context, LinearLayout containerView, Message message, KmRichMessageListener listener, CustomizationSettings customizationSettings, boolean showTimestamp, boolean isDarkModeEnabled) {
        super(context, containerView, message, listener, customizationSettings, showTimestamp, isDarkModeEnabled);
    }

    @Override
    public void createRichMessage(boolean isMessageProcessed) {
        super.createRichMessage(isMessageProcessed);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        genericCardRecycler.setLayoutManager(layoutManager);
        KmCardRMAdapter adapter = (KmCardRMAdapter) KmRichMessageAdapterFactory.getInstance().getRMAdapter(context, model, listener, message, KmThemeHelper.getInstance(context, customizationSettings), isMessageProcessed, customizationSettings);
        genericCardRecycler.setAdapter(adapter);
    }
}
