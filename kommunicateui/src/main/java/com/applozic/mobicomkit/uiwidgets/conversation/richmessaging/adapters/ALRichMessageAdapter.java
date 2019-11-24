package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.AlRichMessage;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.callbacks.ALRichMessageListener;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.ALRichMessageModel;

import java.util.Map;

/**
 * abstract class for the various adapters
 */
public abstract class ALRichMessageAdapter extends RecyclerView.Adapter {

    protected Context context;
    protected ALRichMessageModel model;
    protected Message message;
    protected ALRichMessageListener listener;

    ALRichMessageAdapter(Context context, ALRichMessageModel model, ALRichMessageListener listener, Message message) {
        this.context = context;
        this.model = model;
        this.listener = listener;
        this.message = message;
    }

    ALRichMessageAdapter(Context context, ALRichMessageListener listener, Message message) {
        this.context = context;
        this.listener = listener;
        this.message = message;
    }

    //to populate the views etc.
    void bindItems(RecyclerView.ViewHolder viewHolder, final int position) {}

    //utility functions >>>
    View.OnClickListener getActionClickListener(final ALRichMessageModel.AlButtonModel buttonModel, final Map<String, Object> replyMetadata) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context.getApplicationContext() instanceof ALRichMessageListener) {
                    ((ALRichMessageListener) context.getApplicationContext()).onAction(context, getActionType(buttonModel), message, buttonModel, replyMetadata);
                } else if (listener != null) {
                    listener.onAction(context, getActionType(buttonModel), message, buttonModel, replyMetadata);
                }
            }
        };
    }

    View.OnClickListener getGenericCardClickListener(final ALRichMessageModel.AlButtonModel action) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context.getApplicationContext() instanceof ALRichMessageListener) {
                    ((ALRichMessageListener) context.getApplicationContext()).onAction(context, getActionType(action), message, action, getReplyMetadata(action));
                } else if (listener != null) {
                    listener.onAction(context, getActionType(action), message, action, getReplyMetadata(action));
                }
            }
        };
    }

    private Map<String, Object> getReplyMetadata(ALRichMessageModel.AlButtonModel alButtonModel) {
        if (alButtonModel != null && alButtonModel.getAction() != null) {
            if (alButtonModel.getAction().getPayload() != null) {
                return alButtonModel.getAction().getPayload().getReplyMetadata();
            }
        }
        return null;
    }

    private String getActionType(ALRichMessageModel.AlButtonModel alButtonModel) {
        if (alButtonModel != null && alButtonModel.getAction() != null) {
            if (!TextUtils.isEmpty(alButtonModel.getAction().getType())) {
                return alButtonModel.getAction().getType();
            }
        }
        return AlRichMessage.TEMPLATE_ID + model.getTemplateId();
    }
    //utility functions <<<
}
