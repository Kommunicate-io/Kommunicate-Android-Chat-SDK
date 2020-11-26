package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.callbacks.KmRichMessageListener;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.KmRichMessage;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.KmRichMessageModel;
import com.applozic.mobicomkit.uiwidgets.kommunicate.utils.KmThemeHelper;
import com.applozic.mobicommons.json.GsonUtils;
import com.bumptech.glide.Glide;

import java.util.Arrays;
import java.util.List;

public class KmImageRMAdapter extends KmRichMessageAdapter {

    private List<KmRichMessageModel.KmPayloadModel> payloadList;
    private AlCustomizationSettings alCustomizationSettings;

    KmImageRMAdapter(Context context, KmRichMessageModel model, KmRichMessageListener listener, Message message, AlCustomizationSettings alCustomizationSettings) {
        super(context, model, listener, message, KmThemeHelper.getInstance(context, alCustomizationSettings));
        this.alCustomizationSettings = alCustomizationSettings;

        if (model.getPayload() != null) {
            this.payloadList = Arrays.asList((KmRichMessageModel.KmPayloadModel[])
                    GsonUtils.getObjectFromJson(model.getPayload(), KmRichMessageModel.KmPayloadModel[].class));
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.km_image_rich_message_layout, parent, false);
        return new ImageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (payloadList != null && !payloadList.isEmpty()) {
            KmRichMessageModel.KmPayloadModel payloadModel = payloadList.get(position);

            final ImageViewHolder imageViewHolder = (ImageViewHolder) holder;

            if (payloadModel != null) {
                if (alCustomizationSettings != null) {
                    GradientDrawable bgShape = (GradientDrawable) imageViewHolder.rootLayout.getBackground();
                    bgShape.setColor(message.isTypeOutbox() ? themeHelper.getSentMessageBackgroundColor() : Color.parseColor(alCustomizationSettings.getReceivedMessageBackgroundColor()));
                    bgShape.setStroke(3, message.isTypeOutbox() ? themeHelper.getSentMessageBorderColor() :
                            Color.parseColor(alCustomizationSettings.getReceivedMessageBorderColor()));
                }
                if (!TextUtils.isEmpty(payloadModel.getUrl())) {
                    Glide.with(context)
                            .load(payloadModel.getUrl())
                            .into(imageViewHolder.imageView);
                } else {
                    Glide.with(context).load(R.drawable.mobicom_attachment_file).into(imageViewHolder.imageView);
                }

                if (payloadModel.getCaption() != null && !TextUtils.isEmpty(payloadModel.getCaption().trim())) {
                    imageViewHolder.captionText.setVisibility(View.VISIBLE);
                    imageViewHolder.captionText.setText(payloadModel.getCaption());
                    imageViewHolder.captionText.setTextColor(Color.parseColor(message.isTypeOutbox() ? alCustomizationSettings.getSentMessageTextColor() : alCustomizationSettings.getReceivedMessageTextColor()));
                } else {
                    imageViewHolder.captionText.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return payloadList != null ? payloadList.size() : 0;
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView captionText;
        LinearLayout rootLayout;

        public ImageViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.alImageView);
            captionText = itemView.findViewById(R.id.alCaptionText);
            rootLayout = itemView.findViewById(R.id.rootLayout);

            if (listener != null) {
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onAction(context, KmRichMessage.TEMPLATE_ID + model.getTemplateId(), message, payloadList != null ? payloadList.get(getLayoutPosition()) : null, null);
                    }
                });
            }
        }
    }
}
