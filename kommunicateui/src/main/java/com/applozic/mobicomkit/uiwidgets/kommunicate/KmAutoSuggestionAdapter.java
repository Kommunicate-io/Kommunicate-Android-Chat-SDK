package com.applozic.mobicomkit.uiwidgets.kommunicate;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.ALRichMessageListener;
import com.applozic.mobicomkit.uiwidgets.kommunicate.models.KmAutoSuggestionModel;

import java.util.List;

public class KmAutoSuggestionAdapter extends RecyclerView.Adapter {

    private Context context;
    private List<KmAutoSuggestionModel> quickReplyList;
    private ALRichMessageListener listener;
    public static final String KM_AUTO_SUGGESTION_ACTION = "KM_AUTO_SUGGESTION_ACTION";

    public KmAutoSuggestionAdapter(Context context, ALRichMessageListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setQuickReplyList(List<KmAutoSuggestionModel> quickReplyList) {
        this.quickReplyList = quickReplyList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.km_quick_reply_item_layout, parent, false);
        return new KmQuickReplyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        KmQuickReplyViewHolder mViewHolder = (KmQuickReplyViewHolder) holder;

        KmAutoSuggestionModel quickReplyModel = quickReplyList != null ? quickReplyList.get(position) : null;

        if (quickReplyModel != null) {
            if (!TextUtils.isEmpty(quickReplyModel.getCategory())) {
                mViewHolder.kmQuickReplyTitle.setVisibility(View.VISIBLE);
                mViewHolder.kmQuickReplyTitle.setText("/" + quickReplyModel.getCategory().trim());
            } else {
                mViewHolder.kmQuickReplyTitle.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(quickReplyModel.getContent())) {
                mViewHolder.kmQuickReplyMessage.setVisibility(View.VISIBLE);
                mViewHolder.kmQuickReplyMessage.setText(quickReplyModel.getContent().trim());
            } else {
                mViewHolder.kmQuickReplyMessage.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return quickReplyList != null ? quickReplyList.size() : 0;
    }

    private class KmQuickReplyViewHolder extends RecyclerView.ViewHolder {

        private TextView kmQuickReplyTitle;
        private TextView kmQuickReplyMessage;

        public KmQuickReplyViewHolder(View itemView) {
            super(itemView);

            kmQuickReplyTitle = itemView.findViewById(R.id.kmAutoSuggestionTitle);
            kmQuickReplyMessage = itemView.findViewById(R.id.kmAutoSuggestionMessage);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        KmAutoSuggestionModel quickReplyModel = quickReplyList != null ? quickReplyList.get(getAdapterPosition()) : null;
                        if (quickReplyModel != null) {
                            listener.onAction(context, KM_AUTO_SUGGESTION_ACTION, null, quickReplyModel, null);
                        }
                    }
                }
            });
        }
    }
}
