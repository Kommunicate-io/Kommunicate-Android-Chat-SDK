package com.applozic.mobicomkit.uiwidgets.conversation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;

public class KmViewModelFactory implements ViewModelProvider.Factory {
    private AlCustomizationSettings alCustomizationSettings;

    public KmViewModelFactory(AlCustomizationSettings alCustomizationSettings) {
        this.alCustomizationSettings = alCustomizationSettings;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.equals(KmResolveViewModel.class)) {
            return (T) new KmResolveViewModel(alCustomizationSettings);
        }
        return null;
    }
}
