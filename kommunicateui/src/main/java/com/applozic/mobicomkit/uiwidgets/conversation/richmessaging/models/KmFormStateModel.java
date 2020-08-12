package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models;

import android.util.SparseArray;
import android.util.SparseIntArray;

import com.applozic.mobicommons.json.JsonMarker;

import java.util.HashSet;
import java.util.Map;

public class KmFormStateModel extends JsonMarker {

    private SparseArray<String> textFields;
    private SparseIntArray selectedRadioButtonIndex;
    private SparseArray<HashSet<Integer>> checkBoxStates;
    private Map<String, String> hiddenFields;
    private SparseIntArray validationArray;
    private SparseArray<Long> dateFieldArray;

    public SparseArray<String> getTextFields() {
        return textFields;
    }

    public void setTextFields(SparseArray<String> textFields) {
        this.textFields = textFields;
    }

    public SparseIntArray getSelectedRadioButtonIndex() {
        return selectedRadioButtonIndex;
    }

    public void setSelectedRadioButtonIndex(SparseIntArray selectedRadioButtonIndex) {
        this.selectedRadioButtonIndex = selectedRadioButtonIndex;
    }

    public SparseArray<HashSet<Integer>> getCheckBoxStates() {
        return checkBoxStates;
    }

    public void setCheckBoxStates(SparseArray<HashSet<Integer>> checkBoxStates) {
        this.checkBoxStates = checkBoxStates;
    }

    public Map<String, String> getHiddenFields() {
        return hiddenFields;
    }

    public void setHiddenFields(Map<String, String> hiddenFields) {
        this.hiddenFields = hiddenFields;
    }

    public SparseIntArray getValidationArray() {
        return validationArray;
    }

    public void setValidationArray(SparseIntArray validationArray) {
        this.validationArray = validationArray;
    }

    public SparseArray<Long> getDateFieldArray() {
        return dateFieldArray;
    }

    public void setDateFieldArray(SparseArray<Long> dateFieldArray) {
        this.dateFieldArray = dateFieldArray;
    }
}
