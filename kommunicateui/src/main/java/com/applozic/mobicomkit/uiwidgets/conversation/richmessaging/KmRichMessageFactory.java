package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging;

import android.content.Context;
import android.widget.LinearLayout;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.callbacks.KmRichMessageListener;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.v2.KmRichMessageModel;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.types.ButtonKmRichMessage;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.types.CardTypeKmRichMessage;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.types.FaqKmRichMessage;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.types.ImageKmRichMessage;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.types.KmFormRichMessage;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.types.ListKmRichMessage;

/**
 * factory class for creating `AlRichMessage` instances
 * the factory pattern is being used here
 *
 * @author shubhamtewari
 * 15 Nov 2019
 */
public class KmRichMessageFactory {

    //constants for specifying which instance to instantiate
    public final static int CARD_RICH_MESSAGE = 10;
    public final static int IMAGE_RICH_MESSAGE = 9;
    public final static int FAQ_RICH_MESSAGE = 8;
    public final static int LIST_RICH_MESSAGE = 7;
    public final static int BUTTON_RICH_MESSAGE = 3;
    public final static int REPLY_RICH_MESSAGE = 6;
    public final static int MIXED_BUTTON_RICH_MESSAGE = 11;

    //factory class is a singleton
    private KmRichMessageFactory() {
    }

    //singleton helper (Bill Pugh Method)
    private static class RMFactoryHelper {
        static final KmRichMessageFactory INSTANCE = new KmRichMessageFactory();
    }

    public static KmRichMessageFactory getInstance() {
        return RMFactoryHelper.INSTANCE;
    }

    public KmRichMessage getRichMessage(Context context, LinearLayout containerView, Message message, KmRichMessageListener listener, AlCustomizationSettings alCustomizationSettings) {
        int type = -1;
        if (message.getMetadata().containsKey("templateId")) {
            type = Integer.parseInt(message.getMetadata().get("templateId"));
        }

        if (type == KmRichMessageFactory.CARD_RICH_MESSAGE) {
            return new CardTypeKmRichMessage(context, containerView, message, listener, alCustomizationSettings);
        } else if (type == KmRichMessageFactory.IMAGE_RICH_MESSAGE) {
            return new ImageKmRichMessage(context, containerView, message, listener, alCustomizationSettings);
        } else if (type == KmRichMessageFactory.LIST_RICH_MESSAGE) {
            return new ListKmRichMessage(context, containerView, message, listener, alCustomizationSettings);
        } else if (type == KmRichMessageFactory.FAQ_RICH_MESSAGE) {
            return new FaqKmRichMessage(context, containerView, message, listener, alCustomizationSettings);
        } else if (type == KmRichMessageModel.TemplateId.FORM.getValue()) {
            return new KmFormRichMessage(context, containerView, message, listener, alCustomizationSettings);
        } else if (type == KmRichMessageFactory.BUTTON_RICH_MESSAGE || type == KmRichMessageFactory.REPLY_RICH_MESSAGE || type == KmRichMessageFactory.MIXED_BUTTON_RICH_MESSAGE) {
            return new ButtonKmRichMessage(context, containerView, message, listener, alCustomizationSettings);
        } else {
            return null;
        }
    }
}
