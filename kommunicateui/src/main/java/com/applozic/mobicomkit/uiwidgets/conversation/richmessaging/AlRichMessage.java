package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Dimension;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.DimensionsUtils;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicommons.json.GsonUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by ashish on 28/02/18.
 */

public class AlRichMessage {
    private Context context;
    private RecyclerView recyclerView;
    private Message message;
    private ALRichMessageListener listener;
    private LinearLayout containerView;
    private LinearLayout listItemLayout;

    public AlRichMessage(Context context, RecyclerView recyclerView, LinearLayout containerView, Message message, ALRichMessageListener listener) {
        this.context = context;
        this.recyclerView = recyclerView;
        this.message = message;
        this.listener = listener;
        this.containerView = containerView;

        setupRichMessage();
    }

    private void setupRichMessage() {
        ALRichMessageModel model = (ALRichMessageModel) GsonUtils.getObjectFromJson(GsonUtils.getJsonFromObject(message.getMetadata(), Map.class), ALRichMessageModel.class);
        listItemLayout = containerView.findViewById(R.id.listItemLayout);

        if (model.getTemplateId() == 8) {
            listItemLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            setupListItemView(listItemLayout, model);
        } else {
            listItemLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(layoutManager);
            ALRichMessageAdapter adapter = new ALRichMessageAdapter(context, model, listener);
            recyclerView.setAdapter(adapter);
        }
    }

    private void setupListItemView(LinearLayout layout, ALRichMessageModel model) {

        if (model != null) {

            layout.removeAllViews();

            if (!TextUtils.isEmpty(model.getHeaderText())) {
                TextView headerTv = new TextView(context);
                LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                headerTv.setLetterSpacing(0.04f);
                headerTv.setText(model.getHeaderText());
                headerTv.setTextColor(Color.parseColor("#646262"));
                headerTv.setBackgroundColor(Color.parseColor("#e6e5ec"));
                headerTv.setPadding(DimensionsUtils.dp(9), DimensionsUtils.dp(9), DimensionsUtils.dp(9), DimensionsUtils.dp(9));
                headerTv.setTextSize(Dimension.SP, 14);
                headerTv.setLayoutParams(textViewParams);
                layout.addView(headerTv);
            }

            if (!TextUtils.isEmpty(model.getPayload())) {
                List<ALRichMessageModel.ALPayloadModel> payLoadList = Arrays.asList((ALRichMessageModel.ALPayloadModel[])
                        GsonUtils.getObjectFromJson(model.getPayload(), ALRichMessageModel.ALPayloadModel[].class));

                for (final ALRichMessageModel.ALPayloadModel payload : payLoadList) {

                    View view = new View(context);
                    LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DimensionsUtils.dp(1));
                    view.setLayoutParams(viewParams);
                    view.setBackgroundColor(Color.parseColor("#e7e4e4"));

                    TextView textView = new TextView(context);
                    LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    textView.setLayoutParams(textViewParams);
                    textView.setGravity(Gravity.CENTER);
                    textView.setLetterSpacing(0.05f);
                    textView.setPadding(DimensionsUtils.dp(9), DimensionsUtils.dp(7), DimensionsUtils.dp(9), DimensionsUtils.dp(7));
                    textView.setTextSize(Dimension.SP, 12);
                    textView.setTextColor(context.getResources().getColor(R.color.applozic_theme_color_primary));
                    textView.setText(payload.getTitle());

                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.onAction(context, "listItemClick", payload.getMessage());
                        }
                    });

                    layout.addView(view);
                    layout.addView(textView);
                }
            }
        }
    }
}
