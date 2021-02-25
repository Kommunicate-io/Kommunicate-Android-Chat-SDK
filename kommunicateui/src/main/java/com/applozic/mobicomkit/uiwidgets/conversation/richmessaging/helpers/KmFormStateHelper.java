package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.helpers;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.KmFormStateModel;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.v2.KmFormPayloadModel;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.v2.KmRichMessageModel;
import com.applozic.mobicommons.json.GsonUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.kommunicate.utils.KmDateUtils;

public class KmFormStateHelper {

    private static Map<String, KmFormStateModel> formStateModelMap;

    public static void initFormState() {
        if (formStateModelMap == null) {
            formStateModelMap = new HashMap<>();
        }
    }

    public static void addFormState(String messageKey, KmFormStateModel formStateModel) {
        initFormState();

        formStateModelMap.put(messageKey, formStateModel);
    }

    public static KmFormStateModel getFormState(String messageKey) {
        if (formStateModelMap != null) {
            return formStateModelMap.get(messageKey);
        }
        return null;
    }

    public static void removeFormState(String messageKey) {
        if (formStateModelMap != null) {
            formStateModelMap.remove(messageKey);
        }
    }

    public static void clearInstance() {
        formStateModelMap = null;
    }

    public static Map<String, Object> getKmFormMap(Message message, KmFormStateModel formStateModel) {
        Map<String, Object> formDataMap = new HashMap<>();

        if (message.getMetadata() != null) {
            KmRichMessageModel<List<KmFormPayloadModel>> richMessageModel = new Gson().fromJson(GsonUtils.getJsonFromObject(message.getMetadata(), Map.class), new TypeToken<KmRichMessageModel>() {
            }.getType());

            if (formStateModel == null) {
                return formDataMap;
            }

            List<KmFormPayloadModel> formPayloadModelList = richMessageModel.getFormModelList();

            if (formStateModel.getTextFields() != null) {
                int size = formStateModel.getTextFields().size();
                for (int i = 0; i < size; i++) {
                    int key = formStateModel.getTextFields().keyAt(i);
                    KmFormPayloadModel.Text textModel = formPayloadModelList.get(key).getTextModel();
                    formDataMap.put(textModel.getLabel(), formStateModel.getTextFields().get(key));
                }
            }

            if (formStateModel.getCheckBoxStates() != null) {
                int size = formStateModel.getCheckBoxStates().size();
                for (int i = 0; i < size; i++) {
                    int key = formStateModel.getCheckBoxStates().keyAt(i);
                    List<KmFormPayloadModel.Options> allOptions = formPayloadModelList.get(key).getSelectionModel().getOptions();

                    HashSet<Integer> checkedOptions = formStateModel.getCheckBoxStates().get(key);

                    if (checkedOptions != null && allOptions != null) {
                        String[] checkBoxesArray = new String[checkedOptions.size()];

                        Iterator<Integer> iterator = checkedOptions.iterator();
                        int index = 0;
                        while (iterator.hasNext()) {
                            checkBoxesArray[index] = allOptions.get(iterator.next()).getValue();
                            index++;
                        }
                        formDataMap.put(formPayloadModelList.get(key).getSelectionModel().getName(), checkBoxesArray);
                    }
                }
            }

            if (formStateModel.getSelectedRadioButtonIndex() != null) {
                int size = formStateModel.getSelectedRadioButtonIndex().size();

                for (int i = 0; i < size; i++) {
                    int key = formStateModel.getSelectedRadioButtonIndex().keyAt(i);
                    KmFormPayloadModel.Selections radioOptions = formPayloadModelList.get(key).getSelectionModel();
                    KmFormPayloadModel.Options selectedOption = radioOptions.getOptions().get(formStateModel.getSelectedRadioButtonIndex().get(key));
                    formDataMap.put(radioOptions.getName(), selectedOption.getValue());
                }
            }

            if (formStateModel.getHiddenFields() != null) {
                formDataMap.putAll(formStateModel.getHiddenFields());
            }

            if (formStateModel.getDateFieldArray() != null) {
                for (int i = 0; i < formStateModel.getDateFieldArray().size(); i++) {
                    int key = formStateModel.getDateFieldArray().keyAt(i);
                    KmFormPayloadModel.DateTimePicker dateTimePicker = formPayloadModelList.get(key).getDatePickerModel();

                    if (KmFormPayloadModel.Type.DATE.getValue().equals(formPayloadModelList.get(key).getType())) {
                        formDataMap.put(dateTimePicker.getLabel(), KmDateUtils.getFormSerialisedDateFormat(formStateModel.getDateFieldArray().get(key)));
                    } else if (KmFormPayloadModel.Type.TIME.getValue().equals(formPayloadModelList.get(key).getType())) {
                        formDataMap.put(dateTimePicker.getLabel(), KmDateUtils.getFormSerialisedTimeFormat(formStateModel.getDateFieldArray().get(key)));
                    } else {
                        formDataMap.put(dateTimePicker.getLabel(), KmDateUtils.getFormSerialisedDateTimeFormat(formStateModel.getDateFieldArray().get(key)));
                    }
                }
            }

            if (formStateModel.getDropdownFieldArray() != null) {
                for (int i = 0; i < formStateModel.getDropdownFieldArray().size(); i++) {
                    formDataMap.put(formPayloadModelList.get(formStateModel.getDropdownFieldArray().keyAt(i)).getDropdownList().getName(), formStateModel.getDropdownFieldArray().valueAt(i).getValue());
                }
            }
        }

        return formDataMap;
    }
}
