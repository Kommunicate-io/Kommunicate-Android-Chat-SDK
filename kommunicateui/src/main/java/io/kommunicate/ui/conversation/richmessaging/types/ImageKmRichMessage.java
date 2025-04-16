package io.kommunicate.ui.conversation.richmessaging.types;

import android.content.Context;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;

import io.kommunicate.devkit.api.conversation.Message;
import io.kommunicate.ui.CustomizationSettings;
import io.kommunicate.ui.conversation.richmessaging.KmRichMessage;
import io.kommunicate.ui.conversation.richmessaging.adapters.KmImageRMAdapter;
import io.kommunicate.ui.conversation.richmessaging.adapters.KmRichMessageAdapterFactory;
import io.kommunicate.ui.conversation.richmessaging.callbacks.KmRichMessageListener;

public class ImageKmRichMessage extends KmRichMessage {

    public ImageKmRichMessage(Context context, LinearLayout containerView, Message message, KmRichMessageListener listener, CustomizationSettings customizationSettings, boolean showTimestamp, boolean isDarkModeEnabled) {
        super(context, containerView, message, listener, customizationSettings, showTimestamp, isDarkModeEnabled);
    }

    @Override
    public void createRichMessage(boolean isMessageProcessed) {
        super.createRichMessage(isMessageProcessed);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        imageListRecycler.setLayoutManager(layoutManager);
        KmImageRMAdapter imageAdapter = (KmImageRMAdapter) KmRichMessageAdapterFactory.getInstance().getImageRMAdapter(context, model, listener, message, customizationSettings);
        imageListRecycler.setAdapter(imageAdapter);
    }
}
