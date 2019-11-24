package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.R;
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
        RecyclerView recyclerView = containerView.findViewById(R.id.alRichMessageContainer);
        containerView.findViewById(R.id.alListMessageLayout).setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        containerView.findViewById(R.id.alFaqLayout).setVisibility(View.GONE);
        containerView.findViewById(R.id.alFaqReplyLayout).setVisibility(View.GONE);
        containerView.findViewById(R.id.alQuickReplyRecycler).setVisibility(View.GONE);
        setupAlRichMessage(recyclerView, model);
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
