package com.applozic.mobicomkit.uiwidgets.attachmentview;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.widget.Toast;

import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.instruction.ApplozicPermissions;
import com.applozic.mobicomkit.uiwidgets.kommunicate.views.KmToast;
import com.applozic.mobicomkit.uiwidgets.uilistener.KmStoragePermission;
import com.applozic.mobicomkit.uiwidgets.uilistener.KmStoragePermissionListener;
import com.applozic.mobicommons.commons.core.utils.PermissionsUtils;
import com.applozic.mobicommons.commons.core.utils.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Rahul-PC on 17-07-2017.
 */

public class ApplozicAudioRecordManager implements MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener {

    private FragmentActivity context;
    private ConversationUIService conversationUIService;
    private MediaRecorder audioRecorder;
    private String outputFile = null;
    private boolean isRecording;
    private static final String AUDIO_TAG = "AUD_";
    private static final String FILE_FORMAT = ".m4a";
    private static final String TAG = "AudioRecordManager";


    public ApplozicAudioRecordManager(FragmentActivity context) {
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

    public String getOutputFileName() {
        return outputFile;
    }

    public void recordAudio() {
        if (((KmStoragePermissionListener) context).isPermissionGranted()) {
            startRecording();
        } else {
            ((KmStoragePermissionListener) context).checkPermission(new KmStoragePermission() {
                @Override
                public void onAction(boolean didGrant) {
                    if (didGrant) {
                        startRecording();
                    }
                }
            });
        }
    }

    private void startRecording() {
        try {
            if (PermissionsUtils.isAudioRecordingPermissionGranted(context)) {
                prepareDefaultFileData();
                if (isRecording) {
                    stopRecording();
                } else {
                    if (audioRecorder == null) {
                        prepareMediaRecorder();
                    }
                    audioRecorder.prepare();
                    audioRecorder.start();
                    isRecording = true;
                }
            } else {
                requestAudioPermission();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean cancelAudio() {
        if (isRecording) {
            ApplozicAudioRecordManager.this.stopRecording();
        }

        if (outputFile != null) {
            File file = new File(outputFile);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    Utils.printLog(context, TAG, "File deleted...");
                }
                return deleted;
            }
        }
        return false;
    }

    public void sendAudio() {
        if (isRecording) {
            stopRecording();
        }

        if (outputFile != null) {
            if (!(new File(outputFile).exists())) {
                KmToast.makeText(context, context.getString(R.string.km_audio_record_toast_message), Toast.LENGTH_SHORT).show();
                return;
            }
            conversationUIService.sendAudioMessage(outputFile);
        }
    }

    public void stopRecording() {

        if (audioRecorder != null) {
            try {
                audioRecorder.stop();
            } catch (RuntimeException stopException) {
                Utils.printLog(context, TAG, "Runtime exception.This is thrown intentionally if stop is called just after start");
            } finally {
                audioRecorder.release();
                audioRecorder = null;
                isRecording = false;
            }
        }
    }

    public MediaRecorder prepareMediaRecorder() {

        audioRecorder = new MediaRecorder();
        audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        audioRecorder.setAudioEncodingBitRate(256);
        audioRecorder.setAudioChannels(1);
        audioRecorder.setAudioSamplingRate(44100);
        audioRecorder.setOutputFile(outputFile);
        audioRecorder.setOnInfoListener(this);
        audioRecorder.setOnErrorListener(this);

        return audioRecorder;
    }


    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {

    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {

    }

    private void requestAudioPermission() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.RECORD_AUDIO},
                    10);
        }
    }
}