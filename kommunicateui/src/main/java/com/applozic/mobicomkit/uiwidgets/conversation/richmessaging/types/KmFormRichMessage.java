package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.types;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.KmRichMessage;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.adapters.KmFormItemAdapter;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.callbacks.KmRichMessageListener;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.v2.KmFormPayloadModel;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.v2.KmRMActionModel;
import com.applozic.mobicomkit.uiwidgets.kommunicate.utils.DimensionsUtils;

import java.util.ArrayList;
import java.util.List;

import io.kommunicate.utils.KmUtils;

public class KmFormRichMessage extends KmRichMessage {

    public KmFormRichMessage(Context context, LinearLayout containerView, Message message, KmRichMessageListener listener, AlCustomizationSettings alCustomizationSettings) {
        super(context, containerView, message, listener, alCustomizationSettings);
    }

    @Override
    public void createRichMessage(boolean isMessageProcessed) {
        super.createRichMessage(isMessageProcessed);

        LinearLayoutManager formLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        alFormLayoutRecycler.setLayoutManager(formLayoutManager);
        final KmFormItemAdapter formItemAdapter = new KmFormItemAdapter(context, kmRichMessageModel.getFormModelList(), message.getKeyString());
        alFormLayoutRecycler.setAdapter(formItemAdapter);

        if (isMessageProcessed && themeHelper.hideSubmitButtonsPostCTA()) {
            return;
        }

        List<Object> actionModelList = new ArrayList<>();

        for (Object object : kmRichMessageModel.getFormModelList()) {
            if (object instanceof KmFormPayloadModel) {
                KmFormPayloadModel formPayloadModel = (KmFormPayloadModel) object;
                if (KmFormPayloadModel.Type.SUBMIT.getValue().equals(formPayloadModel.getType()) || KmFormPayloadModel.Type.ACTION.getValue().equals(formPayloadModel.getType()) || TextUtils.isEmpty(formPayloadModel.getType())) {
                    actionModelList.add(formPayloadModel.getAction());
                }
            }
        }

        if (!actionModelList.isEmpty()) {
            if (flowLayout != null) {
                flowLayout.setVisibility(View.VISIBLE);
                flowLayout.removeAllViews();
                View view = LayoutInflater.from(context).inflate(R.layout.km_rich_message_single_text_item, null);
                TextView itemTextView = view.findViewById(R.id.singleTextItem);

                KmUtils.setGradientStrokeColor(itemTextView, DimensionsUtils.convertDpToPx(1), themeHelper.getRichMessageThemeColor());
                itemTextView.setTextColor(themeHelper.getRichMessageThemeColor());

                final KmRMActionModel<KmRMActionModel.SubmitButton> submitButtonModel = (KmRMActionModel<KmRMActionModel.SubmitButton>) actionModelList.get(0);
                itemTextView.setText(submitButtonModel.getName());

                itemTextView.setText(((KmRMActionModel) actionModelList.get(0)).getName());
                itemTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (formItemAdapter != null && formItemAdapter.isFormDataValid()) {
                            if (submitButtonModel != null && TextUtils.isEmpty(submitButtonModel.getType())) {
                                submitButtonModel.setType(KmFormPayloadModel.Type.SUBMIT.getValue());
                            }
                            if (context != null && context.getApplicationContext() instanceof KmRichMessageListener) {
                                ((KmRichMessageListener) context.getApplicationContext()).onAction(context, submitButtonModel.getType(), message, submitButtonModel.getAction(), null);
                            } else {
                                listener.onAction(context, submitButtonModel.getType(), message, submitButtonModel.getAction(), null);
                            }
                        }
                    }
                });
                flowLayout.addView(view);
            }
        } else {
            flowLayout.setVisibility(View.GONE);
        }
    }
}
