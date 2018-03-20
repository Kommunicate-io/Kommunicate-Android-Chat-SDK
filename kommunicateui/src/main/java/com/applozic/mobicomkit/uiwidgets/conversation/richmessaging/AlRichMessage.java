package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicommons.json.GsonUtils;

import java.util.Map;

/**
 * Created by ashish on 28/02/18.
 */

public class AlRichMessage {
    private Context context;
    private RecyclerView recyclerView;
    private Message message;
    private ALRichMessageListener listener;

    public AlRichMessage(Context context, RecyclerView recyclerView, Message message, ALRichMessageListener listener) {
        this.context = context;
        this.recyclerView = recyclerView;
        this.message = message;
        this.listener = listener;

        setupRichMessage();
    }

    private void setupRichMessage() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        ALRichMessageModel model = (ALRichMessageModel) GsonUtils.getObjectFromJson(GsonUtils.getJsonFromObject(message.getMetadata(), Map.class), ALRichMessageModel.class);
        ALRichMessageAdapter adapter = new ALRichMessageAdapter(context, model, listener);
        recyclerView.setAdapter(adapter);
    }
}
