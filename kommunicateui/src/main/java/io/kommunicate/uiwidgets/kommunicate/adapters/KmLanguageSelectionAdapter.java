package io.kommunicate.uiwidgets.kommunicate.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.kommunicate.uiwidgets.R;
import io.kommunicate.uiwidgets.kommunicate.KmPrefSettings;
import io.kommunicate.uiwidgets.kommunicate.callbacks.KmClickHandler;
import io.kommunicate.uiwidgets.kommunicate.settings.KmSpeechToTextSetting;
import io.kommunicate.utils.Utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class KmLanguageSelectionAdapter extends RecyclerView.Adapter {
    private Context context;
    private LinkedHashMap<String, String> languageMap;
    private KmClickHandler<String> kmClickHandler;
    private String selectedLanguageCode;

    public KmLanguageSelectionAdapter(Context context, Map<String, String> languages, KmClickHandler<String> kmClickHandler) {
        this.kmClickHandler = kmClickHandler;
        this.context = context;
        this.selectedLanguageCode = KmPrefSettings.getInstance(context).getSpeechToTextLanguage();
        this.languageMap = new LinkedHashMap<>(languages);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.km_languages_item_layout, parent, false);
        return new LanguageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
       KmLanguageSelectionAdapter.LanguageViewHolder mViewHolder = (KmLanguageSelectionAdapter.LanguageViewHolder) viewHolder;
        mViewHolder.kmLanguageName.setVisibility(View.VISIBLE);
        mViewHolder.kmLanguageCode.setVisibility(KmSpeechToTextSetting.getInstance(context).isShowLanguageCode() ? View.VISIBLE : View.GONE);

        mViewHolder.kmLanguageName.setText(getMapValue(position));
        mViewHolder.kmLanguageCode.setText(getMapKey(position));
        if (getMapKey(position).equals(selectedLanguageCode)) {
            mViewHolder.parentView.setBackgroundColor(Utils.getColor(context, R.color.km_language_selected_background_color));
            mViewHolder.kmLanguageName.setTextColor(Utils.getColor(context, R.color.km_language_selected_name_text_color));
            mViewHolder.kmLanguageCode.setTextColor(Utils.getColor(context, R.color.km_language_selected_code_text_color));
        }
    }

    private String getMapValue(int position) {
        String[] keys = languageMap.keySet().toArray(new String[0]);
        return languageMap.get(keys[position]);
    }

    private String getMapKey(int position) {
        String[] keys = languageMap.keySet().toArray(new String[0]);
        return keys[position];
    }

    @Override
    public int getItemCount() {
        return languageMap.size();
    }

    private class LanguageViewHolder extends RecyclerView.ViewHolder {
        private final TextView kmLanguageName;
        private final TextView kmLanguageCode;
        private final LinearLayout parentView;
        public LanguageViewHolder(final View itemView) {
            super(itemView);
            kmLanguageName = itemView.findViewById(R.id.km_language_name);
            kmLanguageCode = itemView.findViewById(R.id.km_language_code);
            parentView = itemView.findViewById(R.id.km_language_parent_view);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (kmClickHandler != null) {
                        kmClickHandler.onItemClicked(itemView, kmLanguageCode.getText().toString());
                    }
                }
            });
        }
    }
}

