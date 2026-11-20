package io.kommunicate.ui.conversation.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import io.kommunicate.ui.CustomizationSettings;
import io.kommunicate.ui.R;
import io.kommunicate.commons.commons.image.ImageUtils;
import io.kommunicate.commons.file.FileUtils;

import java.util.ArrayList;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;


public class MobiComAttachmentGridViewAdapter extends BaseAdapter {

    private static final int REQUEST_CODE = 100;
    private CustomizationSettings customizationSettings;
    private boolean disableNewAttachment;
    private Context context;
    private ArrayList<Uri> uris;
    private ArrayList<String> captions = new ArrayList<>();
    private FileUtils.GalleryFilterOptions filterOptions;
    private Function0<Unit> openAttachmentPickerCallback;

    public MobiComAttachmentGridViewAdapter(
            Context context,
            ArrayList<Uri> uris,
            CustomizationSettings customizationSettings,
            boolean disableNewAttachment,
            FileUtils.GalleryFilterOptions filterOptions,
            Function0<Unit> openAttachmentPickerCallback
    ) {
        this.context = context;
        this.customizationSettings = customizationSettings;
        this.uris = uris;
        this.disableNewAttachment = disableNewAttachment;
        this.filterOptions = filterOptions;
        this.openAttachmentPickerCallback = openAttachmentPickerCallback;
    }

    @Override
    public int getCount() {
        //Extra one item is added
        return uris.size() + (disableNewAttachment ? 0 : 1);
    }


    @Override
    public Object getItem(int i) {
        return uris.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    /**
     * Captions aligned by index with the attachment list.
     */
    public ArrayList<String> getCaptions() {
        syncCaptions();
        return new ArrayList<>(captions);
    }

    public String getCaption(int position) {
        syncCaptions();
        return position >= 0 && position < captions.size() ? captions.get(position) : "";
    }

    private void syncCaptions() {
        while (captions.size() < uris.size()) {
            captions.add("");
        }
        while (captions.size() > uris.size()) {
            captions.remove(captions.size() - 1);
        }
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.attachment_gridview_item, viewGroup, false);//Inflate layout
        }
        syncCaptions();
        final ImageButton deleteButton = view.findViewById(R.id.attachment_delete_btn);
        final ImageView galleryImageView = view.findViewById(R.id.galleryImageView);
        final TextView fileSize = view.findViewById(R.id.attachment_file_size);
        final ImageView attachmentImageView = view.findViewById(R.id.attachment_image);
        final TextView fileName = view.findViewById(R.id.attachment_file_name);
        final EditText captionEditText = view.findViewById(R.id.attachment_caption_edit_text);
        final View tile = view.findViewById(R.id.attachment_tile);
        tile.setClipToOutline(true);

        galleryImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (position < getCount() - 1) {
                    return;
                }

                if (getCount() > customizationSettings.getMaxAttachmentAllowed()) {
                    Toast.makeText(context, R.string.max_attachment_warning, Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    ImageView galleryImageView = (ImageView) v;
                    galleryImageView.setEnabled(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                openAttachmentPickerCallback.invoke();
            }
        });

        if (disableNewAttachment) {
            deleteButton.setVisibility(View.GONE);
        }
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position < uris.size()) {
                    uris.remove(position);
                    if (position < captions.size()) {
                        captions.remove(position);
                    }
                    notifyDataSetChanged();
                }
            }
        });

        // Reset any state left over from a recycled view.
        galleryImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        galleryImageView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        galleryImageView.setPadding(0, 0, 0, 0);
        fileSize.setBackgroundResource(R.color.km_attachment_bar_background);
        fileSize.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        fileSize.setPadding(dp(12), 0, dp(56), 0);

        if (position == getCount() - 1 && !disableNewAttachment) {
            setNewAttachmentView(deleteButton, galleryImageView, fileSize, fileName, attachmentImageView, captionEditText);
            return view;
        }

        if (!disableNewAttachment) {
            deleteButton.setVisibility(View.VISIBLE);
        }
        bindCaption(captionEditText, position);

        try {
            Uri uri = (Uri) getItem(position);
            Bitmap previewBitmap = ImageUtils.getPreview(context, uri);
            if (previewBitmap != null) {
                setGalleryView(galleryImageView, fileName, attachmentImageView, previewBitmap);
            } else {
                setAttachmentView(galleryImageView, fileName, attachmentImageView, uri);
            }
            fileSize.setText(FileUtils.getSize(context, uri));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }

    private void bindCaption(final EditText captionEditText, final int position) {
        Object previousWatcher = captionEditText.getTag(R.id.attachment_caption_edit_text);
        if (previousWatcher instanceof TextWatcher) {
            captionEditText.removeTextChangedListener((TextWatcher) previousWatcher);
        }
        captionEditText.setVisibility(View.VISIBLE);
        captionEditText.setText(captions.get(position));

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (position < captions.size()) {
                    captions.set(position, s.toString());
                }
            }
        };
        captionEditText.addTextChangedListener(watcher);
        captionEditText.setTag(R.id.attachment_caption_edit_text, watcher);
    }

    private int dp(int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    private void setAttachmentView(ImageView galleryImageView, TextView fileName, ImageView attachmentImageView, Uri uri) {
        attachmentImageView.setVisibility(View.VISIBLE);
        fileName.setVisibility(View.VISIBLE);
        fileName.setText(FileUtils.getFileName(uri));
        galleryImageView.setImageBitmap(null);
    }

    private void setGalleryView(ImageView galleryImageView, TextView fileName, ImageView attachmentImageView, Bitmap previewBitmap) {
        galleryImageView.setImageBitmap(previewBitmap);
        fileName.setVisibility(View.GONE);
        attachmentImageView.setVisibility(View.GONE);
    }

    private void setNewAttachmentView(ImageButton deleteButton, ImageView galleryImageView, TextView fileSize,
                                      TextView fileName, ImageView attachmentImageView, EditText captionEditText) {
        deleteButton.setVisibility(View.GONE);
        captionEditText.setVisibility(View.INVISIBLE);
        galleryImageView.setImageResource(R.drawable.km_attachment_ic_add);
        galleryImageView.setScaleType(ImageView.ScaleType.CENTER);
        galleryImageView.setEnabled(true);
        fileName.setVisibility(View.GONE);
        attachmentImageView.setVisibility(View.GONE);
        fileSize.setBackgroundResource(R.color.km_attachment_new_label_background);
        fileSize.setGravity(Gravity.CENTER);
        fileSize.setPadding(dp(12), 0, dp(12), 0);
        fileSize.setText(R.string.New_Attachment);
    }

}
