package io.kommunicate.ui.conversation.voice;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Coordinates the non-UI voice conversation flow. */
public class KmVoiceModeController {
    private static final String TAG = "KmVoiceMode";
    public enum State {
        IDLE,
        LISTENING,
        TRANSCRIBING,
        SENDING,
        WAITING_FOR_RESPONSE,
        PROCESSING_RESPONSE,
        SPEAKING,
        ERROR
    }

    public interface Listener {
        void onStateChanged(@NonNull State state);

        void onTranscriptReady(@NonNull String transcript);

        void onError(@NonNull Exception exception);
    }

    private static final int MAX_PROCESSED_MESSAGE_IDS = 100;
    private static final int RECORDER_RETRY_DELAY_MS = 50;

    private final Context context;
    private final Listener listener;
    private final KmVoiceApiClient apiClient;
    private final KmVoiceAudioRecorder audioRecorder;
    private final KmVoicePlaybackManager playbackManager;
    private final AudioManager audioManager;
    private final AudioAttributes audioAttributes;
    private final AudioManager.OnAudioFocusChangeListener focusChangeListener;
    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Set<String> processedMessageIds = new LinkedHashSet<>();

    private volatile boolean active;
    private long conversationId;
    private int sessionGeneration;
    private AudioFocusRequest audioFocusRequest;

    public KmVoiceModeController(@NonNull Context context, @NonNull Listener listener) {
        this(context, listener, new KmVoiceApiClient());
    }

    KmVoiceModeController(@NonNull Context context,
                          @NonNull Listener listener,
                          @NonNull KmVoiceApiClient apiClient) {
        this.context = context.getApplicationContext();
        this.listener = listener;
        this.apiClient = apiClient;
        this.audioManager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
        this.audioAttributes = new AudioAttributes.Builder()
                .setUsage(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? AudioAttributes.USAGE_ASSISTANT
                        : AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();
        this.focusChangeListener = focusChange -> {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS ||
                    focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                    focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                runOnMain(() -> {
                    if (active) {
                        Log.d(TAG, "voice_session_stopped audioFocusLoss=" + focusChange);
                        stop();
                    }
                });
            }
        };
        this.playbackManager = new KmVoicePlaybackManager(this.context, audioAttributes);
        this.audioRecorder = new KmVoiceAudioRecorder(new KmVoiceAudioRecorder.Listener() {
            @Override
            public void onAudioCaptured(@NonNull byte[] pcmAudio) {
                transcribe(pcmAudio, sessionGeneration);
            }

            @Override
            public void onNoSpeech() {
                runOnMain(() -> beginListening(sessionGeneration));
            }

            @Override
            public void onError(@NonNull Exception exception) {
                fail(exception, sessionGeneration);
            }
        });
    }

    public boolean start(long conversationId) {
        if (active) {
            return true;
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            SecurityException exception =
                    new SecurityException("Audio recording permission is required");
            Log.e(TAG, "voice_session_start_failed", exception);
            notifyError(exception);
            return false;
        }
        if (!requestAudioFocus()) {
            IllegalStateException exception =
                    new IllegalStateException("Unable to obtain audio focus");
            Log.e(TAG, "voice_session_start_failed", exception);
            notifyError(exception);
            return false;
        }
        this.conversationId = conversationId;
        processedMessageIds.clear();
        active = true;
        sessionGeneration++;
        audioRecorder.resetNoiseCalibration();
        Log.d(TAG, "voice_session_started conversationId=" + conversationId);
        beginListening(sessionGeneration);
        return true;
    }

    public void stop() {
        boolean wasActive = active;
        active = false;
        sessionGeneration++;
        audioRecorder.stop();
        apiClient.cancelActiveRequest();
        playbackManager.stop();
        abandonAudioFocus();
        processedMessageIds.clear();
        setState(State.IDLE);
        if (wasActive) {
            Log.d(TAG, "voice_session_stopped");
        }
    }

    public void release() {
        stop();
        mainHandler.removeCallbacksAndMessages(null);
        networkExecutor.shutdownNow();
    }

    public boolean isActive() {
        return active;
    }

    public boolean onBotMessage(String messageId, String text) {
        if (!active || text == null || text.trim().isEmpty()) {
            return false;
        }
        String normalizedMessageId = messageId == null ? "" : messageId;
        if (!normalizedMessageId.isEmpty() && processedMessageIds.contains(normalizedMessageId)) {
            return false;
        }
        if (!normalizedMessageId.isEmpty()) {
            rememberMessageId(normalizedMessageId);
        }

        Log.d(TAG, "bot_response_received textLength=" + text.trim().length());
        int generation = sessionGeneration;
        audioRecorder.stop();
        setState(State.PROCESSING_RESPONSE);
        networkExecutor.execute(() -> {
            if (!isCurrentSession(generation)) {
                return;
            }
            try {
                KmVoiceApiClient.AudioResponse response = apiClient.synthesize(text.trim());
                runOnMain(() -> {
                    if (!isCurrentSession(generation)) {
                        return;
                    }
                    setState(State.SPEAKING);
                    playbackManager.play(response, new KmVoicePlaybackManager.Listener() {
                        @Override
                        public void onCompleted() {
                            beginListening(generation);
                        }

                        @Override
                        public void onError(@NonNull Exception exception) {
                            recover(exception, generation);
                        }
                    });
                });
            } catch (Exception exception) {
                recover(exception, generation);
            }
        });
        return true;
    }

    private void beginListening(int generation) {
        if (!isCurrentSession(generation) || audioRecorder.isRecording()) {
            return;
        }
        try {
            if (audioRecorder.start()) {
                setState(State.LISTENING);
            } else if (isCurrentSession(generation)) {
                mainHandler.postDelayed(
                        () -> beginListening(generation),
                        RECORDER_RETRY_DELAY_MS
                );
            }
        } catch (Exception exception) {
            fail(exception, generation);
        }
    }

    private void transcribe(byte[] pcmAudio, int generation) {
        if (!isCurrentSession(generation)) {
            return;
        }
        setState(State.TRANSCRIBING);
        networkExecutor.execute(() -> {
            if (!isCurrentSession(generation)) {
                return;
            }
            try {
                String transcript = apiClient.transcribe(pcmAudio, conversationId);
                runOnMain(() -> {
                    if (!isCurrentSession(generation)) {
                        return;
                    }
                    if (transcript.isEmpty()) {
                        beginListening(generation);
                        return;
                    }
                    setState(State.SENDING);
                    listener.onTranscriptReady(transcript);
                    setState(State.WAITING_FOR_RESPONSE);
                });
            } catch (Exception exception) {
                recover(exception, generation);
            }
        });
    }

    private void recover(Exception exception, int generation) {
        runOnMain(() -> {
            if (!isCurrentSession(generation)) {
                return;
            }
            Log.e(TAG, "voice_session_recoverable_error", exception);
            setState(State.ERROR);
            listener.onError(exception);
            beginListening(generation);
        });
    }

    private void notifyError(Exception exception) {
        runOnMain(() -> listener.onError(exception));
    }

    private void fail(Exception exception, int generation) {
        Log.e(TAG, "voice_session_failed", exception);
        runOnMain(() -> {
            if (!isCurrentSession(generation)) {
                return;
            }
            active = false;
            sessionGeneration++;
            audioRecorder.stop();
            apiClient.cancelActiveRequest();
            playbackManager.stop();
            abandonAudioFocus();
            processedMessageIds.clear();
            updateState(State.ERROR);
            listener.onError(exception);
        });
    }

    private boolean isCurrentSession(int generation) {
        return active && sessionGeneration == generation;
    }

    private boolean requestAudioFocus() {
        if (audioManager == null) {
            return false;
        }
        int result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = new AudioFocusRequest.Builder(
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
            )
                    .setAudioAttributes(audioAttributes)
                    .setOnAudioFocusChangeListener(focusChangeListener)
                    .build();
            result = audioManager.requestAudioFocus(audioFocusRequest);
        } else {
            result = audioManager.requestAudioFocus(
                    focusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
            );
        }
        boolean granted = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        if (!granted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = null;
        }
        return granted;
    }

    private void abandonAudioFocus() {
        if (audioManager == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (audioFocusRequest != null) {
                audioManager.abandonAudioFocusRequest(audioFocusRequest);
                audioFocusRequest = null;
            }
        } else {
            audioManager.abandonAudioFocus(focusChangeListener);
        }
    }

    private void setState(@NonNull State nextState) {
        int generation = sessionGeneration;
        runOnMain(() -> {
            if (nextState == State.IDLE) {
                if (active || sessionGeneration != generation) {
                    return;
                }
            } else if (!isCurrentSession(generation)) {
                return;
            }
            updateState(nextState);
        });
    }

    private void updateState(@NonNull State nextState) {
        Log.d(TAG, "state_changed state=" + nextState.name());
        listener.onStateChanged(nextState);
    }

    private void rememberMessageId(String messageId) {
        processedMessageIds.add(messageId);
        if (processedMessageIds.size() > MAX_PROCESSED_MESSAGE_IDS) {
            String oldestMessageId = processedMessageIds.iterator().next();
            processedMessageIds.remove(oldestMessageId);
        }
    }

    private void runOnMain(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            mainHandler.post(runnable);
        }
    }
}
