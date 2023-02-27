package io.kommunicate.uiwidgets.conversation.richmessaging.types;

import android.content.Context;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;

import io.kommunicate.data.conversation.Message;
import io.kommunicate.uiwidgets.AlCustomizationSettings;
import io.kommunicate.uiwidgets.conversation.richmessaging.KmRichMessage;
import io.kommunicate.uiwidgets.conversation.richmessaging.adapters.KmImageRMAdapter;
import io.kommunicate.uiwidgets.conversation.richmessaging.adapters.KmRichMessageAdapterFactory;
import io.kommunicate.uiwidgets.conversation.richmessaging.callbacks.KmRichMessageListener;

public class ImageKmRichMessage extends KmRichMessage {

    public ImageKmRichMessage(Context context, LinearLayout containerView, Message message, KmRichMessageListener listener, AlCustomizationSettings alCustomizationSettings, boolean showTimestamp) {
        super(context, containerView, message, listener, alCustomizationSettings, showTimestamp);
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
