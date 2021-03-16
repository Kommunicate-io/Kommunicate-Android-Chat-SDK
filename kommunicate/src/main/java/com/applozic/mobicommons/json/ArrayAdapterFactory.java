package com.applozic.mobicommons.json;

/**
 * Created by devashish on 13/3/15.
 */

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.ParameterizedType;
import java.util.List;

public class ArrayAdapterFactory implements TypeAdapterFactory {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> type) {

        TypeAdapter<T> typeAdapter = null;

        try {
            if (type.getRawType() == List.class)
                typeAdapter = new ArrayAdapter(
                        (Class) ((ParameterizedType) type.getType())
                                .getActualTypeArguments()[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return typeAdapter;


    }

}
