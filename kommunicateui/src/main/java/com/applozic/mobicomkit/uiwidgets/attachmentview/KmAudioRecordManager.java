package com.applozic.mobicomkit.uiwidgets.attachmentview;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.widget.Toast;

import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.KmAudioSampler;
import com.applozic.mobicommons.commons.core.utils.PermissionsUtils;
import com.applozic.mobicommons.commons.core.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Rahul-PC on 17-07-2017.
 */

public class KmAudioRecordManager implements MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener {

    private static final String TAG = "KmAudioRecordManager";
    public static final int SAMPLING_RATE = 44100;
    public static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    public static final int CHANNEL_IN_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLING_RATE, CHANNEL_IN_CONFIG, AUDIO_FORMAT);
    FragmentActivity context;
    ConversationUIService conversationUIService;
    private AudioRecord audioRecorder;
    private String outputFile = null;
    private boolean isRecording;
    byte[] audioData = null;
    private Thread recordingThread = null;
    int bufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    int bytesPerElement = 2; // 2 bytes in 16bit format
    private static final String AUDIO_TAG = "AUD_";
    private String FILE_FORMAT = ".pcm";


    public KmAudioRecordManager(FragmentActivity context) {
        this.conversationUIService = new ConversationUIService(context);
        this.context = context;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public void prepareDefaultFileData() {
        String audioFileName = AUDIO_TAG + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + FILE_FORMAT;
        setOutputFile(FileClientService.getFilePath(audioFileName, context.getApplicationContext(), "audio/m4a").getAbsolutePath());
    }

    public void recordAudio() {
        try {
            if (audioRecorder != null) {
                return;
            }

            if (isRecording) {
                stopRecording();
            }

            if (PermissionsUtils.isAudioRecordingPermissionGranted(context)) {
                prepareDefaultFileData();
                audioRecorder = new AudioRecord(AUDIO_SOURCE,
                        SAMPLING_RATE, CHANNEL_IN_CONFIG,
                        AUDIO_FORMAT, bufferElements2Rec * bytesPerElement);

                audioData = new byte[BUFFER_SIZE];
                audioRecorder.startRecording();
                isRecording = true;

                createRecordingThread();
                recordingThread.start();
            } else {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.RECORD_AUDIO},
                            10);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] short2byte(short[] sData) {
        int shortArrSize = sData.length;
        byte[] bytes = new byte[shortArrSize * 2];
        for (int i = 0; i < shortArrSize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;
    }

    private void createRecordingThread() {
        recordingThread = new Thread(new Runnable() {
            public void run() {
                short[] audioData = new short[bufferElements2Rec];
                FileOutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(outputFile);
                } catch (FileNotFoundException e) {
                    Utils.printLog(context, TAG, "File not found for recording " + e.getLocalizedMessage());
                }
                while (isRecording) {
                    int status = audioRecorder.read(audioData, 0, bufferElements2Rec);

                    if (status == AudioRecord.ERROR_INVALID_OPERATION || status == AudioRecord.ERROR_BAD_VALUE) {
                        Utils.printLog(context, TAG, "Error reading audio data!");
                        return;
                    }

                    try {
                        byte[] byteData = short2byte(audioData);
                        outputStream.write(byteData, 0, bufferElements2Rec * bytesPerElement);
                    } catch (IOException e) {
                        Utils.printLog(context, TAG, "Error saving recording " + e.getLocalizedMessage());
                        return;
                    }
                }

                try {
                    outputStream.close();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
    }

    public void cancelAudio() {
        if (isRecording) {
            stopRecording();
        }

        if (outputFile != null) {
            File file = new File(outputFile);
            if (file != null && file.exists()) {
                Utils.printLog(context, TAG, "File deleted...");
                file.delete();
            }
        }
    }

    public void sendAudio() {
        //IF recording is running stoped it ...
        if (isRecording) {
            stopRecording();
        }

        //FILE CHECK ....

        if (outputFile != null) {
            if (!(new File(outputFile).exists())) {
                Toast.makeText(context, R.string.tap_on_mic_button_to_record_audio, Toast.LENGTH_SHORT).show();
                return;
            }
            String destFilePath = outputFile.replace("pcm", "wav");
            KmAudioSampler.copyWaveFile(outputFile, destFilePath, BUFFER_SIZE);
            File file = new File(outputFile);
            if (file.exists()) {
                file.delete();
            }
            conversationUIService.sendAudioMessage(destFilePath);
        }

    }

    public void stopRecording() {
        if (audioRecorder != null) {
            try {
                audioRecorder.stop();
                recordingThread = null;
            } catch (RuntimeException stopException) {
                Utils.printLog(context, TAG, "Runtime exception.This is thrown intentionally if stop is called just after start");
            } finally {
                audioRecorder.release();
                audioRecorder = null;
                isRecording = false;
            }
        }
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {

    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {

    }
}