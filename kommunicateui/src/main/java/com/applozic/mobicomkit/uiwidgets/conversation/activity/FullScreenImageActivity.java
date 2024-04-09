package com.applozic.mobicomkit.uiwidgets.conversation.activity;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.broadcast.ConnectivityReceiver;
import com.applozic.mobicomkit.uiwidgets.R;

import com.applozic.mobicomkit.uiwidgets.conversation.TouchImageView;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.models.KmRichMessageModel;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.KmRichMessage;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.commons.image.ImageUtils;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.json.GsonUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.util.List;

/**
 * Created by devashish on 22/9/14.
 */
public class FullScreenImageActivity extends AppCompatActivity {
    TouchImageView mediaImageView;
    ImageView gifImageView;
    private Message message;
    private ConnectivityReceiver connectivityReceiver;
    private static final int HEIGHT = 1600;
    private static final int WIDTH = 1600;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobicom_image_full_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().show();
        showUi();

        mediaImageView = (TouchImageView) findViewById(R.id.full_screen_image);
        gifImageView = findViewById(R.id.gif_image_view);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.full_screen_progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        String payload = getIntent().getStringExtra(KmRichMessage.TEMPLATE_ID + 9);

        if (payload != null) {
            TextView captionText = findViewById(R.id.captionText);
            KmRichMessageModel.KmPayloadModel payloadModel = (KmRichMessageModel.KmPayloadModel) GsonUtils.getObjectFromJson(payload, KmRichMessageModel.KmPayloadModel.class);
            if(payloadModel.getUrl().endsWith("gif")) {
                Glide.with(this)
                        .asGif()
                        .load(payloadModel.getUrl())
                        .apply(new RequestOptions().override(WIDTH, HEIGHT)) //This is important
                        .into(new ImageViewTarget<GifDrawable>(gifImageView) {
                            @Override
                            protected void setResource(@Nullable GifDrawable gifDrawable) {
                                progressBar.setVisibility(View.GONE);
                                gifImageView.setImageDrawable(gifDrawable);
                            }
                        });
            }
            else {
                Glide.with(this)
                        .asBitmap()
                        .load(payloadModel.getUrl())
                        .apply(new RequestOptions().override(WIDTH, HEIGHT)) //This is important
                        .into(new BitmapImageViewTarget(mediaImageView) {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                super.onResourceReady(resource, transition);
                                progressBar.setVisibility(View.GONE);
                                mediaImageView.setImageBitmap(resource);
                                mediaImageView.setZoom(1);
                            }
                        });
            }
            if (captionText != null && !TextUtils.isEmpty(payloadModel.getCaption())) {
                captionText.setVisibility(View.VISIBLE);
                captionText.setText(payloadModel.getCaption());
            }
        } else {
            String messageJson = getIntent().getStringExtra(MobiComKitConstants.MESSAGE_JSON_INTENT);

            if (!TextUtils.isEmpty(messageJson)) {
                message = (Message) GsonUtils.getObjectFromJson(messageJson, Message.class);
            }

            if (message != null && message.getFilePaths() != null && !message.getFilePaths().isEmpty() && message.getFileMetas() != null) {
                try {
                    if(message.getFileMetas().getContentType().contains("gif")) {
                        Glide.with(this)
                                .asGif()
                                .load(message.getFilePaths().get(0))
                                .into(gifImageView);
                    }
                    else {
                        Bitmap imageBitmap = ImageUtils.decodeSampledBitmapFromPath(message.getFilePaths().get(0));
                        mediaImageView.setImageBitmap(imageBitmap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        if (visibility == 0) {
                            getSupportActionBar().show();
                        }
                    }
                });
            }
            progressBar.setVisibility(View.GONE);

            connectivityReceiver = new ConnectivityReceiver();
            registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }


    private void toggleActionBar() {

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            if (actionBar.isShowing()) {
                actionBar.hide();
                hideUi();
            } else {
                showUi();
                actionBar.show();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            showUi();
        } else {
            hideUi();
        }
    }

    private void showUi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
            }
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private void hideUi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        } else {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.attachment_menu, menu);
        if (message == null) {
            menu.findItem(R.id.shareOptions).setVisible(false);
        }

        // Return true to display menu
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.shareOptions) {

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);

            Uri uri = FileProvider.getUriForFile(this, Utils.getMetaDataValue(this, MobiComKitConstants.PACKAGE_NAME) + ".provider", new File(message.getFilePaths().get(0)));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ClipData clip =
                        ClipData.newUri(getContentResolver(), "a Photo", uri);

                shareIntent.setClipData(clip);
                shareIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            } else {
                List<ResolveInfo> resInfoList =
                        getPackageManager()
                                .queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    grantUriPermission(packageName, uri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    grantUriPermission(packageName, uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            }

            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.setType(FileUtils.getMimeType(new File(message.getFilePaths().get(0))));
            startActivity(Intent.createChooser(shareIntent, ""));

        } else if (i == R.id.forward) {
            Intent intent = new Intent();
            intent.putExtra(MobiComKitConstants.MESSAGE_JSON_INTENT, GsonUtils.getJsonFromObject(message, Message.class));
            setResult(RESULT_OK, intent);
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (connectivityReceiver != null) {
                unregisterReceiver(connectivityReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
