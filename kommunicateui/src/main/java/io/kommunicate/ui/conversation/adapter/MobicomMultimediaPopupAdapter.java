package io.kommunicate.ui.conversation.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import io.kommunicate.ui.CustomizationSettings;
import io.kommunicate.ui.R;
import io.kommunicate.ui.kommunicate.utils.KmThemeHelper;

import java.util.List;

/**
 * Created by reytum on 18/3/16.
 */
@Deprecated
public class MobicomMultimediaPopupAdapter extends BaseAdapter {
    Context context;
    List<String> multimediaIcons;
    List<String> multimediaText;
    CustomizationSettings customizationSettings;
    private static final String FONTS = "";

    public MobicomMultimediaPopupAdapter(Context context, List<String> multimediaIcons, List<String> multimediaText) {
        this.context = context;
        this.multimediaIcons = multimediaIcons;
        this.multimediaText = multimediaText;
    }

    public void setAlCustomizationSettings(CustomizationSettings customizationSettings) {
        this.customizationSettings = customizationSettings;
    }

    @Override
    public int getCount() {
        return multimediaText.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.individual_multimedia_option_item, null);

        TextView icon = (TextView) convertView.findViewById(R.id.multimedia_icon);
        Typeface iconTypeface = Typeface.createFromAsset(context.getAssets(), FONTS);
        icon.setTypeface(iconTypeface);
        KmThemeHelper themeHelper = KmThemeHelper.getInstance(context,new CustomizationSettings());
        TextView text = (TextView) convertView.findViewById(R.id.multimedia_text);
        icon.setTextColor(Color.parseColor(themeHelper.isDarkModeEnabledForSDK() ? customizationSettings.getAttachmentIconsBackgroundColor().get(1) : customizationSettings.getAttachmentIconsBackgroundColor().get(0)));
        icon.setText(multimediaIcons.get(position));
        text.setText(multimediaText.get(position));
        return convertView;
    }

}
