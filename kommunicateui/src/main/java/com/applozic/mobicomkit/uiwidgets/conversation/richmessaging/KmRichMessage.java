package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;

import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.callbacks.KmRichMessageListener;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.KmRichMessageModel;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.views.KmFlowLayout;
import com.applozic.mobicomkit.uiwidgets.kommunicate.utils.KmThemeHelper;
import com.applozic.mobicommons.commons.core.utils.DateUtils;
import com.applozic.mobicommons.json.GsonUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

/**
 * abstract class for a `Rich Message` implementing the factory pattern
 * <p>
 * Created by: ashish on 28/02/18.
 * Updated by: shubhamtewari on 15 Nov 2019.
 */
public abstract class KmRichMessage {
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
    public static final String IS_DEEP_LINK = "isDeepLink";

    protected Context context;
    protected Message message;
    protected KmRichMessageListener listener;
    private LinearLayout containerView;
    protected LinearLayout listItemLayout;
    protected LinearLayout faqReplyLayout;
    protected LinearLayout faqLayout;
    protected RecyclerView genericCardRecycler;
    protected RecyclerView imageListRecycler;
    protected RecyclerView alFormLayoutRecycler;
    protected RecyclerView videoTemplateRecycler;
    protected KmFlowLayout flowLayout;
    protected AlCustomizationSettings alCustomizationSettings;
    protected KmRichMessageModel model;
    protected com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.v2.KmRichMessageModel kmRichMessageModel;
    protected KmThemeHelper themeHelper;
    protected Gson gson;
    protected TextView createdAtTime;
    protected boolean showTimestamp;


    public KmRichMessage(Context context, LinearLayout containerView, Message message, KmRichMessageListener listener, AlCustomizationSettings alCustomizationSettings, boolean showTimestamp) {
        this.context = context;
        this.message = message;
        this.listener = listener;
        this.containerView = containerView;
        this.alCustomizationSettings = alCustomizationSettings;
        this.gson = new Gson();
        this.showTimestamp = showTimestamp;
        this.model = (KmRichMessageModel) GsonUtils.getObjectFromJson(GsonUtils.getJsonFromObject(message.getMetadata(), Map.class), KmRichMessageModel.class);
        this.kmRichMessageModel = gson.fromJson(GsonUtils.getJsonFromObject(message.getMetadata(), Map.class), new TypeToken<com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.v2.KmRichMessageModel>() {
        }.getType());
        themeHelper = KmThemeHelper.getInstance(context, alCustomizationSettings);
    }

    //bind views and set the visibilities according to the type of message
    public void createRichMessage(boolean isMessageProcessed) {
        if (model.getTemplateId() <= 0) {
            containerView.setVisibility(View.GONE);
            return;
        }

        listItemLayout = containerView.findViewById(R.id.alListMessageLayout);
        faqReplyLayout = containerView.findViewById(R.id.alFaqReplyLayout);
        faqLayout = containerView.findViewById(R.id.alFaqLayout);
        genericCardRecycler = containerView.findViewById(R.id.alGenericCardContainer);
        imageListRecycler = containerView.findViewById(R.id.alImageListContainer);
        videoTemplateRecycler = containerView.findViewById(R.id.videoTemplateContainer);
        flowLayout = containerView.findViewById(R.id.kmFlowLayout);
        alFormLayoutRecycler = containerView.findViewById(R.id.alFormLayoutRecycler);
        createdAtTime = containerView.findViewById(R.id.createdAt);
        createdAtTime.setVisibility(showTimestamp ? View.VISIBLE : View.GONE);
        createdAtTime.setText(DateUtils.getFormattedDate(message.getCreatedAtTime()));
        if(!TextUtils.isEmpty(alCustomizationSettings.getReceivedMessageCreatedAtTimeColor())) {
            createdAtTime.setTextColor(Color.parseColor(alCustomizationSettings.getReceivedMessageCreatedAtTimeColor()));
        }

        handleLayoutVisibilities(model.getTemplateId());
    }

    private void handleLayoutVisibilities(Short templateId) {
        listItemLayout.setVisibility(templateId == KmRichMessageFactory.LIST_RICH_MESSAGE ? View.VISIBLE : View.GONE);
        genericCardRecycler.setVisibility(templateId == KmRichMessageFactory.CARD_RICH_MESSAGE ? View.VISIBLE : View.GONE);
        faqLayout.setVisibility(templateId == KmRichMessageFactory.FAQ_RICH_MESSAGE ? View.VISIBLE : View.GONE);
        faqReplyLayout.setVisibility(templateId == KmRichMessageFactory.FAQ_RICH_MESSAGE ? View.VISIBLE : View.GONE);
        imageListRecycler.setVisibility(templateId == KmRichMessageFactory.IMAGE_RICH_MESSAGE ? View.VISIBLE : View.GONE);
        videoTemplateRecycler.setVisibility(templateId == KmRichMessageFactory.VIDEO_RICH_MESSAGE ? View.VISIBLE : View.GONE);

        alFormLayoutRecycler.setVisibility(templateId == KmRichMessageFactory.FORM_RICH_MESSAGE ? View.VISIBLE : View.GONE);
        flowLayout.setVisibility((templateId == KmRichMessageFactory.BUTTON_RICH_MESSAGE || templateId == KmRichMessageFactory.REPLY_RICH_MESSAGE || templateId == KmRichMessageFactory.MIXED_BUTTON_RICH_MESSAGE || templateId == KmRichMessageFactory.FORM_RICH_MESSAGE) ? View.VISIBLE : View.GONE);

    }

    private String getActionType(KmRichMessageModel model, KmRichMessageModel.KmButtonModel buttonModel) {
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

    protected void setActionListener(View view, final KmRichMessageModel model, final KmRichMessageModel.KmButtonModel buttonModel, final KmRichMessageModel.KmPayloadModel payloadModel) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context.getApplicationContext() instanceof KmRichMessageListener) {
                    ((KmRichMessageListener) context.getApplicationContext()).onAction(context, getActionType(model, buttonModel), message, buttonModel, payloadModel != null ? payloadModel.getReplyMetadata() : null);
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