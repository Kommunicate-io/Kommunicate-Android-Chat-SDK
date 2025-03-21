package io.kommunicate.ui.conversation.richmessaging.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.kommunicate.devkit.api.conversation.Message;
import io.kommunicate.ui.R;
import io.kommunicate.ui.conversation.richmessaging.KmRichMessage;
import io.kommunicate.ui.conversation.richmessaging.callbacks.KmRichMessageListener;
import io.kommunicate.ui.conversation.richmessaging.models.KmRichMessageModel;
import io.kommunicate.ui.kommunicate.utils.KmThemeHelper;

import java.util.List;

public class KmActionButtonRMAdapter extends KmRichMessageAdapter {

    private final List<KmRichMessageModel.KmButtonModel> actionButtonList;

    KmActionButtonRMAdapter(Context context, Message message, List<KmRichMessageModel.KmButtonModel> actionButtonList, KmRichMessageListener messageListener, KmThemeHelper themeHelper) {
        super(context, messageListener, message, themeHelper);
        this.actionButtonList = actionButtonList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.km_rich_list_button_item, parent, false);
        return new AlActionButtonItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        bindItems(holder, position);
    }

    @Override
    public void bindItems(RecyclerView.ViewHolder viewHolder, int position) {
        KmRichMessageModel.KmButtonModel button = actionButtonList.get(position);
        AlActionButtonItemViewHolder holder = (AlActionButtonItemViewHolder) viewHolder;

        if (!TextUtils.isEmpty(button.getName())) {
            holder.actionButtonNameTv.setVisibility(View.VISIBLE);
            holder.actionButtonNameTv.setText(KmRichMessage.getHtmlText(button.getName().trim()));
        } else {
            holder.actionButtonNameTv.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return actionButtonList != null ? actionButtonList.size() : 0;
    }

    private class AlActionButtonItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView actionButtonNameTv;

        public AlActionButtonItemViewHolder(View itemView) {
            super(itemView);

            actionButtonNameTv = itemView.findViewById(R.id.tv_action_button_name);
            RelativeLayout rootLayout = itemView.findViewById(R.id.rootLayout);

            actionButtonNameTv.setTextColor(themeHelper.getRichMessageThemeColor());
            rootLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int itemPosition = this.getLayoutPosition();
            if (itemPosition != -1 && actionButtonList != null && !actionButtonList.isEmpty()) {
                if (context.getApplicationContext() instanceof KmRichMessageListener) {
                    ((KmRichMessageListener) context.getApplicationContext()).onAction(context, getAction(actionButtonList.get(itemPosition)), message, actionButtonList.get(itemPosition), null);
                } else if (listener != null) {
                    listener.onAction(context, getAction(actionButtonList.get(itemPosition)), message, actionButtonList.get(itemPosition), null);
                }
            }
        }


        private String getAction(KmRichMessageModel.KmButtonModel buttonModel) {
            if (buttonModel != null) {
                if (buttonModel.getAction() != null) {
                    if (!TextUtils.isEmpty(buttonModel.getAction().getType())) {
                        return buttonModel.getAction().getType();
                    } else if (buttonModel.getAction().getPayload() != null && !TextUtils.isEmpty(buttonModel.getAction().getPayload().getType())) {
                        return buttonModel.getAction().getPayload().getType();
                    }
                }
            }
            return KmRichMessage.TEMPLATE_ID + 7;
        }
    }
}
