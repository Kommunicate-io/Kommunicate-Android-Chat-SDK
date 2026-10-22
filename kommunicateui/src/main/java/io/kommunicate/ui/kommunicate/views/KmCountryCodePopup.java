package io.kommunicate.ui.kommunicate.views;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.kommunicate.ui.R;
import io.kommunicate.ui.kommunicate.adapters.KmCountryCodeAdapter;
import io.kommunicate.ui.kommunicate.callbacks.KmClickHandler;
import io.kommunicate.ui.kommunicate.models.KmCountryCode;
import io.kommunicate.ui.kommunicate.utils.DimensionsUtils;
import io.kommunicate.ui.kommunicate.utils.KmCountryCodeHelper;

public class KmCountryCodePopup extends PopupWindow {
    private static final int POPUP_HEIGHT_DP = 280;
    private static final int POPUP_ELEVATION_DP = 8;

    public KmCountryCodePopup(@NonNull Context context, KmCountryCode selectedCountry, @NonNull final KmClickHandler<KmCountryCode> kmClickHandler) {
        super(LayoutInflater.from(context).inflate(R.layout.km_country_code_picker_layout, null),
                ViewGroup.LayoutParams.WRAP_CONTENT, DimensionsUtils.convertDpToPx(POPUP_HEIGHT_DP), true);
        setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.km_country_code_popup_background));
        setElevation(DimensionsUtils.convertDpToPx(POPUP_ELEVATION_DP));
        setOutsideTouchable(true);
        setInputMethodMode(INPUT_METHOD_NEEDED);

        View view = getContentView();
        final KmCountryCodeAdapter adapter = new KmCountryCodeAdapter(context, KmCountryCodeHelper.getCountries(),
                selectedCountry != null ? selectedCountry.getIsoCode() : null, (itemView, country) -> {
            dismiss();
            kmClickHandler.onItemClicked(itemView, country);
        });

        RecyclerView countryRecyclerView = view.findViewById(R.id.km_country_recycler_view);
        countryRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        countryRecyclerView.setAdapter(adapter);
        countryRecyclerView.scrollToPosition(adapter.getSelectedPosition());

        EditText searchEditText = view.findViewById(R.id.km_country_search_et);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.filter(s.toString());
                countryRecyclerView.scrollToPosition(0);
            }
        });
    }

    public void show(View anchor) {
        setWidth(anchor.getWidth());
        showAsDropDown(anchor);
    }
}
