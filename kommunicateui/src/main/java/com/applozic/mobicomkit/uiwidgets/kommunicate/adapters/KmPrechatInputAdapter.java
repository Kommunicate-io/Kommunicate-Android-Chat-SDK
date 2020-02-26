package com.applozic.mobicomkit.uiwidgets.kommunicate.adapters;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.applozic.mobicomkit.uiwidgets.R;

import io.kommunicate.models.KmPrechatInputModel;

import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class KmPrechatInputAdapter extends RecyclerView.Adapter<KmPrechatInputAdapter.KmPrechatInputViewHolder> {

    private List<KmPrechatInputModel> inputModelList;
    private Map<String, String> dataMap;

    public KmPrechatInputAdapter(List<KmPrechatInputModel> inputModelList) {
        this.inputModelList = inputModelList;
        this.dataMap = new HashMap<>();
    }

    @NonNull
    @Override
    public KmPrechatInputViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new KmPrechatInputViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.km_prechat_input_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull KmPrechatInputViewHolder holder, int position) {
        KmPrechatInputModel inputModel = inputModelList.get(position);
        if (inputModel != null) {
            holder.bind(inputModel);
        }
    }

    @Override
    public int getItemCount() {
        return inputModelList != null ? inputModelList.size() : 0;
    }

    public class KmPrechatInputViewHolder extends RecyclerView.ViewHolder {
        private TextInputEditText inputEditText;
        private TextInputLayout textInputLayout;

        public KmPrechatInputViewHolder(@NonNull View itemView) {
            super(itemView);
            inputEditText = itemView.findViewById(R.id.prechatInputEt);
            textInputLayout = itemView.findViewById(R.id.prechatTextInputLayout);

            inputEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    KmPrechatInputModel model = inputModelList.get(getAdapterPosition());
                    if (model != null) {
                        dataMap.put(model.getField(), s.toString());
                    }
                }
            });
        }

        public void bind(KmPrechatInputModel inputModel) {
            if (inputModel != null) {
                inputEditText.setInputType(KmPrechatInputModel.KmInputType.getInputType(inputModel.getType()));
                inputEditText.setTransformationMethod(KmPrechatInputModel.KmInputType.PASSWORD.equals(inputModel.getType()) ? PasswordTransformationMethod.getInstance() : null);
                textInputLayout.setHint(inputModel.getField());
                inputEditText.setText(dataMap != null && !TextUtils.isEmpty(dataMap.get(inputModel.getField())) ? dataMap.get(inputModel.getField()) : "");
                inputEditText.setSelection(dataMap != null && dataMap.get(inputModel.getField()) != null ? dataMap.get(inputModel.getField()).length() : 0);
                inputEditText.setError(getErrorText(inputModel));
            }
        }
    }

    public Map<String, String> getDataMap() {
        return dataMap;
    }

    public boolean areFieldsValid() {
        boolean isValid = true;
        for (KmPrechatInputModel inputModel : inputModelList) {
            boolean isEmptyFieldError = inputModel.isRequired() && TextUtils.isEmpty(dataMap.get(inputModel.getField()));
            boolean isValidationError = !isInValidCompositeField(inputModel) && !TextUtils.isEmpty(dataMap.get(inputModel.getField())) && !TextUtils.isEmpty(inputModel.getValidationRegex()) && !Pattern.compile(inputModel.getValidationRegex()).matcher(dataMap.get(inputModel.getField())).matches();

            if (isEmptyFieldError || isValidationError) {
                isValid = false;
            }

            inputModel.setDisplayEmptyFieldError(isEmptyFieldError);
            inputModel.setDisplayValidationError(isValidationError);

            if (isInValidCompositeField(inputModel)) {
                Toast.makeText(ApplozicService.getAppContext(), getString(R.string.prechat_screen_toast_error_message, inputModel.getField(), inputModel.getCompositeRequiredField()), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        notifyDataSetChanged();
        return isValid;
    }

    private boolean isInValidCompositeField(KmPrechatInputModel inputModel) {
        return !TextUtils.isEmpty(inputModel.getCompositeRequiredField()) && TextUtils.isEmpty(dataMap.get(inputModel.getField())) && TextUtils.isEmpty(dataMap.get(inputModel.getCompositeRequiredField()));
    }

    private String getString(int resId) {
        return Utils.getString(ApplozicService.getAppContext(), resId);
    }

    private String getString(int resId, Object... args) {
        return ApplozicService.getAppContext().getString(resId, args);
    }

    private String getErrorText(KmPrechatInputModel inputModel) {
        if (inputModel.isDisplayEmptyFieldError()) {
            return getString(R.string.km_empty_field_error);
        } else if (inputModel.isDisplayValidationError()) {
            return !TextUtils.isEmpty(inputModel.getValidationError()) ? inputModel.getValidationError() : getString(R.string.km_validation_error, inputModel.getField());
        }
        return null;
    }
}
