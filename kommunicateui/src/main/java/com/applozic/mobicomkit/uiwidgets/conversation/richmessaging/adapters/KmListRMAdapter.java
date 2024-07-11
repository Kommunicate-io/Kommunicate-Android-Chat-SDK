package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.callbacks.KmRichMessageListener;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.KmRichMessageModel;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.KmRichMessage;
import com.applozic.mobicomkit.uiwidgets.kommunicate.utils.KmThemeHelper;
import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Map;

public class KmListRMAdapter extends KmRichMessageAdapter {

    private List<KmRichMessageModel.KmElementModel> elementList;
    private Map<String, Object> replyMetadata;

    KmListRMAdapter(Context context, Message message, List<KmRichMessageModel.KmElementModel> elementList, Map<String, Object> replyMetadata, KmRichMessageListener messageListener, KmThemeHelper themeHelper) {
        super(context, messageListener, message, themeHelper);
        this.elementList = elementList;
        this.replyMetadata = replyMetadata;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.km_rich_list_item, parent, false);
        return new AlListItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        bindItems(holder, position);
    }

    @Override
    public void bindItems(RecyclerView.ViewHolder viewHolder, int position) {
        KmRichMessageModel.KmElementModel element = elementList.get(position);
        AlListItemViewHolder holder = (AlListItemViewHolder) viewHolder;

        if (!TextUtils.isEmpty(element.getTitle())) {
            holder.headerTv.setVisibility(View.VISIBLE);
            holder.headerTv.setText(KmRichMessage.getHtmlText(element.getTitle().trim()));
        } else {
            holder.headerTv.setVisibility(View.GONE);
        }

        if (element.getDescription() != null && !TextUtils.isEmpty(element.getDescription().trim())) {
            holder.detailsTv.setVisibility(View.VISIBLE);
            holder.detailsTv.setText(KmRichMessage.getHtmlText(element.getDescription()));
        } else {
            holder.detailsTv.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(element.getImgSrc())) {
            holder.listImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(element.getImgSrc()).into(holder.listImage);
        } else {
            holder.listImage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return elementList != null ? elementList.size() : 0;
    }

    private class AlListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView headerTv;
        private TextView detailsTv;
        private RelativeLayout rootLayout;
        private ImageView listImage;

        public AlListItemViewHolder(View itemView) {
            super(itemView);

            headerTv = itemView.findViewById(R.id.listItemHeaderText);
            detailsTv = itemView.findViewById(R.id.listItemText);
            rootLayout = itemView.findViewById(R.id.rootLayout);
            listImage = itemView.findViewById(R.id.listItemImage);

            headerTv.setTextColor(themeHelper.getRichMessageThemeColor());
            rootLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int itemPosition = this.getLayoutPosition();
            if (itemPosition != -1 && elementList != null && !elementList.isEmpty()) {
                if (context.getApplicationContext() instanceof KmRichMessageListener) {
                    ((KmRichMessageListener) context.getApplicationContext()).onAction(context, getAction(elementList.get(itemPosition)), message, elementList.get(itemPosition), getReplyMetadata(elementList.get(itemPosition)));
                } else if (listener != null) {
                    listener.onAction(context, getAction(elementList.get(itemPosition)), message, elementList.get(itemPosition), getReplyMetadata(elementList.get(itemPosition)));
                }
            }
        }

        private Map<String, Object> getReplyMetadata(KmRichMessageModel.KmElementModel elementModel) {
            if (elementModel != null) {
                if (elementModel.getAction() != null && elementModel.getAction().getPayload() != null && elementModel.getAction().getPayload().getReplyMetadata() != null) {
                    return elementModel.getAction().getPayload().getReplyMetadata();
                }
            }
            return replyMetadata;
        }

        private String getAction(KmRichMessageModel.KmElementModel elementModel) {
            if (elementModel != null) {
                if (elementModel.getAction() != null) {
                    if (!TextUtils.isEmpty(elementModel.getAction().getType())) {
                        return elementModel.getAction().getType();
                    } else if (elementModel.getAction().getPayload() != null && !TextUtils.isEmpty(elementModel.getAction().getPayload().getType())) {
                        return elementModel.getAction().getPayload().getType();
                    }
                }
            }
            return KmRichMessage.TEMPLATE_ID + 7;
        }
    }
}
