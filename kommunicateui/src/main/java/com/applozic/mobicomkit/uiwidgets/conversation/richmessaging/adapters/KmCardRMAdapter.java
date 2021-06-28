package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.KmRichMessage;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.callbacks.KmRichMessageListener;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.KmRichMessageModel;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.types.ButtonKmRichMessage;
import com.applozic.mobicomkit.uiwidgets.kommunicate.utils.KmThemeHelper;
import com.applozic.mobicommons.json.GsonUtils;
import com.bumptech.glide.Glide;

import java.util.Arrays;
import java.util.List;

import static android.view.View.GONE;

public class KmCardRMAdapter extends KmRichMessageAdapter {

    private List<KmRichMessageModel.KmPayloadModel> payloadList;
    private KmThemeHelper themeHelper;
    private boolean isMessageProcessed;

    KmCardRMAdapter(Context context, KmRichMessageModel model, KmRichMessageListener listener, Message message, KmThemeHelper themeHelper, boolean isMessageProcessed) {
        super(context, model, listener, message, themeHelper);
        this.themeHelper = themeHelper;
        this.isMessageProcessed = isMessageProcessed;
        this.payloadList = Arrays.asList((KmRichMessageModel.KmPayloadModel[])
                GsonUtils.getObjectFromJson(model.getPayload(), KmRichMessageModel.KmPayloadModel[].class));
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.km_rich_message_item, parent, false);
        return new CardViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        bindItems(holder, position);
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        LinearLayout roomRootLayout;
        TextView productNameSingleLine;
        TextView productPrice;
        ImageView productImage;
        TextView productRating;
        TextView productLocation;
        TextView productDescription;
        RelativeLayout productNameSplitLayout;
        TextView productName;
        View productImageOverlay;
        TextView[] bookActions = new TextView[3];
        View[] viewActions = new View[3];

        public CardViewHolder(View itemView) {
            super(itemView);

            roomRootLayout = itemView.findViewById(R.id.roomRootLayout);
            productNameSingleLine = itemView.findViewById(R.id.productNameSingleLine);
            productImage = itemView.findViewById(R.id.productImage);
            productRating = itemView.findViewById(R.id.productRating);
            productLocation = itemView.findViewById(R.id.productLocation);
            productPrice = itemView.findViewById(R.id.productPrice);
            productDescription = itemView.findViewById(R.id.productDescription);
            productName = itemView.findViewById(R.id.productName);
            productImageOverlay = itemView.findViewById(R.id.productImageOverlay);
            productNameSplitLayout = itemView.findViewById(R.id.productNameSplitLayout);
            productRating = itemView.findViewById(R.id.productRating);
            bookActions[0] = itemView.findViewById(R.id.bookingAction1);
            bookActions[1] = itemView.findViewById(R.id.bookingAction2);
            bookActions[2] = itemView.findViewById(R.id.bookingAction3);
            viewActions[0] = itemView.findViewById(R.id.viewAction1);
            viewActions[1] = itemView.findViewById(R.id.viewAction2);
            viewActions[2] = itemView.findViewById(R.id.viewAction3);
        }
    }

    private void setupBookActions(CardViewHolder viewHolder, int index, List<KmRichMessageModel.KmButtonModel> actionsList) {
        viewHolder.bookActions[index].setVisibility(View.VISIBLE);
        viewHolder.viewActions[index].setVisibility(View.VISIBLE);
        viewHolder.bookActions[index].setText(actionsList.get(index).getName());
        viewHolder.bookActions[index].setTextColor(themeHelper.getRichMessageThemeColor());
        viewHolder.bookActions[index].setOnClickListener(getGenericCardClickListener(actionsList.get(index)));
    }

    @Override
    protected void bindItems(RecyclerView.ViewHolder holder, int position) {
        super.bindItems(holder, position);
        CardViewHolder viewHolder = (CardViewHolder) holder;
        if (payloadList != null) {
            final KmRichMessageModel.KmPayloadModel payloadModel = payloadList.get(position);

            if (payloadModel.getHeader() != null && !TextUtils.isEmpty(payloadModel.getHeader().getImgSrc())) {
                Glide.with(context).load(payloadModel.getHeader().getImgSrc()).into(viewHolder.productImage);
                viewHolder.productImage.setVisibility(View.VISIBLE);
                viewHolder.productImageOverlay.setVisibility(View.VISIBLE);
                viewHolder.productPrice.setBackground(context.getResources().getDrawable(R.drawable.km_rich_messaging_price_border));
            } else {
                viewHolder.productImage.setVisibility(View.GONE);
                viewHolder.productImageOverlay.setVisibility(GONE);
                viewHolder.productPrice.setBackground(context.getResources().getDrawable(R.drawable.km_imageless_rich_message_price_border));
            }

            if (payloadModel.getHeader() != null && !TextUtils.isEmpty(payloadModel.getHeader().getOverlayText())) {
                viewHolder.productPrice.setText(payloadModel.getHeader().getOverlayText());
                viewHolder.productPrice.setVisibility(View.VISIBLE);
            } else {
                viewHolder.productPrice.setVisibility(View.GONE);
            }

            if (TextUtils.isEmpty(payloadModel.getTitleExt())) {
                viewHolder.productNameSplitLayout.setVisibility(GONE);
                if (!TextUtils.isEmpty(payloadModel.getTitle())) {
                    viewHolder.productNameSingleLine.setVisibility(View.VISIBLE);
                    viewHolder.productNameSingleLine.setText(payloadModel.getTitle());
                } else {
                    viewHolder.productNameSingleLine.setVisibility(View.GONE);
                }
            } else {
                viewHolder.productNameSplitLayout.setVisibility(View.VISIBLE);
                viewHolder.productNameSingleLine.setVisibility(View.GONE);
                viewHolder.productName.setVisibility(View.VISIBLE);
                viewHolder.productRating.setText(payloadModel.getTitleExt());

                if (!TextUtils.isEmpty(payloadModel.getTitle())) {
                    viewHolder.productName.setText(payloadModel.getTitle());
                } else {
                    viewHolder.productName.setText("");
                }
            }

            if (!TextUtils.isEmpty(payloadModel.getSubtitle())) {
                viewHolder.productLocation.setVisibility(View.VISIBLE);
                viewHolder.productLocation.setText(KmRichMessage.getHtmlText(payloadModel.getSubtitle()));
            } else {
                viewHolder.productLocation.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(payloadModel.getDescription())) {
                viewHolder.productDescription.setVisibility(View.VISIBLE);
                viewHolder.productDescription.setText(KmRichMessage.getHtmlText(payloadModel.getDescription()));
            } else {
                viewHolder.productDescription.setVisibility(View.GONE);
            }

            for (int i = 0; i < 3; i++) {
                viewHolder.bookActions[i].setVisibility(GONE);
                viewHolder.viewActions[i].setVisibility(GONE);
            }

            if (payloadModel.getButtons() != null && !payloadModel.getButtons().isEmpty()) {
                try {
                    List<KmRichMessageModel.KmButtonModel> actionsList = payloadModel.getButtons();
                    for (int i = 0; i < actionsList.size(); i++) {
                        if (isMessageProcessed && ButtonKmRichMessage.hideMessage(themeHelper, actionsList.get(i).getType())) {
                            continue;
                        }
                        setupBookActions(viewHolder, i, actionsList);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return payloadList.size();
    }
}
