package io.kommunicate.ui.conversation.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import io.kommunicate.ui.CustomizationSettings;
import io.kommunicate.ui.R;
import io.kommunicate.commons.commons.image.ImageUtils;
import io.kommunicate.commons.file.FileUtils;

import java.util.ArrayList;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;


public class MobiComAttachmentGridViewAdapter extends BaseAdapter {

    private static final int REQUEST_CODE = 100;
    private ImageButton deleteButton;
    private ImageView galleryImageView;
    private TextView fileSize;
    private ImageView attachmentImageView;
    private TextView fileName;
    private CustomizationSettings customizationSettings;
    private boolean disableNewAttachment;
    private Context context;
    private ArrayList<Uri> uris;
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

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (view == null) {
            view = inflater.inflate(R.layout.attachment_gridview_item, viewGroup, false);//Inflate layout
        }
        deleteButton = (ImageButton) view.findViewById(R.id.attachment_delete_btn);
        galleryImageView = (ImageView) view.findViewById(R.id.galleryImageView);
        fileSize = (TextView) view.findViewById(R.id.attachment_file_size);
        attachmentImageView = (ImageView) view.findViewById(R.id.attachment_image);
        fileName = (TextView) view.findViewById(R.id.attachment_file_name);

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
                uris.remove(position);
                notifyDataSetChanged();
            }
        });

        if (position == getCount() - 1) {
            if (!disableNewAttachment) {
                setNewAttachmentView();
                return view;
            }
        } else {
            if (!disableNewAttachment) {
                deleteButton.setVisibility(View.VISIBLE);
            }
        }

        try {
            Uri uri = (Uri) getItem(position);
            Bitmap previewBitmap = ImageUtils.getPreview(context, uri);
            if (previewBitmap != null) {
                setGalleryView(previewBitmap);
            } else {
                setAttachmentView(uri);
            }
            fileSize.setText(FileUtils.getSize(context, uri));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }


    private void setAttachmentView(Uri uri) {
        attachmentImageView.setVisibility(View.VISIBLE);
        fileName.setVisibility(View.VISIBLE);
        fileName.setText(FileUtils.getFileName(uri));
        galleryImageView.setImageBitmap(null);
    }

    private void setGalleryView(Bitmap previewBitmap) {
        galleryImageView.setImageBitmap(previewBitmap);
        fileName.setVisibility(View.GONE);
        attachmentImageView.setVisibility(View.GONE);
    }

    private void setNewAttachmentView() {
        deleteButton.setVisibility(View.GONE);
        galleryImageView.setImageResource(R.drawable.km_ic_action_add);
        galleryImageView.setEnabled(true);
        fileName.setVisibility(View.GONE);
        attachmentImageView.setVisibility(View.GONE);
        fileSize.setText(R.string.New_Attachment);
    }

}