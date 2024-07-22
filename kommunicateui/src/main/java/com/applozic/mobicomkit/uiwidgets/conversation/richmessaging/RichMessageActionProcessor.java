package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.broadcast.AlEventManager;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private static final String DETAILS_SUBMITTED = "Your details have been submitted";
    private static final String GUEST_DETAIL = "guestDetail";
    private static final String PERSON_INFO = "personInfo";
    private static final String SESSION_ID = "sessionId";
    private static final String SKIPBOT = "skipBot";
    private static final String HOTEL_RESULT_IDX = "HotelResultIndex";
    private static final String NO_OF_ROOMS = "NoOfRooms";
    private static final String ROOM_IDX = "RoomIndex";
    private static final String BLOCK_HOTEL_ROOM = "blockHotelRoom";
    private static final String BOOK_HOTEL = "Book Hotel ";
    private static final String ROOM = ", Room ";
    private static final String GET_ROOM_DETAIL = "Get room detail of ";
    private static final String RESULT_IDX = "resutIndex";
    private static final String HOTEL_SELECTED = "hotelSelected";
    private static final String GUEST_TYPE_ID = "guestTypeId";
    private static final String IS_ROOM_GUEST_JSON = "isRoomGuestJSON";
    private static final String ROOM_GUEST_JSON = "roomGuestJson";
    private static final String APPLI_JSON = "application/json";
    private static final String REPLY_TEXT = "replyText";
    private static final String ADULTS = "ADULTS";
    private static final String GUEST = " Guest ";
    private static final String CHILDREN = " Children ";


    public RichMessageActionProcessor(KmRichMessageListener richMessageListener) {
        this.richMessageListener = richMessageListener;
    }

    public KmRichMessageListener getRichMessageListener() {
        return this;
    }

    @Override
    public void onAction(Context context, String action, Message message, Object object, Map<String, Object> replyMetadata) {
        AlEventManager.getInstance().sendOnRichMessageButtonClickEvent(message.getGroupId(), action, object);
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
                Map<String, String> localeMetadata = new HashMap<>();
                localeMetadata.put(null, null);
                String replyText = buttonModel.getAction().getPayload().getReplyText();
                if (TextUtils.isEmpty(replyText)) {
                    replyText = buttonModel.getName();
                }
                sendMessage(replyText, localeMetadata, Message.ContentType.DEFAULT.getValue());
                if (!buttonModel.getAction().getPayload().getFormAction().isEmpty()) {
                    new KmPostDataAsyncTask(context,
                            buttonModel.getAction().getPayload().getFormAction(),
                            null,
                            buttonModel.getAction().getPayload().getRequestType(),
                            buttonModel.getAction().getPayload().getFormData().toString(),
                            new KmCallback() {
                                @Override
                                public void onSuccess(Object messageString) {
                                    Utils.printLog(context, TAG, "Submit post success : " + messageString);
                                }

                                @Override
                                public void onFailure(Object error) {
                                    Utils.printLog(context, TAG, "Submit post error : " + error);
                                }
                            }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        } else if (object instanceof KmRichMessageModel) {
            KmRichMessageModel model = (KmRichMessageModel) object;
               try {
                   Map<String, String> localeMetadata = new HashMap<>();
                   localeMetadata.put(null,null);
                   String payloadString = model.getPayload();
                   JSONArray payloadArray = new JSONArray(payloadString);
                   JSONObject jsonObject = payloadArray.getJSONObject(0);
                   String replyText = jsonObject.getString(REPLY_TEXT);
                   sendMessage(replyText,localeMetadata, Message.ContentType.DEFAULT.getValue());
                   if (!model.getFormAction().isEmpty()) {
                       new KmPostDataAsyncTask(context,
                               model.getFormAction(),
                               null,
                               ((KmRichMessageModel) object).getRequestType(),
                               model.getFormData(),
                               new KmCallback() {
                                   @Override
                                   public void onSuccess(Object messageString) {
                                       Utils.printLog(context, TAG, "Submit post success : " + messageString);
                                   }

                                   @Override
                                   public void onFailure(Object error) {
                                       Utils.printLog(context, TAG, "Submit post error : " + error);
                                   }
                               }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                   }

               } catch (JSONException e) {
                   e.printStackTrace();
               }

        } else if (object instanceof KmRichMessageModel.KmPayloadModel) {
            makeFormRequest(context, (KmRichMessageModel.KmPayloadModel) object);
        }
    }

    private boolean isInvalidData(Map<String, Object> dataMap, KmRMActionModel.SubmitButton submitButton) {
        return (dataMap == null) && (submitButton.getFormData() == null || submitButton.getFormData().isEmpty());
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
        boolean postBackToBotPlatform = KmRMActionModel.SubmitButton.KM_POST_DATA_TO_BOT_PLATFORM.equals(submitButtonModel.getRequestType());
        Map<String, String> metadata = handleMetaData(postBackToBotPlatform, getStringMap(submitButtonModel.getReplyMetadata()), dataMap, getStringMap(submitButtonModel.getMetadata()));
        Utils.printLog(context, TAG, "Submitting data : " + GsonUtils.getJsonFromObject(formStateModel != null ? dataMap : submitButtonModel.getFormData(), Map.class));

        if (submitButtonModel.getPostBackToKommunicate() != null && submitButtonModel.getPostBackToKommunicate().equalsIgnoreCase("true")) {
            sendFormDataAsMessage(message, metadata, dataMap, "", "");

            if (richMessageListener != null) {
                richMessageListener.onAction(context, NOTIFY_ITEM_CHANGE, message, dataMap, submitButtonModel.getReplyMetadata());
            }
        }
        if (submitButtonModel.getPostFormDataAsMessage() != null && submitButtonModel.getPostFormDataAsMessage().equalsIgnoreCase("true")) {
            sendFormDataAsMessage(message, metadata, dataMap, submitButtonModel.getMessage(), submitButtonModel.getAddFormLabelInMessage());

            if (richMessageListener != null) {
                richMessageListener.onAction(context, NOTIFY_ITEM_CHANGE, message, dataMap, submitButtonModel.getReplyMetadata());
            }
        } else if (!TextUtils.isEmpty(submitButtonModel.getMessage())) {
            sendMessage(submitButtonModel.getMessage(), metadata);
        }
        if (!TextUtils.isEmpty(submitButtonModel.getFormAction())) {
            new KmPostDataAsyncTask(context,
                    submitButtonModel.getFormAction(),
                    null,
                    KmWebViewActivity.REQUEST_TYPE_JSON.equals(submitButtonModel.getRequestType()) ? APPLI_JSON : KmWebViewActivity.DEFAULT_REQUEST_TYPE,
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

    //TO SEND FORM DATA AS MESSAGE for both postBackToKommunicate and postFormDataAsMessage
    private void sendFormDataAsMessage(Message message, Map<String, String> replyMetadata, Map<String, Object> formSelectedData, String submitButtonMessage, String addFormLabelInMessage) {
        if (message.getMetadata() != null) {
            com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.v2.KmRichMessageModel<List<KmFormPayloadModel>> richMessageModel = new Gson().fromJson(GsonUtils.getJsonFromObject(message.getMetadata(), Map.class), new TypeToken<com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.v2.KmRichMessageModel>() {
            }.getType());

            List<KmFormPayloadModel> formPayloadModelList = richMessageModel.getFormModelList();
            if (TextUtils.isEmpty(submitButtonMessage)) {
                for (KmFormPayloadModel model: formPayloadModelList) {
                    if (model.isTypeAction()) {
                        submitButtonMessage = model.getAction().getName();
                        break;
                    }
                }
            }
            StringBuilder messageToSend = new StringBuilder(submitButtonMessage);
            if (!TextUtils.isEmpty(messageToSend)) {
                messageToSend.append("\n");
            }

            for (KmFormPayloadModel model : formPayloadModelList) {
                //Submit Button
                if (model.isTypeAction()) {
                    continue;
                }
                //TextField
                if (model.isTypeText()) {
                    KmFormPayloadModel.Text textModel = model.getTextModel();
                    if (formSelectedData.containsKey(textModel.getLabel())) {
                        if (addFormLabelInMessage.equalsIgnoreCase("false")) {
                            messageToSend.append(formSelectedData.get(textModel.getLabel()).toString()).append("\n");
                        } else {
                            messageToSend.append(textModel.getLabel()).append(" : ").append(formSelectedData.get(textModel.getLabel()).toString()).append("\n");
                        }
                    } else {
                        messageToSend.append(textModel.getLabel()).append(" : ").append("\n");

                    }
                }
                //TextArea
                if (model.isTypeTextArea()) {
                    KmFormPayloadModel.TextArea textAreaModel = model.getTextAreaModel();
                    if (formSelectedData.containsKey(textAreaModel.getTitle())) {
                        messageToSend.append(textAreaModel.getTitle()).append(" : ").append(formSelectedData.get(textAreaModel.getTitle()).toString()).append("\n");
                    } else {
                        messageToSend.append(textAreaModel.getTitle()).append(" : ").append("\n");
                    }
                }
                //Radio Button or Check Boxes
                else if (model.isTypeSelection()) {
                    KmFormPayloadModel.Selections selectionModel = model.getSelectionModel();
                    if (formSelectedData.containsKey(selectionModel.getName())) {

                        if (formSelectedData.get(selectionModel.getName()) instanceof Object[] && ((Object[]) formSelectedData.get(selectionModel.getName())).length > 0) {
                            String[] valueList = (String[]) formSelectedData.get(selectionModel.getName());
                            String valueString = "";
                            if (valueList != null && valueList.length > 0) {
                                for (int i = 0; i < valueList.length; i++) {

                                    valueString += valueList[i];
                                    if (i < valueList.length - 1) {
                                        valueString += ", ";
                                    }
                                }
                            }
                            messageToSend.append(selectionModel.getName()).append(" : ").append(valueString).append("\n");
                        } else {
                            String selectedData = "";
                            if (!TextUtils.isEmpty(selectionModel.getName())) {
                                Object formDataObject = formSelectedData.get(selectionModel.getName());
                                if ( (formDataObject instanceof String[] && ((String[])formSelectedData.get(selectionModel.getName())).length != 0) || (formDataObject instanceof String && ((String)formSelectedData.get(selectionModel.getName())).length() != 0) ) {
                                    selectedData = formSelectedData.get(selectionModel.getName()).toString();
                                }
                            }
                            messageToSend.append(selectionModel.getName()).append(" : ").append(selectedData).append("\n");
                        }
                    } else {
                        messageToSend.append(selectionModel.getName()).append(" : ").append("\n");

                    }
                }
                //Date or Time Picker
                else if (model.isTypeDateTime()) {
                    KmFormPayloadModel.DateTimePicker datePickerModel = model.getDatePickerModel();
                    if (formSelectedData.containsKey(datePickerModel.getLabel())) {
                        messageToSend.append(datePickerModel.getLabel()).append(" : ").append(formSelectedData.get(datePickerModel.getLabel()).toString()).append("\n");
                    } else {
                        messageToSend.append(datePickerModel.getLabel()).append(" : ").append("\n");

                    }
                }
                //Drop Down
                else if (model.isTypeDropdown()) {
                    KmFormPayloadModel.DropdownList dropdownList = model.getDropdownList();
                    if (formSelectedData.containsKey(dropdownList.getName())) {
                        messageToSend.append(dropdownList.getName()).append(" : ").append(formSelectedData.get(dropdownList.getName()).toString()).append("\n");
                    } else {
                        messageToSend.append(dropdownList.getName()).append(" : ").append("\n");

                    }
                }
                // Hidden
                else if (model.isTypeHidden()) {
                    KmFormPayloadModel.Hidden hiddenModel = model.getHiddenModel();
                    messageToSend.append(hiddenModel.getName()).append(" : ").append(hiddenModel.getValue()).append("\n");

                }
            }

            sendMessage(messageToSend.toString(), replyMetadata);
        }
    }

    private Map<String, String> handleMetaData(boolean postBackToBotPlatform, Map<String, String> replyMetadata, Map<String, Object> formSelectedData, Map<String, String> messageMetadata) {
        Map<String, String> metadata = new HashMap<>();
        if (replyMetadata != null) {
            metadata.putAll(replyMetadata);
        }
        if (messageMetadata != null) {
            metadata.putAll(messageMetadata);
        }
        if (formSelectedData != null && postBackToBotPlatform) {
            Map<String, Object> formDataMap = new HashMap<>();
            formDataMap.put(KmFormPayloadModel.KM_FORM_DATA, new JSONObject(formSelectedData));
            metadata.put(Kommunicate.KM_CHAT_CONTEXT, String.valueOf(new JSONObject(formDataMap)));
        }
        return metadata;
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
                    KmWebViewActivity.REQUEST_TYPE_JSON.equals(payloadModel.getRequestType()) ? APPLI_JSON : KmWebViewActivity.DEFAULT_REQUEST_TYPE,
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
        metadata.put(GUEST_TYPE_ID, ADULTS);
        metadata.put(IS_ROOM_GUEST_JSON, "true");
        metadata.put(ROOM_GUEST_JSON, GsonUtils.getJsonFromObject(guestList, List.class));

        StringBuilder message = new StringBuilder("");
        int count = 0;

        for (KmGuestCountModel guestModel : guestList) {
            message.append("Room ");
            message.append(count + 1);
            message.append(GUEST);
            message.append(guestModel.getNoOfAdults());
            message.append(CHILDREN);
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
        metadata.put(HOTEL_SELECTED, "true");
        metadata.put(RESULT_IDX, String.valueOf(hotel.getResultIndex()));
        metadata.put(SESSION_ID, hotel.getSessionId());
        metadata.put(SKIPBOT, "true");

        String message = GET_ROOM_DETAIL + hotel.getHotelName();

        if (replyMetadata != null) {
            metadata.putAll(replyMetadata);
        }

        sendMessage(message, metadata, Message.ContentType.DEFAULT.getValue());
    }

    public void sendRoomDetailsMessage(KmHotelBookingModel hotel, Map<String, String> replyMetadata) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put(HOTEL_RESULT_IDX, String.valueOf(hotel.getHotelResultIndex()));
        metadata.put(NO_OF_ROOMS, String.valueOf(hotel.getNoOfRooms()));
        metadata.put(ROOM_IDX, String.valueOf(hotel.getRoomIndex()));
        metadata.put(BLOCK_HOTEL_ROOM, "true");
        metadata.put(SESSION_ID, hotel.getSessionId());
        metadata.put(SKIPBOT, "true");

        if (replyMetadata != null) {
            metadata.putAll(replyMetadata);
        }

        String message = BOOK_HOTEL + hotel.getHotelName() + ROOM + hotel.getRoomTypeName();

        sendMessage(message, metadata, Message.ContentType.DEFAULT.getValue());
    }

    public void sendBookingDetailsMessage(KmBookingDetailsModel model, Map<String, String> replyMetadata) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put(GUEST_DETAIL, "true");
        metadata.put(PERSON_INFO, GsonUtils.getJsonFromObject(model.getPersonInfo(), KmBookingDetailsModel.ALBookingDetails.class));
        metadata.put(SESSION_ID, model.getSessionId());
        metadata.put(SKIPBOT, "true");

        if (replyMetadata != null) {
            metadata.putAll(replyMetadata);
        }

        sendMessage(DETAILS_SUBMITTED, metadata, Message.ContentType.DEFAULT.getValue());
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