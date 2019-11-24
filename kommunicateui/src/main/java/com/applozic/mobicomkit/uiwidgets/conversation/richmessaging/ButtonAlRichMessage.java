package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging;

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
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.callbacks.ALRichMessageListener;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.ALRichMessageModel;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.views.KmCustomLayoutManager;
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
        KmCustomLayoutManager quickRepliesRecycler = containerView.findViewById(R.id.alQuickReplyRecycler);
        containerView.findViewById(R.id.alListMessageLayout).setVisibility(View.GONE);
        containerView.findViewById(R.id.alRichMessageContainer).setVisibility(View.GONE);
        containerView.findViewById(R.id.alFaqLayout).setVisibility(View.GONE);
        containerView.findViewById(R.id.alFaqReplyLayout).setVisibility(View.GONE);
        quickRepliesRecycler.setVisibility(View.VISIBLE);
        setupAlRichMessage(quickRepliesRecycler, model);
    }

    @Override
    protected void setupAlRichMessage(ViewGroup flowLayout, final ALRichMessageModel model) {
        final List<ALRichMessageModel.ALPayloadModel> payloadList = Arrays.asList((ALRichMessageModel.ALPayloadModel[])
                GsonUtils.getObjectFromJson(model.getPayload(), ALRichMessageModel.ALPayloadModel[].class));

        if (model.getTemplateId() == 3 || model.getTemplateId() == 6) {
            flowLayout.removeAllViews();
            for (final ALRichMessageModel.ALPayloadModel payloadModel : payloadList) {
                View view = LayoutInflater.from(context).inflate(R.layout.al_rich_message_single_text_item, null);
                TextView itemTextView = view.findViewById(R.id.singleTextItem);

                if (model.getTemplateId() == 3) {
                    if (!TextUtils.isEmpty(payloadModel.getName())) {
                        itemTextView.setText(payloadModel.getName().trim());
                    } else {
                        itemTextView.setText("");
                    }
                } else {
                    if (payloadModel.getTitle() != null) {
                        itemTextView.setText(payloadModel.getTitle().trim());
                    } else {
                        itemTextView.setText("");
                    }
                }

                itemTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (model.getTemplateId() == 6) {
                            if (context.getApplicationContext() instanceof ALRichMessageListener) {
                                ((ALRichMessageListener) context.getApplicationContext()).onAction(context, TEMPLATE_ID + model.getTemplateId(), message, payloadModel, payloadModel.getReplyMetadata());
                            } else {
                                listener.onAction(context, AlRichMessage.SEND_HOTEL_RATING, message, payloadModel.getMessage().trim(), payloadModel.getReplyMetadata());
                            }
                        } else {
                            if (context.getApplicationContext() instanceof ALRichMessageListener) {
                                ((ALRichMessageListener) context.getApplicationContext()).onAction(context, TEMPLATE_ID + model.getTemplateId(), message, payloadModel, payloadModel.getReplyMetadata());
                            } else {
                                if (!TextUtils.isEmpty(model.getFormData()) && !TextUtils.isEmpty(model.getFormAction())) {
                                    listener.onAction(context, AlRichMessage.MAKE_PAYMENT, message, model, payloadModel != null ? payloadModel.getReplyMetadata() : null);
                                } else {
                                    listener.onAction(context, AlRichMessage.WEB_LINK, message, payloadModel, payloadModel.getReplyMetadata());
                                }
                            }
                        }
                    }
                });
                flowLayout.addView(view);
            }
        }
    }
}
