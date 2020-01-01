package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.adapters;

import android.content.Context;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.callbacks.ALRichMessageListener;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.ALRichMessageModel;

import java.util.List;
import java.util.Map;

/**
 * factory class to create instances of rich message adapters
 * implementing the factory pattern
 *
 * @author shubhamtewari
 * created on: 23 Nov, 2019
 */
public class AlRichMessageAdapterFactory {
    //constants for specifying which instance to instantiate
    public final static int CARD_RICH_MESSAGE = 10;
    public final static int IMAGE_RICH_MESSAGE = 9;
    public final static int FAQ_RICH_MESSAGE = 8;
    public final static int LIST_RICH_MESSAGE = 7;
    public final static int BUTTON_RICH_MESSAGE = 3;
    public final static int REPLY_RICH_MESSAGE = 6;

    //factory class is a singleton
    private AlRichMessageAdapterFactory() {
    }

    //singleton helper (Bill Pugh Method)
    private static class RMFactoryHelper {
        static final AlRichMessageAdapterFactory INSTANCE = new AlRichMessageAdapterFactory();
    }

    public static AlRichMessageAdapterFactory getInstance() {
        return RMFactoryHelper.INSTANCE;
    }

    public ALRichMessageAdapter getRMAdapter(Context context, ALRichMessageModel model, ALRichMessageListener listener, Message message) {
        if (model.getTemplateId() == AlRichMessageAdapterFactory.CARD_RICH_MESSAGE)
            return new AlCardRMAdapter(context, model, listener, message);
        else if (model.getTemplateId() == AlRichMessageAdapterFactory.BUTTON_RICH_MESSAGE || model.getTemplateId() == AlRichMessageAdapterFactory.REPLY_RICH_MESSAGE)
            return new AlButtonRMAdapter(context, model, listener, message);
        else return null;
    }

    public ALRichMessageAdapter getImageRMAdapter(Context context, ALRichMessageModel model, ALRichMessageListener listener, Message message, AlCustomizationSettings alCustomizationSettings) {
        return new AlImageRMAdapter(context, model, listener, message, alCustomizationSettings);
    }

    public ALRichMessageAdapter getListRMAdapter(Context context, Message message, List<ALRichMessageModel.AlElementModel> elementList, Map<String, Object> replyMetadata, ALRichMessageListener messageListener) {
        return new AlListRMAdapter(context, message, elementList, replyMetadata, messageListener);
    }
}
