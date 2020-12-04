package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.FullScreenImageActivity;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.MobiComKitActivityInterface;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.callbacks.KmRichMessageListener;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.helpers.KmFormStateHelper;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.KmBookingDetailsModel;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.KmGuestCountModel;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.KmRichMessageModel;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.KmHotelBookingModel;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.KmFormStateModel;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.v2.KmFormPayloadModel;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.v2.KmRMActionModel;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.webview.KmWebViewActivity;
import com.applozic.mobicomkit.uiwidgets.kommunicate.adapters.KmAutoSuggestionAdapter;
import com.applozic.mobicomkit.uiwidgets.kommunicate.views.KmToast;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.kommunicate.KmSettings;
import io.kommunicate.Kommunicate;
import io.kommunicate.async.KmPostDataAsyncTask;
import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.models.KmAutoSuggestionModel;

public class RichMessageActionProcessor implements KmRichMessageListener {

    private KmRichMessageListener richMessageListener;
    public static final String NOTIFY_ITEM_CHANGE = "notifyItemChange";
    private static final String TAG = "AlRichMessageAction";

    public RichMessageActionProcessor(KmRichMessageListener richMessageListener) {
        this.richMessageListener = richMessageListener;
    }

    public KmRichMessageListener getRichMessageListener() {
        return this;
    }

    @Override
    public void onAction(Context context, String action, Message message, Object object, Map<String, Object> replyMetadata) {
        switch (action) {
            case KmRichMessage.SEND_GUEST_LIST:
                List<KmGuestCountModel> guestCountModels = (List<KmGuestCountModel>) object;
                sendGuestListMessage(guestCountModels, getStringMap(replyMetadata));
                break;

            case KmRichMessage.SEND_HOTEL_RATING:
                sendMessage((String) object, getStringMap(replyMetadata));
                break;

            case KmRichMessage.SEND_HOTEL_DETAILS:
                sendHotelDetailMessage((KmHotelBookingModel) object, getStringMap(replyMetadata));
                break;

            case KmRichMessage.SEND_ROOM_DETAILS_MESSAGE:
                sendRoomDetailsMessage((KmHotelBookingModel) object, getStringMap(replyMetadata));
                break;

            case KmRichMessage.SEND_BOOKING_DETAILS:
                sendBookingDetailsMessage((KmBookingDetailsModel) object, getStringMap(replyMetadata));
                break;

            case KmRichMessage.MAKE_PAYMENT:
            case KmRichMessage.SUBMIT_BUTTON:
                if (object instanceof KmRMActionModel.SubmitButton) {
                    handleKmSubmitButton(context, message, (KmRMActionModel.SubmitButton) object);
                } else {
                    handleSubmitButton(context, object);
                }
                break;

            case KmRichMessage.QUICK_REPLY_OLD:
            case KmRichMessage.QUICK_REPLY:
                if (object instanceof String) {
                    sendMessage((String) object, getStringMap(replyMetadata));
                } else {
                    handleQuickReplies(object, replyMetadata);
                }
                break;

            case KmRichMessage.TEMPLATE_ID + 9:
                loadImageOnFullScreen(context, action, (KmRichMessageModel.KmPayloadModel) object);
                break;

            case KmRichMessage.WEB_LINK:
                handleWebLinks(object);
                break;
            case KmAutoSuggestionAdapter.KM_AUTO_SUGGESTION_ACTION:
                try {
                    KmAutoSuggestionModel autoSuggestionModel = (KmAutoSuggestionModel) object;
                    if (richMessageListener != null) {
                        richMessageListener.onAction(context, KmAutoSuggestionAdapter.KM_AUTO_SUGGESTION_ACTION, null, autoSuggestionModel.getContent(), null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public void handleWebLinks(Object object) {
        KmRichMessageModel.KmAction kmAction = null;

        if (object instanceof KmRichMessageModel.KmButtonModel) {
            kmAction = ((KmRichMessageModel.KmButtonModel) object).getAction();
        } else if (object instanceof KmRichMessageModel.KmElementModel) {
            kmAction = ((KmRichMessageModel.KmElementModel) object).getAction();
        } else if (object instanceof KmRichMessageModel.KmAction) {
            kmAction = (KmRichMessageModel.KmAction) object;
        } else if (object instanceof KmRichMessageModel.KmPayloadModel) {
            kmAction = ((KmRichMessageModel.KmPayloadModel) object).getAction();
        }

        if (kmAction != null) {
            if (!TextUtils.isEmpty(kmAction.getUrl())) {
                openWebLink(kmAction.getUrl(), kmAction.isDeepLink());
            } else if (kmAction.getPayload() != null && !TextUtils.isEmpty(kmAction.getPayload().getUrl())) {
                openWebLink(kmAction.getPayload().getUrl(), kmAction.getPayload().isDeepLink());
            }
        }

        if (object instanceof KmRichMessageModel.KmPayloadModel) {
            KmRichMessageModel.KmPayloadModel payloadModel = (KmRichMessageModel.KmPayloadModel) object;
            if (!TextUtils.isEmpty(payloadModel.getUrl())) {
                openWebLink(payloadModel.getUrl(), payloadModel.isDeepLink());
            }
        }
    }

    private void updateLanguage(String languageCode) {
        if (!TextUtils.isEmpty(languageCode)) {
            KmSettings.updateUserLanguage(ApplozicService.getAppContext(), languageCode);
        }
    }

    public void handleQuickReplies(Object object, Map<String, Object> replyMetadata) {
        String message = null;

        if (object instanceof KmRichMessageModel.KmPayloadModel) {
            KmRichMessageModel.KmPayloadModel payloadModel = (KmRichMessageModel.KmPayloadModel) object;
            updateLanguage(payloadModel.getUpdateLanguage());
            if (payloadModel.getAction() != null && !TextUtils.isEmpty(payloadModel.getAction().getMessage())) {
                handleQuickReplies(payloadModel.getAction(), payloadModel.getReplyMetadata());
            } else {
                message = !TextUtils.isEmpty(payloadModel.getMessage()) ? payloadModel.getMessage() : payloadModel.getName();
            }
        } else if (object instanceof KmRichMessageModel.KmButtonModel) {
            KmRichMessageModel.KmButtonModel buttonModel = (KmRichMessageModel.KmButtonModel) object;
            if (isValidAction(buttonModel.getAction())) {
                handleQuickReplies(buttonModel.getAction(), replyMetadata);
            } else {
                message = buttonModel.getName();
            }
        } else if (object instanceof KmRichMessageModel.KmAction) {
            KmRichMessageModel.KmAction action = (KmRichMessageModel.KmAction) object;
            updateLanguage(action.getUpdateLanguage());
            if (action.getPayload() != null) {
                updateLanguage(action.getPayload().getUpdateLanguage());
                if (!TextUtils.isEmpty(action.getPayload().getMessage())) {
                    message = action.getPayload().getMessage();
                } else if (!TextUtils.isEmpty(action.getPayload().getTitle())) {
                    message = action.getPayload().getTitle();
                }
            } else {
                message = !TextUtils.isEmpty(action.getMessage()) ? action.getMessage() : !TextUtils.isEmpty(action.getText()) ? action.getText() : !TextUtils.isEmpty(action.getTitle()) ? action.getTitle() : action.getName();
            }
        } else if (object instanceof KmRichMessageModel.KmElementModel) {
            KmRichMessageModel.KmElementModel elementModel = (KmRichMessageModel.KmElementModel) object;
            if (replyMetadata == null) {
                replyMetadata = new HashMap<>();
            }
            if (elementModel.getArticleId() != null) {
                replyMetadata.put(KmRichMessage.KM_FAQ_ID, elementModel.getArticleId());
            }
            if (!TextUtils.isEmpty(elementModel.getSource())) {
                replyMetadata.put(KmRichMessage.KM_SOURCE, elementModel.getSource());
            }

            if (isValidAction(elementModel.getAction())) {
                handleQuickReplies(elementModel.getAction(), replyMetadata);
            } else {
                message = elementModel.getTitle();
            }
        }

        if (!TextUtils.isEmpty(message)) {
            sendMessage(message, getStringMap(replyMetadata));
        }
    }

    public boolean isValidAction(KmRichMessageModel.KmAction action) {
        return action != null && (action.getPayload() != null || !TextUtils.isEmpty(action.getText()));
    }

    public void handleSubmitButton(Context context, Object object) {
        if (object instanceof KmRMActionModel.SubmitButton) {
            KmRMActionModel.SubmitButton submitButton = (KmRMActionModel.SubmitButton) object;
            // Need to implement
        } else if (object instanceof KmRichMessageModel.KmButtonModel) {
            KmRichMessageModel.KmButtonModel buttonModel = (KmRichMessageModel.KmButtonModel) object;
            if (buttonModel.getAction() != null && buttonModel.getAction().getPayload() != null) {
                openWebLink(GsonUtils.getJsonFromObject(buttonModel.getAction().getPayload().getFormData(), KmRichMessageModel.KmFormDataModel.class)
                        , buttonModel.getAction().getPayload().getFormAction());
            }
        } else if (object instanceof KmRichMessageModel) {
            KmRichMessageModel model = (KmRichMessageModel) object;
            openWebLink(model.getFormData(), model.getFormAction());
        } else if (object instanceof KmRichMessageModel.KmPayloadModel) {
            makeFormRequest(context, (KmRichMessageModel.KmPayloadModel) object);
        }
    }

    private boolean isInvalidData(Map<String, Object> dataMap, KmRMActionModel.SubmitButton submitButton) {
        return (dataMap == null || dataMap.isEmpty()) && (submitButton.getFormData() == null || submitButton.getFormData().isEmpty());
    }

    public void handleKmSubmitButton(final Context context, final Message message, final KmRMActionModel.SubmitButton submitButtonModel) {
        KmFormStateModel formStateModel = null;
        if (message != null) {
            formStateModel = KmFormStateHelper.getFormState(message.getKeyString());
        }
        final Map<String, Object> dataMap = KmFormStateHelper.getKmFormMap(message, formStateModel);

        if (isInvalidData(dataMap, submitButtonModel)) {
            KmToast.error(context, Utils.getString(context, R.string.km_invalid_form_data_error), Toast.LENGTH_SHORT).show();
            return;
        }

        Utils.printLog(context, TAG, "Submitting data : " + GsonUtils.getJsonFromObject(formStateModel != null ? dataMap : submitButtonModel.getFormData(), Map.class));

        if (KmRMActionModel.SubmitButton.KM_POST_DATA_TO_BOT_PLATFORM.equals(submitButtonModel.getRequestType())) {
            sendMessage(submitButtonModel.getMessage(), getStringMap(submitButtonModel.getReplyMetadata()), dataMap, submitButtonModel.getFormData());
            if (richMessageListener != null) {
                richMessageListener.onAction(context, NOTIFY_ITEM_CHANGE, message, dataMap, submitButtonModel.getReplyMetadata());
            }
        } else {
            if (!TextUtils.isEmpty(submitButtonModel.getMessage())) {
                sendMessage(submitButtonModel.getMessage(), getStringMap(submitButtonModel.getReplyMetadata()));
            }
            new KmPostDataAsyncTask(context,
                    submitButtonModel.getFormAction(),
                    null,
                    KmWebViewActivity.REQUEST_TYPE_JSON.equals(submitButtonModel.getRequestType()) ? "application/json" : KmWebViewActivity.DEFAULT_REQUEST_TYPE,
                    GsonUtils.getJsonFromObject(formStateModel != null ? dataMap : submitButtonModel.getFormData(), Map.class),
                    new KmCallback() {
                        @Override
                        public void onSuccess(Object messageString) {
                            Utils.printLog(context, TAG, "Submit post success : " + messageString);
                            if (richMessageListener != null) {
                                richMessageListener.onAction(context, NOTIFY_ITEM_CHANGE, message, dataMap, submitButtonModel.getReplyMetadata());
                            }
                        }

                        @Override
                        public void onFailure(Object error) {
                            Utils.printLog(context, TAG, "Submit post error : " + error);
                        }
                    }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void sendMessage(String message, Map<String, String> replyMetadata, Map<String, Object> formSelectedData, Map<String, String> formData) {
        Map<String, String> metadata = new HashMap<>();
        if (replyMetadata != null) {
            metadata.putAll(replyMetadata);
        }
        if (formSelectedData != null) {
            Map<String, String> formDataMap = new HashMap<>();
            formDataMap.put(KmFormPayloadModel.KM_FORM_DATA, GsonUtils.getJsonFromObject(getStringMap(formSelectedData), Map.class));
            metadata.put(Kommunicate.KM_CHAT_CONTEXT, GsonUtils.getJsonFromObject(formDataMap, Map.class));
        } else {
            metadata.putAll(formData);
        }
        sendMessage(message, metadata);
    }

    public Map<String, String> getStringMap(Map<String, Object> objectMap) {
        if (objectMap == null) {
            return null;
        }
        Map<String, String> newMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
            newMap.put(entry.getKey(), entry.getValue() instanceof String ? (String) entry.getValue() : GsonUtils.getJsonFromObject(entry.getValue(), Object.class));
        }
        return newMap;
    }

    public void sendMessage(String message, Map<String, String> replyMetadata) {
        sendMessage(message, replyMetadata, Message.ContentType.DEFAULT.getValue());
    }

    public void openWebLink(String url, boolean isDeepLink) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(KmRichMessage.WEB_LINK, true);
        bundle.putString(KmRichMessage.LINK_URL, url);
        bundle.putBoolean(KmRichMessage.IS_DEEP_LINK, isDeepLink);
        if (richMessageListener != null) {
            richMessageListener.onAction(null, KmRichMessage.OPEN_WEB_VIEW_ACTIVITY, null, bundle, null);
        }
    }

    public void makeFormRequest(final Context context, KmRichMessageModel.KmPayloadModel payloadModel) {
        if (payloadModel != null && payloadModel.getAction() != null) {
            if (!TextUtils.isEmpty(payloadModel.getAction().getMessage())) {
                sendMessage(payloadModel.getAction().getMessage(), getStringMap(payloadModel.getReplyMetadata()));
            } else if (!TextUtils.isEmpty(payloadModel.getAction().getName())) {
                sendMessage(payloadModel.getAction().getName(), getStringMap(payloadModel.getReplyMetadata()));
            }

            new KmPostDataAsyncTask(context,
                    payloadModel.getAction().getFormAction(),
                    null,
                    KmWebViewActivity.REQUEST_TYPE_JSON.equals(payloadModel.getRequestType()) ? "application/json" : KmWebViewActivity.DEFAULT_REQUEST_TYPE,
                    GsonUtils.getJsonFromObject(payloadModel.getFormData(), KmRichMessageModel.KmFormDataModel.class),
                    new KmCallback() {
                        @Override
                        public void onSuccess(Object message) {
                            Utils.printLog(context, TAG, "Submit post success : " + message);
                        }

                        @Override
                        public void onFailure(Object error) {
                            Utils.printLog(context, TAG, "Submit post error : " + error);
                        }
                    }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void openWebLink(String formData, String formAction) {
        Bundle bundle = new Bundle();
        if (!TextUtils.isEmpty(formData)) {
            bundle.putString(KmRichMessage.KM_FORM_DATA, formData);
        }
        if (!TextUtils.isEmpty(formAction)) {
            bundle.putString(KmRichMessage.KM_FORM_ACTION, formAction);
        }

        if (richMessageListener != null) {
            richMessageListener.onAction(null, KmRichMessage.OPEN_WEB_VIEW_ACTIVITY, null, bundle, null);
        }
    }

    public void sendGuestListMessage(List<KmGuestCountModel> guestList, Map<String, String> replyMetadata) {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("guestTypeId", "ADULTS");
        metadata.put("isRoomGuestJSON", "true");
        metadata.put("roomGuestJson", GsonUtils.getJsonFromObject(guestList, List.class));

        StringBuilder message = new StringBuilder("");
        int count = 0;

        for (KmGuestCountModel guestModel : guestList) {
            message.append("Room ");
            message.append(count + 1);
            message.append(" Guest ");
            message.append(guestModel.getNoOfAdults());
            message.append(" Children ");
            message.append(guestModel.getNoOfChild());
            message.append(", ");
        }

        if (replyMetadata != null) {
            metadata.putAll(replyMetadata);
        }

        sendMessage(message.toString(), metadata, Message.ContentType.DEFAULT.getValue());
    }

    public void sendHotelDetailMessage(KmHotelBookingModel hotel, Map<String, String> replyMetadata) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("hotelSelected", "true");
        metadata.put("resultIndex", String.valueOf(hotel.getResultIndex()));
        metadata.put("sessionId", hotel.getSessionId());
        metadata.put("skipBot", "true");

        String message = "Get room detail of " + hotel.getHotelName();

        if (replyMetadata != null) {
            metadata.putAll(replyMetadata);
        }

        sendMessage(message, metadata, Message.ContentType.DEFAULT.getValue());
    }

    public void sendRoomDetailsMessage(KmHotelBookingModel hotel, Map<String, String> replyMetadata) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("HotelResultIndex", String.valueOf(hotel.getHotelResultIndex()));
        metadata.put("NoOfRooms", String.valueOf(hotel.getNoOfRooms()));
        metadata.put("RoomIndex", String.valueOf(hotel.getRoomIndex()));
        metadata.put("blockHotelRoom", "true");
        metadata.put("sessionId", hotel.getSessionId());
        metadata.put("skipBot", "true");

        if (replyMetadata != null) {
            metadata.putAll(replyMetadata);
        }

        String message = "Book Hotel " + hotel.getHotelName() + ", Room " + hotel.getRoomTypeName();

        sendMessage(message, metadata, Message.ContentType.DEFAULT.getValue());
    }

    public void sendBookingDetailsMessage(KmBookingDetailsModel model, Map<String, String> replyMetadata) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("guestDetail", "true");
        metadata.put("personInfo", GsonUtils.getJsonFromObject(model.getPersonInfo(), KmBookingDetailsModel.ALBookingDetails.class));
        metadata.put("sessionId", model.getSessionId());
        metadata.put("skipBot", "true");

        if (replyMetadata != null) {
            metadata.putAll(replyMetadata);
        }

        sendMessage("Your details have been submitted", metadata, Message.ContentType.DEFAULT.getValue());
    }

    public void loadImageOnFullScreen(Context context, String action, KmRichMessageModel.KmPayloadModel payloadModel) {
        Intent intent = new Intent(context, FullScreenImageActivity.class);
        intent.putExtra(action, GsonUtils.getJsonFromObject(payloadModel, KmRichMessageModel.KmPayloadModel.class));
        ((MobiComKitActivityInterface) context).startActivityForResult(intent, MobiComKitActivityInterface.REQUEST_CODE_FULL_SCREEN_ACTION);
    }

    public void sendMessage(String message, Map<String, String> messageMetaData, Short contentType) {
        if (richMessageListener != null) {
            Message messageToSend = new Message();
            messageToSend.setMessage(message);
            messageToSend.setMetadata(messageMetaData);
            messageToSend.setContentType(contentType);
            richMessageListener.onAction(null, KmRichMessage.SEND_MESSAGE, messageToSend, null, null);
        }
    }
}
