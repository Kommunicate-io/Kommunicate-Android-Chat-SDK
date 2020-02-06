package com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models;

import android.util.SparseArray;
import android.util.SparseIntArray;

import com.applozic.mobicommons.json.JsonMarker;

import java.util.HashSet;

public class KmFormStateModel extends JsonMarker {

    private SparseArray<String> textFields;
    private SparseIntArray selectedRadioButtonIndex;
    private SparseArray<HashSet<Integer>> checkBoxStates;

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
}
