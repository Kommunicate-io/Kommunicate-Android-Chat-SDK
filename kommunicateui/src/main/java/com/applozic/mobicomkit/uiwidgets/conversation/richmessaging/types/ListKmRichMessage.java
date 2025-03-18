package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.types;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.kommunicate.devkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.KmRichMessage;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.adapters.KmActionButtonRMAdapter;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.adapters.KmListRMAdapter;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.adapters.KmRichMessageAdapterFactory;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.callbacks.KmRichMessageListener;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.KmRichMessageModel;
import com.applozic.mobicomkit.uiwidgets.kommunicate.utils.KmThemeHelper;
import io.kommunicate.commons.json.GsonUtils;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ListKmRichMessage extends KmRichMessage {

    public static final int MAX_ACTIONS_LIMIT = 3;
    public static final int QUICK_REPLY_TEMPLATE_ID = 6;

    public ListKmRichMessage(Context context, LinearLayout containerView, Message message, KmRichMessageListener listener, AlCustomizationSettings alCustomizationSettings, boolean showTimestamp, boolean isDarkModeEnabled) {
        super(context, containerView, message, listener, alCustomizationSettings, showTimestamp, isDarkModeEnabled);
    }

    @Override
    public void createRichMessage(boolean isMessageProcessed) {
        super.createRichMessage(isMessageProcessed);

        if (model != null) {
            if (model.getPayload() != null) {
                TextView headerText = listItemLayout.findViewById(R.id.headerText);
                ImageView headerImage = listItemLayout.findViewById(R.id.headerImage);

                KmRichMessageModel.KmPayloadModel payload = (KmRichMessageModel.KmPayloadModel) GsonUtils.getObjectFromJson(model.getPayload(), KmRichMessageModel.KmPayloadModel.class);
                if (payload != null) {
                    RecyclerView listRecycler = listItemLayout.findViewById(R.id.alListItemRecycler);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
                    listRecycler.setLayoutManager(layoutManager);
                    KmListRMAdapter adapter = (KmListRMAdapter) KmRichMessageAdapterFactory.getInstance().getListRMAdapter(context, message, getFilteredList(isMessageProcessed, payload.getElements()), payload.getReplyMetadata(), listener, alCustomizationSettings, isMessageProcessed);
                    listRecycler.setAdapter(adapter);

                    if (!TextUtils.isEmpty(payload.getHeaderText())) {
                        headerText.setVisibility(View.VISIBLE);
                        headerText.setText(getHtmlText(payload.getHeaderText()));
                    } else {
                        headerText.setVisibility(View.GONE);
                    }

                    if (!TextUtils.isEmpty(payload.getHeaderImgSrc())) {
                        headerImage.setVisibility(View.VISIBLE);
                        Glide.with(context).load(payload.getHeaderImgSrc()).into(headerImage);
                    } else {
                        headerImage.setVisibility(View.GONE);
                    }

                    if (payload.getButtons() != null) {
                        final List<KmRichMessageModel.KmButtonModel> actionButtons = payload.getButtons();
                        final List<KmRichMessageModel.KmButtonModel> actionButtonsToBeShown = new ArrayList<>();
                        for (KmRichMessageModel.KmButtonModel actionButton : actionButtons) {
                            if (!showAction(isMessageProcessed, actionButton)) {
                                actionButtonsToBeShown.add(actionButton);
                            }
                        }
                        RecyclerView actionButtonRecycler = listItemLayout.findViewById(R.id.alActionButtonRecycler);
                        LinearLayoutManager actionButtonLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
                        actionButtonRecycler.setLayoutManager(actionButtonLayoutManager);
                        KmActionButtonRMAdapter actionButtonAdapter = (KmActionButtonRMAdapter) KmRichMessageAdapterFactory.getInstance().getActionButtonRMAdapter(context, message, actionButtonsToBeShown, listener, themeHelper);
                        actionButtonRecycler.setAdapter(actionButtonAdapter);
                    }
                }

            }
        }
    }

    private void setActionTextView(TextView actionTextView, View actionDivider, KmRichMessageModel.KmButtonModel buttonModel, KmRichMessageModel.KmPayloadModel payload, KmRichMessageModel model) {
        actionTextView.setVisibility(View.VISIBLE);
        actionTextView.setText(buttonModel.getName());
        actionTextView.setTextColor(themeHelper.getRichMessageThemeColor());
        setActionListener(actionTextView, model, buttonModel, payload);

        if (actionDivider != null) {
            actionDivider.setVisibility(View.VISIBLE);
        }
    }

    private List<KmRichMessageModel.KmElementModel> getFilteredList(boolean isMessageProcessed, List<KmRichMessageModel.KmElementModel> elementList) {
        if (elementList == null) return new ArrayList<>();
        List<KmRichMessageModel.KmElementModel> newList = new ArrayList<>();
        for (KmRichMessageModel.KmElementModel element : elementList) {
            if (isMessageProcessed && hideMessage(themeHelper, element.getAction().getType())) {
                continue;
            }
            newList.add(element);
        }
        return newList;
    }

    private boolean showAction(boolean isMessageProcessed, KmRichMessageModel.KmButtonModel action) {

        if (action.getType() == null) {
            return (isMessageProcessed && hideMessage(themeHelper, action.getAction().getType()));
        }
        return (isMessageProcessed && hideMessage(themeHelper, action.getType()));
    }

    public static boolean hideMessage(KmThemeHelper themeHelper, String action) {
        return (themeHelper.hideLinkButtonsPostCTA() && KmRichMessage.WEB_LINK.equals(action))
                || (themeHelper.hideQuickRepliesPostCTA() && KmRichMessage.QUICK_REPLY_OLD.equals(action))
                || (themeHelper.hideSubmitButtonsPostCTA() && KmRichMessage.SUBMIT_BUTTON.equals(action))
                || (themeHelper.hideQuickRepliesPostCTA() && KmRichMessage.QUICK_REPLY.equals(action));
    }

    private String getActionType(KmRichMessageModel.KmButtonModel payloadModel, Short templateId) {
        if (payloadModel.getAction() == null) {
            return templateId == QUICK_REPLY_TEMPLATE_ID ? QUICK_REPLY : SUBMIT_BUTTON;
        }
        return payloadModel.getAction() != null && !TextUtils.isEmpty(payloadModel.getAction().getType()) ? payloadModel.getAction().getType() : payloadModel.getType();
    }
}
