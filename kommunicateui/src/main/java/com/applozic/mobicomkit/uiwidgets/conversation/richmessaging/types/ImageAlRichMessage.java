package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.types;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.AlRichMessage;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.adapters.AlImageRMAdapter;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.adapters.AlRichMessageAdapterFactory;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.callbacks.ALRichMessageListener;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.ALRichMessageModel;

public class ImageAlRichMessage extends AlRichMessage {

    public ImageAlRichMessage(Context context, LinearLayout containerView, Message message, ALRichMessageListener listener, AlCustomizationSettings alCustomizationSettings) {
        super(context, containerView, message, listener, alCustomizationSettings);
    }

    @Override
    public void createRichMessage() {
        super.createRichMessage();
        setupAlRichMessage((RecyclerView) containerView.findViewById(R.id.alImageListContainer), model);
    }

    @Override
    protected void setupAlRichMessage(ViewGroup recyclerView, ALRichMessageModel alRichMessageModel) {
        super.setupAlRichMessage(recyclerView, alRichMessageModel);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        ((RecyclerView) recyclerView).setLayoutManager(layoutManager);
        AlImageRMAdapter imageAdapter = (AlImageRMAdapter) AlRichMessageAdapterFactory.getInstance().getImageRMAdapter(context, alRichMessageModel, listener, message, alCustomizationSettings);
        ((RecyclerView) recyclerView).setAdapter(imageAdapter);
    }
}
