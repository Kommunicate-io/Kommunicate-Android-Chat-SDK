package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging;

import android.content.Context;
import android.os.Build;

import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.callbacks.ALRichMessageListener;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.ALRichMessageModel;
import com.applozic.mobicommons.json.GsonUtils;

import java.util.Map;

/** abstract class for a `Rich Message` implementing the factory pattern
 *
 * Created by: ashish on 28/02/18.
 * Updated by: shubhamtewari on 15 Nov 2019.
 */
public abstract class AlRichMessage {
    public static final String SEND_GUEST_LIST = "sendGuestList";
    public static final String SEND_HOTEL_RATING = "sendHotelRating";
    public static final String SEND_HOTEL_DETAILS = "sendHotelDetails";
    public static final String SEND_ROOM_DETAILS_MESSAGE = "sendRoomDetailsMessage";
    public static final String SEND_BOOKING_DETAILS = "sendBookingDetails";
    public static final String MAKE_PAYMENT = "makePayment";
    public static final String TEMPLATE_ID = "templateId_";
    public static final String LINK_URL = "linkUrl";
    public static final String WEB_LINK = "link";
    public static final String QUICK_REPLY = "quickReply";
    public static final String QUICK_REPLY_OLD = "quick_reply";
    public static final String SUBMIT_BUTTON = "submit";
    public static final String KM_FAQ_ID = "KM_FAQ_ID";
    public static final String KM_SOURCE = "source";
    public static final String KM_FORM_DATA = "formData";
    public static final String KM_FORM_ACTION = "formAction";
    public static final String SEND_MESSAGE = "sendMessage";
    public static final String OPEN_WEB_VIEW_ACTIVITY = "openWebViewActivity";

    protected Context context;
    protected Message message;
    protected ALRichMessageListener listener;
    protected LinearLayout containerView;
    protected AlCustomizationSettings alCustomizationSettings;
    protected ALRichMessageModel model;

    public AlRichMessage(Context context, LinearLayout containerView, Message message, ALRichMessageListener listener, AlCustomizationSettings alCustomizationSettings) {
        this.context = context;
        this.message = message;
        this.listener = listener;
        this.containerView = containerView;
        this.alCustomizationSettings = alCustomizationSettings;
        this.model = (ALRichMessageModel) GsonUtils.getObjectFromJson(GsonUtils.getJsonFromObject(message.getMetadata(), Map.class), ALRichMessageModel.class);
    }

    //bind views and set the visibilities according to the type of message
    public void createRichMessage() {
        if(model.getTemplateId() <= 0) {
            containerView.setVisibility(View.GONE);
            return;
        }
    }

    //display the message details from the message model
    protected void setupAlRichMessage(ViewGroup layout, final ALRichMessageModel alRichMessageModel) { }

    //display the message details from the message model
    //over riding due to faq rich message template requirement of two layouts
    protected void setupAlRichMessage(ViewGroup layout1, ViewGroup layout2, final ALRichMessageModel alRichMessageModel) { }

    private String getActionType(ALRichMessageModel model, ALRichMessageModel.AlButtonModel buttonModel) {
        if (buttonModel != null) {
            if (!TextUtils.isEmpty(buttonModel.getType())) {
                return buttonModel.getType();
            }
            if (buttonModel.getAction() != null && !TextUtils.isEmpty(buttonModel.getAction().getType())) {
                return buttonModel.getAction().getType();
            }
        }
        return TEMPLATE_ID + model.getTemplateId();
    }

    protected void setActionListener(View view, final ALRichMessageModel model, final ALRichMessageModel.AlButtonModel buttonModel, final ALRichMessageModel.ALPayloadModel payloadModel) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context.getApplicationContext() instanceof ALRichMessageListener) {
                    ((ALRichMessageListener) context.getApplicationContext()).onAction(context, getActionType(model, buttonModel), message, buttonModel, payloadModel != null ? payloadModel.getReplyMetadata() : null);
                } else {
                    if (listener != null) {
                        listener.onAction(context, getActionType(model, buttonModel), message, buttonModel, payloadModel != null ? payloadModel.getReplyMetadata() : null);
                    }
                }
            }
        });
    }

    public static Spanned getHtmlText(String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(message, Html.FROM_HTML_MODE_COMPACT);
        } else {
            return Html.fromHtml(message);
        }
    }
}