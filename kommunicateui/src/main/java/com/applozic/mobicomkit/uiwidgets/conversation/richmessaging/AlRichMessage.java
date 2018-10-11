package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.lists.AlRichListsAdapter;
import com.applozic.mobicommons.json.GsonUtils;
import com.bumptech.glide.Glide;

import java.util.Map;

/**
 * Created by ashish on 28/02/18.
 */

public class AlRichMessage {
    public static final String FAQ_ACTIONS = "alFaqActions";
    public static final String SEND_GUEST_LIST = "sendGuestList";
    public static final String SEND_HOTEL_RATING = "sendHotelRating";
    public static final String SEND_HOTEL_DETAILS = "sendHotelDetails";
    public static final String SEND_ROOM_DETAILS_MESSAGE = "sendRoomDetailsMessage";
    public static final String SEND_BOOKING_DETAILS = "sendBookingDetails";
    public static final String MAKE_PAYMENT = "makePayment";
    public static final String LIST_ITEM_CLICK = "listItemClick";

    private Context context;
    private Message message;
    private ALRichMessageListener listener;
    private LinearLayout containerView;

    public AlRichMessage(Context context, LinearLayout containerView, Message message, ALRichMessageListener listener) {
        this.context = context;
        this.message = message;
        this.listener = listener;
        this.containerView = containerView;
    }

    public void createRichMessage() {
        ALRichMessageModel model = (ALRichMessageModel) GsonUtils.getObjectFromJson(GsonUtils.getJsonFromObject(message.getMetadata(), Map.class), ALRichMessageModel.class);

        LinearLayout listItemlayout = containerView.findViewById(R.id.alListMessageLayout);
        LinearLayout faqReplyLayout = containerView.findViewById(R.id.alFaqReplyLayout);
        LinearLayout faqLayout = containerView.findViewById(R.id.alFaqLayout);
        RecyclerView recyclerView = containerView.findViewById(R.id.alRichMessageContainer);

        if (model.getTemplateId() == 7) {
            listItemlayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            faqLayout.setVisibility(View.GONE);
            faqReplyLayout.setVisibility(View.GONE);
            setupListItemView(listItemlayout, model);
        } else if (model.getTemplateId() == 8) {
            faqLayout.setVisibility(View.VISIBLE);
            faqReplyLayout.setVisibility(View.VISIBLE);
            listItemlayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            setupFaqItemView(faqLayout, faqReplyLayout, model);
        } else {
            listItemlayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            faqLayout.setVisibility(View.GONE);
            faqReplyLayout.setVisibility(View.GONE);

            LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(layoutManager);
            ALRichMessageAdapter adapter = new ALRichMessageAdapter(context, model, listener, message);
            recyclerView.setAdapter(adapter);
        }
    }

    private void setupFaqItemView(LinearLayout faqLayout, LinearLayout faqReplyLayout, ALRichMessageModel model) {
        if (model != null) {
            TextView headerText = faqLayout.findViewById(R.id.headerText);
            TextView titleText = faqLayout.findViewById(R.id.questionText);
            TextView descriptionText = faqLayout.findViewById(R.id.bodyText);
            TextView buttonLabel = faqReplyLayout.findViewById(R.id.buttonLabel);
            TextView actionYes = faqReplyLayout.findViewById(R.id.actionYes);
            TextView actionNo = faqReplyLayout.findViewById(R.id.actionNo);

            if (model.getPayload() != null) {
                ALRichMessageModel.ALPayloadModel payload = (ALRichMessageModel.ALPayloadModel) GsonUtils.getObjectFromJson(model.getPayload(), ALRichMessageModel.ALPayloadModel.class);
                if (payload != null) {
                    if (!TextUtils.isEmpty(payload.getTitle())) {
                        titleText.setVisibility(View.VISIBLE);
                        titleText.setText(payload.getTitle());
                    } else {
                        titleText.setVisibility(View.GONE);
                    }

                    if (!TextUtils.isEmpty(payload.getDescription())) {
                        descriptionText.setVisibility(View.VISIBLE);
                        descriptionText.setText(payload.getDescription());
                    } else {
                        descriptionText.setVisibility(View.GONE);
                    }

                    ALRichMessageModel.AlActionModel[] actionModel = payload.getButtons();
                    if (actionModel != null) {
                        faqReplyLayout.setVisibility(View.VISIBLE);

                        if (!TextUtils.isEmpty(payload.getButtonLabel())) {
                            buttonLabel.setVisibility(View.VISIBLE);
                            buttonLabel.setText(payload.getButtonLabel());
                        } else {
                            buttonLabel.setVisibility(View.GONE);
                        }

                        if (actionModel.length > 0 && actionModel[0] != null) {
                            if (!TextUtils.isEmpty(actionModel[0].getName())) {
                                actionYes.setVisibility(View.VISIBLE);
                                actionYes.setText(actionModel[0].getName());
                                setClickListener(actionYes, actionModel[0]);
                            } else {
                                actionYes.setVisibility(View.GONE);
                            }
                        }

                        if (actionModel.length > 1 && actionModel[1] != null) {
                            if (!TextUtils.isEmpty(actionModel[1].getName())) {
                                actionNo.setVisibility(View.VISIBLE);
                                actionNo.setText(actionModel[1].getName());
                                setClickListener(actionNo, actionModel[1]);
                            } else {
                                actionNo.setVisibility(View.GONE);
                            }
                        }
                    } else {
                        faqReplyLayout.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    private void setClickListener(View view, final ALRichMessageModel.AlActionModel actionModel) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context.getApplicationContext() instanceof ALRichMessageListener) {
                    ((ALRichMessageListener) context.getApplicationContext()).onAction(context, FAQ_ACTIONS, message, actionModel.getName());
                } else {
                    if (listener != null) {
                        listener.onAction(context, FAQ_ACTIONS, message, actionModel.getName());
                    }
                }
            }
        });
    }

    private void setListActionListener(View view, final ALRichMessageModel.AlActionModel actionModel) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context.getApplicationContext() instanceof ALRichMessageListener) {
                    ((ALRichMessageListener) context.getApplicationContext()).onAction(context, FAQ_ACTIONS, message, actionModel.getName());
                } else {
                    if (listener != null) {
                        listener.onAction(context, FAQ_ACTIONS, message, actionModel.getName());
                    }
                }
            }
        });
    }

    private void setupListItemView(LinearLayout listItemLayout, ALRichMessageModel model) {
        if (model != null) {
            if (model.getPayload() != null) {
                TextView headerText = listItemLayout.findViewById(R.id.headerText);
                ImageView headerImage = listItemLayout.findViewById(R.id.headerImage);
                final TextView actionText = listItemLayout.findViewById(R.id.actionButton);
                ALRichMessageModel.ALPayloadModel payload = (ALRichMessageModel.ALPayloadModel) GsonUtils.getObjectFromJson(model.getPayload(), ALRichMessageModel.ALPayloadModel.class);
                if (payload != null) {
                    RecyclerView listRecycler = listItemLayout.findViewById(R.id.alListItemRecycler);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
                    listRecycler.setLayoutManager(layoutManager);
                    AlRichListsAdapter adapter = new AlRichListsAdapter(context, payload.getElements(), listener);
                    listRecycler.setAdapter(adapter);

                    if (!TextUtils.isEmpty(payload.getHeaderText())) {
                        headerText.setVisibility(View.VISIBLE);
                        headerText.setText(payload.getHeaderText());
                    } else {
                        headerText.setVisibility(View.GONE);
                    }

                    if (!TextUtils.isEmpty(payload.getHeaderImgSrc())) {
                        headerImage.setVisibility(View.VISIBLE);
                        Glide.with(context).load(payload.getHeaderImgSrc()).into(headerImage);
                    } else {
                        headerImage.setVisibility(View.GONE);
                    }

                    if (payload.getButtons() != null) {
                        actionText.setVisibility(View.VISIBLE);
                        final ALRichMessageModel.AlActionModel[] action = payload.getButtons();

                        if (action[0] != null) {
                            actionText.setText(action[0].getName());
                            setListActionListener(actionText, action[0]);
                        }
                    } else {
                        actionText.setVisibility(View.GONE);
                    }
                }
            }
        }
    }
}
