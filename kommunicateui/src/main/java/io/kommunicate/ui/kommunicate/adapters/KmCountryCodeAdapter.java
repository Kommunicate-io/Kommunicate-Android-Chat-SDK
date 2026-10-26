package io.kommunicate.ui.kommunicate.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.kommunicate.commons.commons.core.utils.Utils;
import io.kommunicate.ui.R;
import io.kommunicate.ui.kommunicate.callbacks.KmClickHandler;
import io.kommunicate.ui.kommunicate.models.KmCountryCode;

public class KmCountryCodeAdapter extends RecyclerView.Adapter<KmCountryCodeAdapter.CountryViewHolder> {
    private final Context context;
    private final List<KmCountryCode> countries;
    private final List<KmCountryCode> filteredCountries;
    private final String selectedIsoCode;
    private final KmClickHandler<KmCountryCode> kmClickHandler;
    private final int selectedBackgroundColor;
    private final int defaultBackgroundColor;

    public KmCountryCodeAdapter(Context context, List<KmCountryCode> countries, String selectedIsoCode, KmClickHandler<KmCountryCode> kmClickHandler) {
        this.context = context;
        this.countries = countries;
        this.filteredCountries = new ArrayList<>(countries);
        this.selectedIsoCode = selectedIsoCode;
        this.kmClickHandler = kmClickHandler;
        this.selectedBackgroundColor = Utils.getColor(context, R.color.km_language_divider_color);
        this.defaultBackgroundColor = Utils.getColor(context, R.color.core_transparent_color);
    }

    @NonNull
    @Override
    public CountryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.km_country_code_item_layout, parent, false);
        return new CountryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CountryViewHolder viewHolder, int position) {
        KmCountryCode country = filteredCountries.get(position);
        boolean isSelected = country.getIsoCode().equals(selectedIsoCode);

        viewHolder.flag.setText(country.getFlag());
        viewHolder.name.setText(country.getName());
        viewHolder.dialCode.setText(country.getDialCode());
        viewHolder.parentView.setBackgroundColor(isSelected ? selectedBackgroundColor : defaultBackgroundColor);
    }

    @Override
    public int getItemCount() {
        return filteredCountries.size();
    }

    public void filter(String query) {
        filteredCountries.clear();
        String searchText = query == null ? "" : query.trim().toLowerCase(Locale.getDefault());
        for (KmCountryCode country : countries) {
            if (TextUtils.isEmpty(searchText)
                    || country.getName().toLowerCase(Locale.getDefault()).contains(searchText)
                    || country.getDialCode().contains(searchText)
                    || country.getIsoCode().equalsIgnoreCase(searchText)) {
                filteredCountries.add(country);
            }
        }
        notifyDataSetChanged();
    }

    public int getSelectedPosition() {
        for (int i = 0; i < filteredCountries.size(); i++) {
            if (filteredCountries.get(i).getIsoCode().equals(selectedIsoCode)) {
                return i;
            }
        }
        return 0;
    }

    class CountryViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout parentView;
        private final TextView flag;
        private final TextView name;
        private final TextView dialCode;

        CountryViewHolder(@NonNull View itemView) {
            super(itemView);
            parentView = itemView.findViewById(R.id.km_country_parent_view);
            flag = itemView.findViewById(R.id.km_country_flag);
            name = itemView.findViewById(R.id.km_country_name);
            dialCode = itemView.findViewById(R.id.km_country_dial_code);
            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (kmClickHandler != null && position != RecyclerView.NO_POSITION) {
                    kmClickHandler.onItemClicked(view, filteredCountries.get(position));
                }
            });
        }
    }
}
