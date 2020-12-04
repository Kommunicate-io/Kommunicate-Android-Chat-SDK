package com.applozic.mobicomkit.uiwidgets.attachmentview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

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
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.kommunicate.views.KmToast;
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
public class KmDocumentView {

    private static final String TAG = "ApplozicDocumentView";
    RelativeLayout mainLayout;
    RelativeLayout downloadInProgressLayout;
    RelativeLayout downloadedLayout;
    RelativeLayout previewLayout;
    RelativeLayout retryLayout;
    TextView sizeTextView;
    TextView fileText;
    Message message;
    Context context;
    AttachmentViewProperties attachmentViewProperties;
    ProgressBar progressBar;
    ImageView uploadDownloadImage;
    SeekBar audioseekbar;
    Uri uri;
    String filePath, mimeType = null;
    ImageView docIcon;
    TextView audio_duration_textView;
    String audio_duration;
    private AttachmentTask mDownloadThread;
    private boolean mCacheFlag = false;
    private KmStoragePermissionListener kmStoragePermissionListener;
    private Handler mHandler = new Handler();


    public KmDocumentView(Context context, KmStoragePermissionListener kmStoragePermissionListener) {
        this.context = context;
        this.kmStoragePermissionListener = kmStoragePermissionListener;
    }

    public void inflateViewWithMessage(View rootview, Message message) {
        this.message = message;
        mainLayout = (RelativeLayout) rootview.findViewById(R.id.attachment_doc_relative_layout);
        downloadInProgressLayout = (RelativeLayout) rootview.findViewById(R.id.applozic_doc_download_progress_rl);
        downloadedLayout = (RelativeLayout) rootview.findViewById(R.id.applozic_doc_downloaded);
        previewLayout = (RelativeLayout) rootview.findViewById(R.id.download_doc_relative_layout);
        retryLayout = (RelativeLayout) rootview.findViewById(R.id.retry_doc_relative_layout);
        progressBar = (ProgressBar) rootview.findViewById(R.id.applozic_doc_download_progress);
        sizeTextView = (TextView) rootview.findViewById(R.id.applozic_doc_file_size);
        fileText = (TextView) rootview.findViewById(R.id.applozic_doc_file_name);
        uploadDownloadImage = (ImageView) rootview.findViewById(R.id.applozic_download_image);
        docIcon = (ImageView) rootview.findViewById(R.id.doc_icon);
        ImageView cancelIcon = (ImageView) rootview.findViewById(R.id.download_calcle_icon);
        audioseekbar = (SeekBar) rootview.findViewById(R.id.applozic_audio_seekbar);
        audio_duration_textView = (TextView) rootview.findViewById(R.id.audio_duration_textView);

        if (!message.hasAttachment()) {
            return;
        }

        if (audio_duration_textView != null) {
            audio_duration_textView.setTextColor(context.getResources().getColor(message.isTypeOutbox() ? R.color.white : R.color.black));
        }
        progressBar.getIndeterminateDrawable().setColorFilter(message.isTypeOutbox() ? context.getResources().getColor(R.color.applozic_green_color) : context.getResources().getColor(R.color.black), android.graphics.PorterDuff.Mode.MULTIPLY);
        cancelIcon.setColorFilter(message.isTypeOutbox() ? R.color.white : R.color.black, android.graphics.PorterDuff.Mode.MULTIPLY);
        if (message.getFileMetas() != null) {
            if (message.getFileMetas().getContentType().contains("audio")) {
                setAudioIcons();
                updateApplozicSeekBar();
            } else {
                audio_duration_textView.setVisibility(GONE);
                audioseekbar.setVisibility(GONE);
                fileText.setVisibility(View.VISIBLE);
                fileText.setText(message.getFileMetas().getName());
            }
        } else if (message.getFilePaths() != null) {
            filePath = message.getFilePaths().get(0);
            mimeType = FileUtils.getMimeType(filePath);
            if (mimeType != null && mimeType.contains("audio")) {
                setAudioIcons();
                updateApplozicSeekBar();
            } else {
                audio_duration_textView.setVisibility(GONE);
                audioseekbar.setVisibility(GONE);
                fileText.setVisibility(View.VISIBLE);
                fileText.setText(new File(filePath).getName());
                docIcon.setImageResource(R.drawable.ic_documentreceive);
            }
        }

        fileText.setTextColor(ContextCompat.getColor(context, message.isTypeOutbox() ? R.color.message_text_color : R.color.km_received_message_text_color));
        audioseekbar.getProgressDrawable().setColorFilter(message.isTypeOutbox() ? 0xFFFFFFFF : 0xFFFFB242, PorterDuff.Mode.MULTIPLY);
        cancelIcon.setVisibility(message.isTypeOutbox() ? GONE : View.VISIBLE);
        if (message.isTypeOutbox()) {
            docIcon.setColorFilter(0xffffffff);
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
                        fileText.setVisibility(GONE);
                        audio_duration_textView.setVisibility(View.VISIBLE);
                        audioseekbar.setVisibility(View.VISIBLE);
                    } else {
                        fileText.setVisibility(View.VISIBLE);
                        audio_duration_textView.setVisibility(GONE);
                        audioseekbar.setVisibility(GONE);
                        docIcon.setImageResource(R.drawable.ic_documentreceive);
                    }
                }
            }
        } else {
            showPreview();
        }

        if (message.getFileMetas() != null && message.getFilePaths() == null) {
            sizeTextView.setText(message.getFileMetas().getSizeInReadableFormat());
            if (!(message.getFileMetas().getContentType().contains("audio"))) {
                fileText.setText(message.getFileMetas().getName());
                audioseekbar.setVisibility(GONE);
                audio_duration_textView.setVisibility(GONE);
            } else {
                fileText.setVisibility(GONE);
                if (message.isAttachmentDownloaded()) {
                    KommunicateAudioManager.getInstance(context).updateAudioDuration(audio_duration_textView, filePath);
                    audio_duration_textView.setVisibility(View.VISIBLE);
                } else {
                    audio_duration_textView.setVisibility(View.VISIBLE);
                    audio_duration_textView.setText("00:00");
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
                    fileText.setText(fileName);
                    audioseekbar.setVisibility(GONE);
                    audio_duration_textView.setVisibility(GONE);
                    docIcon.setVisibility(View.VISIBLE);
                    docIcon.setImageResource(R.drawable.ic_documentreceive);
                } else {
                    if (message.isAttachmentDownloaded()) {
                        KommunicateAudioManager.getInstance(context).updateAudioDuration(audio_duration_textView, filePath);
                        audio_duration_textView.setVisibility(View.VISIBLE);
                    } else {
                        audio_duration_textView.setVisibility(View.VISIBLE);
                        audio_duration_textView.setText("00:00");
                    }
                    fileText.setVisibility(GONE);
                    docIcon.setVisibility(GONE);
                    audioseekbar.setVisibility(View.VISIBLE);
                    setAudioIcons();
                }
            }
        }
    }

    private void showRetry() {
        if (isDownloadRequire()) {
            showPreview();
        } else {
            mainLayout.setVisibility(View.VISIBLE);
            retryLayout.setVisibility(View.VISIBLE);

            uploadDownloadImage.setImageResource(R.drawable.circle_arrow_upload);
            downloadInProgressLayout.setVisibility(GONE);
            downloadedLayout.setVisibility(GONE);
            previewLayout.setVisibility(GONE);
        }
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
        mainLayout.setVisibility(View.VISIBLE);
        previewLayout.setVisibility(View.VISIBLE);
        uploadDownloadImage.setImageResource(R.drawable.circle_arrow_down_download);
        downloadInProgressLayout.setVisibility(GONE);
        downloadedLayout.setVisibility(GONE);
        retryLayout.setVisibility(GONE);
    }

    public void showDownloadInProgress() {
        Utils.printLog(context, TAG, "showDownloadInProgress");
        mainLayout.setVisibility(View.VISIBLE);
        downloadInProgressLayout.setVisibility(View.VISIBLE);
        previewLayout.setVisibility(GONE);
        downloadedLayout.setVisibility(GONE);
        retryLayout.setVisibility(GONE);
    }

    public void showDownloaded() {
        Utils.printLog(context, TAG, "showDownloaded");
        mainLayout.setVisibility(View.VISIBLE);
        downloadedLayout.setVisibility(View.VISIBLE);
        previewLayout.setVisibility(GONE);
        downloadInProgressLayout.setVisibility(GONE);
        retryLayout.setVisibility(GONE);
    }

    public void registerEvents() {
        previewLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (kmStoragePermissionListener.isPermissionGranted()) {
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
            }
        });

        downloadedLayout.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
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

        });

        downloadInProgressLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelDownload();
            }
        });

        retryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message.setCanceled(false);
                MessageDatabaseService messageDatabaseService = new MessageDatabaseService(context);
                messageDatabaseService.updateCanceledFlag(message.getMessageId(), 0);
                Intent intent = new Intent(context, MessageIntentService.class);
                intent.putExtra(MobiComKitConstants.MESSAGE_JSON_INTENT, GsonUtils.getJsonFromObject(message, Message.class));
                MessageIntentService.enqueueWork(context, intent, null);
                showDownloadInProgress();

            }
        });
    }

    public void playAudio() {
        final String mimeType = FileUtils.getMimeType(message.getFileMetas().getName());
        if (Utils.hasNougat()) {
            uri = FileProvider.getUriForFile(context, Utils.getMetaDataValue(context, MobiComKitConstants.PACKAGE_NAME) + ".provider", new File(message.getFilePaths().get(0)));
        } else {
            uri = Uri.fromFile(new File(message.getFilePaths().get(0)));
            Log.i(TAG, uri.toString());
        }
        if (mimeType != null && mimeType.contains("audio")) {
            KommunicateAudioManager.getInstance(context).play(uri, KmDocumentView.this);
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
                KmToast.error(context, R.string.info_app_not_found_to_open_file, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void setAudioIcons() {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (message != null && docIcon != null) {
                        int state = KommunicateAudioManager.getInstance(context).getAudioState(message.getKeyString());
                        Utils.printLog(context, "state:", String.valueOf(state));
                        docIcon.setVisibility(View.VISIBLE);
                        if (state == 1) {
                            docIcon.setImageResource(R.drawable.ic_pause_circle_outline);
                        } else {
                            docIcon.setImageResource(R.drawable.ic_play_circle_outline);
                        }
                    }
                }
            });
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
        getDownloadProgressLayout().setVisibility(GONE);
        showPreview();
    }

    public View getDownloadProgressLayout() {
        return downloadInProgressLayout;
    }

    public void updateApplozicSeekBar() {
        MediaPlayer mediaplayer = KommunicateAudioManager.getInstance(context).getMediaPlayer(message.getKeyString());
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