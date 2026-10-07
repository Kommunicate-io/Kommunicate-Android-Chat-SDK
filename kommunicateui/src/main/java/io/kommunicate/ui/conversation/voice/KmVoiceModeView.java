package io.kommunicate.ui.conversation.voice;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.kommunicate.ui.R;

/** Renders voice-mode state while keeping voice orchestration outside the view. */
public class KmVoiceModeView extends FrameLayout {
    public interface ActionListener {
        void onMicrophoneClicked();

        void onCloseClicked();
    }

    private ImageButton microphoneButton;
    private ActionListener actionListener;

    public KmVoiceModeView(@NonNull Context context) {
        super(context);
    }

    public KmVoiceModeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public KmVoiceModeView(@NonNull Context context,
                           @Nullable AttributeSet attrs,
                           int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        microphoneButton = findViewById(R.id.km_voice_microphone_button);
        ImageButton closeButton = findViewById(R.id.km_voice_close_button);

        microphoneButton.setOnClickListener(view -> {
            if (actionListener != null) {
                actionListener.onMicrophoneClicked();
            }
        });
        closeButton.setOnClickListener(view -> {
            if (actionListener != null) {
                actionListener.onCloseClicked();
            }
        });
    }

    public void setActionListener(@Nullable ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void showMode() {
        if (getVisibility() != View.VISIBLE) {
            setState(KmVoiceModeController.State.IDLE);
        }
        setVisibility(View.VISIBLE);
    }

    public void hideMode() {
        setVisibility(View.GONE);
    }

    public boolean isModeVisible() {
        return getVisibility() == View.VISIBLE;
    }

    public void showError() {
        microphoneButton.setAlpha(0.7f);
    }

    public void setState(@NonNull KmVoiceModeController.State state) {
        microphoneButton.setAlpha(state == KmVoiceModeController.State.LISTENING ? 1f : 0.7f);
    }
}
