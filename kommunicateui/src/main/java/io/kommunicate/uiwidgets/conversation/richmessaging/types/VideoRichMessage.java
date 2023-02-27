package io.kommunicate.uiwidgets.conversation.richmessaging.types;

import android.content.Context;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;

import io.kommunicate.data.conversation.Message;
import io.kommunicate.uiwidgets.AlCustomizationSettings;
import io.kommunicate.uiwidgets.conversation.richmessaging.KmRichMessage;
import io.kommunicate.uiwidgets.conversation.richmessaging.adapters.KmRichMessageAdapterFactory;
import io.kommunicate.uiwidgets.conversation.richmessaging.adapters.KmVideoRMAdapter;
import io.kommunicate.uiwidgets.conversation.richmessaging.callbacks.KmRichMessageListener;
import io.kommunicate.uiwidgets.kommunicate.utils.KmThemeHelper;

public class VideoRichMessage extends KmRichMessage {
    public VideoRichMessage(Context context, LinearLayout containerView, Message message, KmRichMessageListener listener, AlCustomizationSettings alCustomizationSettings, boolean showTimestamp) {
        super(context, containerView, message, listener, alCustomizationSettings, showTimestamp);
    }

    @Override
    public void createRichMessage(boolean isMessageProcessed) {
        super.createRichMessage(isMessageProcessed);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        videoTemplateRecycler.setLayoutManager(layoutManager);
        KmVideoRMAdapter imageAdapter = (KmVideoRMAdapter) KmRichMessageAdapterFactory.getInstance().getRMAdapter(context, model, listener, message, KmThemeHelper.getInstance(context, alCustomizationSettings), isMessageProcessed, alCustomizationSettings);
        videoTemplateRecycler.setAdapter(imageAdapter);
    }
}