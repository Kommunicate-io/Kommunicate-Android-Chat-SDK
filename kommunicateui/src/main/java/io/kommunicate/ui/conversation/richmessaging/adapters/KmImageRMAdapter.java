package io.kommunicate.ui.conversation.richmessaging.adapters;

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

import io.kommunicate.devkit.api.conversation.Message;
import io.kommunicate.ui.AlCustomizationSettings;
import io.kommunicate.ui.R;
import io.kommunicate.ui.conversation.richmessaging.callbacks.KmRichMessageListener;
import io.kommunicate.ui.conversation.richmessaging.KmRichMessage;
import io.kommunicate.ui.conversation.richmessaging.models.KmRichMessageModel;
import io.kommunicate.ui.kommunicate.utils.KmThemeHelper;
import io.kommunicate.commons.json.GsonUtils;
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
                    bgShape.setColor(message.isTypeOutbox() ? themeHelper.getSentMessageBackgroundColor() : Color.parseColor(themeHelper.isDarkModeEnabledForSDK() ? alCustomizationSettings.getReceivedMessageBackgroundColor().get(1) : alCustomizationSettings.getReceivedMessageBorderColor().get(0)));
                    bgShape.setStroke(3, message.isTypeOutbox() ? themeHelper.getSentMessageBorderColor() :
                            Color.parseColor(themeHelper.isDarkModeEnabledForSDK() ? alCustomizationSettings.getReceivedMessageBorderColor().get(1) : alCustomizationSettings.getReceivedMessageBorderColor().get(0)));
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
                    imageViewHolder.captionText.setTextColor(Color.parseColor(message.isTypeOutbox() ? (themeHelper.isDarkModeEnabledForSDK() ? alCustomizationSettings.getSentMessageTextColor().get(1) : alCustomizationSettings.getSentMessageTextColor().get(0)) : (themeHelper.isDarkModeEnabledForSDK() ? alCustomizationSettings.getReceivedMessageTextColor().get(1) : alCustomizationSettings.getReceivedMessageTextColor().get(0))));
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
