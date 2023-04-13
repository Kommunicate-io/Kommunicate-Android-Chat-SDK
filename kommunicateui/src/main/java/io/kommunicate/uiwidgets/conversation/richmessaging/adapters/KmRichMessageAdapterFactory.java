package io.kommunicate.uiwidgets.conversation.richmessaging.adapters;

import android.content.Context;

import io.kommunicate.data.conversation.Message;
import io.kommunicate.uiwidgets.AlCustomizationSettings;
import io.kommunicate.uiwidgets.conversation.richmessaging.KmRichMessageFactory;
import io.kommunicate.uiwidgets.conversation.richmessaging.callbacks.KmRichMessageListener;
import io.kommunicate.uiwidgets.conversation.richmessaging.models.KmRichMessageModel;
import io.kommunicate.uiwidgets.kommunicate.utils.KmThemeHelper;

import java.util.List;
import java.util.Map;

/**
 * factory class to create instances of rich message adapters
 * implementing the factory pattern
 *
 * @author shubhamtewari
 * created on: 23 Nov, 2019
 */
public class KmRichMessageAdapterFactory {
    //factory class is a singleton
    private KmRichMessageAdapterFactory() {
    }

    //singleton helper (Bill Pugh Method)
    private static class RMFactoryHelper {
        static final KmRichMessageAdapterFactory INSTANCE = new KmRichMessageAdapterFactory();
    }

    public static KmRichMessageAdapterFactory getInstance() {
        return RMFactoryHelper.INSTANCE;
    }

    public KmRichMessageAdapter getRMAdapter(Context context, KmRichMessageModel model, KmRichMessageListener listener, Message message, KmThemeHelper themeHelper, boolean isMessageProcessed, AlCustomizationSettings alCustomizationSettings) {
        if (model.getTemplateId() == KmRichMessageFactory.CARD_RICH_MESSAGE) {
            return new KmCardRMAdapter(context, model, listener, message, themeHelper, isMessageProcessed);
        } else if (model.getTemplateId() == KmRichMessageFactory.BUTTON_RICH_MESSAGE || model.getTemplateId() == KmRichMessageFactory.REPLY_RICH_MESSAGE || model.getTemplateId() == KmRichMessageFactory.MIXED_BUTTON_RICH_MESSAGE) {
            return new KmButtonRMAdapter(context, model, listener, message, themeHelper);
        } else if (model.getTemplateId() == KmRichMessageFactory.VIDEO_RICH_MESSAGE) {
            return new KmVideoRMAdapter(context, model, listener, message, themeHelper, isMessageProcessed, alCustomizationSettings);
        } else {
            return null;
        }
    }

    public KmRichMessageAdapter getImageRMAdapter(Context context, KmRichMessageModel model, KmRichMessageListener listener, Message message, AlCustomizationSettings alCustomizationSettings) {
        return new KmImageRMAdapter(context, model, listener, message, alCustomizationSettings);
    }

    public KmRichMessageAdapter getListRMAdapter(Context context, Message message, List<KmRichMessageModel.KmElementModel> elementList, Map<String, Object> replyMetadata, KmRichMessageListener messageListener, AlCustomizationSettings alCustomizationSettings, boolean isMessageProcessed) {
        return new KmListRMAdapter(context, message, elementList, replyMetadata, messageListener, KmThemeHelper.getInstance(context, alCustomizationSettings));
    }
}
