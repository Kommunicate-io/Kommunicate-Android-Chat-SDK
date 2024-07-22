package com.applozic.mobicomkit.uiwidgets.attachmentview;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.kommunicate.views.KmToast;
import com.applozic.mobicommons.ApplozicService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Rahul-PC on 28-02-2017.
 */

public class KommunicateAudioManager implements AudioManager.OnAudioFocusChangeListener {
    private static KommunicateAudioManager myObj;
    private static final int MAX_SIZE = 5;
    private KmDocumentView currentView;
    private Map<String, MediaPlayer> pool = new HashMap<>();
    private Context context;
    private AudioManager audioManager;
    private int minute, second;

    private KommunicateAudioManager(Context context) {
        this.context = ApplozicService.getContext(context);
    }

    public static KommunicateAudioManager getInstance(Context context) {
        if (myObj == null) {
            myObj = new KommunicateAudioManager(ApplozicService.getContext(context));
        }
        return myObj;
    }

    void play(final Uri uri, final KmDocumentView view) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }

        final String key = view.message.getKeyString();
        MediaPlayer mp = pool.get(key);
        if (mp != null) {
            if (mp.isPlaying()) {
                mp.pause();
                return;
            } else {
                mp.seekTo(mp.getCurrentPosition());
                if (requestAudioFocus()) {
                    mp.start();
                }
            }
        } else {
            mp = new MediaPlayer();
            if (pool.size() >= MAX_SIZE) {
                Map.Entry<String, MediaPlayer> entry = pool.entrySet().iterator().next();
                String first = entry.getKey();
                pool.remove(first);
            }
            pool.put(key, mp);
        }
        pauseOthersifPlaying();
        mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                KmToast.error(context, context.getString(R.string.unable_to_play_requested_audio_file), Toast.LENGTH_LONG).show();
                return false;
            }
        });
        try {
            if (context != null) {
                mp.setDataSource(context, uri);
                if (requestAudioFocus()) {
                    mp.prepare();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mp.start();
        if (currentView != null) {
            currentView.setAudioIcons();
        }
        this.currentView = view;
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
                pool.remove(key);
                currentView.setAudioIcons();
                updateAudioDuration(view.audio_duration_textView, uri.getPath());
            }
        });

        currentView.audioseekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int duration = progress / 1000;
                int min = duration / 60;
                int seconds = duration % 60;
                if (view != null && view.audio_duration_textView != null) {
                    view.audio_duration_textView.setText(String.format("%02d:%02d", min, seconds));
                }

                if (fromUser) {
                    if (getMediaPlayer(key) != null) {
                        getMediaPlayer(key).seekTo(progress);
                    }
                }
            }
        });
    }

    public void pauseOthersifPlaying() {
        MediaPlayer m;
        Iterator it = pool.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            {
                m = (MediaPlayer) pair.getValue();
                if (m.isPlaying()) {
                    m.pause();
                }
            }
        }
    }

    private void pauseIfPlaying() {
        MediaPlayer m;
        Iterator it = pool.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            {
                m = (MediaPlayer) pair.getValue();
                if (m.isPlaying()) {
                    m.pause();
                }
            }
        }
    }

    int getAudioState(String key) {
        MediaPlayer mp = pool.get(key);
        if (mp != null) {
            if (mp.isPlaying()) {
                return 1;
            }
            return 0;
        }
        return -1;
    }

    public MediaPlayer getMediaPlayer(String key) {
        if (key == null) {
            return null;
        }
        return pool.get(key);
    }

    public void audiostop() {
        if (pool != null) {
            MediaPlayer temp;
            Iterator it = pool.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                {
                    temp = (MediaPlayer) pair.getValue();
                    temp.stop();
                    temp.release();
                }
            }
            pool.clear();
        }
    }

    public String refreshAudioDuration(String filePath) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            int duration = mediaPlayer.getDuration();
            duration = duration / 1000;
            minute = duration / 60;
            second = (duration % 60) + 1;
            mediaPlayer.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.format("%02d:%02d", minute, second);
    }

    public void updateAudioDuration(final TextView durationTextView, String filePath) {
        MediaPlayer mediaPlayer = new MediaPlayer();

        if (durationTextView == null || filePath == null) {
            return;
        }

        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    int currentProgress = mediaPlayer.getDuration() / 1000;
                    int minutes = currentProgress / 60;
                    int seconds = (currentProgress % 60);
                    if (durationTextView != null) {
                        durationTextView.setText(String.format("%02d:%02d", minutes, seconds));
                    }
                    mediaPlayer.release();
                }
            });
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAudioFocusChange(int i) {
        switch (i) {
            case AudioManager.AUDIOFOCUS_GAIN:
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                pauseIfPlaying();
                if (currentView != null) {
                    currentView.setAudioIcons();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                pauseIfPlaying();
                if (currentView != null) {
                    currentView.setAudioIcons();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                pauseIfPlaying();
                if (currentView != null) {
                    currentView.setAudioIcons();
                }
                break;
        }
    }

    private boolean requestAudioFocus() {
        boolean gotFocus;
        int audioFocus = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        if (audioFocus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            gotFocus = true;
        } else {
            gotFocus = false;
        }
        return gotFocus;
    }
}