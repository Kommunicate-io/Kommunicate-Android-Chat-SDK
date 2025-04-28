package io.kommunicate.ui.conversation.richmessaging.models;

import android.util.SparseArray;
import android.util.SparseIntArray;

import io.kommunicate.ui.conversation.richmessaging.models.v2.KmFormPayloadModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import io.kommunicate.commons.json.GsonUtils;
import io.kommunicate.commons.json.JsonMarker;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;

public class KmFormStateModel extends JsonMarker {

    private SparseArray<String> textFields;
    private SparseArray<String> textAreaFields;
    private SparseIntArray selectedRadioButtonIndex;
    private SparseArray<HashSet<Integer>> checkBoxStates;
    private SparseArray<HashSet<Integer>> unCheckBoxStates;
    private Map<String, String> hiddenFields;
    private SparseIntArray validationArray;
    private SparseArray<Long> dateFieldArray;
    private SparseArray<String> dropdownFieldArray;

    public SparseArray<String> getTextFields() {
        return textFields;
    }

    public SparseArray<String> getTextAreaFields() {
        return textAreaFields;
    }

    public void setTextAreaFields(SparseArray<String> textAreaFields) {
        this.textAreaFields = textAreaFields;
    }

    public void setTextFields(SparseArray<String> textFields) {
        this.textFields = textFields;
    }

    public SparseIntArray getSelectedRadioButtonIndex() {
        return selectedRadioButtonIndex;
    }

    public void setSelectedRadioButtonIndex(SparseIntArray selectedRadioButtonIndex) {
        this.selectedRadioButtonIndex = selectedRadioButtonIndex;
    }

    public SparseArray<HashSet<Integer>> getCheckBoxStates() {
        return checkBoxStates;
    }

    public void setCheckBoxStates(SparseArray<HashSet<Integer>> checkBoxStates) {
        this.checkBoxStates = checkBoxStates;
    }

    public SparseArray<HashSet<Integer>> getUncheckBoxStates() {
        return unCheckBoxStates;
    }

    public void setUncheckBoxStates(SparseArray<HashSet<Integer>> unCheckBoxStates) {
        this.unCheckBoxStates = unCheckBoxStates;
    }

    public Map<String, String> getHiddenFields() {
        return hiddenFields;
    }

    public void setHiddenFields(Map<String, String> hiddenFields) {
        this.hiddenFields = hiddenFields;
    }

    public SparseIntArray getValidationArray() {
        return validationArray;
    }

    public void setValidationArray(SparseIntArray validationArray) {
        this.validationArray = validationArray;
    }

    public SparseArray<Long> getDateFieldArray() {
        return dateFieldArray;
    }

    public void setDateFieldArray(SparseArray<Long> dateFieldArray) {
        this.dateFieldArray = dateFieldArray;
    }

    public SparseArray<KmFormPayloadModel.Options> getDropdownFieldArray() {
        if (dropdownFieldArray == null) {
            return null;
        }
        SparseArray<KmFormPayloadModel.Options> result = new SparseArray<>();
        for (int i=0; i<dropdownFieldArray.size(); i++) {
            result.put(dropdownFieldArray.keyAt(i), GsonUtils.getObjectFromJson(dropdownFieldArray.get(i), KmFormPayloadModel.Options.class));
        }
        return result;
    }

    public void setDropdownFieldArray(SparseArray<KmFormPayloadModel.Options> dropdownFieldArray) {
        SparseArray<String> result = new SparseArray<>();
        for (int i=0; i<dropdownFieldArray.size(); i++) {
            result.put(dropdownFieldArray.keyAt(i), GsonUtils.getJsonFromObject(dropdownFieldArray.get(i), KmFormPayloadModel.Options.class));
        }
        this.dropdownFieldArray = result;
    }

    public static class SparseArrayStringAdapter implements JsonSerializer<SparseArray<String>>, JsonDeserializer<SparseArray<String>> {
        @Override
        public JsonElement serialize(SparseArray<String> src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            for (int i = 0; i < src.size(); i++) {
                int key = src.keyAt(i);
                String value = src.valueAt(i);
                jsonObject.addProperty(String.valueOf(key), value);
            }
            return jsonObject;
        }

        @Override
        public SparseArray<String> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            SparseArray<String> result = new SparseArray<>();
            JsonObject jsonObject = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                int key = Integer.parseInt(entry.getKey());
                String value = entry.getValue().getAsString();
                result.put(key, value);
            }
            return result;
        }
    }

    public static class SparseIntArrayAdapter implements JsonSerializer<SparseIntArray>, JsonDeserializer<SparseIntArray> {
        @Override
        public JsonElement serialize(SparseIntArray src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            for (int i = 0; i < src.size(); i++) {
                int key = src.keyAt(i);
                int value = src.valueAt(i);
                jsonObject.addProperty(String.valueOf(key), value);
            }
            return jsonObject;
        }

        @Override
        public SparseIntArray deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            SparseIntArray result = new SparseIntArray();
            JsonObject jsonObject = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                int key = Integer.parseInt(entry.getKey());
                int value = entry.getValue().getAsInt();
                result.put(key, value);
            }
            return result;
        }
    }

    public static class SparseArrayHashSetAdapter implements JsonSerializer<SparseArray<HashSet<Integer>>>, JsonDeserializer<SparseArray<HashSet<Integer>>> {
        @Override
        public JsonElement serialize(SparseArray<HashSet<Integer>> src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            for (int i = 0; i < src.size(); i++) {
                int key = src.keyAt(i);
                HashSet<Integer> value = src.valueAt(i);
                JsonArray jsonArray = new JsonArray();
                for (Integer item : value) {
                    jsonArray.add(item);
                }
                jsonObject.add(String.valueOf(key), jsonArray);
            }
            return jsonObject;
        }

        @Override
        public SparseArray<HashSet<Integer>> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            SparseArray<HashSet<Integer>> result = new SparseArray<>();
            JsonObject jsonObject = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                int key = Integer.parseInt(entry.getKey());
                JsonArray jsonArray = entry.getValue().getAsJsonArray();
                HashSet<Integer> hashSet = new HashSet<>();
                for (JsonElement element : jsonArray) {
                    hashSet.add(element.getAsInt());
                }
                result.put(key, hashSet);
            }
            return result;
        }
    }

    public static class SparseArrayLongAdapter implements JsonSerializer<SparseArray<Long>>, JsonDeserializer<SparseArray<Long>> {
        @Override
        public JsonElement serialize(SparseArray<Long> src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            for (int i = 0; i < src.size(); i++) {
                int key = src.keyAt(i);
                Long value = src.valueAt(i);
                jsonObject.addProperty(String.valueOf(key), value);
            }
            return jsonObject;
        }

        @Override
        public SparseArray<Long> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            SparseArray<Long> result = new SparseArray<>();
            JsonObject jsonObject = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                int key = Integer.parseInt(entry.getKey());
                Long value = entry.getValue().getAsLong();
                result.put(key, value);
            }
            return result;
        }
    }

    public static class SparseArrayOptionsAdapter implements JsonSerializer<SparseArray<KmFormPayloadModel.Options>>, JsonDeserializer<SparseArray<KmFormPayloadModel.Options>> {
        @Override
        public JsonElement serialize(SparseArray<KmFormPayloadModel.Options> src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            for (int i = 0; i < src.size(); i++) {
                int key = src.keyAt(i);
                KmFormPayloadModel.Options value = src.valueAt(i);
                jsonObject.add(String.valueOf(key), context.serialize(value));
            }
            return jsonObject;
        }

        @Override
        public SparseArray<KmFormPayloadModel.Options> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            SparseArray<KmFormPayloadModel.Options> result = new SparseArray<>();
            JsonObject jsonObject = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                int key = Integer.parseInt(entry.getKey());
                KmFormPayloadModel.Options value = context.deserialize(entry.getValue(), KmFormPayloadModel.Options.class);
                result.put(key, value);
            }
            return result;
        }
    }

    public static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(new TypeToken<SparseArray<String>>() {}.getType(), new SparseArrayStringAdapter())
                .registerTypeAdapter(new TypeToken<SparseIntArray>() {}.getType(), new SparseIntArrayAdapter())
                .registerTypeAdapter(new TypeToken<SparseArray<HashSet<Integer>>>() {}.getType(), new SparseArrayHashSetAdapter())
                .registerTypeAdapter(new TypeToken<SparseArray<Long>>() {}.getType(), new SparseArrayLongAdapter())
                .registerTypeAdapter(new TypeToken<SparseArray<KmFormPayloadModel.Options>>() {}.getType(), new SparseArrayOptionsAdapter())
                .create();
    }

    public String toJson() {
        return getGson().toJson(this);
    }

    public static KmFormStateModel fromJson(String json) {
        return getGson().fromJson(json, KmFormStateModel.class);
    }
}