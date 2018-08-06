package com.applozic.mobicomkit.uiwidgets.conversation;

import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupWindow;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.applozic.mobicomkit.uiwidgets.uilistener.KmStoragePermission;
import com.applozic.mobicomkit.uiwidgets.uilistener.KmStoragePermissionListener;

import java.util.List;

/**
 * Created by reytum on 19/3/16.
 */
public class MultimediaOptionsGridView {
    public PopupWindow showPopup;
    FragmentActivity context;
    GridView multimediaOptions;
    private Uri capturedImageUri;
    private KmStoragePermissionListener storagePermissionListener;

    public MultimediaOptionsGridView(FragmentActivity context, GridView multimediaOptions) {
        this.context = context;
        this.multimediaOptions = multimediaOptions;

        if (context instanceof KmStoragePermissionListener) {
            storagePermissionListener = (KmStoragePermissionListener) context;
        } else {
            storagePermissionListener = new KmStoragePermissionListener() {
                @Override
                public boolean isPermissionGranted() {
                    return false;
                }

                @Override
                public void checkPermission(KmStoragePermission storagePermission) {
                }
            };
        }
    }

    public void setMultimediaClickListener(final List<String> keys) {
        capturedImageUri = null;

        multimediaOptions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                executeMethod(keys.get(position));
            }
        });
    }

    public void executeMethod(String key) {
        if (key.equals(context.getResources().getString(R.string.al_location))) {
            ((ConversationActivity) context).processLocation();
        } else if (key.equals(context.getString(R.string.al_camera))) {
            if (storagePermissionListener.isPermissionGranted()) {
                ((ConversationActivity) context).isTakePhoto(true);
                ((ConversationActivity) context).processCameraAction();
            } else {
                storagePermissionListener.checkPermission(new KmStoragePermission() {
                    @Override
                    public void onAction(boolean didGrant) {
                        if (didGrant) {
                            ((ConversationActivity) context).isTakePhoto(true);
                            ((ConversationActivity) context).processCameraAction();
                        }
                    }
                });
            }
        } else if (key.equals(context.getString(R.string.al_file))) {
            if (storagePermissionListener.isPermissionGranted()) {
                ((ConversationActivity) context).isAttachment(true);
                ((ConversationActivity) context).processAttachment();
            } else {
                storagePermissionListener.checkPermission(new KmStoragePermission() {
                    @Override
                    public void onAction(boolean didGrant) {
                        if (didGrant) {
                            ((ConversationActivity) context).isAttachment(true);
                            ((ConversationActivity) context).processAttachment();
                        }
                    }
                });
            }
        } else if (key.equals(context.getString(R.string.al_audio))) {
            if (storagePermissionListener.isPermissionGranted()) {
                ((ConversationActivity) context).showAudioRecordingDialog();
            } else {
                storagePermissionListener.checkPermission(new KmStoragePermission() {
                    @Override
                    public void onAction(boolean didGrant) {
                        if (didGrant) {
                            ((ConversationActivity) context).showAudioRecordingDialog();
                        }
                    }
                });
            }
        } else if (key.equals(context.getString(R.string.al_video))) {
            if (storagePermissionListener.isPermissionGranted()) {
                ((ConversationActivity) context).isTakePhoto(false);
                ((ConversationActivity) context).processVideoRecording();
            } else {
                storagePermissionListener.checkPermission(new KmStoragePermission() {
                    @Override
                    public void onAction(boolean didGrant) {
                        if (didGrant) {
                            ((ConversationActivity) context).isTakePhoto(false);
                            ((ConversationActivity) context).processVideoRecording();
                        }
                    }
                });
            }
        } else if (key.equals(context.getString(R.string.al_contact))) {
            if (storagePermissionListener.isPermissionGranted()) {
                ((ConversationActivity) context).processContact();
            } else {
                storagePermissionListener.checkPermission(new KmStoragePermission() {
                    @Override
                    public void onAction(boolean didGrant) {
                        if (didGrant) {
                            ((ConversationActivity) context).processContact();
                        }
                    }
                });
            }
        } else if (key.equals(context.getString(R.string.al_price))) {
            new ConversationUIService(context).sendPriceMessage();
        }
        multimediaOptions.setVisibility(View.GONE);
    }
}