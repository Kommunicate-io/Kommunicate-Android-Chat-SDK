package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.helpers;

import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.KmFormStateModel;

import java.util.HashMap;
import java.util.Map;

public class KmFormStateHelper {

    public static Map<String, KmFormStateModel> formStateModelMap;

    public static void addFormState(String messageKey, KmFormStateModel formStateModel) {
        if (formStateModelMap == null) {
            formStateModelMap = new HashMap<>();
        }

        formStateModelMap.put(messageKey, formStateModel);
    }

    public static KmFormStateModel getFormState(String messageKey) {
        if (formStateModelMap != null) {
            return formStateModelMap.get(messageKey);
        }
        return null;
    }

    public static void clearInstance() {
        formStateModelMap = null;
    }
}
