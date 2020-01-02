package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging;

import android.content.Context;
import android.widget.LinearLayout;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.callbacks.ALRichMessageListener;

/**
 * factory class for creating `AlRichMessage` instances
 * the factory pattern is being used here
 *
 * @author shubhamtewari
 * 15 Nov 2019
 */
public class AlRichMessageFactory {

    //constants for specifying which instance to instantiate
    public final static int CARD_RICH_MESSAGE = 10;
    public final static int IMAGE_RICH_MESSAGE = 9;
    public final static int FAQ_RICH_MESSAGE = 8;
    public final static int LIST_RICH_MESSAGE = 7;
    public final static int BUTTON_RICH_MESSAGE = 3;
    public final static int REPLY_RICH_MESSAGE = 6;
    public final static int MIXED_BUTTON_RICH_MESSAGE = 11;

    //factory class is a singleton
    private AlRichMessageFactory() {
    }

    //singleton helper (Bill Pugh Method)
    private static class RMFactoryHelper {
        static final AlRichMessageFactory INSTANCE = new AlRichMessageFactory();
    }

    public static AlRichMessageFactory getInstance() {
        return RMFactoryHelper.INSTANCE;
    }

    public AlRichMessage getRichMessage(Context context, LinearLayout containerView, Message message, ALRichMessageListener listener, AlCustomizationSettings alCustomizationSettings) {
        int type = -1;
        if (message.getMetadata().containsKey("templateId")) {
            type = Integer.parseInt(message.getMetadata().get("templateId"));
        }

        if (type == AlRichMessageFactory.CARD_RICH_MESSAGE)
            return new CardTypeAlRichMessage(context, containerView, message, listener, alCustomizationSettings);
        else if (type == AlRichMessageFactory.IMAGE_RICH_MESSAGE)
            return new ImageAlRichMessage(context, containerView, message, listener, alCustomizationSettings);
        else if (type == AlRichMessageFactory.LIST_RICH_MESSAGE)
            return new ListAlRichMessage(context, containerView, message, listener, alCustomizationSettings);
        else if (type == AlRichMessageFactory.FAQ_RICH_MESSAGE)
            return new FaqAlRichMessage(context, containerView, message, listener, alCustomizationSettings);
        else if (type == AlRichMessageFactory.BUTTON_RICH_MESSAGE || type == AlRichMessageFactory.REPLY_RICH_MESSAGE || type == AlRichMessageFactory.MIXED_BUTTON_RICH_MESSAGE)
            return new ButtonAlRichMessage(context, containerView, message, listener, alCustomizationSettings);
        else return null;
    }
}
