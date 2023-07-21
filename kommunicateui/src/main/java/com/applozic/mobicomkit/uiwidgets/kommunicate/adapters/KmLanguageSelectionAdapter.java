package com.applozic.mobicomkit.uiwidgets.kommunicate.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.kommunicate.KmPrefSettings;
import com.applozic.mobicomkit.uiwidgets.kommunicate.callbacks.KmClickHandler;
import com.applozic.mobicomkit.uiwidgets.kommunicate.models.KmSpeechToTextModel;
import com.applozic.mobicomkit.uiwidgets.kommunicate.settings.KmSpeechToTextSetting;
import com.applozic.mobicommons.commons.core.utils.Utils;


import java.util.List;

public class KmLanguageSelectionAdapter extends RecyclerView.Adapter {
    private Context context;
    private List<KmSpeechToTextModel> languages;
    private KmClickHandler<KmSpeechToTextModel> kmClickHandler;
    private String selectedLanguageCode;

    public KmLanguageSelectionAdapter(Context context, List<KmSpeechToTextModel> languages, KmClickHandler<KmSpeechToTextModel> kmClickHandler) {
        this.kmClickHandler = kmClickHandler;
        this.context = context;
        this.selectedLanguageCode = KmPrefSettings.getInstance(context).getSpeechToTextLanguage();
        this.languages = languages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.km_languages_item_layout, parent, false);
        return new LanguageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
       final KmLanguageSelectionAdapter.LanguageViewHolder mViewHolder = (KmLanguageSelectionAdapter.LanguageViewHolder) viewHolder;
        mViewHolder.kmLanguageName.setVisibility(View.VISIBLE);
        mViewHolder.kmLanguageCode.setVisibility(KmSpeechToTextSetting.getInstance(context).isShowLanguageCode() ? View.VISIBLE : View.GONE);

        mViewHolder.kmLanguageName.setText(languages.get(position).getName());
//        mViewHolder.kmLanguageName.setText("name");
//        mViewHolder.kmLanguageCode.setText("code");
        mViewHolder.kmLanguageCode.setText(languages.get(position).getCode());
        if (languages.get(position).getCode().equals(selectedLanguageCode)) {
            mViewHolder.parentView.setBackgroundColor(Utils.getColor(context, R.color.km_language_selected_background_color));
            mViewHolder.kmLanguageName.setTextColor(Utils.getColor(context, R.color.km_language_selected_name_text_color));
            mViewHolder.kmLanguageCode.setTextColor(Utils.getColor(context, R.color.km_language_selected_code_text_color));
        }
        mViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view != null && kmClickHandler != null) {
                    kmClickHandler.onItemClicked(mViewHolder.itemView, languages.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return languages.size();
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
        }
    }
}

