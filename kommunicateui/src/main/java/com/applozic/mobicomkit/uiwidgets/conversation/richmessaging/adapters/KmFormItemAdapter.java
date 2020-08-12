package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.adapters;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.AlRichMessage;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.helpers.KmFormStateHelper;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.KmFormStateModel;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.v2.KmFormPayloadModel;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.views.KmFlowLayout;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.views.KmRadioGroup;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class KmFormItemAdapter extends RecyclerView.Adapter {

    private Context context;
    private List<KmFormPayloadModel> payloadList;

    private SparseArray<String> textFieldArray;
    private SparseArray<HashSet<Integer>> checkBoxStateArray;
    private SparseIntArray radioButtonSelectedIndices;
    private SparseArray<Long> dateFieldArray;
    private Map<String, String> hiddenFields;
    private KmFormStateModel formStateModel;
    private String messageKey;
    private SparseIntArray validationArray;
    private static final int VALID_DATA = 2;
    private static final int INVALID_DATA = 1;

    private static final int VIEW_TYPE_TEXT_FIELD = 1;
    private static final int VIEW_TYPE_SELECTION = 2;
    private static final int VIEW_TYPE_DATETIME = 3;

    public KmFormItemAdapter(Context context, List<KmFormPayloadModel> payloadList, String messageKey) {
        this.context = context;
        this.payloadList = payloadList;
        this.messageKey = messageKey;

        this.formStateModel = KmFormStateHelper.getFormState(messageKey);

        KmFormStateHelper.initFormState();

        if (formStateModel == null) {
            formStateModel = new KmFormStateModel();
        }

        textFieldArray = formStateModel.getTextFields();
        checkBoxStateArray = formStateModel.getCheckBoxStates();
        radioButtonSelectedIndices = formStateModel.getSelectedRadioButtonIndex();
        hiddenFields = formStateModel.getHiddenFields();
        validationArray = formStateModel.getValidationArray();
        dateFieldArray = formStateModel.getDateFieldArray();

        if (textFieldArray == null) {
            textFieldArray = new SparseArray<>();
        }

        if (checkBoxStateArray == null) {
            checkBoxStateArray = new SparseArray<>();
        }

        if (radioButtonSelectedIndices == null) {
            radioButtonSelectedIndices = new SparseIntArray();
        }

        if (hiddenFields == null) {
            hiddenFields = new HashMap<>();
        }

        if (validationArray == null) {
            validationArray = new SparseIntArray();
        }

        if (dateFieldArray == null) {
            dateFieldArray = new SparseArray<>();
        }

        formStateModel.setTextFields(textFieldArray);
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType > 0) {
            return new KmFormItemViewHolder(LayoutInflater.from(context).inflate(R.layout.km_form_item_layout, parent, false));
        }

        return new KmFormItemViewHolder(new View(context));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (payloadList != null && !payloadList.isEmpty()) {

            KmFormPayloadModel payloadModel = payloadList.get(position);
            try {
                if (payloadModel != null) {
                    final KmFormItemViewHolder formItemViewHolder = (KmFormItemViewHolder) holder;

                    if (!isViewTypeField(payloadModel.getType()) || getItemViewType(position) == 0) {
                        if (formItemViewHolder.formItemLayout != null) {
                            formItemViewHolder.formItemLayout.setVisibility(View.GONE);
                        }
                        if (KmFormPayloadModel.Type.HIDDEN.getValue().equals(payloadModel.getType())) {
                            KmFormPayloadModel.Hidden hiddenModel = payloadModel.getHiddenModel();
                            if (hiddenModel != null && !hiddenFields.containsKey(hiddenModel.getName())) {
                                hiddenFields.put(hiddenModel.getName(), hiddenModel.getValue());
                            }
                        }
                        return;
                    }

                    if(payloadModel.getType().equals("datetime-local")) {
                        payloadModel.setType("time");
                    }

                    formItemViewHolder.formItemLayout.setVisibility(View.VISIBLE);

                    if (isTypeText(payloadModel.getType())) {
                        KmFormPayloadModel.Text textModel = payloadModel.getTextModel();

                        formItemViewHolder.formLabel.setVisibility(!TextUtils.isEmpty(textModel.getLabel()) ? View.VISIBLE : View.GONE);
                        formItemViewHolder.flowLayout.setVisibility(View.GONE);

                        EditText editText = KmFormPayloadModel.Type.PASSWORD.getValue().equals(payloadModel.getType()) ? formItemViewHolder.getPasswordTextField() : formItemViewHolder.getEditTextField();

                        editText.setVisibility(View.VISIBLE);
                        formItemViewHolder.formLabel.setText(textModel.getLabel());
                        editText.setHint(TextUtils.isEmpty(textModel.getPlaceholder()) ? "" : textModel.getPlaceholder());

                        String savedStr = textFieldArray.get(position, null);

                        if (savedStr != null) {
                            editText.setText(savedStr);
                        } else {
                            editText.setText(null);
                        }

                        if (validationArray.get(position) == 1) {
                            editText.setError(textModel.getValidation() != null && !TextUtils.isEmpty(textModel.getValidation().getErrorText()) ? textModel.getValidation().getErrorText() : Utils.getString(context, R.string.default_form_validation_error_text));
                        } else {
                            editText.setError(null);
                        }
                    } else if (KmFormPayloadModel.Type.RADIO.getValue().equals(payloadModel.getType())) {
                        KmFormPayloadModel.Selections selectionModel = payloadModel.getSelectionModel();

                        formItemViewHolder.formLabel.setVisibility(!TextUtils.isEmpty(selectionModel.getTitle()) ? View.VISIBLE : View.GONE);
                        formItemViewHolder.formEditText.setVisibility(View.GONE);

                        formItemViewHolder.formLabel.setText(selectionModel.getTitle());

                        List<KmFormPayloadModel.Options> options = payloadModel.getSelectionModel().getOptions();

                        new KmRadioGroup(context, new KmRadioGroup.KmRadioButtonClickListener() {
                            @Override
                            public void onClick(int index) {
                                radioButtonSelectedIndices.put(position, index);
                                formStateModel.setSelectedRadioButtonIndex(radioButtonSelectedIndices);
                                KmFormStateHelper.addFormState(messageKey, formStateModel);
                            }
                        }, formItemViewHolder.flowLayout, options).createLayout(radioButtonSelectedIndices.get(position, -1));
                    } else if (KmFormPayloadModel.Type.CHECKBOX.getValue().equals(payloadModel.getType())) {
                        KmFormPayloadModel.Selections selectionModel = payloadModel.getSelectionModel();

                        formItemViewHolder.formLabel.setVisibility(!TextUtils.isEmpty(selectionModel.getTitle()) ? View.VISIBLE : View.GONE);
                        formItemViewHolder.formEditText.setVisibility(View.GONE);

                        formItemViewHolder.formLabel.setText(selectionModel.getTitle());

                        List<KmFormPayloadModel.Options> options = payloadModel.getSelectionModel().getOptions();

                        final HashSet<Integer> checkedBoxes = checkBoxStateArray.get(position, new HashSet<Integer>());

                        if (options != null && !options.isEmpty()) {
                            formItemViewHolder.flowLayout.setVisibility(View.VISIBLE);

                            formItemViewHolder.flowLayout.removeAllViews();
                            for (KmFormPayloadModel.Options option : options) {
                                final int index = options.indexOf(option);
                                final CheckBox checkBox = new CheckBox(context);
                                checkBox.setChecked(checkedBoxes.contains(index));
                                checkBox.setText(option.getLabel());

                                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                        boolean isDone = isChecked ? checkedBoxes.add(index) : checkedBoxes.remove(index);
                                        checkBoxStateArray.put(position, checkedBoxes);
                                        formStateModel.setCheckBoxStates(checkBoxStateArray);
                                        KmFormStateHelper.addFormState(messageKey, formStateModel);
                                    }
                                });

                                formItemViewHolder.flowLayout.addView(checkBox);
                            }
                        } else {
                            formItemViewHolder.flowLayout.setVisibility(View.GONE);
                        }
                    } else if (KmFormPayloadModel.Type.DATE.getValue().equals(payloadModel.getType())
                            || KmFormPayloadModel.Type.TIME.getValue().equals(payloadModel.getType())
                            || KmFormPayloadModel.Type.DATE_TIME.getValue().equals(payloadModel.getType())) {

                        KmFormPayloadModel.DateTimePicker dateTimePickerModel = payloadModel.getDatePickerModel();
                        if (dateTimePickerModel != null) {
                            formItemViewHolder.formLabel.setVisibility(!TextUtils.isEmpty(dateTimePickerModel.getLabel()) ? View.VISIBLE : View.GONE);
                            formItemViewHolder.formEditText.setVisibility(View.GONE);
                            formItemViewHolder.formLabel.setText(dateTimePickerModel.getLabel());
                            formItemViewHolder.formDatePicker.setVisibility(View.VISIBLE);

                            if (dateFieldArray.get(position) != null) {
                                formItemViewHolder.formDatePicker.setText(getFormattedDateByType(payloadModel.getType(), dateFieldArray.get(position), dateTimePickerModel.isAmPm()));
                            } else {
                                formItemViewHolder.formDatePicker.setText("dd-mm-yyyy");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void openDatePickerDialog(final int position, final boolean withTime, final boolean isAmPm) {
        Calendar calendar = Calendar.getInstance();

        if (dateFieldArray.get(position) != null) {
            calendar.setTimeInMillis(dateFieldArray.get(position));
        }

        new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                final Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(Calendar.YEAR, year);
                selectedDate.set(Calendar.MONTH, month);
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                if (withTime) {
                    openTimePickerDialog(position, isAmPm, selectedDate);
                } else {
                    dateFieldArray.put(position, selectedDate.getTimeInMillis());
                    formStateModel.setDateFieldArray(dateFieldArray);
                    KmFormStateHelper.addFormState(messageKey, formStateModel);
                    notifyItemChanged(position);
                }
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void openTimePickerDialog(final int position, boolean isAmPm, final Calendar selectedDate) {
        Calendar calendar = Calendar.getInstance();

        if (dateFieldArray.get(position) != null) {
            calendar.setTimeInMillis(dateFieldArray.get(position));
        }

        new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Calendar selectedTime = selectedDate == null ? Calendar.getInstance() : selectedDate;
                selectedTime.set(Calendar.HOUR, hourOfDay);
                selectedTime.set(Calendar.MINUTE, minute);

                dateFieldArray.put(position, selectedTime.getTimeInMillis());
                formStateModel.setDateFieldArray(dateFieldArray);
                KmFormStateHelper.addFormState(messageKey, formStateModel);
                notifyItemChanged(position);
            }
        }, calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), !isAmPm).show();
    }

    private String getFormattedDate(Long timeInMillis) {
        return new SimpleDateFormat("dd-MM-yyyy").format(new Date(timeInMillis));
    }

    private String getFormattedTime(Long timeInMillis, boolean isAmPm) {
        return new SimpleDateFormat("hh:mm" + (isAmPm ? " aa" : "")).format(new Date(timeInMillis));
    }

    private String getFormattedDateTime(Long timeInMillis, boolean isAmPm) {
        return new SimpleDateFormat("dd-MM-yyyy hh:mm" + (isAmPm ? " aa" : "")).format(new Date(timeInMillis));
    }

    private String getFormattedDateByType(String type, Long timeInMillis, boolean isAmPm) {
        if (KmFormPayloadModel.Type.DATE.getValue().equals(type)) {
            return getFormattedDate(timeInMillis);
        } else if (KmFormPayloadModel.Type.TIME.getValue().equals(type)) {
            return getFormattedTime(timeInMillis, isAmPm);
        } else if (KmFormPayloadModel.Type.DATE_TIME.getValue().equals(type)) {
            return getFormattedDateTime(timeInMillis, isAmPm);
        }
        return "";
    }

    public boolean isFormDataValid() {
        boolean isValid = true;
        if (payloadList != null) {
            for (int i = 0; i < payloadList.size(); i++) {
                if (KmFormPayloadModel.Type.TEXT.getValue().equals(payloadList.get(i).getType())) {
                    KmFormPayloadModel.Text textField = payloadList.get(i).getTextModel();
                    if (textField != null) {

                        String enteredText;
                        if (KmFormStateHelper.getFormState(messageKey) == null || TextUtils.isEmpty(KmFormStateHelper.getFormState(messageKey).getTextFields().get(i))) {
                            enteredText = "";
                        } else {
                            enteredText = KmFormStateHelper.getFormState(messageKey).getTextFields().get(i);
                        }

                        if (textField.getValidation() != null
                                && !TextUtils.isEmpty(textField.getValidation().getRegex())
                                && !Pattern.compile(textField.getValidation().getRegex()).matcher(enteredText).matches()) {
                            validationArray.put(i, INVALID_DATA);
                            formStateModel.setValidationArray(validationArray);
                            KmFormStateHelper.addFormState(messageKey, formStateModel);
                            isValid = false;
                        } else {
                            validationArray.put(i, VALID_DATA);
                        }
                    }
                }
            }
        }
        if (!isValid) {
            notifyDataSetChanged();
        }
        return isValid;
    }

    @Override
    public int getItemCount() {
        return payloadList != null ? payloadList.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (payloadList != null && !payloadList.isEmpty()) {
            if (KmFormPayloadModel.Type.TEXT.getValue().equals(payloadList.get(position).getType())
                    || KmFormPayloadModel.Type.PASSWORD.getValue().equals(payloadList.get(position).getType())) {
                return VIEW_TYPE_TEXT_FIELD;
            } else if (KmFormPayloadModel.Type.RADIO.getValue().equals(payloadList.get(position).getType())
                    || KmFormPayloadModel.Type.CHECKBOX.getValue().equals(payloadList.get(position).getType())) {
                return VIEW_TYPE_SELECTION;
            } else if (KmFormPayloadModel.Type.DATE.getValue().equals(payloadList.get(position).getType())
                    || KmFormPayloadModel.Type.TIME.getValue().equals(payloadList.get(position).getType())
                    || KmFormPayloadModel.Type.DATE_TIME.getValue().equals(payloadList.get(position).getType())) {
                return VIEW_TYPE_DATETIME;
            }
        }
        return 0;
    }

    public boolean isViewTypeField(String type) {
        return !(KmFormPayloadModel.Type.HIDDEN.getValue().equals(type) || AlRichMessage.SUBMIT_BUTTON.equals(type));
    }

    public class KmFormItemViewHolder extends RecyclerView.ViewHolder {

        TextView formLabel;
        EditText formEditText;
        LinearLayout formItemLayout;
        KmFlowLayout flowLayout;
        TextView formDatePicker;

        public KmFormItemViewHolder(@NonNull View itemView) {
            super(itemView);

            formLabel = itemView.findViewById(R.id.kmFormLabel);
            formEditText = itemView.findViewById(R.id.kmFormEditText);
            formItemLayout = itemView.findViewById(R.id.kmFormItemLayout);
            flowLayout = itemView.findViewById(R.id.kmFormSelectionItems);
            formDatePicker = itemView.findViewById(R.id.kmFormDatePicker);

            if (formEditText != null) {
                formEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (formEditText.hasFocus()) {
                            String text = s.toString().trim();
                            textFieldArray.put(getAdapterPosition(), text);
                            formStateModel.setTextFields(textFieldArray);
                            KmFormStateHelper.addFormState(messageKey, formStateModel);
                        }
                    }
                });
            }

            if (formDatePicker != null) {
                formDatePicker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        KmFormPayloadModel payloadModel = payloadList.get(position);
                        KmFormPayloadModel.DateTimePicker dateTimePicker = payloadModel.getDatePickerModel();

                        if (payloadModel != null) {
                            if (KmFormPayloadModel.Type.DATE.getValue().equals(payloadModel.getType())) {
                                openDatePickerDialog(position, false, dateTimePicker.isAmPm());
                            } else if (KmFormPayloadModel.Type.TIME.getValue().equals(payloadModel.getType())) {
                                openTimePickerDialog(position, dateTimePicker.isAmPm(), null);
                            } else if (KmFormPayloadModel.Type.DATE_TIME.getValue().equals(payloadModel.getType())) {
                                openDatePickerDialog(position, true, dateTimePicker.isAmPm());
                            }
                        }
                    }
                });
            }
        }

        public EditText getEditTextField() {
            formEditText.setInputType(InputType.TYPE_CLASS_TEXT);
            formEditText.setTransformationMethod(null);
            return formEditText;
        }

        public EditText getPasswordTextField() {
            formEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            formEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            return formEditText;
        }
    }

    private boolean isTypeText(String type) {
        return KmFormPayloadModel.Type.TEXT.getValue().equals(type) || KmFormPayloadModel.Type.PASSWORD.getValue().equals(type);
    }
}
