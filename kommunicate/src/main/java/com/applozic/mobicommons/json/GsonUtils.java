package com.applozic.mobicommons.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import java.lang.reflect.Type;

/**
 * Created by devashish on 22/12/14.
 */
public class GsonUtils {

    private static final String TAG = "GsonUtils";

    public static String getJsonWithExposeFromObject(Object object, Type type) {
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        return gson.toJson(object, type);
    }

    public static String getJsonFromObject(Object object, Type type) {
        return new GsonBuilder().setPrettyPrinting().create().toJson(object, type);
    }

    public static Object getObjectFromJson(String json, Type type) {
        return new GsonBuilder().create().fromJson(json, type);
    }

    public static Object getObjectListFromJson(String json, String key, Type type) {
        Gson gson = new GsonBuilder().setExclusionStrategies(new AnnotationExclusionStrategy()).create();
        JsonParser parser = new JsonParser();
        String element = parser.parse(json).getAsJsonObject().get(key).toString();
        return gson.fromJson(element, type);
    }

}
