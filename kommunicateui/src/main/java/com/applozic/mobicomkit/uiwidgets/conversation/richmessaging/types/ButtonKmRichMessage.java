package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.types;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.KmRichMessage;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.callbacks.KmRichMessageListener;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.KmRichMessageModel;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.v2.KmRMActionModel;
import com.applozic.mobicomkit.uiwidgets.kommunicate.utils.DimensionsUtils;
import com.applozic.mobicommons.json.GsonUtils;

import java.util.Arrays;
import java.util.List;

import io.kommunicate.utils.KmUtils;

public class ButtonKmRichMessage extends KmRichMessage {

    public ButtonKmRichMessage(Context context, LinearLayout containerView, Message message, KmRichMessageListener listener, AlCustomizationSettings alCustomizationSettings) {
        super(context, containerView, message, listener, alCustomizationSettings);
    }

    @Override
    public void createRichMessage() {
        super.createRichMessage();
        final List<KmRichMessageModel.KmPayloadModel> payloadList = Arrays.asList((KmRichMessageModel.KmPayloadModel[])
                GsonUtils.getObjectFromJson(model.getPayload(), KmRichMessageModel.KmPayloadModel[].class));

        flowLayout.removeAllViews();
        for (final KmRichMessageModel.KmPayloadModel payloadModel : payloadList) {
            View view = LayoutInflater.from(context).inflate(R.layout.km_rich_message_single_text_item, null);
            TextView itemTextView = view.findViewById(R.id.singleTextItem);

            KmUtils.setGradientStrokeColor(itemTextView, DimensionsUtils.convertDpToPx(1), themeHelper.getRichMessageThemeColor());
            itemTextView.setTextColor(themeHelper.getRichMessageThemeColor());

            //for 3 and 11 use name, for 6 use title
            String buttonTitle = (model.getTemplateId() == 3 || model.getTemplateId() == 11) ?
                    (!TextUtils.isEmpty(payloadModel.getName()) ? payloadModel.getName().trim() : "") :
                    (payloadModel.getTitle() != null) ? payloadModel.getTitle().trim() : "";

            itemTextView.setText(buttonTitle);

            itemTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (context.getApplicationContext() instanceof KmRichMessageListener) {
                        ((KmRichMessageListener) context.getApplicationContext()).onAction(context, TEMPLATE_ID + model.getTemplateId(), message, payloadModel, payloadModel.getReplyMetadata());
                    } else {
                        String actionType = payloadModel.getAction() != null && !TextUtils.isEmpty(payloadModel.getAction().getType()) ? payloadModel.getAction().getType() : payloadModel.getType();
                        if (payloadModel.getAction() != null && !TextUtils.isEmpty(payloadModel.getAction().getType()) || !TextUtils.isEmpty(payloadModel.getType())) {
                            listener.onAction(context, actionType, message, payloadModel, payloadModel.getReplyMetadata());
                        } else {
                            listener.onAction(context, model.getTemplateId() == 6 ? QUICK_REPLY : SUBMIT_BUTTON, message, model.getTemplateId() == 6 ? payloadModel : model, payloadModel.getReplyMetadata());
                        }
                    }
                }
            });

            flowLayout.addView(view);
        }
    }

    public void createRichMessageV2() {
        final List<KmRMActionModel> actionModelList = kmRichMessageModel.getButtonList();

        flowLayout.removeAllViews();

        for (final KmRMActionModel actionModel : actionModelList) {
            View view = LayoutInflater.from(context).inflate(R.layout.km_rich_message_single_text_item, null);
            TextView itemTextView = view.findViewById(R.id.singleTextItem);

            itemTextView.setText(actionModel.getName());

            itemTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (context.getApplicationContext() instanceof KmRichMessageListener) {
                        ((KmRichMessageListener) context.getApplicationContext()).onAction(context, actionModel.getType(), message, actionModel.getAction(), null);
                    } else {
                        listener.onAction(context, actionModel.getType(), message, actionModel.getAction(), null);
                    }
                }
            });

            flowLayout.addView(view);
        }
    }
}
