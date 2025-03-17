package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.types;

import android.content.Context;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;

import dev.kommunicate.devkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.KmRichMessage;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.adapters.KmImageRMAdapter;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.adapters.KmRichMessageAdapterFactory;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.callbacks.KmRichMessageListener;

public class ImageKmRichMessage extends KmRichMessage {

    public ImageKmRichMessage(Context context, LinearLayout containerView, Message message, KmRichMessageListener listener, AlCustomizationSettings alCustomizationSettings, boolean showTimestamp, boolean isDarkModeEnabled) {
        super(context, containerView, message, listener, alCustomizationSettings, showTimestamp, isDarkModeEnabled);
    }

    @Override
    public void createRichMessage(boolean isMessageProcessed) {
        super.createRichMessage(isMessageProcessed);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        imageListRecycler.setLayoutManager(layoutManager);
        KmImageRMAdapter imageAdapter = (KmImageRMAdapter) KmRichMessageAdapterFactory.getInstance().getImageRMAdapter(context, model, listener, message, alCustomizationSettings);
        imageListRecycler.setAdapter(imageAdapter);
    }
}
