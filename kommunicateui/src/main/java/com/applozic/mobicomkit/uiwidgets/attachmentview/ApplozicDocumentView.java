package com.applozic.mobicomkit.uiwidgets.attachmentview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.attachment.AttachmentManager;
import com.applozic.mobicomkit.api.attachment.AttachmentTask;
import com.applozic.mobicomkit.api.attachment.AttachmentViewProperties;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MessageIntentService;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.uilistener.KmStoragePermission;
import com.applozic.mobicomkit.uiwidgets.uilistener.KmStoragePermissionListener;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.json.GsonUtils;

import java.io.File;

import static android.view.View.GONE;

/**
 * Created by devashish on 22/07/16.
 */
public class ApplozicDocumentView {

    private static final String TAG = "ApplozicDocumentView";
    RelativeLayout mainLayout;
    View dividerView;
    TextView infoTextView, nameTextView, audioTextView;
    ImageView icon, cancelIcon;
    ProgressBar progressBar;
    Message message;
    Context context;
    AttachmentViewProperties attachmentViewProperties;
    SeekBar audioseekbar;
    Uri uri;
    String filePath, mimeType = null;
    String audio_duration;
    private AttachmentTask mDownloadThread;
    private boolean mCacheFlag = false;
    private boolean isPreview, isRetry, isDownloadProgress, isDownloaded;
    private Handler mHandler = new Handler();
    private AlCustomizationSettings alCustomizationSettings;
    private KmStoragePermissionListener kmStoragePermissionListener;

    public ApplozicDocumentView(Context context, KmStoragePermissionListener kmStoragePermissionListener) {
        this.context = context;
        this.kmStoragePermissionListener = kmStoragePermissionListener;
    }

    public void inflateViewWithMessage(View rootview, Message message, AlCustomizationSettings alCustomizationSettings) {
        this.message = message;
        this.alCustomizationSettings = alCustomizationSettings;
        mainLayout = rootview.findViewById(R.id.attachmentLayout);
        dividerView = rootview.findViewById(R.id.attachmentDivider);
        infoTextView = rootview.findViewById(R.id.attachmentInfo);
        nameTextView = rootview.findViewById(R.id.attachmentName);
        icon = rootview.findViewById(R.id.attachmentIcon);
        cancelIcon = rootview.findViewById(R.id.attachmentCancelIcon);
        audioTextView = rootview.findViewById(R.id.audio_duration_textView);
        progressBar = rootview.findViewById(R.id.applozic_doc_download_progress);
        audioseekbar = rootview.findViewById(R.id.applozic_audio_seekbar);
        if (!message.hasAttachment()) {
            return;
        }
        setBooleanViews(true, false, false, false);
        setColorResources(message, alCustomizationSettings);
        if (message.getFileMetas() != null) {
            if (message.getFileMetas().getContentType().contains("audio")) {
                setAudioIcons();
                updateApplozicSeekBar();
            } else {
                audioTextView.setVisibility(GONE);
                audioseekbar.setVisibility(GONE);
                setViewsVisibility(View.VISIBLE);
                infoTextView.setVisibility(View.GONE);
                nameTextView.setText(message.getFileMetas().getName());
            }
        } else if (message.getFilePaths() != null) {
            filePath = message.getFilePaths().get(0);
            mimeType = FileUtils.getMimeType(filePath);
            if (mimeType != null && mimeType.contains("audio")) {
                setAudioIcons();
                updateApplozicSeekBar();
            } else {
                audioTextView.setVisibility(GONE);
                audioseekbar.setVisibility(GONE);
                setViewsVisibility(View.VISIBLE);
                nameTextView.setText(new File(filePath).getName());
                infoTextView.setVisibility(View.GONE);
                icon.setImageResource(R.drawable.ic_description_white_24dp);
            }
        }
        setupAttachmentView();
        registerEvents();
        if (message.isCanceled()) {
            showRetry();
        } else if (message.isAttachmentUploadInProgress() && !message.isCanceled()) {
            showUploadingProgress();
        } else if (AttachmentManager.isAttachmentInProgress(message.getKeyString())) {
            this.mDownloadThread = AttachmentManager.getBGThreadForAttachment(message.getKeyString());
            this.mDownloadThread.setAttachementViewNew(attachmentViewProperties);
            showDownloadInProgress();
        } else if (message.isAttachmentDownloaded()) {
            showDownloaded();
            if (message.getFilePaths() != null) {
                String mimeType = FileUtils.getMimeType(message.getFilePaths().get(0));
                if (mimeType != null) {
                    if (mimeType.contains("audio")) {
                        setAudioIcons();
                        nameTextView.setVisibility(View.GONE);
                        infoTextView.setVisibility(View.GONE);
                        audioTextView.setVisibility(View.VISIBLE);
                        audioseekbar.setVisibility(View.VISIBLE);
                        icon.setVisibility(View.VISIBLE);
                    } else {
                        nameTextView.setVisibility(View.VISIBLE);
                        audioTextView.setVisibility(GONE);
                        audioseekbar.setVisibility(GONE);
                        infoTextView.setVisibility(View.GONE);
                        icon.setVisibility(View.VISIBLE);
                        icon.setImageResource(R.drawable.ic_description_white_24dp);
                    }
                }
            }
        } else {
            showPreview();
        }

        if (message.getFileMetas() != null && message.getFilePaths() == null) {
            infoTextView.setVisibility(View.VISIBLE);
            infoTextView.setText(message.getFileMetas().getSizeInReadableFormat());
            if (!(message.getFileMetas().getContentType().contains("audio"))) {
                nameTextView.setText(message.getFileMetas().getName());
                audioseekbar.setVisibility(GONE);
                audioTextView.setVisibility(GONE);
            } else {
                nameTextView.setVisibility(GONE);
                if (message.isAttachmentDownloaded()) {
                    ApplozicAudioManager.getInstance(context).updateAudioDuration(audioTextView, filePath);
                    audioTextView.setVisibility(View.VISIBLE);
                } else {
                    audioTextView.setVisibility(View.VISIBLE);
                    audioTextView.setText("00:00");
                }
                setAudioIcons();
                audioseekbar.setVisibility(View.VISIBLE);
            }
        } else {
            if (message.getFilePaths() != null) {
                filePath = message.getFilePaths().get(0);
                mimeType = FileUtils.getMimeType(filePath);
                if (mimeType != null && !(mimeType.contains("audio"))) {
                    String fileName = new File(filePath).getName();
                    nameTextView.setText(fileName);
                    audioseekbar.setVisibility(GONE);
                    audioTextView.setVisibility(GONE);
                    icon.setVisibility(View.VISIBLE);
                } else {
                    if (message.isAttachmentDownloaded()) {
                        ApplozicAudioManager.getInstance(context).updateAudioDuration(audioTextView, filePath);
                        audioTextView.setVisibility(View.VISIBLE);
                    } else {
                        audioTextView.setVisibility(View.VISIBLE);
                        audioTextView.setText("00:00");
                    }
                    nameTextView.setVisibility(GONE);
                    audioseekbar.setVisibility(View.VISIBLE);
                    setAudioIcons();
                }
            }
        }
    }

    private void setViewsVisibility(int visibility) {
        icon.setVisibility(visibility);
        dividerView.setVisibility(visibility);
        nameTextView.setVisibility(visibility);
    }

    public void setBooleanViews(boolean preview, boolean downloadProgres, boolean downloaded, boolean retry) {
        isPreview = preview;
        isDownloadProgress = downloadProgres;
        isDownloaded = downloaded;
        isRetry = retry;
    }

    private void setColorResources(Message message, AlCustomizationSettings alCustomizationSettings) {
        progressBar.getIndeterminateDrawable().setColorFilter(context.getResources().getColor(R.color.applozic_green_color), PorterDuff.Mode.MULTIPLY);
        cancelIcon.setColorFilter(message.isTypeOutbox() ? Color.parseColor(alCustomizationSettings.getSentMessageTextColor()) :
                Color.parseColor(alCustomizationSettings.getReceivedMessageTextColor()), PorterDuff.Mode.MULTIPLY);
        icon.setColorFilter(message.isTypeOutbox() ? Color.parseColor(alCustomizationSettings.getSentMessageTextColor()) :
                Color.parseColor(alCustomizationSettings.getReceivedMessageTextColor()), PorterDuff.Mode.MULTIPLY);
        dividerView.setBackgroundColor(message.isTypeOutbox() ? Color.parseColor(alCustomizationSettings.getSentMessageTextColor()) :
                Color.parseColor(alCustomizationSettings.getReceivedMessageTextColor()));
        nameTextView.setTextColor(message.isTypeOutbox() ? Color.parseColor(alCustomizationSettings.getSentMessageTextColor()) :
                Color.parseColor(alCustomizationSettings.getReceivedMessageTextColor()));
        infoTextView.setTextColor(message.isTypeOutbox() ? Color.parseColor(alCustomizationSettings.getSentMessageTextColor()) :
                Color.parseColor(alCustomizationSettings.getReceivedMessageTextColor()));
        audioTextView.setTextColor(message.isTypeOutbox() ? Color.parseColor(alCustomizationSettings.getSentMessageTextColor()) :
                Color.parseColor(alCustomizationSettings.getReceivedMessageTextColor()));
        audioseekbar.getProgressDrawable().setColorFilter(message.isTypeOutbox() ? 0xFFFFFFFF : 0xFFFFB242, PorterDuff.Mode.MULTIPLY);
    }

    private void showRetry() {
        setBooleanViews(false, false, false, true);
        if (isDownloadRequire()) {
            showPreview();
        } else {
            mainLayout.setVisibility(View.VISIBLE);
            setViewsVisibility(View.VISIBLE);
            icon.setImageResource(R.drawable.ic_outline_unarchive_white_24dp);
            infoTextView.setVisibility(View.VISIBLE);
            infoTextView.setText(R.string.attachment_retry);
            showProgress(View.GONE);
        }
    }

    private void showProgress(int visibility) {
        progressBar.setVisibility(visibility);
        cancelIcon.setVisibility(message.isTypeOutbox() ? GONE : visibility);
    }

    private void showUploadingProgress() {
        Utils.printLog(context, TAG, "showUploadingProgress :: ");
        showDownloadInProgress();
    }

    private void setupAttachmentView() {
        attachmentViewProperties = new AttachmentViewProperties(mainLayout.getWidth(), mainLayout.getHeight(), context, message);
        if (mDownloadThread == null && AttachmentManager.isAttachmentInProgress(message.getKeyString())) {
            mDownloadThread = AttachmentManager.getBGThreadForAttachment(message.getKeyString());
            if (mDownloadThread != null)
                mDownloadThread.setAttachementViewNew(attachmentViewProperties);
        }
    }

    public void hideView(boolean hideView) {
        mainLayout.setVisibility(hideView ? GONE : View.VISIBLE);
    }

    public void showPreview() {
        setBooleanViews(true, false, false, false);
        mainLayout.setVisibility(View.VISIBLE);
        setViewsVisibility(View.VISIBLE);
        cancelIcon.setVisibility(View.GONE);
        infoTextView.setVisibility(View.GONE);
        icon.setImageResource(R.drawable.ic_outline_archive_white_24dp);
        showProgress(View.GONE);
    }

    public void showDownloadInProgress() {
        setBooleanViews(false, true, false, false);
        Utils.printLog(context, TAG, "showDownloadInProgress :: ");
        mainLayout.setVisibility(View.VISIBLE);
        showProgress(View.VISIBLE);
    }

    public void showDownloaded() {
        setBooleanViews(false, false, true, false);
        Utils.printLog(context, TAG, "showDownloaded :: ");
        mainLayout.setVisibility(View.VISIBLE);
        showProgress(View.GONE);
    }

    public void registerEvents() {
        mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (kmStoragePermissionListener.isPermissionGranted()) {
                    if (AttachmentManager.isAttachmentInProgress(message.getKeyString())) {
                        // Starts downloading this View, using the current cache setting
                        mDownloadThread = AttachmentManager.startDownload(attachmentViewProperties, mCacheFlag);
                        // After successfully downloading the image, this marks that it's available.
                        showDownloadInProgress();
                    } else if (message.isAttachmentDownloaded()) {
                        showDownloaded();
                    }
                    if (mDownloadThread == null) {
                        mDownloadThread = AttachmentManager.getBGThreadForAttachment(message.getKeyString());
                        if (mDownloadThread != null)
                            mDownloadThread.setAttachementViewNew(attachmentViewProperties);
                    }
                } else {
                    kmStoragePermissionListener.checkPermission(new KmStoragePermission() {
                        @Override
                        public void onAction(boolean didGrant) {
                            if (didGrant) {
                                if (!AttachmentManager.isAttachmentInProgress(message.getKeyString())) {
                                    // Starts downloading this View, using the current cache setting
                                    mDownloadThread = AttachmentManager.startDownload(attachmentViewProperties, mCacheFlag);
                                    // After successfully downloading the image, this marks that it's available.
                                    showDownloadInProgress();
                                }
                                if (mDownloadThread == null) {
                                    mDownloadThread = AttachmentManager.getBGThreadForAttachment(message.getKeyString());
                                    if (mDownloadThread != null)
                                        mDownloadThread.setAttachementViewNew(attachmentViewProperties);
                                }
                            }
                        }
                    });
                }
                if (isDownloaded) {
                    downloadedClickListener();
                } else if (isDownloadProgress) {
                    cancelDownload();
                } else if (isPreview) {
                    previewClickListener();
                } else if (isRetry) {
                    retryClickListener();
                }
            }
        });
    }

    private void retryClickListener() {
        message.setCanceled(false);
        MessageDatabaseService messageDatabaseService = new MessageDatabaseService(context);
        messageDatabaseService.updateCanceledFlag(message.getMessageId(), 0);
        Intent intent = new Intent(context, MessageIntentService.class);
        intent.putExtra(MobiComKitConstants.MESSAGE_JSON_INTENT, GsonUtils.getJsonFromObject(message, Message.class));
        MessageIntentService.enqueueWork(context, intent, null);
        showDownloadInProgress();
    }

    private void downloadedClickListener() {
        if (kmStoragePermissionListener.isPermissionGranted()) {
            playAudio();
        } else {
            kmStoragePermissionListener.checkPermission(new KmStoragePermission() {
                @Override
                public void onAction(boolean didGrant) {
                    if (didGrant) {
                        playAudio();
                    }
                }
            });
        }
    }

    private void playAudio() {
        final String mimeType = FileUtils.getMimeType(message.getFileMetas().getName());
        if (Utils.hasNougat()) {
            uri = FileProvider.getUriForFile(context, Utils.getMetaDataValue(context, MobiComKitConstants.PACKAGE_NAME) + ".provider", new File(message.getFilePaths().get(0)));
        } else {
            uri = Uri.fromFile(new File(message.getFilePaths().get(0)));
            Log.i(TAG, uri.toString());
        }
        if (mimeType != null && mimeType.contains("audio")) {
            ApplozicAudioManager.getInstance(context).play(uri, ApplozicDocumentView.this);
            setAudioIcons();
            updateApplozicSeekBar();
        } else {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                Toast.makeText(context, R.string.info_app_not_found_to_open_file, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void previewClickListener() {
        if (!AttachmentManager.isAttachmentInProgress(message.getKeyString())) {
            // Starts downloading this View, using the current cache setting
            mDownloadThread = AttachmentManager.startDownload(attachmentViewProperties, mCacheFlag);
            // After successfully downloading the image, this marks that it's available.
            showDownloadInProgress();
        }
        if (mDownloadThread == null) {
            mDownloadThread = AttachmentManager.getBGThreadForAttachment(message.getKeyString());
            if (mDownloadThread != null) {
                mDownloadThread.setAttachementViewNew(attachmentViewProperties);
            }
        }
    }

    public void setAudioIcons() {
        int state = ApplozicAudioManager.getInstance(context).getAudioState(message.getKeyString());
        Utils.printLog(context, "state:", String.valueOf(state));
        icon.setVisibility(View.VISIBLE);
        icon.setImageResource(R.drawable.ic_description_white_24dp);
        dividerView.setVisibility(View.VISIBLE);
        infoTextView.setVisibility(View.GONE);
        if (state == 1) {
            icon.setImageResource(R.drawable.ic_pause_white_24dp);
        } else {
            icon.setImageResource(R.drawable.ic_play_arrow_white_24dp);
            isDownloaded = true;
        }
    }

    public boolean isUploadRequire() {
        return (message.hasAttachment() && message.isTypeOutbox() && !message.isSentToServer());
    }

    public boolean isDownloadRequire() {
        return (message.hasAttachment() && message.isSentToServer() && message.getFileMetas() != null && message.getFilePaths() != null);
    }

    public void cancelDownload() {
        if (mDownloadThread == null) {
            if (message.isAttachmentUploadInProgress()) {
                message.setCanceled(true);
            }
            return;
        }
        AttachmentManager.removeDownload(mDownloadThread, true);
        showProgress(View.GONE);
        showPreview();
    }

    public void updateApplozicSeekBar() {
        MediaPlayer mediaplayer = ApplozicAudioManager.getInstance(context).getMediaPlayer(message.getKeyString());
        if (mediaplayer == null) {
            audioseekbar.setProgress(0);
        } else if (mediaplayer.isPlaying()) {
            audioseekbar.setMax(mediaplayer.getDuration());
            audioseekbar.setProgress(mediaplayer.getCurrentPosition());
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    updateApplozicSeekBar();
                }
            };
            mHandler.postDelayed(runnable, 500);
        } else {
            audioseekbar.setMax(mediaplayer.getDuration());
            audioseekbar.setProgress(mediaplayer.getCurrentPosition());
        }
    }
}
