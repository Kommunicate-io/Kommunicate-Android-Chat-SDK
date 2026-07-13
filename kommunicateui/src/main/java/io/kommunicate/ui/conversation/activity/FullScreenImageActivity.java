package io.kommunicate.ui.conversation.activity;

import static io.kommunicate.ui.utils.SentryUtils.configureSentryWithKommunicateUI;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import io.kommunicate.devkit.api.MobiComKitConstants;
import io.kommunicate.devkit.api.conversation.Message;
import io.kommunicate.devkit.broadcast.ConnectivityReceiver;
import io.kommunicate.ui.CustomizationSettings;
import io.kommunicate.ui.R;

import io.kommunicate.ui.activities.KmBaseActivity;
import io.kommunicate.ui.conversation.TouchImageView;
import io.kommunicate.ui.conversation.richmessaging.models.KmRichMessageModel;
import io.kommunicate.ui.conversation.richmessaging.KmRichMessage;
import io.kommunicate.commons.commons.core.utils.Utils;
import io.kommunicate.commons.commons.image.ImageUtils;
import io.kommunicate.commons.file.FileUtils;
import io.kommunicate.commons.json.GsonUtils;
import io.kommunicate.ui.kommunicate.utils.KmThemeHelper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;

/**
 * Created by devashish on 22/9/14.
 */
public class FullScreenImageActivity extends KmBaseActivity {
    TouchImageView mediaImageView;
    ImageView gifImageView;
    private Message message;
    private ConnectivityReceiver connectivityReceiver;
    private CustomizationSettings customizationSettings;
    private static final int HEIGHT = 1600;
    private static final int WIDTH = 1600;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        String jsonString = FileUtils.loadSettingsJsonFile(getApplicationContext());
        if (!TextUtils.isEmpty(jsonString)) {
            customizationSettings = (CustomizationSettings) GsonUtils.getObjectFromJson(jsonString, CustomizationSettings.class);
        } else {
            customizationSettings = new CustomizationSettings();
        }

        setupEdgeToEdge(customizationSettings);
        setContentView(R.layout.image_full_screen);
        configureSentryWithKommunicateUI(this, "");
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        KmThemeHelper themeHelper = KmThemeHelper.getInstance(this, customizationSettings);
        int toolbarColor = themeHelper.getStatusBarColor();
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(toolbarColor));

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
            if (payloadModel.getUrl().endsWith("gif")) {
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
            } else {
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
                    if (message.getFileMetas().getContentType().contains("gif")) {
                        Glide.with(this)
                                .asGif()
                                .load(message.getFilePaths().get(0))
                                .into(gifImageView);
                    } else {
                        Bitmap imageBitmap = ImageUtils.decodeSampledBitmapFromPath(message.getFilePaths().get(0));
                        mediaImageView.setImageBitmap(imageBitmap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            ViewCompat.setOnApplyWindowInsetsListener(getWindow().getDecorView(), (view, insets) -> {
                boolean systemBarsVisible = insets.isVisible(WindowInsetsCompat.Type.systemBars());
                if (systemBarsVisible) {
                    getSupportActionBar().show();
                }
                return insets;
            });
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
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.show(WindowInsetsCompat.Type.systemBars());
        }
    }

    private void hideUi() {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            controller.hide(WindowInsetsCompat.Type.systemBars());
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

            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

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
