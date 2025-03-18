package com.applozic.mobicomkit.uiwidgets.kommunicate.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.kommunicate.KmPrefSettings;
import com.applozic.mobicomkit.uiwidgets.kommunicate.adapters.KmLanguageSelectionAdapter;
import com.applozic.mobicomkit.uiwidgets.kommunicate.callbacks.KmClickHandler;
import com.applozic.mobicomkit.uiwidgets.kommunicate.models.KmSpeechToTextModel;
import io.kommunicate.commons.commons.core.utils.Utils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.kommunicate.KmSettings;
/**
 * This UI class is for switching between multiple languages
 * It can only be used if multiple languages are set by end-user from @KmSpeechToTextSetting
 *
 * @author Aman
 * @date February '23
 */
public class KmLanguageSlideView extends BottomSheetDialogFragment implements KmClickHandler<KmSpeechToTextModel> {
    private static final String TAG = "KmLanguageSlideView";
    private KmLanguageSelectionAdapter kmLanguageSelectionAdapter;
    private RecyclerView languageRecyclerView;
    private List<KmSpeechToTextModel> languages;
    private ImageButton dismissButton;
    private ConversationUIService conversationUIService;


    public KmLanguageSlideView(ConversationUIService conversationUIService, List<KmSpeechToTextModel> languages) {
        this.languages = languages;
        this.conversationUIService = conversationUIService;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.km_language_change_view, container, false);
        languageRecyclerView = view.findViewById(R.id.km_language_recycler_view);
        dismissButton = view.findViewById(R.id.dismiss_button);

        kmLanguageSelectionAdapter = new KmLanguageSelectionAdapter(getContext(), languages, this);
        languageRecyclerView.setAdapter(kmLanguageSelectionAdapter);
        languageRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissAllowingStateLoss();
            }
        });
        return view;
    }

    public static String getFragTag() {
        return TAG;
    }

    @Override
    public void onItemClicked(View view, KmSpeechToTextModel data) {
        if(data != null && getContext() != null) {
            KmToast.makeText(getContext(), getContext().getString(R.string.changed_language_to, data.getName()), Toast.LENGTH_SHORT).show();
            dismissAllowingStateLoss();
            KmSettings.updateUserLanguage(getContext(), data.getCode());
            KmPrefSettings.getInstance(getContext()).setSpeechToTextLanguage(data.getCode());
            if(data.isSendMessageOnClick()) {
                conversationUIService.sendMessage(data.getMessageToSend());
            }
        } else {
            Utils.printLog(getContext(), TAG, "Failed to change language");
        }
    }
}
