package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.callbacks.KmRichMessageListener;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.KmRichMessage;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.KmBookingDetailsModel;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.KmGuestCountModel;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.KmRichMessageModel;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.KmHotelBookingModel;
import com.applozic.mobicomkit.uiwidgets.kommunicate.utils.KmThemeHelper;
import com.applozic.mobicommons.json.GsonUtils;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.view.View.GONE;
import static com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.types.ListKmRichMessage.MAX_ACTIONS_LIMIT;

/**
 * Created by ashish on 28/02/18.
 */

public class KmBookingRMAdapter extends KmRichMessageAdapter {

    private List<KmHotelBookingModel> hotelList;
    private List<KmHotelBookingModel> roomList;
    private List<KmGuestCountModel> guestList;
    private List<String> titleList;
    private List<KmRichMessageModel.KmPayloadModel> payloadList;
    public final int MAX_GUEST_COUNT = 5;
    public final int MIN_GUEST_COUNT = 0;
    public final int MAX_CHILD_GUEST_COUNT = 2;
    public final int MAX_RATING_VALUE = 5;

    KmBookingRMAdapter(Context context, KmRichMessageModel model, KmRichMessageListener listener, Message message, KmThemeHelper themeHelper) {
        super(context, model, listener, message, themeHelper);

        if (model.getHotelList() != null) {
            this.hotelList = Arrays.asList((KmHotelBookingModel[])
                    GsonUtils.getObjectFromJson(model.getHotelList(), KmHotelBookingModel[].class));
        }

        if (model.getPayload() != null) {
            this.payloadList = Arrays.asList((KmRichMessageModel.KmPayloadModel[])
                    GsonUtils.getObjectFromJson(model.getPayload(), KmRichMessageModel.KmPayloadModel[].class));
        }

        if (model.getTemplateId() == 1) {
            KmGuestCountModel guestCount = new KmGuestCountModel();
            guestList = new ArrayList<>();
            guestList.add(guestCount);
        }

        if (model.getHotelRoomDetail() != null) {
            KmHotelBookingModel.RoomDetailModel roomDetailModel = (KmHotelBookingModel.RoomDetailModel) GsonUtils.getObjectFromJson(model.getHotelRoomDetail(), KmHotelBookingModel.RoomDetailModel.class);
            this.roomList = roomDetailModel.getHotelRoomsDetails();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (model != null && model.getHotelRoomDetail() != null) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.km_hotel_details_layout, parent, false);
            return new HotelDetailHolder(itemView);
        } else if (model != null && model.getTemplateId() == 5) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.km_booking_details_layout, parent, false);
            return new BookingDetailsHolder(itemView);
        } else if (model != null && model.getTemplateId() == 1) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.km_guest_details_layout, parent, false);
            return new GuestCountHolder(itemView);
        } else if (model != null && (model.getTemplateId() == 2 || hotelList != null)) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.km_rich_message_item, parent, false);
            return new KmCardRMAdapter.CardViewHolder(itemView);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        bindItems(holder, position);
    }

    @Override
    public int getItemCount() {
        if (hotelList != null) {
            return hotelList.size();
        } else if (model.getTemplateId() == 1) {
            return guestList.size();
        } else if (model.getTemplateId() == 2) {
            return payloadList != null ? payloadList.size() : 0;
        } else if (roomList != null) {
            return roomList.size();
        } else if (model.getTemplateId() == 5) {
            return 1;
        }
        return 0;
    }

    //holder classes >>>
    private class HotelDetailHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView roomTypeTv;
        TextView noOfGuestTv;
        TextView priceTv;
        TextView totalPriceHeaderTv;
        TextView totalPriceTv;
        TextView bookAction;

        public HotelDetailHolder(View itemView) {
            super(itemView);

            productImage = itemView.findViewById(R.id.productImage);
            roomTypeTv = itemView.findViewById(R.id.roomTypeTv);
            noOfGuestTv = itemView.findViewById(R.id.noOfGuestsTv);
            priceTv = itemView.findViewById(R.id.hotelPriceTv);
            totalPriceHeaderTv = itemView.findViewById(R.id.totalPriceDistTv);
            totalPriceTv = itemView.findViewById(R.id.totalPriceTv);
            bookAction = itemView.findViewById(R.id.bookingAction);
        }
    }

    private class BookingDetailsHolder extends RecyclerView.ViewHolder {
        Spinner titleSpinner;
        EditText firstNameEt;
        EditText lastNameEt;
        EditText emailIdEt;
        EditText contactNumberEt;
        TextView submitAction;

        public BookingDetailsHolder(View itemView) {
            super(itemView);

            titleSpinner = itemView.findViewById(R.id.titleSpinner);
            firstNameEt = itemView.findViewById(R.id.firstNameEt);
            lastNameEt = itemView.findViewById(R.id.lastNameEt);
            emailIdEt = itemView.findViewById(R.id.emailIdEt);
            contactNumberEt = itemView.findViewById(R.id.contactNumberEt);
            submitAction = itemView.findViewById(R.id.submitDetails);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, Arrays.asList(context.getString(R.string.list_item_title), context.getString(R.string.list_item_mr), context.getString(R.string.list_item_ms), context.getString(R.string.list_item_mrs)));
            titleSpinner.setAdapter(adapter);
        }
    }

    private class GuestCountHolder extends RecyclerView.ViewHolder {
        TextView adultCountTv, childCountTv;
        Button adultCountDecBt, childCountDecBt, adultCountIncrementBt, childCountIncrementBt;
        TextView roomDetailTv;
        TextView addRoomTv, removeRoomTv, doneTv;
        LinearLayout selectionActionLayout;
        LinearLayout selectionRootLayout;

        public GuestCountHolder(View itemView) {
            super(itemView);

            adultCountTv = itemView.findViewById(R.id.adultCountTv);
            childCountTv = itemView.findViewById(R.id.childCountTv);
            adultCountIncrementBt = itemView.findViewById(R.id.adultCountIncrementBt);
            childCountIncrementBt = itemView.findViewById(R.id.childCountIncrementBt);
            adultCountDecBt = itemView.findViewById(R.id.adultCountDecBt);
            childCountDecBt = itemView.findViewById(R.id.childCountDecBt);
            roomDetailTv = itemView.findViewById(R.id.alRoomDetailsTv);
            addRoomTv = itemView.findViewById(R.id.addRoomTv);
            removeRoomTv = itemView.findViewById(R.id.removeRoomTv);
            doneTv = itemView.findViewById(R.id.doneButtonTv);
            selectionActionLayout = itemView.findViewById(R.id.actionLayout);
            selectionRootLayout = itemView.findViewById(R.id.rootSelectionLayout);
        }
    }
    //holder classes <<<

    private void setGuestInfoClickListeners(final GuestCountHolder holder, final int position) {
        holder.adultCountDecBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = Integer.parseInt(holder.adultCountTv.getText().toString());
                KmGuestCountModel guestModel = guestList.get(position);
                if (count > 1) {
                    holder.adultCountTv.setText(String.valueOf(count - 1));
                    guestModel.setNoOfAdults(String.valueOf(count - 1));
                }
            }
        });

        holder.adultCountIncrementBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = Integer.parseInt(holder.adultCountTv.getText().toString());
                KmGuestCountModel guestModel = guestList.get(position);
                if (count < MAX_GUEST_COUNT) {
                    holder.adultCountTv.setText(String.valueOf(count + 1));
                    guestModel.setNoOfAdults(String.valueOf(count + 1));
                }
            }
        });

        holder.childCountDecBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = Integer.parseInt(holder.childCountTv.getText().toString());
                KmGuestCountModel guestModel = guestList.get(position);
                if (count > MIN_GUEST_COUNT) {
                    holder.childCountTv.setText(String.valueOf(count - 1));
                    guestModel.getChildAge().remove(guestModel.getChildAge().size() - 1);
                    guestModel.setNoOfChild(String.valueOf(count - 1));
                }
            }
        });

        holder.childCountIncrementBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = Integer.parseInt(holder.childCountTv.getText().toString());
                KmGuestCountModel guestModel = guestList.get(position);
                if (count < MAX_CHILD_GUEST_COUNT) {
                    holder.childCountTv.setText(String.valueOf(count + 1));
                    guestModel.getChildAge().add(10);
                    guestModel.setNoOfChild(String.valueOf(count + 1));
                }
            }
        });

        holder.addRoomTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KmGuestCountModel model = new KmGuestCountModel();
                guestList.add(model);
                notifyDataSetChanged();
            }
        });

        holder.removeRoomTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guestList.remove(position);
                notifyDataSetChanged();
            }
        });

        holder.doneTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onAction(context, KmRichMessage.SEND_GUEST_LIST, null, guestList, null);
            }
        });
    }

    private void bindGuestInfoView(final GuestCountHolder holder, final int position) {
        final KmGuestCountModel guestModel = guestList.get(position);

        if (guestModel != null) {
            holder.roomDetailTv.setText(context.getString(R.string.room, position + 1));
            holder.adultCountTv.setText(guestModel.getNoOfAdults());
            holder.childCountTv.setText(guestModel.getNoOfChild());

            if (position == guestList.size() - 1) {
                holder.addRoomTv.setVisibility(View.VISIBLE);
                holder.doneTv.setVisibility(View.VISIBLE);
            } else {
                holder.addRoomTv.setVisibility(GONE);
                holder.doneTv.setVisibility(GONE);
            }

            if (guestList.size() > 1 && position == 0) {
                holder.selectionActionLayout.setVisibility(GONE);
            } else {
                holder.selectionActionLayout.setVisibility(View.VISIBLE);
            }

            if (position == 0) {
                holder.removeRoomTv.setVisibility(GONE);
            } else {
                holder.removeRoomTv.setVisibility(View.VISIBLE);
            }
            setGuestInfoClickListeners(holder, position);
        }
    }

    private void setupBookActions(TextView bookAction, View viewAction, int index, KmRichMessageModel.KmPayloadModel payload, List<KmRichMessageModel.KmButtonModel> actionsList) {
        bookAction.setVisibility(View.VISIBLE);
        viewAction.setVisibility(View.VISIBLE);
        bookAction.setText(actionsList.get(index).getName());
        bookAction.setOnClickListener(getActionClickListener(actionsList.get(index), payload.getReplyMetadata()));
    }

    //for templateId = 2
    private void bindHotelView(KmCardRMAdapter.CardViewHolder viewHolder, int position) {
        if (hotelList != null) {
            final KmHotelBookingModel hotel = hotelList.get(position);

            if (!TextUtils.isEmpty(hotel.getHotelName())) {
                viewHolder.productNameSingleLine.setText(KmRichMessage.getHtmlText(context.getString(R.string.hotel_rating, hotel.getHotelName(), String.valueOf(hotel.getStarRating()), MAX_RATING_VALUE)));
            } else {
                viewHolder.productNameSingleLine.setText(KmRichMessage.getHtmlText(context.getString(R.string.name_unavailable, String.valueOf(hotel.getStarRating()), MAX_RATING_VALUE)));
            }

            viewHolder.productPrice.setText(context.getString(R.string.rupee_symbol, String.valueOf(hotel.getPrice().getRoomPrice())));

            if (!TextUtils.isEmpty(hotel.getHotelPicture())) {
                Glide.with(context).load(hotel.getHotelPicture()).into(viewHolder.productImage);
            } else {
                viewHolder.productImage.setImageBitmap(null);
            }

            if (!TextUtils.isEmpty(hotel.getHotelAddress())) {
                viewHolder.productLocation.setText(KmRichMessage.getHtmlText(hotel.getHotelAddress()));
            } else {
                viewHolder.productLocation.setText(context.getString(R.string.address_unavailable));
            }

            if (!TextUtils.isEmpty(hotel.getHotelDescription())) {
                viewHolder.productDescription.setText(KmRichMessage.getHtmlText(hotel.getHotelDescription()));
            } else {
                viewHolder.productDescription.setText(context.getString(R.string.description_unavailable));
            }

            for (TextView textView : viewHolder.bookActions)
                textView.setVisibility(View.GONE);
            for (View view : viewHolder.viewActions)
                view.setVisibility(View.GONE);

            for (int i = 0; i < 2; i++) {
                viewHolder.bookActions[i].setVisibility(GONE);
                viewHolder.viewActions[i].setVisibility(GONE);
            }

            viewHolder.bookActions[0].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hotel.setSessionId(model.getSessionId());
                    listener.onAction(context, KmRichMessage.SEND_HOTEL_DETAILS, null, hotel, null);
                }
            });
        } else if (payloadList != null) {
            final KmRichMessageModel.KmPayloadModel payload = payloadList.get(position);

            if (!TextUtils.isEmpty(payload.getHeaderImageUrl())) {
                Glide.with(context).load(payload.getHeaderImageUrl()).into(viewHolder.productImage);
                viewHolder.productImage.setVisibility(View.VISIBLE);
                viewHolder.productImageOverlay.setVisibility(View.VISIBLE);
                viewHolder.productPrice.setBackground(context.getResources().getDrawable(R.drawable.km_rich_messaging_price_border));
            } else {
                viewHolder.productImage.setVisibility(View.GONE);
                viewHolder.productImageOverlay.setVisibility(GONE);
                viewHolder.productPrice.setBackground(context.getResources().getDrawable(R.drawable.km_imageless_rich_message_price_border));
            }

            if (!TextUtils.isEmpty(payload.getOverlayText())) {
                viewHolder.productPrice.setText(payload.getOverlayText());
                viewHolder.productPrice.setVisibility(View.VISIBLE);
            } else {
                viewHolder.productPrice.setVisibility(View.GONE);
            }

            if (TextUtils.isEmpty(payload.getRating())) {
                viewHolder.productNameSplitLayout.setVisibility(GONE);
                if (!TextUtils.isEmpty(payload.getTitle())) {
                    viewHolder.productNameSingleLine.setVisibility(View.VISIBLE);
                    viewHolder.productNameSingleLine.setText(payload.getTitle());
                } else {
                    viewHolder.productNameSingleLine.setVisibility(View.GONE);
                }
            } else {
                viewHolder.productNameSplitLayout.setVisibility(View.VISIBLE);
                viewHolder.productNameSingleLine.setVisibility(View.GONE);
                viewHolder.productName.setVisibility(View.VISIBLE);
                viewHolder.productRating.setText(payload.getRating());

                if (!TextUtils.isEmpty(payload.getTitle())) {
                    viewHolder.productName.setText(payload.getTitle());
                } else {
                    viewHolder.productName.setText("");
                }
            }

            if (!TextUtils.isEmpty(payload.getSubtitle())) {
                viewHolder.productLocation.setVisibility(View.VISIBLE);
                viewHolder.productLocation.setText(KmRichMessage.getHtmlText(payload.getSubtitle()));
            } else {
                viewHolder.productLocation.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(payload.getDescription())) {
                viewHolder.productDescription.setVisibility(View.VISIBLE);
                viewHolder.productDescription.setText(KmRichMessage.getHtmlText(payload.getDescription()));
            } else {
                viewHolder.productDescription.setVisibility(View.GONE);
            }

            for (int i = 0; i < MAX_ACTIONS_LIMIT; i++) {
                viewHolder.bookActions[i].setVisibility(GONE);
                viewHolder.viewActions[i].setVisibility(GONE);
            }

            if (payload.getActions() != null && !payload.getActions().isEmpty()) {
                try {
                    List<KmRichMessageModel.KmButtonModel> actionsList = payload.getActions();
                    for (int i = 0; i < actionsList.size(); i++) {
                        setupBookActions(viewHolder.bookActions[i], viewHolder.viewActions[i], i, payload, actionsList);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void bindRoomDetailView(HotelDetailHolder holder, final int position) {
        final KmHotelBookingModel hotel = roomList.get(position);

        if (hotel != null) {
            if (!TextUtils.isEmpty(hotel.getRoomTypeName())) {
                holder.roomTypeTv.setText(KmRichMessage.getHtmlText(hotel.getRoomTypeName()));
            } else {
                holder.roomTypeTv.setText(context.getString(R.string.room_name_unavailable));
            }

            if (!TextUtils.isEmpty(hotel.getHotelPicture())) {
                Glide.with(context).load(hotel.getHotelPicture()).into(holder.productImage);
            } else {
                holder.productImage.setImageDrawable(null);
            }

            holder.noOfGuestTv.setText(String.valueOf(hotel.getNoOfGuest()));

            String text = context.getString(R.string.room_night_detail, hotel.getNoOfNights());
            holder.totalPriceHeaderTv.setText(text);

            holder.priceTv.setText(context.getString(R.string.rupee_symbol, String.valueOf(hotel.getPrice().getRoomPrice())));

            holder.totalPriceTv.setText(context.getString(R.string.rupee_symbol, String.valueOf(hotel.getPrice().getRoomPrice() * hotel.getNoOfNights())));

            holder.bookAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hotel.setSessionId(model.getSessionId());
                    listener.onAction(context, KmRichMessage.SEND_ROOM_DETAILS_MESSAGE, null, hotel, null);
                }
            });
        }
    }

    private void bindBookingDetailHolder(final BookingDetailsHolder holder, final int position) {
        final KmBookingDetailsModel detailsModel = new KmBookingDetailsModel();
        detailsModel.setSessionId(model.getSessionId());

        final KmBookingDetailsModel.ALBookingDetails bookingDetails = detailsModel.getPersonInfo();

        holder.titleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bookingDetails.setTitle(titleList.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                bookingDetails.setTitle(context.getString(R.string.list_item_title));
            }
        });

        holder.submitAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(holder.firstNameEt.getText().toString().trim())
                        || TextUtils.isEmpty(holder.lastNameEt.getText().toString().trim())
                        || TextUtils.isEmpty(holder.emailIdEt.getText().toString().trim())
                        || TextUtils.isEmpty(holder.firstNameEt.getText().toString().trim())
                        || "Title *".equals(titleList.get(holder.titleSpinner.getSelectedItemPosition()))) {
                    Toast.makeText(context, R.string.mandatory_fields, Toast.LENGTH_SHORT).show();
                } else {
                    bookingDetails.setTitle(titleList.get(holder.titleSpinner.getSelectedItemPosition()));
                    bookingDetails.setFirstName(holder.firstNameEt.getText().toString());
                    bookingDetails.setLastName(holder.lastNameEt.getText().toString());
                    bookingDetails.setEmailId(holder.emailIdEt.getText().toString());
                    bookingDetails.setPhoneNo(holder.contactNumberEt.getText().toString());

                    listener.onAction(context, KmRichMessage.SEND_BOOKING_DETAILS, null, detailsModel, null);
                }
            }
        });
    }

    @Override
    void bindItems(RecyclerView.ViewHolder holder, int position) {
        super.bindItems(holder, position);
        if (model.getHotelRoomDetail() != null) {
            HotelDetailHolder detailHolder = (HotelDetailHolder) holder;
            bindRoomDetailView(detailHolder, position);
        } else if (model.getTemplateId() == 5) {
            BookingDetailsHolder bookingDetailsHolder = (BookingDetailsHolder) holder;
            bindBookingDetailHolder(bookingDetailsHolder, position);
        } else if (hotelList != null || (model != null && model.getTemplateId() == 2)) {
            KmCardRMAdapter.CardViewHolder viewHolder = (KmCardRMAdapter.CardViewHolder) holder;
            bindHotelView(viewHolder, position);
        } else if (model != null && model.getTemplateId() == 1) {
            GuestCountHolder guestCountHolder = (GuestCountHolder) holder;
            bindGuestInfoView(guestCountHolder, position);
        }
    }
}
