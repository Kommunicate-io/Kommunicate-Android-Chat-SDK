package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.callbacks.ALRichMessageListener;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.ALRichMessageModel;
import com.applozic.mobicommons.json.GsonUtils;

import java.util.List;

public class FaqAlRichMessage extends AlRichMessage {

    public FaqAlRichMessage(Context context, LinearLayout containerView, Message message, ALRichMessageListener listener, AlCustomizationSettings alCustomizationSettings) {
        super(context, containerView, message, listener, alCustomizationSettings);
    }

    @Override
    public void createRichMessage() {
        super.createRichMessage();
        LinearLayout faqReplyLayout = containerView.findViewById(R.id.alFaqReplyLayout);
        LinearLayout faqLayout = containerView.findViewById(R.id.alFaqLayout);
        faqLayout.setVisibility(View.VISIBLE);
        faqReplyLayout.setVisibility(View.VISIBLE);
        containerView.findViewById(R.id.alListMessageLayout).setVisibility(View.GONE);
        containerView.findViewById(R.id.alRichMessageContainer).setVisibility(View.GONE);
        containerView.findViewById(R.id.alQuickReplyRecycler).setVisibility(View.GONE);
        setupAlRichMessage(faqLayout, faqReplyLayout, model);
    }

    @Override
    protected void setupAlRichMessage(ViewGroup faqLayout, ViewGroup faqReplyLayout, ALRichMessageModel model) {
        super.setupAlRichMessage(faqLayout, faqReplyLayout, model);
        if (model != null) {
            TextView headerText = faqLayout.findViewById(R.id.headerText);
            TextView titleText = faqLayout.findViewById(R.id.questionText);
            TextView descriptionText = faqLayout.findViewById(R.id.bodyText);
            TextView buttonLabel = faqReplyLayout.findViewById(R.id.buttonLabel);
            TextView actionYes = faqReplyLayout.findViewById(R.id.actionYes);
            TextView actionNo = faqReplyLayout.findViewById(R.id.actionNo);

            if (model.getPayload() != null) {
                ALRichMessageModel.ALPayloadModel payload = (ALRichMessageModel.ALPayloadModel) GsonUtils.getObjectFromJson(model.getPayload(), ALRichMessageModel.ALPayloadModel.class);
                if (payload != null) {
                    if (!TextUtils.isEmpty(payload.getTitle())) {
                        titleText.setVisibility(View.VISIBLE);
                        titleText.setText(getHtmlText(payload.getTitle()));
                    } else {
                        titleText.setVisibility(View.GONE);
                    }

                    if (!TextUtils.isEmpty(payload.getDescription())) {
                        descriptionText.setVisibility(View.VISIBLE);
                        descriptionText.setText(getHtmlText(payload.getDescription()));
                    } else {
                        descriptionText.setVisibility(View.GONE);
                    }

                    List<ALRichMessageModel.AlButtonModel> actionModel = payload.getButtons();
                    if (actionModel != null) {
                        faqReplyLayout.setVisibility(View.VISIBLE);

                        if (!TextUtils.isEmpty(payload.getButtonLabel())) {
                            buttonLabel.setVisibility(View.VISIBLE);
                            buttonLabel.setText(payload.getButtonLabel());
                        } else {
                            buttonLabel.setVisibility(View.GONE);
                        }

                        if (actionModel.size() > 0 && actionModel.get(0) != null) {
                            if (!TextUtils.isEmpty(actionModel.get(0).getName())) {
                                actionYes.setVisibility(View.VISIBLE);
                                actionYes.setText(actionModel.get(0).getName());
                                setActionListener(actionYes, model, actionModel.get(0), payload);
                            } else {
                                actionYes.setVisibility(View.GONE);
                            }
                        }

                        if (actionModel.size() > 1 && actionModel.get(1) != null) {
                            if (!TextUtils.isEmpty(actionModel.get(1).getName())) {
                                actionNo.setVisibility(View.VISIBLE);
                                actionNo.setText(actionModel.get(1).getName());
                                setActionListener(actionNo, model, actionModel.get(1), payload);
                            } else {
                                actionNo.setVisibility(View.GONE);
                            }
                        }
                    } else {
                        faqReplyLayout.setVisibility(View.GONE);
                    }
                }
            }
        }
    }
}
