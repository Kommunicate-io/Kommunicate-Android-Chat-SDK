package com.applozic.mobicomkit.uiwidgets.kommunicate.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.v2.KmFormPayloadModel;


import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.kommunicate.models.KmPrechatInputModel;

/**
 * Adapter class to handle prechat dropdown option
 */

public class KmPrechatDropdownAdapter extends ArrayAdapter<String> {
    private KmPrechatInputModel prechatInputModel;

    public  KmPrechatDropdownAdapter(@NonNull Context context, int textViewResourceId, KmPrechatInputModel inputModel, List<String> data) {
        super(context, textViewResourceId, data);
        this.prechatInputModel = inputModel;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        TextView dropdownText = (TextView) view;
        if(position == 0 && prechatInputModel.getPlaceholder() != null){
            dropdownText.setTextColor(Color.GRAY);
        }
        else {
            dropdownText.setTextColor(Color.BLACK);
        }
        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        TextView dropdownText = (TextView) view;
        if(position == 0 && prechatInputModel.getPlaceholder() != null){
            dropdownText.setTextColor(Color.GRAY);
        }
        else {
            dropdownText.setTextColor(Color.BLACK);
        }
        return view;
    }

    @Override
    public boolean isEnabled(int position) {
        return position != 0;
    }
}
