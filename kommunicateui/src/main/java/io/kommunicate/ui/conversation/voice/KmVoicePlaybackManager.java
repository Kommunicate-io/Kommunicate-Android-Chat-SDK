package io.kommunicate.ui.conversation.voice;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/** Plays binary TTS responses and owns their temporary files. */
public class KmVoicePlaybackManager {
    private static final String TAG = "KmVoiceMode";
    public interface Listener {
        void onCompleted();

        void onError(@NonNull Exception exception);
    }

    private final Context context;
    private final AudioAttributes audioAttributes;
    private MediaPlayer mediaPlayer;
    private File audioFile;
    private Listener playbackListener;

    public KmVoicePlaybackManager(@NonNull Context context,
                                  @NonNull AudioAttributes audioAttributes) {
        this.context = context.getApplicationContext();
        this.audioAttributes = audioAttributes;
    }

    public void play(@NonNull KmVoiceApiClient.AudioResponse response,
                     @NonNull Listener listener) {
        stop();
        try {
            audioFile = File.createTempFile("km_voice_response_", fileSuffix(response), context.getCacheDir());
            try (FileOutputStream outputStream = new FileOutputStream(audioFile)) {
                outputStream.write(response.getAudio());
            }

            playbackListener = listener;
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(audioAttributes);
            mediaPlayer.setDataSource(audioFile.getAbsolutePath());
            mediaPlayer.setOnPreparedListener(player -> {
                Log.d(TAG, "tts_playback_started audioBytes=" + response.getAudio().length);
                player.start();
            });
            mediaPlayer.setOnCompletionListener(player -> {
                Listener completedListener = playbackListener;
                cleanup();
                Log.d(TAG, "tts_playback_completed");
                if (completedListener != null) {
                    completedListener.onCompleted();
                }
            });
            mediaPlayer.setOnErrorListener((player, what, extra) -> {
                finishWithError(new IOException(
                        "Unable to play voice response (" + what + ", " + extra + ")"
                ));
                return true;
            });
            mediaPlayer.prepareAsync();
        } catch (Exception exception) {
            Log.e(TAG, "tts_playback_failed", exception);
            cleanup();
            listener.onError(exception);
        }
    }

    public void stop() {
        cleanup();
    }

    private void finishWithError(Exception exception) {
        Log.e(TAG, "tts_playback_failed", exception);
        Listener failedListener = playbackListener;
        cleanup();
        if (failedListener != null) {
            failedListener.onError(exception);
        }
    }

    private String fileSuffix(KmVoiceApiClient.AudioResponse response) {
        String contentType = response.getContentType();
        return contentType != null && contentType.toLowerCase().contains("wav") ? ".wav" : ".mp3";
    }

    private void cleanup() {
        playbackListener = null;
        if (mediaPlayer != null) {
            mediaPlayer.setOnPreparedListener(null);
            mediaPlayer.setOnCompletionListener(null);
            mediaPlayer.setOnErrorListener(null);
            try {
                mediaPlayer.stop();
            } catch (IllegalStateException ignored) {
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (audioFile != null) {
            //noinspection ResultOfMethodCallIgnored
            audioFile.delete();
            audioFile = null;
        }
    }
}
