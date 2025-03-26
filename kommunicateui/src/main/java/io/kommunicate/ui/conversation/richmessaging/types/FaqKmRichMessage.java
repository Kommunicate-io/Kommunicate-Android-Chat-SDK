package io.kommunicate.ui.conversation.richmessaging.types;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.kommunicate.devkit.api.conversation.Message;
import io.kommunicate.ui.AlCustomizationSettings;
import io.kommunicate.ui.R;
import io.kommunicate.ui.conversation.richmessaging.KmRichMessage;
import io.kommunicate.ui.conversation.richmessaging.callbacks.KmRichMessageListener;
import io.kommunicate.ui.conversation.richmessaging.models.KmRichMessageModel;
import io.kommunicate.ui.kommunicate.utils.DimensionsUtils;
import io.kommunicate.commons.json.GsonUtils;

import java.util.List;

import io.kommunicate.utils.KmUtils;

public class FaqKmRichMessage extends KmRichMessage {

    public FaqKmRichMessage(Context context, LinearLayout containerView, Message message, KmRichMessageListener listener, AlCustomizationSettings alCustomizationSettings, boolean showTimestamp, boolean isDarkModeEnabled) {
        super(context, containerView, message, listener, alCustomizationSettings, showTimestamp, isDarkModeEnabled);
    }

    @Override
    public void createRichMessage(boolean isMessageProcessed) {
        super.createRichMessage(isMessageProcessed);

        if (model != null) {
            TextView headerText = faqLayout.findViewById(R.id.headerText);
            TextView titleText = faqLayout.findViewById(R.id.questionText);
            TextView descriptionText = faqLayout.findViewById(R.id.bodyText);
            TextView buttonLabel = faqReplyLayout.findViewById(R.id.buttonLabel);
            TextView actionYes = faqReplyLayout.findViewById(R.id.actionYes);
            TextView actionNo = faqReplyLayout.findViewById(R.id.actionNo);

            if (model.getPayload() != null) {
                KmRichMessageModel.KmPayloadModel payload = (KmRichMessageModel.KmPayloadModel) GsonUtils.getObjectFromJson(model.getPayload(), KmRichMessageModel.KmPayloadModel.class);
                if (payload != null) {
                    if (!TextUtils.isEmpty(payload.getTitle())) {
                        titleText.setVisibility(View.VISIBLE);
                        titleText.setText(getHtmlText(payload.getTitle()));
                    }

                    if (!TextUtils.isEmpty(payload.getDescription())) {
                        descriptionText.setVisibility(View.VISIBLE);
                        descriptionText.setText(getHtmlText(payload.getDescription()));
                    }

                    List<KmRichMessageModel.KmButtonModel> actionModel = payload.getButtons();
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

    //setup he actionYes and actionNo text views
    void setActionTextView(TextView actionTextView, KmRichMessageModel.KmButtonModel actionModel, KmRichMessageModel.KmPayloadModel payload) {
        if (!TextUtils.isEmpty(actionModel.getName())) {
            actionTextView.setVisibility(View.VISIBLE);
            actionTextView.setText(actionModel.getName());
            KmUtils.setGradientStrokeColor(actionTextView, DimensionsUtils.convertDpToPx(1), themeHelper.getRichMessageThemeColor());
            actionTextView.setTextColor(themeHelper.getRichMessageThemeColor());
            setActionListener(actionTextView, model, actionModel, payload);
        }
    }
}
