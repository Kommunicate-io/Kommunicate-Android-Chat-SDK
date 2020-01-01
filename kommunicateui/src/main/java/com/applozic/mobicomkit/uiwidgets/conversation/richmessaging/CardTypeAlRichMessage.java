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
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.adapters.AlCardRMAdapter;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.adapters.AlRichMessageAdapterFactory;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.callbacks.ALRichMessageListener;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.ALRichMessageModel;

public class CardTypeAlRichMessage extends AlRichMessage {

    public CardTypeAlRichMessage(Context context, LinearLayout containerView, Message message, ALRichMessageListener listener, AlCustomizationSettings alCustomizationSettings) {
        super(context, containerView, message, listener, alCustomizationSettings);
    }

    @Override
    public void createRichMessage() {
        super.createRichMessage();
        setupAlRichMessage((RecyclerView) containerView.findViewById(R.id.alGenericCardContainer), model);
    }

    @Override
    protected void setupAlRichMessage(ViewGroup recyclerView, ALRichMessageModel alRichMessageModel) {
        super.setupAlRichMessage(recyclerView, alRichMessageModel);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        ((RecyclerView) recyclerView).setLayoutManager(layoutManager);
        AlCardRMAdapter adapter = (AlCardRMAdapter) AlRichMessageAdapterFactory.getInstance().getRMAdapter(context, model, listener, message);
        ((RecyclerView) recyclerView).setAdapter(adapter);
    }
}
