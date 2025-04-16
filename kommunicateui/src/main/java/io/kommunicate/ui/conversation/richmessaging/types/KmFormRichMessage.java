package io.kommunicate.ui.conversation.richmessaging.types;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.kommunicate.devkit.api.conversation.Message;
import io.kommunicate.ui.CustomizationSettings;
import io.kommunicate.ui.R;
import io.kommunicate.ui.conversation.richmessaging.KmRichMessage;
import io.kommunicate.ui.conversation.richmessaging.adapters.KmFormItemAdapter;
import io.kommunicate.ui.conversation.richmessaging.callbacks.KmRichMessageListener;
import io.kommunicate.ui.conversation.richmessaging.models.v2.KmFormPayloadModel;
import io.kommunicate.ui.conversation.richmessaging.models.v2.KmRMActionModel;
import io.kommunicate.ui.kommunicate.utils.DimensionsUtils;

import java.util.ArrayList;
import java.util.List;

import io.kommunicate.utils.KmUtils;

public class KmFormRichMessage extends KmRichMessage {

    public KmFormRichMessage(Context context, LinearLayout containerView, Message message, KmRichMessageListener listener, CustomizationSettings customizationSettings, boolean showTimestamp, boolean isDarkModeEnabled) {
        super(context, containerView, message, listener, customizationSettings, showTimestamp, isDarkModeEnabled);
    }

    @Override
    public void createRichMessage(boolean isMessageProcessed) {
        super.createRichMessage(isMessageProcessed);

        LinearLayoutManager formLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        alFormLayoutRecycler.setLayoutManager(formLayoutManager);
        final KmFormItemAdapter formItemAdapter = new KmFormItemAdapter(context, kmRichMessageModel.getFormModelList(), message.getKeyString(), customizationSettings);
        GradientDrawable drawable = (GradientDrawable) alFormLayoutRecycler.getBackground();
        if (themeHelper.isDarkModeEnabledForSDK()) {
            drawable.setColorFilter(context.getResources().getColor(R.color.received_message_bg_color_night), PorterDuff.Mode.MULTIPLY);
        } else {
            drawable.clearColorFilter();
        }
        alFormLayoutRecycler.setAdapter(formItemAdapter);
        alFormLayoutRecycler.removeOnItemTouchListener(formListenerTrue);
        alFormLayoutRecycler.removeOnItemTouchListener(formListenerFalse);
        if (isMessageProcessed && themeHelper.isDisableFormPostSubmit()) {
            alFormLayoutRecycler.addOnItemTouchListener(formListenerTrue);
        } else {
            alFormLayoutRecycler.addOnItemTouchListener(formListenerFalse);
        }

        List<Object> actionModelList = new ArrayList<>();

        for (Object object : kmRichMessageModel.getFormModelList()) {
            if (object instanceof KmFormPayloadModel) {
                KmFormPayloadModel formPayloadModel = (KmFormPayloadModel) object;

                if (isMessageProcessed && themeHelper.hideFormSubmitButtonsPostCTA() && KmFormPayloadModel.Type.SUBMIT.getValue().equals(formPayloadModel.getType())) {
                    continue;
                }

                if (KmFormPayloadModel.Type.SUBMIT.getValue().equals(formPayloadModel.getType()) || KmFormPayloadModel.Type.ACTION.getValue().equals(formPayloadModel.getType()) || TextUtils.isEmpty(formPayloadModel.getType())) {
                    if (formPayloadModel.getAction() != null) {
                        actionModelList.add(formPayloadModel.getAction());
                    } else {
                        actionModelList.add(formPayloadModel.getDialogFlowActionModel());
                    }
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
                if (themeHelper.isDarkModeEnabledForSDK()) {
                    itemTextView.setTextColor(Color.WHITE);
                } else {
                    itemTextView.setTextColor(themeHelper.getRichMessageThemeColor());
                }

                final KmRMActionModel<KmRMActionModel.SubmitButton> submitButtonModel = (KmRMActionModel<KmRMActionModel.SubmitButton>) actionModelList.get(0);
                itemTextView.setText(submitButtonModel.getName());

                KmRMActionModel model = (KmRMActionModel) actionModelList.get(0);
                if (!TextUtils.isEmpty(model.getName())) {
                    itemTextView.setText(model.getName());
                } else if (!TextUtils.isEmpty(model.getLabel())) {
                    itemTextView.setText(model.getLabel());
                }

                itemTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (formItemAdapter != null && formItemAdapter.isFormDataValid()) {
                            if (submitButtonModel != null && TextUtils.isEmpty(submitButtonModel.getType())) {
                                submitButtonModel.setType(KmFormPayloadModel.Type.SUBMIT.getValue());
                            }
                            if (context != null && context.getApplicationContext() instanceof KmRichMessageListener) {
                                if (submitButtonModel.getAction() != null) {
                                    ((KmRichMessageListener) context.getApplicationContext()).onAction(context, submitButtonModel.getType(), message, submitButtonModel.getAction(), null);
                                } else {
                                    listener.onAction(context, submitButtonModel.getType(), message, submitButtonModel.getDialogFlowAction(), null);
                                }
                            } else {
                                if (submitButtonModel.getAction() != null) {
                                    listener.onAction(context, submitButtonModel.getType(), message, submitButtonModel.getAction(), null);
                                } else {
                                    listener.onAction(context, submitButtonModel.getType(), message, submitButtonModel.getDialogFlowAction(), null);
                                }
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

    static RecyclerView.OnItemTouchListener formListenerTrue = new RecyclerView.OnItemTouchListener() {
        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            return true; // **True** will restrict the interaction on recyclerView
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    };

    static RecyclerView.OnItemTouchListener formListenerFalse = new RecyclerView.OnItemTouchListener() {
        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            return false;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    };

}
