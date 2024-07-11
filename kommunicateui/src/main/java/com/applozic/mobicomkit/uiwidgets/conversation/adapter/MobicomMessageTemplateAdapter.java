package com.applozic.mobicomkit.uiwidgets.conversation.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import androidx.recyclerview.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.MobicomMessageTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by reytum on 1/8/17.
 */

public class MobicomMessageTemplateAdapter extends RecyclerView.Adapter<MobicomMessageTemplateAdapter.ViewHolder> {

    private MobicomMessageTemplate messageTemplate;
    private MessageTemplateDataListener listener;
    private List<String> messageList;
    private Map<String, String> messageMap;

    public MobicomMessageTemplateAdapter(MobicomMessageTemplate messageTemplate) {
        this.messageTemplate = messageTemplate;
        this.messageList = new ArrayList<>(messageTemplate.getMessages().keySet());
        this.messageMap = messageTemplate.getMessages();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mobicom_message_template_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.messageText.setText(messageList.get(position));
        holder.messageText.setTextColor(Color.parseColor(messageTemplate.getTextColor()));
        holder.messageText.setBackgroundDrawable(getShape(holder.messageText.getContext()));

        holder.messageText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemSelected(messageMap.get(messageList.get(position)));
            }
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void setMessageList(Map<String, String> messageList) {
        this.messageMap = messageList;
        this.messageList = new ArrayList<>(messageList.keySet());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;

        public ViewHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.messageTemplateTv);
        }
    }

    public GradientDrawable getShape(Context context) {
        GradientDrawable bgShape = new GradientDrawable();
        bgShape.setShape(GradientDrawable.RECTANGLE);
        bgShape.setColor(Color.parseColor(messageTemplate.getBackGroundColor()));
        bgShape.setCornerRadius(dpToPixels(context, messageTemplate.getCornerRadius()));
        bgShape.setStroke(dpToPixels(context, 2), Color.parseColor(messageTemplate.getBorderColor()));

        return bgShape;
    }

    public int dpToPixels(Context context, float px) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, context.getResources().getDisplayMetrics());
    }

    public void setOnItemSelected(MessageTemplateDataListener listener) {
        this.listener = listener;
    }

    public interface MessageTemplateDataListener {
        void onItemSelected(String messsage);
    }
}