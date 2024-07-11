package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.callbacks.KmRichMessageListener;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.KmRichMessageModel;
import com.applozic.mobicomkit.uiwidgets.kommunicate.utils.DimensionsUtils;
import com.applozic.mobicomkit.uiwidgets.kommunicate.utils.KmThemeHelper;
import com.applozic.mobicommons.json.GsonUtils;

import java.util.Arrays;
import java.util.List;

import io.kommunicate.utils.KmUtils;

public class KmButtonRMAdapter extends KmRichMessageAdapter {

    private List<KmRichMessageModel.KmPayloadModel> payloadList;

    KmButtonRMAdapter(Context context, KmRichMessageModel model, KmRichMessageListener listener, Message message, KmThemeHelper themeHelper) {
        super(context, model, listener, message, themeHelper);
        this.payloadList = Arrays.asList((KmRichMessageModel.KmPayloadModel[])
                GsonUtils.getObjectFromJson(model.getPayload(), KmRichMessageModel.KmPayloadModel[].class));
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.km_rich_message_single_text_item, parent, false);
        return new SingleTextViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        bindItems(holder, position);
    }

    private class SingleTextViewHolder extends RecyclerView.ViewHolder {
        TextView singleTextItem;
        LinearLayout rootLayout;

        public SingleTextViewHolder(View itemView) {
            super(itemView);

            singleTextItem = itemView.findViewById(R.id.singleTextItem);
            rootLayout = itemView.findViewById(R.id.rootLayout);

            KmUtils.setGradientStrokeColor(singleTextItem, DimensionsUtils.convertDpToPx(1), themeHelper.getRichMessageThemeColor());
            singleTextItem.setTextColor(themeHelper.getRichMessageThemeColor());
        }
    }

    @Override
    void bindItems(RecyclerView.ViewHolder holder, int position) {
        super.bindItems(holder, position);
        if (model.getTemplateId() == 3) {
            if (payloadList.get(position).getName() != null) {
                ((SingleTextViewHolder) holder).singleTextItem.setText(payloadList.get(position).getName().trim());
            } else {
                ((SingleTextViewHolder) holder).singleTextItem.setText("");
            }
        } else {
            if (payloadList.get(position).getTitle() != null) {
                ((SingleTextViewHolder) holder).singleTextItem.setText(payloadList.get(position).getTitle().trim());
            } else {
                ((SingleTextViewHolder) holder).singleTextItem.setText("");
            }
        }
    }

    @Override
    public int getItemCount() {
        return payloadList.size();
    }
}
