package io.kommunicate.ui.kommunicate.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.kommunicate.ui.R;

import io.kommunicate.models.KmPrechatInputModel;

import io.kommunicate.ui.kommunicate.models.KmCountryCode;
import io.kommunicate.ui.kommunicate.utils.KmCountryCodeHelper;
import io.kommunicate.ui.kommunicate.views.KmCountryCodePopup;
import io.kommunicate.ui.kommunicate.views.KmToast;
import io.kommunicate.commons.AppContextService;
import io.kommunicate.commons.commons.core.utils.Utils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Recycler view adapter to handle different prechat inputs: Textinput and Dropdown
 */

public class KmPrechatInputAdapter extends RecyclerView.Adapter {

    private List<KmPrechatInputModel> inputModelList;
    private Map<String, String> dataMap;
    private Map<String, String> inputTextMap;
    private Map<String, KmCountryCode> countryCodeMap;
    private Context context;
    public static final String PRECHAT_DROPDOWN_ELEMENT = "select";
    private static final int COUNTRY_CODE_INPUT_VIEW_TYPE = 3;
    private static final int MAX_PHONE_NUMBER_LENGTH = 15;

    public KmPrechatInputAdapter(List<KmPrechatInputModel> inputModelList, Context context) {
        this.inputModelList = inputModelList;
        this.dataMap = new HashMap<>();
        this.inputTextMap = new HashMap<>();
        this.countryCodeMap = new HashMap<>();
        this.context = context;
        if (inputModelList != null) {
            KmCountryCode defaultCountry = null;
            for (KmPrechatInputModel inputModel : inputModelList) {
                if (inputModel != null && inputModel.isEnableCountryCode()) {
                    if (defaultCountry == null) {
                        defaultCountry = KmCountryCodeHelper.getDefaultCountry(context);
                    }
                    selectCountry(inputModel, defaultCountry);
                }
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (layoutInflater == null) {
            return null;
        }

        if(viewType == 1) {
            View dropdownHolder = layoutInflater.inflate(R.layout.km_prechat_dropdown_layout, parent, false);
            return new KmPrechatDropdownViewHolder(dropdownHolder);
        }
        else {
            View inputHolder = layoutInflater.inflate(R.layout.km_prechat_input_item_layout, parent, false);
            return viewType == COUNTRY_CODE_INPUT_VIEW_TYPE ? new KmPrechatPhoneInputViewHolder(inputHolder) : new KmPrechatInputViewHolder(inputHolder);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        int type = getItemViewType(position);
        KmPrechatInputModel inputModel = getItem(position);
        if(inputModel == null)
            return;
        if(type == 1 ){
            KmPrechatDropdownViewHolder dropdownViewHolder = (KmPrechatDropdownViewHolder) viewHolder;
            List<String> dropdownList = new ArrayList<>();
            if(!TextUtils.isEmpty(inputModel.getPlaceholder())) {
                dropdownList.add(inputModel.getPlaceholder());
            }
            if(!inputModel.getOptions().isEmpty()) {
                dropdownList.addAll(inputModel.getOptions());
            }
            ArrayAdapter<String> adapter = new KmPrechatDropdownAdapter(context,
                    android.R.layout.simple_spinner_item, inputModel, dropdownList);
            dropdownViewHolder.dropdownSpinner.setAdapter(adapter);
            adapter.setDropDownViewResource(R.layout.custom_spinner);
        }
        else {
            KmPrechatInputViewHolder inputViewHolder = (KmPrechatInputViewHolder) viewHolder;
            inputViewHolder.inputEditText.setInputType(KmPrechatInputModel.KmInputType.getInputType(inputModel.getType()));
            inputViewHolder.inputEditText.setTransformationMethod(KmPrechatInputModel.KmInputType.PASSWORD.equals(inputModel.getType()) ? PasswordTransformationMethod.getInstance() : null);
            inputViewHolder.textInputLayout.setHint(inputModel.getField());
            inputViewHolder.textInputLayout.setPlaceholderText(inputModel.getPlaceholder());

            inputViewHolder.inputEditText.setText(inputTextMap != null && !TextUtils.isEmpty(inputTextMap.get(inputModel.getField())) ? inputTextMap.get(inputModel.getField()) : "");
            inputViewHolder.inputEditText.setSelection(inputTextMap != null && inputTextMap.get(inputModel.getField()) != null ? inputTextMap.get(inputModel.getField()).length() : 0);
            inputViewHolder.inputEditText.setError(getErrorText(inputModel));

            if (type == COUNTRY_CODE_INPUT_VIEW_TYPE) {
                KmPrechatPhoneInputViewHolder phoneInputViewHolder = (KmPrechatPhoneInputViewHolder) viewHolder;
                phoneInputViewHolder.inputEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                phoneInputViewHolder.setCountryCode(countryCodeMap.get(inputModel.getField()));
            }
        }

    }

    /**
     * Get the type of Viewholder required
     * If model is null: 0
     * If element is "select": 1
     * if element is input: 2
     * if element is input with country code enabled: 3
     */
    @Override
    public int getItemViewType(int position) {
        KmPrechatInputModel model = getItem(position);
        if(model == null) {
            return 0;
        }
        if(model.getElement() != null && model.getElement().equals(PRECHAT_DROPDOWN_ELEMENT)) {
            return 1;
        }
        if(model.isEnableCountryCode()) {
            return COUNTRY_CODE_INPUT_VIEW_TYPE;
        }
        return 2;
    }

    private KmPrechatInputModel getItem(int position) {
        return inputModelList.get(position);
    }

    @Override
    public int getItemCount() {
        return inputModelList != null ? inputModelList.size() : 0;
    }

    public Map<String, String> getDataMap() {
        Map<String, String> data = new HashMap<>(inputTextMap);
        for (Map.Entry<String, KmCountryCode> countryCodeEntry : countryCodeMap.entrySet()) {
            String number = data.get(countryCodeEntry.getKey());
            if (!TextUtils.isEmpty(number)) {
                data.put(countryCodeEntry.getKey(), countryCodeEntry.getValue().getInternationalNumber(number));
            }
        }
        return data;
    }

    public boolean areFieldsValid() {
        boolean isValid = true;
        inputTextMap.putAll(dataMap);
        for (KmPrechatInputModel inputModel : inputModelList) {
            boolean isEmptyFieldError = inputModel.isRequired() && TextUtils.isEmpty(inputTextMap.get(inputModel.getField()));
            boolean isValidationError = !isInValidCompositeField(inputModel) && !TextUtils.isEmpty(inputTextMap.get(inputModel.getField())) && !TextUtils.isEmpty(inputModel.getValidationRegex()) && !Pattern.compile(inputModel.getValidationRegex()).matcher(inputTextMap.get(inputModel.getField())).matches();

            if (isEmptyFieldError || isValidationError) {
                isValid = false;
            }

            inputModel.setDisplayEmptyFieldError(isEmptyFieldError);
            inputModel.setDisplayValidationError(isValidationError);

            if (isInValidCompositeField(inputModel)) {
                KmToast.error(AppContextService.getAppContext(), getString(R.string.prechat_screen_toast_error_message, inputModel.getField(), inputModel.getCompositeRequiredField()), Toast.LENGTH_SHORT).show();
                return false;
            }

            if(inputModel.getElement() != null && inputModel.getElement().equals(PRECHAT_DROPDOWN_ELEMENT) && inputModel.isRequired() && TextUtils.isEmpty(inputTextMap.get(inputModel.getField()))) {
                KmToast.error(AppContextService.getAppContext(), getString(R.string.prechat_dropdown_toast_error_message), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        notifyDataSetChanged();
        return isValid;
    }

    private void selectCountry(KmPrechatInputModel inputModel, KmCountryCode country) {
        countryCodeMap.put(inputModel.getField(), country);
        inputModel.setValidationRegex(country.getNumberValidationRegex());
        inputModel.setDisplayValidationError(false);
    }

    private boolean isInValidCompositeField(KmPrechatInputModel inputModel) {
        return !TextUtils.isEmpty(inputModel.getCompositeRequiredField()) && TextUtils.isEmpty(inputTextMap.get(inputModel.getField())) && TextUtils.isEmpty(inputTextMap.get(inputModel.getCompositeRequiredField()));
    }

    private String getString(int resId) {
        return Utils.getString(AppContextService.getAppContext(), resId);
    }

    private String getString(int resId, Object... args) {
        return AppContextService.getAppContext().getString(resId, args);
    }

    private String getErrorText(KmPrechatInputModel inputModel) {
        if (inputModel.isDisplayEmptyFieldError()) {
            return getString(R.string.km_empty_field_error);
        } else if (inputModel.isDisplayValidationError()) {
            return !TextUtils.isEmpty(inputModel.getValidationError()) ? inputModel.getValidationError() : getString(R.string.km_validation_error, inputModel.getField());
        }
        return null;
    }

    public class KmPrechatInputViewHolder extends RecyclerView.ViewHolder {
        protected TextInputEditText inputEditText;
        protected TextInputLayout textInputLayout;

        public KmPrechatInputViewHolder(@NonNull View itemView) {
            super(itemView);
            inputEditText = itemView.findViewById(R.id.prechatInputEt);
            textInputLayout = itemView.findViewById(R.id.prechatTextInputLayout);
            textInputLayout.setPlaceholderTextAppearance(R.style.KmPrechatPlaceholderText);
            textInputLayout.setPlaceholderTextColor(ColorStateList.valueOf(Utils.getColor(context, R.color.km_prechat_placeholder_text_color)));

            inputEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    KmPrechatInputModel model = getItem(getAdapterPosition());
                    if (model != null) {
                        dataMap.put(model.getField(), s.toString());
                    }
                }
            });
        }
    }

    public class KmPrechatPhoneInputViewHolder extends KmPrechatInputViewHolder {

        public KmPrechatPhoneInputViewHolder(@NonNull View itemView) {
            super(itemView);
            inputEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_PHONE_NUMBER_LENGTH)});
            textInputLayout.setExpandedHintEnabled(false);
            textInputLayout.setPrefixTextColor(ColorStateList.valueOf(Utils.getColor(context, R.color.prechat_edittext_text_color)));

            TextView countryCodeTextView = textInputLayout.getPrefixTextView();
            countryCodeTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, inputEditText.getTextSize());
            countryCodeTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_black_18dp, 0);
            countryCodeTextView.setContentDescription(getString(R.string.km_select_country));
            countryCodeTextView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                final KmPrechatInputModel model = position != RecyclerView.NO_POSITION ? getItem(position) : null;
                if (model == null) {
                    return;
                }
                new KmCountryCodePopup(view.getContext(), countryCodeMap.get(model.getField()), (selectedView, country) -> {
                    selectCountry(model, country);
                    setCountryCode(country);
                }).show(textInputLayout);
            });
        }

        private void setCountryCode(KmCountryCode country) {
            if (country != null) {
                textInputLayout.setPrefixText(country.getFlag() + " " + country.getDialCode());
            }
        }
    }

    /**
     * Dropdown view holder. If placeholder is present, then select the 1st option otherwise neglect it
     */
    public class KmPrechatDropdownViewHolder extends RecyclerView.ViewHolder {

        private Spinner dropdownSpinner;

        public KmPrechatDropdownViewHolder(@NonNull View itemView) {
            super(itemView);
            dropdownSpinner = itemView.findViewById(R.id.prechatDropdownSpinner);
            dropdownSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    KmPrechatInputModel model = getItem(getAdapterPosition());
                    if (model != null) {
                        if(model.getPlaceholder() != null && i > 0) {
                            dataMap.put(model.getField(), adapterView.getItemAtPosition(i).toString());
                        } else if(model.getPlaceholder() == null) {
                            dataMap.put(model.getField(), adapterView.getItemAtPosition(i).toString());
                        }
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });
        }
    }
}
