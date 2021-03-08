package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.form;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.v2.KmFormPayloadModel;

import java.util.List;

public class KmDropdownItemAdapter extends ArrayAdapter<KmFormPayloadModel.Options> {
    public KmDropdownItemAdapter(@NonNull Context context, int textViewResourceId, List<KmFormPayloadModel.Options> itemList) {
        super(context, textViewResourceId, itemList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView label = (TextView) super.getView(position, convertView, parent);
        label.setTextColor(Color.BLACK);
        label.setText(getItem(position).getLabel());
        return label;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView label = (TextView) super.getDropDownView(position, convertView, parent);
        label.setTextColor(Color.BLACK);
        label.setText(getItem(position).getLabel());
        return label;
    }
}
