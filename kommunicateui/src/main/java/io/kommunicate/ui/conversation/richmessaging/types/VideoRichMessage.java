package io.kommunicate.ui.conversation.richmessaging.types;

import android.content.Context;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;

import io.kommunicate.devkit.api.conversation.Message;
import io.kommunicate.ui.CustomizationSettings;
import io.kommunicate.ui.conversation.richmessaging.KmRichMessage;
import io.kommunicate.ui.conversation.richmessaging.adapters.KmRichMessageAdapterFactory;
import io.kommunicate.ui.conversation.richmessaging.adapters.KmVideoRMAdapter;
import io.kommunicate.ui.conversation.richmessaging.callbacks.KmRichMessageListener;
import io.kommunicate.ui.kommunicate.utils.KmThemeHelper;

public class VideoRichMessage extends KmRichMessage {
    public VideoRichMessage(Context context, LinearLayout containerView, Message message, KmRichMessageListener listener, CustomizationSettings customizationSettings, boolean showTimestamp, boolean isDarkModeEnabled) {
        super(context, containerView, message, listener, customizationSettings, showTimestamp, isDarkModeEnabled);
    }

    @Override
    public void createRichMessage(boolean isMessageProcessed) {
        super.createRichMessage(isMessageProcessed);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        videoTemplateRecycler.setLayoutManager(layoutManager);
        KmVideoRMAdapter imageAdapter = (KmVideoRMAdapter) KmRichMessageAdapterFactory.getInstance().getRMAdapter(context, model, listener, message, KmThemeHelper.getInstance(context, customizationSettings), isMessageProcessed, customizationSettings);
        videoTemplateRecycler.setAdapter(imageAdapter);
    }
}
