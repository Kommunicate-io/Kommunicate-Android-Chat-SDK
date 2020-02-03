package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.types;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.AlRichMessage;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.callbacks.ALRichMessageListener;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.ALRichMessageModel;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.views.KmFlowLayout;
import com.applozic.mobicommons.json.GsonUtils;

import java.util.Arrays;
import java.util.List;

public class ButtonAlRichMessage extends AlRichMessage {

    public ButtonAlRichMessage(Context context, LinearLayout containerView, Message message, ALRichMessageListener listener, AlCustomizationSettings alCustomizationSettings) {
        super(context, containerView, message, listener, alCustomizationSettings);
    }

    @Override
    public void createRichMessage() {
        super.createRichMessage();
        setupAlRichMessage((KmFlowLayout) containerView.findViewById(R.id.kmFlowLayout), model);
    }

    @Override
    protected void setupAlRichMessage(ViewGroup flowLayout, final ALRichMessageModel model) {
        super.setupAlRichMessage(flowLayout, model);
        final List<ALRichMessageModel.ALPayloadModel> payloadList = Arrays.asList((ALRichMessageModel.ALPayloadModel[])
                GsonUtils.getObjectFromJson(model.getPayload(), ALRichMessageModel.ALPayloadModel[].class));

        flowLayout.removeAllViews();
        for (final ALRichMessageModel.ALPayloadModel payloadModel : payloadList) {
            View view = LayoutInflater.from(context).inflate(R.layout.al_rich_message_single_text_item, null);
            TextView itemTextView = view.findViewById(R.id.singleTextItem);

            //for 3 and 11 use name, for 6 use title
            String buttonTitle = (model.getTemplateId() == 3 || model.getTemplateId() == 11) ?
                    (!TextUtils.isEmpty(payloadModel.getName()) ? payloadModel.getName().trim() : "") :
                    (payloadModel.getTitle() != null) ? payloadModel.getTitle().trim() : "";

            itemTextView.setText(buttonTitle);

            itemTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (context.getApplicationContext() instanceof ALRichMessageListener) {
                        ((ALRichMessageListener) context.getApplicationContext()).onAction(context, TEMPLATE_ID + model.getTemplateId(), message, payloadModel, payloadModel.getReplyMetadata());
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
}
