package com.applozic.mobicomkit.uiwidgets.conversation.viewmodel;

import androidx.lifecycle.ViewModel;

import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;

public class KmViewModel extends ViewModel {

    protected AlCustomizationSettings alCustomizationSettings;

    public KmViewModel(AlCustomizationSettings alCustomizationSettings) {
        this.alCustomizationSettings = alCustomizationSettings;
    }
}
