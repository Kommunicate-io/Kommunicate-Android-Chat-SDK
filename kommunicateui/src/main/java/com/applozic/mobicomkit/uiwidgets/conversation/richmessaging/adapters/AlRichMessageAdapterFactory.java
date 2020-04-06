package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.adapters;

import android.content.Context;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.AlRichMessageFactory;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.callbacks.ALRichMessageListener;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.ALRichMessageModel;
import com.applozic.mobicomkit.uiwidgets.kommunicate.utils.KmThemeHelper;

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

    public ALRichMessageAdapter getRMAdapter(Context context, ALRichMessageModel model, ALRichMessageListener listener, Message message, KmThemeHelper themeHelper) {
        if (model.getTemplateId() == AlRichMessageFactory.CARD_RICH_MESSAGE) {
            return new AlCardRMAdapter(context, model, listener, message, themeHelper);
        } else if (model.getTemplateId() == AlRichMessageFactory.BUTTON_RICH_MESSAGE || model.getTemplateId() == AlRichMessageFactory.REPLY_RICH_MESSAGE || model.getTemplateId() == AlRichMessageFactory.MIXED_BUTTON_RICH_MESSAGE) {
            return new AlButtonRMAdapter(context, model, listener, message, themeHelper);
        } else return null;
    }

    public ALRichMessageAdapter getImageRMAdapter(Context context, ALRichMessageModel model, ALRichMessageListener listener, Message message, AlCustomizationSettings alCustomizationSettings) {
        return new AlImageRMAdapter(context, model, listener, message, alCustomizationSettings);
    }

    public ALRichMessageAdapter getListRMAdapter(Context context, Message message, List<ALRichMessageModel.AlElementModel> elementList, Map<String, Object> replyMetadata, ALRichMessageListener messageListener, AlCustomizationSettings alCustomizationSettings) {
        return new AlListRMAdapter(context, message, elementList, replyMetadata, messageListener, KmThemeHelper.getInstance(context, alCustomizationSettings));
    }
}
