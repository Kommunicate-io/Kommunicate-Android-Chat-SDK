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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.KmRichMessage;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.form.KmDropdownItemAdapter;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.helpers.KmFormStateHelper;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.KmFormStateModel;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.v2.KmFormPayloadModel;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.views.KmFlowLayout;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.views.KmRadioGroup;
import com.applozic.mobicomkit.uiwidgets.kommunicate.views.KmSelectButton;
import com.applozic.mobicomkit.uiwidgets.kommunicate.views.KmToast;
import com.applozic.mobicommons.commons.core.utils.Utils;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import io.kommunicate.utils.KmDateUtils;

import static android.view.View.FOCUS_FORWARD;

public class KmFormItemAdapter extends RecyclerView.Adapter {

    private Context context;
    private List<KmFormPayloadModel> payloadList;

    private SparseArray<String> textFieldArray;
    private SparseArray<String> textAreaFieldArray;
    private SparseArray<HashSet<Integer>> checkBoxStateArray;
    private SparseArray<HashSet<Integer>> unCheckBoxStateArray;
    private SparseIntArray radioButtonSelectedIndices;
    private SparseArray<Long> dateFieldArray;
    private Map<String, String> hiddenFields;
    private KmFormStateModel formStateModel;
    private String messageKey;
    private SparseIntArray validationArray;
    private SparseArray<KmFormPayloadModel.Options> dropdownFieldArray;
    private AlCustomizationSettings alCustomizationSettings;
    private static final int VALID_DATA = 2;
    private static final int INVALID_DATA = 1;

    private static final int VIEW_TYPE_TEXT_FIELD = 1;
    private static final int VIEW_TYPE_SELECTION = 2;
    private static final int VIEW_TYPE_DATETIME = 3;
    private static final int VIEW_TYPE_DROPDOWN = 4;

    //TODO: Create Adaptor Factory Pattern for this Form rich message type
    public KmFormItemAdapter(Context context, List<KmFormPayloadModel> payloadList, String messageKey, AlCustomizationSettings alCustomizationSettings) {
        this.context = context;
        this.payloadList = payloadList;
        this.messageKey = messageKey;

        this.formStateModel = KmFormStateHelper.getFormState(messageKey);
        this.alCustomizationSettings = alCustomizationSettings;

        KmFormStateHelper.initFormState();

        if (formStateModel == null) {
            formStateModel = new KmFormStateModel();
        }

        textFieldArray = formStateModel.getTextFields();
        textAreaFieldArray = formStateModel.getTextAreaFields();
        checkBoxStateArray = formStateModel.getCheckBoxStates();
        unCheckBoxStateArray = formStateModel.getUncheckBoxStates();
        radioButtonSelectedIndices = formStateModel.getSelectedRadioButtonIndex();
        hiddenFields = formStateModel.getHiddenFields();
        validationArray = formStateModel.getValidationArray();
        dateFieldArray = formStateModel.getDateFieldArray();
        dropdownFieldArray = formStateModel.getDropdownFieldArray();


        if (textFieldArray == null) {
            textFieldArray = new SparseArray<>();
        }
        if (textAreaFieldArray == null) {
            textAreaFieldArray = new SparseArray<>();
        }

        if (checkBoxStateArray == null) {
            checkBoxStateArray = new SparseArray<>();
        }

        if (unCheckBoxStateArray == null) {
            unCheckBoxStateArray = new SparseArray<>();
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

        if (dropdownFieldArray == null) {
            dropdownFieldArray = new SparseArray<>();
        }

        formStateModel.setTextFields(textFieldArray);
        formStateModel.setTextAreaFields(textAreaFieldArray);
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
                        if (formItemViewHolder.formItemRootLayout != null) {
                            formItemViewHolder.formItemRootLayout.setVisibility(View.GONE);
                        }
                        if (KmFormPayloadModel.Type.HIDDEN.getValue().equals(payloadModel.getType())) {
                            KmFormPayloadModel.Hidden hiddenModel = payloadModel.getHiddenModel();
                            if (hiddenModel != null && !hiddenFields.containsKey(hiddenModel.getName())) {
                                hiddenFields.put(hiddenModel.getName(), hiddenModel.getValue());
                            }
                        }
                        return;
                    }

                    formItemViewHolder.formItemRootLayout.setVisibility(View.VISIBLE);

                    if (payloadModel.isTypeText()) {
                        KmFormPayloadModel.Text textModel = payloadModel.getTextModel();
                        setFormLabelText(formItemViewHolder, textModel.getLabel());
                        handleItemVisibility(formItemViewHolder, formItemViewHolder.formEditText);
                        EditText editText = KmFormPayloadModel.Type.PASSWORD.getValue().equals(payloadModel.getType()) ? formItemViewHolder.getPasswordTextField() : formItemViewHolder.getEditTextField();
                        editText.setHint(TextUtils.isEmpty(textModel.getPlaceholder()) ? "" : textModel.getPlaceholder());

                        String savedStr = textFieldArray.get(position, null);

                        editText.setText(savedStr);
                        //Focus next available editText without crashing
                        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                            @Override
                            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                                    View view = textView.focusSearch(FOCUS_FORWARD);
                                    if (view != null) {
                                        if (!view.requestFocus(FOCUS_FORWARD)) {
                                            return true;
                                        }
                                    }
                                    return false;
                                }
                                return false;
                            }
                        });
                        if (validationArray.get(position) == 1) {
                            formItemViewHolder.formValidationText.setVisibility(View.VISIBLE);
                            formItemViewHolder.formValidationText.setText(textModel.getValidation() != null
                                    && !TextUtils.isEmpty(textModel.getValidation().getErrorText())
                                    ? textModel.getValidation().getErrorText()
                                    : Utils.getString(context, R.string.default_form_validation_error_text));
                        } else {
                            formItemViewHolder.formValidationText.setVisibility(View.GONE);
                        }
                    } else if (payloadModel.isTypeTextArea()) {
                        KmFormPayloadModel.TextArea textAreaModel = payloadModel.getTextAreaModel();
                        setFormLabelText(formItemViewHolder, textAreaModel.getTitle());
                        handleItemVisibility(formItemViewHolder, formItemViewHolder.formEditText);
                        EditText editText = formItemViewHolder.getTextAreaEditField();
                        editText.setHint(TextUtils.isEmpty(textAreaModel.getPlaceholder()) ? "" : textAreaModel.getPlaceholder());
                        editText.setLines(textAreaModel.getRows());
                        editText.setVerticalScrollBarEnabled(true);

                        String savedStr = textAreaFieldArray.get(position, null);
                        editText.setText(savedStr);

                        //Focus next available editText without crashing
                        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                            @Override
                            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                                    View view = textView.focusSearch(FOCUS_FORWARD);
                                    if (view != null) {
                                        if (!view.requestFocus(FOCUS_FORWARD)) {
                                            return true;
                                        }
                                    }
                                    return false;
                                }
                                return false;
                            }
                        });
                        if (validationArray.get(position) == 1) {
                            formItemViewHolder.formValidationText.setVisibility(View.VISIBLE);
                            formItemViewHolder.formValidationText.setText(textAreaModel.getValidation() != null
                                    && !TextUtils.isEmpty(textAreaModel.getValidation().getErrorText())
                                    ? textAreaModel.getValidation().getErrorText()
                                    : Utils.getString(context, R.string.default_form_validation_error_text));
                        } else {
                            formItemViewHolder.formValidationText.setVisibility(View.GONE);
                        }
                    } else if (KmFormPayloadModel.Type.RADIO.getValue().equals(payloadModel.getType())) {
                        KmFormPayloadModel.Selections selectionModel = payloadModel.getSelectionModel();

                        setFormLabelText(formItemViewHolder, selectionModel.getTitle());
                        handleItemVisibility(formItemViewHolder, formItemViewHolder.formFlowLayout);

                        List<KmFormPayloadModel.Options> options = payloadModel.getSelectionModel().getOptions();

                        int selectedIndex = -1;
                        for (int i = 0; i < options.size(); i++){
                            if (options.get(i).isSelected()){
                                selectedIndex = i;
                            }
                        }

                        if (selectedIndex >= 0) {
                            radioButtonSelectedIndices.put(position, selectedIndex);
                            formStateModel.setSelectedRadioButtonIndex(radioButtonSelectedIndices);
                            KmFormStateHelper.addFormState(messageKey, formStateModel);
                        }

                        new KmRadioGroup(context, new KmRadioGroup.KmRadioButtonClickListener() {
                            @Override
                            public void onClick(int index) {
                                radioButtonSelectedIndices.put(position, index);
                                formStateModel.setSelectedRadioButtonIndex(radioButtonSelectedIndices);
                                KmFormStateHelper.addFormState(messageKey, formStateModel);
                            }
                        }, formItemViewHolder.formFlowLayout, options).createLayout(radioButtonSelectedIndices.get(position, selectedIndex));
                    } else if (KmFormPayloadModel.Type.CHECKBOX.getValue().equals(payloadModel.getType())) {
                        KmFormPayloadModel.Selections selectionModel = payloadModel.getSelectionModel();
                        setFormLabelText(formItemViewHolder, selectionModel.getTitle());
                        handleItemVisibility(formItemViewHolder, formItemViewHolder.formFlowLayout);
                        List<KmFormPayloadModel.Options> options = payloadModel.getSelectionModel().getOptions();
                        final HashSet<Integer> checkedBoxes = checkBoxStateArray.get(position, new HashSet<Integer>());
                        final HashSet<Integer> unCheckedBoxes = unCheckBoxStateArray.get(position, new HashSet<Integer>());
                        if (options != null && !options.isEmpty()) {
                            formItemViewHolder.formFlowLayout.removeAllViews();
                            for (KmFormPayloadModel.Options option : options) {
                                final int index = options.indexOf(option);
                                if(alCustomizationSettings.isCheckboxAsMultipleButton()) {
                                    final KmSelectButton checkBox = new KmSelectButton(context);
                                    checkBox.setGravity(Gravity.FILL);
                                    if (checkedBoxes.contains(index)){
                                        checkBox.setChecked(true);
                                    } else if (unCheckedBoxes.contains(index)){
                                        checkBox.setChecked(false);
                                    } else {
                                        boolean isChecked = option.isSelected();
                                        checkBox.setChecked(isChecked);
                                        processCheckBoxSelectedState(isChecked,index,position,messageKey,checkedBoxes,unCheckedBoxes,checkBoxStateArray,unCheckBoxStateArray,formStateModel);
                                    }
                                    checkBox.setText(option.getLabel());
                                    checkBox.setOnCheckedChangeListener(new KmSelectButton.onMultipleSelectButtonClicked() {
                                        @Override
                                        public void onSelectionChanged(View view, boolean isChecked) {
                                            checkBox.setChecked(isChecked);
                                            processCheckBoxSelectedState(isChecked,index,position,messageKey,checkedBoxes,unCheckedBoxes,checkBoxStateArray,unCheckBoxStateArray,formStateModel);
                                        }
                                    });
                                    formItemViewHolder.formFlowLayout.addView(checkBox);
                                } else {
                                    final CheckBox checkBox = new CheckBox(context);
                                    checkBox.setGravity(Gravity.FILL);
                                    if (checkedBoxes.contains(index)){
                                        checkBox.setChecked(true);
                                    } else if (unCheckedBoxes.contains(index)){
                                        checkBox.setChecked(false);
                                    } else {
                                        boolean isChecked = option.isSelected();
                                        checkBox.setChecked(isChecked);
                                        processCheckBoxSelectedState(isChecked,index,position,messageKey,checkedBoxes,unCheckedBoxes,checkBoxStateArray,unCheckBoxStateArray,formStateModel);
                                    }
                                    checkBox.setText(option.getLabel());
                                    checkBox.setPaddingRelative(0, 0, 20, 0);

                                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                        @Override
                                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                            checkBox.setChecked(isChecked);
                                            processCheckBoxSelectedState(isChecked,index,position,messageKey,checkedBoxes,unCheckedBoxes,checkBoxStateArray,unCheckBoxStateArray,formStateModel);
                                        }
                                    });
                                    formItemViewHolder.formFlowLayout.addView(checkBox);
                                }
                            }
                        } else {
                            formItemViewHolder.formFlowLayout.setVisibility(View.GONE);
                        }
                    } else if (payloadModel.isTypeDateTime()) {
                        KmFormPayloadModel.DateTimePicker dateTimePickerModel = payloadModel.getDatePickerModel();
                        if (dateTimePickerModel != null) {
                            setFormLabelText(formItemViewHolder, dateTimePickerModel.getLabel());
                            handleItemVisibility(formItemViewHolder, formItemViewHolder.formDatePicker);
                            formItemViewHolder.formDatePicker.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                                    KmFormPayloadModel.Type.TIME.getValue().equals(payloadModel.getType())
                                            ? R.drawable.ic_query_builder_black_18dp
                                            : R.drawable.ic_calendar_today_black_18dp,
                                    0);
                            formItemViewHolder.formDatePicker.setText(getFormattedDateByType(payloadModel.getType(), dateFieldArray.get(position), dateTimePickerModel.isAmPm()));
                        }
                    } else if (payloadModel.isTypeDropdown()) {
                        final KmFormPayloadModel.DropdownList dropdownList = payloadModel.getDropdownList();
                        if (dropdownList != null) {
                            setFormLabelText(formItemViewHolder, dropdownList.getTitle());
                            handleItemVisibility(formItemViewHolder, formItemViewHolder.formDropdownListContainer);
                            if (dropdownList.getOptions() != null) {
                                
                                if (validationArray.get(position) == 1) {
                                    formItemViewHolder.formValidationText.setVisibility(View.VISIBLE);
                                    formItemViewHolder.formValidationText.setText(dropdownList.getValidation().getErrorText());
                                } else {
                                    formItemViewHolder.formValidationText.setVisibility(View.GONE);
                                }

                                KmDropdownItemAdapter dropdownItemAdapter = new KmDropdownItemAdapter(context, android.R.layout.simple_spinner_item, dropdownList.getOptions());

                                formItemViewHolder.formDropdownList.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                    @Override
                                    public void onGlobalLayout() {
                                        Spinner spinner = formItemViewHolder.formDropdownList;
                                        spinner.setDropDownVerticalOffset(spinner.getHeight());
                                        spinner.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                                    }
                                });


                                formItemViewHolder.formDropdownList.setAdapter(dropdownItemAdapter);
                                dropdownItemAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                formItemViewHolder.formDropdownList.setSelection(filterDropdownList(dropdownList.getOptions()));
                                formItemViewHolder.formDropdownList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int dropdownItemPosition, long id) {
                                        dropdownFieldArray.put(position, dropdownList.getOptions().get(dropdownItemPosition));
                                        formStateModel.setDropdownFieldArray(dropdownFieldArray);
                                        KmFormStateHelper.addFormState(messageKey, formStateModel);
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {
                                    }
                                });
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void processCheckBoxSelectedState(boolean isChecked, int index, int position, String messageKey, HashSet<Integer> checkedBoxes, HashSet<Integer> unCheckedBoxes, SparseArray<HashSet<Integer>> checkBoxStateArray, SparseArray<HashSet<Integer>> unCheckBoxStateArray, KmFormStateModel formStateModel) {
        if (isChecked){
            checkedBoxes.add(index);
            unCheckedBoxes.remove(index);
        } else {
            checkedBoxes.remove(index);
            unCheckedBoxes.add(index);
        }

        checkBoxStateArray.put(position, checkedBoxes);
        unCheckBoxStateArray.put(position,unCheckedBoxes);
        formStateModel.setCheckBoxStates(checkBoxStateArray);
        formStateModel.setUncheckBoxStates(unCheckBoxStateArray);
        KmFormStateHelper.addFormState(messageKey, formStateModel);
    }

    //Returns the position of the item to be selected by default
    private int filterDropdownList(List<KmFormPayloadModel.Options> dropdownList) {
        int selectedIndex = 0;
        for (int i = 0; i < dropdownList.size(); i++) {
            if (dropdownList.get(i).isSelected()) {
                selectedIndex = i;
                break;
            }
        }
        return selectedIndex;
    }

    private void setFormLabelText(KmFormItemViewHolder formItemViewHolder, String text) {
        formItemViewHolder.formLabel.setVisibility(!TextUtils.isEmpty(text) ? View.VISIBLE : View.GONE);
        formItemViewHolder.formLabel.setText(text);
    }

    private void handleItemVisibility(KmFormItemViewHolder formItemViewHolder, View view) {
        //starting from 1 as the item at 0 is the label text
        for (int i = 1; i < formItemViewHolder.formItemRootLayout.getChildCount(); i++) {
            formItemViewHolder.formItemRootLayout.getChildAt(i).setVisibility(view.getId() == formItemViewHolder.formItemRootLayout.getChildAt(i).getId() ? View.VISIBLE : View.GONE);
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

    private void openTimePickerDialog(final int position, final boolean isAmPm, final Calendar selectedDate) {
        Calendar calendar = Calendar.getInstance();

        if (dateFieldArray.get(position) != null) {
            calendar.setTimeInMillis(dateFieldArray.get(position));
        }

        new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Calendar selectedTime = selectedDate == null ? Calendar.getInstance() : selectedDate;
                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedTime.set(Calendar.MINUTE, minute);

                dateFieldArray.put(position, selectedTime.getTimeInMillis());
                formStateModel.setDateFieldArray(dateFieldArray);
                KmFormStateHelper.addFormState(messageKey, formStateModel);
                notifyItemChanged(position);
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), !isAmPm).show();
    }

    private String getFormattedDateByType(String type, Long timeInMillis, boolean isAmPm) {
        if (KmFormPayloadModel.Type.DATE.getValue().equals(type)) {
            return timeInMillis == null ? KmDateUtils.getLocalisedDateFormat() : KmDateUtils.getFormattedDate(timeInMillis);
        } else if (KmFormPayloadModel.Type.TIME.getValue().equals(type)) {
            return timeInMillis == null ? KmDateUtils.getTimeFormat(isAmPm) : KmDateUtils.getFormattedTime(timeInMillis, isAmPm);
        } else if (KmFormPayloadModel.Type.DATE_TIME.getValue().equals(type)) {
            return timeInMillis == null ? KmDateUtils.getLocalisedDateTimeFormat(isAmPm) : KmDateUtils.getFormattedDateTime(timeInMillis, isAmPm);
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

                        try {
                            if (textField.getValidation() != null
                                    && !TextUtils.isEmpty(textField.getValidation().getRegex())
                                    && !Pattern.compile(textField.getValidation().getRegex()).matcher(enteredText).matches()) {
                                validationArray.put(i, INVALID_DATA);
                                isValid = false;
                            } else {
                                validationArray.put(i, VALID_DATA);
                            }
                        } catch (PatternSyntaxException exception) {
                            exception.printStackTrace();
                            KmToast.error(context, R.string.invalid_regex_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                } else if (KmFormPayloadModel.Type.TEXTAREA.getValue().equals(payloadList.get(i).getType())) {
                    KmFormPayloadModel.TextArea textField = payloadList.get(i).getTextAreaModel();
                    if (textField != null) {

                        String enteredText;
                        if (KmFormStateHelper.getFormState(messageKey) == null || TextUtils.isEmpty(KmFormStateHelper.getFormState(messageKey).getTextAreaFields().get(i))) {
                            enteredText = "";
                        } else {
                            enteredText = KmFormStateHelper.getFormState(messageKey).getTextAreaFields().get(i);
                        }

                        try {
                            if (textField.getValidation() != null
                                    && !TextUtils.isEmpty(textField.getValidation().getRegex())
                                    && !Pattern.compile(textField.getValidation().getRegex()).matcher(enteredText).matches()) {
                                validationArray.put(i, INVALID_DATA);
                                isValid = false;
                            } else {
                                validationArray.put(i, VALID_DATA);
                            }
                        } catch (PatternSyntaxException exception) {
                            exception.printStackTrace();
                            KmToast.error(context, R.string.invalid_regex_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                } else if (KmFormPayloadModel.Type.DROPDOWN.getValue().equals(payloadList.get(i).getType())) {
                    KmFormPayloadModel.DropdownList dropdownList = payloadList.get(i).getDropdownList();
                    if (dropdownList != null && KmFormStateHelper.getFormState(messageKey) != null
                            && KmFormStateHelper.getFormState(messageKey).getDropdownFieldArray().get(i) != null
                            && KmFormStateHelper.getFormState(messageKey).getDropdownFieldArray().get(i).isDisabled()) {
                        validationArray.put(i, INVALID_DATA);
                        isValid = false;
                    } else {
                        validationArray.put(i, VALID_DATA);
                    }
                } else if (KmFormPayloadModel.Type.HIDDEN.getValue().equals(payloadList.get(i).getType())) {
                    KmFormPayloadModel.Hidden hiddenModel = payloadList.get(i).getHiddenModel();
                    if (hiddenModel != null && !hiddenFields.containsKey(hiddenModel.getName())) {
                        hiddenFields.put(hiddenModel.getName(), hiddenModel.getValue());

                    }
                    formStateModel.setHiddenFields(hiddenFields);
                }
                formStateModel.setValidationArray(validationArray);
                KmFormStateHelper.addFormState(messageKey, formStateModel);
            }
        }
        notifyDataSetChanged();
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
                    || KmFormPayloadModel.Type.PASSWORD.getValue().equals(payloadList.get(position).getType())
                    || KmFormPayloadModel.Type.TEXTAREA.getValue().equals(payloadList.get(position).getType())) {
                return VIEW_TYPE_TEXT_FIELD;
            } else if (KmFormPayloadModel.Type.RADIO.getValue().equals(payloadList.get(position).getType())
                    || KmFormPayloadModel.Type.CHECKBOX.getValue().equals(payloadList.get(position).getType())) {
                return VIEW_TYPE_SELECTION;
            } else if (KmFormPayloadModel.Type.DATE.getValue().equals(payloadList.get(position).getType())
                    || KmFormPayloadModel.Type.TIME.getValue().equals(payloadList.get(position).getType())
                    || KmFormPayloadModel.Type.DATE_TIME.getValue().equals(payloadList.get(position).getType())) {
                return VIEW_TYPE_DATETIME;
            } else if (KmFormPayloadModel.Type.DROPDOWN.getValue().equals(payloadList.get(position).getType())) {
                return VIEW_TYPE_DROPDOWN;
            }
        }
        return 0;
    }

    public boolean isViewTypeField(String type) {
        return !(KmFormPayloadModel.Type.HIDDEN.getValue().equals(type) || KmRichMessage.SUBMIT_BUTTON.equals(type));
    }

    public class KmFormItemViewHolder extends RecyclerView.ViewHolder {

        TextView formLabel;
        EditText formEditText;
        LinearLayout formItemRootLayout;
        KmFlowLayout formFlowLayout;
        TextView formDatePicker;
        Spinner formDropdownList;
        FrameLayout formDropdownListContainer;
        TextView formValidationText;

        public KmFormItemViewHolder(@NonNull View itemView) {
            super(itemView);

            formLabel = itemView.findViewById(R.id.km_form_label_text);
            formEditText = itemView.findViewById(R.id.km_form_edit_text);
            formItemRootLayout = itemView.findViewById(R.id.km_form_item_root_layout);
            formFlowLayout = itemView.findViewById(R.id.km_form_selection_layout);
            formDatePicker = itemView.findViewById(R.id.km_form_date_picker);
            formDropdownList = itemView.findViewById(R.id.km_form_dropdown_list);
            formDropdownListContainer = itemView.findViewById(R.id.km_form_dropdown_list_container);
            formValidationText = itemView.findViewById(R.id.km_form_validation_text);

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
                            if (payloadList.get(getAdapterPosition()).isTypeTextArea()) {
                                textAreaFieldArray.put(getAdapterPosition(), text);
                                formStateModel.setTextAreaFields(textAreaFieldArray);
                            } else {
                                textFieldArray.put(getAdapterPosition(), text);
                                formStateModel.setTextFields(textFieldArray);
                            }

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

        public EditText getTextAreaEditField() {
            formEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            return formEditText;
        }
    }
}
