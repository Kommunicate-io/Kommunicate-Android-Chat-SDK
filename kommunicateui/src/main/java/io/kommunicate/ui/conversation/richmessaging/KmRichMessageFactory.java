package io.kommunicate.ui.conversation.richmessaging;

import android.content.Context;
import android.widget.LinearLayout;

import io.kommunicate.devkit.api.conversation.Message;
import io.kommunicate.ui.AlCustomizationSettings;
import io.kommunicate.ui.conversation.richmessaging.callbacks.KmRichMessageListener;
import io.kommunicate.ui.conversation.richmessaging.models.v2.KmRichMessageModel;
import io.kommunicate.ui.conversation.richmessaging.types.ButtonKmRichMessage;
import io.kommunicate.ui.conversation.richmessaging.types.CardTypeKmRichMessage;
import io.kommunicate.ui.conversation.richmessaging.types.FaqKmRichMessage;
import io.kommunicate.ui.conversation.richmessaging.types.ImageKmRichMessage;
import io.kommunicate.ui.conversation.richmessaging.types.KmFormRichMessage;
import io.kommunicate.ui.conversation.richmessaging.types.ListKmRichMessage;
import io.kommunicate.ui.conversation.richmessaging.types.VideoRichMessage;

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
    public final static int VIDEO_RICH_MESSAGE = 14;
    public final static int FORM_RICH_MESSAGE = 12;
    private static final String TEMPLATE_ID = "templateId";

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

    public KmRichMessage getRichMessage(Context context, LinearLayout containerView, Message message, KmRichMessageListener listener, AlCustomizationSettings alCustomizationSettings, boolean showTimestamp, boolean isDarkModeEnabled) {
        int type = -1;
        if (message.getMetadata().containsKey(TEMPLATE_ID)) {
            type = Integer.parseInt(message.getMetadata().get(TEMPLATE_ID));
        }

        if (type == KmRichMessageFactory.CARD_RICH_MESSAGE) {
            return new CardTypeKmRichMessage(context, containerView, message, listener, alCustomizationSettings, showTimestamp, isDarkModeEnabled);
        } else if (type == KmRichMessageFactory.IMAGE_RICH_MESSAGE) {
            return new ImageKmRichMessage(context, containerView, message, listener, alCustomizationSettings, showTimestamp, isDarkModeEnabled);
        } else if (type == KmRichMessageFactory.LIST_RICH_MESSAGE) {
            return new ListKmRichMessage(context, containerView, message, listener, alCustomizationSettings, showTimestamp, isDarkModeEnabled);
        } else if (type == KmRichMessageFactory.FAQ_RICH_MESSAGE) {
            return new FaqKmRichMessage(context, containerView, message, listener, alCustomizationSettings, showTimestamp, isDarkModeEnabled);
        } else if (type == KmRichMessageModel.TemplateId.FORM.getValue()) {
            return new KmFormRichMessage(context, containerView, message, listener, alCustomizationSettings, showTimestamp, isDarkModeEnabled);
        } else if (type == KmRichMessageFactory.BUTTON_RICH_MESSAGE || type == KmRichMessageFactory.REPLY_RICH_MESSAGE || type == KmRichMessageFactory.MIXED_BUTTON_RICH_MESSAGE) {
            return new ButtonKmRichMessage(context, containerView, message, listener, alCustomizationSettings, showTimestamp, isDarkModeEnabled);
        } else if (type == KmRichMessageFactory.VIDEO_RICH_MESSAGE) {
            return new VideoRichMessage(context, containerView, message, listener, alCustomizationSettings, showTimestamp, isDarkModeEnabled);
        } else {
            return null;
        }
    }
}
