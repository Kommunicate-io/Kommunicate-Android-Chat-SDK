package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.types;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.AlRichMessage;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.adapters.KmFormItemAdapter;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.callbacks.ALRichMessageListener;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.ALRichMessageModel;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.v2.KmFormPayloadModel;

import java.util.ArrayList;
import java.util.List;

public class KmFormRichMessage extends AlRichMessage {

    public KmFormRichMessage(Context context, LinearLayout containerView, Message message, ALRichMessageListener listener, AlCustomizationSettings alCustomizationSettings) {
        super(context, containerView, message, listener, alCustomizationSettings);
    }

    @Override
    public void createRichMessage() {
        super.createRichMessage();

        LinearLayoutManager formLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        alFormLayoutRecycler.setLayoutManager(formLayoutManager);
        KmFormItemAdapter formItemAdapter = new KmFormItemAdapter(context, kmRichMessageModel.getFormModelList(), message.getKeyString());
        alFormLayoutRecycler.setAdapter(formItemAdapter);


        List<Object> actionModelList = new ArrayList<>();

        for (Object object : kmRichMessageModel.getFormModelList()) {
            if (object instanceof KmFormPayloadModel) {
                KmFormPayloadModel formPayloadModel = (KmFormPayloadModel) object;
                if (KmFormPayloadModel.Type.ACTION.getValue().equals(formPayloadModel.getType())) {
                    actionModelList.add(formPayloadModel.getAction());
                }
            } else if (object instanceof ALRichMessageModel.ALPayloadModel) {
                ALRichMessageModel.ALPayloadModel model = (ALRichMessageModel.ALPayloadModel) object;
                if ("submit".equals(model.getType())) {
                    actionModelList.add(model);
                }
            }
        }

        if (!actionModelList.isEmpty()) {
            flowLayout.setVisibility(View.GONE);
            if (flowLayout != null) {
                flowLayout.removeAllViews();

                View view = LayoutInflater.from(context).inflate(R.layout.al_rich_message_single_text_item, null);
                TextView itemTextView = view.findViewById(R.id.singleTextItem);

                if (itemTextView != null) {
                    itemTextView.setText(((ALRichMessageModel.ALPayloadModel) actionModelList.get(0)).getName());

                    itemTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });
                }
            }
        } else {
            flowLayout.setVisibility(View.GONE);
        }
    }
}
