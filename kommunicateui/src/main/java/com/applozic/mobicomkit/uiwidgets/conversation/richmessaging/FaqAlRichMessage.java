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
        setupAlRichMessage((LinearLayout) containerView.findViewById(R.id.alFaqLayout), (LinearLayout) containerView.findViewById(R.id.alFaqReplyLayout), model);
    }

    //setup he actionYes and actionNo text views
    void setActionTextView(TextView actionTextView, ALRichMessageModel.AlButtonModel actionModel, ALRichMessageModel.ALPayloadModel payload) {
        if (!TextUtils.isEmpty(actionModel.getName())) {
            actionTextView.setVisibility(View.VISIBLE);
            actionTextView.setText(actionModel.getName());
            setActionListener(actionTextView, model, actionModel, payload);
        }
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
                    }

                    if (!TextUtils.isEmpty(payload.getDescription())) {
                        descriptionText.setVisibility(View.VISIBLE);
                        descriptionText.setText(getHtmlText(payload.getDescription()));
                    }

                    List<ALRichMessageModel.AlButtonModel> actionModel = payload.getButtons();
                    if (actionModel != null) {
                        faqReplyLayout.setVisibility(View.VISIBLE);

                        if (!TextUtils.isEmpty(payload.getButtonLabel())) {
                            buttonLabel.setVisibility(View.VISIBLE);
                            buttonLabel.setText(payload.getButtonLabel());
                        }

                        if (actionModel.size() > 0 && actionModel.get(0) != null) {
                            setActionTextView(actionYes, actionModel.get(0), payload);
                        }

                        if (actionModel.size() > 1 && actionModel.get(1) != null) {
                            setActionTextView(actionNo, actionModel.get(1), payload);
                        }
                    } else {
                        faqReplyLayout.setVisibility(View.GONE);
                    }
                }
            }
        }
    }
}
