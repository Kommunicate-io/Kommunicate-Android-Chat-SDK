package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.types;

import android.content.Context;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.KmRichMessage;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.adapters.KmCardRMAdapter;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.adapters.KmRichMessageAdapterFactory;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.callbacks.KmRichMessageListener;
import com.applozic.mobicomkit.uiwidgets.kommunicate.utils.KmThemeHelper;

public class CardTypeKmRichMessage extends KmRichMessage {

    public CardTypeKmRichMessage(Context context, LinearLayout containerView, Message message, KmRichMessageListener listener, AlCustomizationSettings alCustomizationSettings) {
        super(context, containerView, message, listener, alCustomizationSettings);
    }

    @Override
    public void createRichMessage(boolean isMessageProcessed) {
        super.createRichMessage(isMessageProcessed);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        genericCardRecycler.setLayoutManager(layoutManager);
        KmCardRMAdapter adapter = (KmCardRMAdapter) KmRichMessageAdapterFactory.getInstance().getRMAdapter(context, model, listener, message, KmThemeHelper.getInstance(context, alCustomizationSettings), isMessageProcessed);
        genericCardRecycler.setAdapter(adapter);
    }
}
