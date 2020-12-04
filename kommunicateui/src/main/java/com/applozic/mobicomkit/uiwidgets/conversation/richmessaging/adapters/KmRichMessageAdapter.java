package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.KmRichMessage;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.callbacks.KmRichMessageListener;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.KmRichMessageModel;
import com.applozic.mobicomkit.uiwidgets.kommunicate.utils.KmThemeHelper;

import java.util.Map;

/**
 * abstract class for the various adapters
 */
public abstract class KmRichMessageAdapter extends RecyclerView.Adapter {

    protected Context context;
    protected KmRichMessageModel model;
    protected Message message;
    protected KmRichMessageListener listener;
    protected KmThemeHelper themeHelper;

    KmRichMessageAdapter(Context context, KmRichMessageModel model, KmRichMessageListener listener, Message message, KmThemeHelper themeHelper) {
        this.context = context;
        this.model = model;
        this.listener = listener;
        this.message = message;
        this.themeHelper = themeHelper;
    }

    KmRichMessageAdapter(Context context, KmRichMessageListener listener, Message message, KmThemeHelper themeHelper) {
        this(context, null, listener, message, themeHelper);
    }

    //to populate the views etc.
    void bindItems(RecyclerView.ViewHolder viewHolder, final int position) {
    }

    //utility functions >>>
    View.OnClickListener getActionClickListener(final KmRichMessageModel.KmButtonModel buttonModel, final Map<String, Object> replyMetadata) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context.getApplicationContext() instanceof KmRichMessageListener) {
                    ((KmRichMessageListener) context.getApplicationContext()).onAction(context, getActionType(buttonModel), message, buttonModel, replyMetadata);
                } else if (listener != null) {
                    listener.onAction(context, getActionType(buttonModel), message, buttonModel, replyMetadata);
                }
            }
        };
    }

    View.OnClickListener getGenericCardClickListener(final KmRichMessageModel.KmButtonModel action) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context.getApplicationContext() instanceof KmRichMessageListener) {
                    ((KmRichMessageListener) context.getApplicationContext()).onAction(context, getActionType(action), message, action, getReplyMetadata(action));
                } else if (listener != null) {
                    listener.onAction(context, getActionType(action), message, action, getReplyMetadata(action));
                }
            }
        };
    }

    private Map<String, Object> getReplyMetadata(KmRichMessageModel.KmButtonModel kmButtonModel) {
        if (kmButtonModel != null && kmButtonModel.getAction() != null) {
            if (kmButtonModel.getAction().getPayload() != null) {
                return kmButtonModel.getAction().getPayload().getReplyMetadata();
            }
        }
        return null;
    }

    private String getActionType(KmRichMessageModel.KmButtonModel kmButtonModel) {
        if (kmButtonModel != null && kmButtonModel.getAction() != null) {
            if (!TextUtils.isEmpty(kmButtonModel.getAction().getType())) {
                return kmButtonModel.getAction().getType();
            }
        }
        return KmRichMessage.TEMPLATE_ID + model.getTemplateId();
    }
    //utility functions <<<
}
