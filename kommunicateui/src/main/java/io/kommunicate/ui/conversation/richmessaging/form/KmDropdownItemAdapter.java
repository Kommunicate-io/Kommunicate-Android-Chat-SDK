package io.kommunicate.ui.conversation.richmessaging.form;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.kommunicate.ui.AlCustomizationSettings;
import io.kommunicate.ui.conversation.richmessaging.models.v2.KmFormPayloadModel;
import io.kommunicate.ui.kommunicate.utils.KmThemeHelper;

import java.util.List;

public class KmDropdownItemAdapter extends ArrayAdapter<KmFormPayloadModel.Options> {

    private Context context;

    public KmDropdownItemAdapter(@NonNull Context context, int textViewResourceId, List<KmFormPayloadModel.Options> itemList) {
        super(context, textViewResourceId, itemList);
        this.context = context;
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
        KmThemeHelper kmThemeHelper = KmThemeHelper.getInstance(context, new AlCustomizationSettings());
        if (kmThemeHelper.isDarkModeEnabledForSDK()){
            label.setTextColor(Color.WHITE);
            label.setBackgroundColor(Color.BLACK);
        } else {
            label.setTextColor(Color.BLACK);
        }
        label.setText(getItem(position).getLabel());
        return label;
    }
}
