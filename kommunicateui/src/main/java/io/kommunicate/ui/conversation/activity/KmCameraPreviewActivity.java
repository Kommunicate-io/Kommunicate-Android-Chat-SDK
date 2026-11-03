package io.kommunicate.ui.conversation.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.AtomicFile;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.kommunicate.ui.R;
import io.kommunicate.ui.kommunicate.views.KmToast;

/**
 * Full screen preview shown right after a photo is taken with the camera.
 * Step 1: rotate / accept / discard the photo. Step 2: add a caption and send.
 * Returns {@link #EXTRA_CAPTION} with RESULT_OK; the image file at {@link #EXTRA_IMAGE_PATH}
 * is rewritten in place when the user rotated it.
 */
public class KmCameraPreviewActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_PATH = "km.preview.imagePath";
    public static final String EXTRA_CAPTION = "km.preview.caption";
    private static final String STATE_ROTATION = "km.preview.rotation";
    private static final String STATE_CAPTION_STEP = "km.preview.captionStep";
    private static final int MAX_PREVIEW_SIZE = 2048;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private String imagePath;
    private Bitmap baseBitmap;
    private int rotation;
    private boolean captionStep;
    private ImageView reviewImage;
    private ImageView captionImage;
    private View reviewPanel;
    private View captionPanel;
    private EditText captionEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.km_camera_preview_activity);

        imagePath = getIntent().getStringExtra(EXTRA_IMAGE_PATH);
        if (imagePath == null || !new File(imagePath).exists()) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.km_preview_root), (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        reviewPanel = findViewById(R.id.km_preview_review_panel);
        captionPanel = findViewById(R.id.km_preview_caption_panel);
        reviewImage = findViewById(R.id.km_preview_review_image);
        captionImage = findViewById(R.id.km_preview_caption_image);
        captionEditText = findViewById(R.id.km_preview_caption);

        if (savedInstanceState != null) {
            rotation = savedInstanceState.getInt(STATE_ROTATION, 0);
            captionStep = savedInstanceState.getBoolean(STATE_CAPTION_STEP, false);
        }

        findViewById(R.id.km_preview_rotate).setOnClickListener(v -> {
            rotation = (rotation + 270) % 360;
            showBitmap();
        });
        findViewById(R.id.km_preview_confirm).setOnClickListener(v -> showCaptionStep(true));
        findViewById(R.id.km_preview_discard).setOnClickListener(v -> cancel());
        findViewById(R.id.km_preview_cancel_btn).setOnClickListener(v -> cancel());
        findViewById(R.id.km_preview_send_btn).setOnClickListener(v -> send());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                cancel();
            }
        });

        showCaptionStep(captionStep);
        executor.execute(() -> {
            Bitmap bitmap = decode(imagePath, MAX_PREVIEW_SIZE, 0);
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                if (bitmap == null) {
                    KmToast.error(this, R.string.km_preview_save_failed, Toast.LENGTH_SHORT).show();
                    cancel();
                    return;
                }
                baseBitmap = bitmap;
                showBitmap();
            });
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_ROTATION, rotation);
        outState.putBoolean(STATE_CAPTION_STEP, captionStep);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void showCaptionStep(boolean show) {
        captionStep = show;
        reviewPanel.setVisibility(show ? View.GONE : View.VISIBLE);
        captionPanel.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            captionEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                captionEditText.post(() -> imm.showSoftInput(captionEditText, InputMethodManager.SHOW_IMPLICIT));
            }
        }
    }

    private void showBitmap() {
        if (baseBitmap == null) {
            return;
        }
        Bitmap shown = baseBitmap;
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            shown = Bitmap.createBitmap(baseBitmap, 0, 0, baseBitmap.getWidth(), baseBitmap.getHeight(), matrix, true);
        }
        reviewImage.setImageBitmap(shown);
        captionImage.setImageBitmap(shown);
    }

    private void cancel() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void send() {
        findViewById(R.id.km_preview_send_btn).setEnabled(false);
        final String caption = captionEditText.getText().toString().trim();
        executor.execute(() -> {
            boolean saved = rotation == 0 || saveRotatedImage();
            runOnUiThread(() -> {
                if (!saved) {
                    findViewById(R.id.km_preview_send_btn).setEnabled(true);
                    KmToast.error(this, R.string.km_preview_save_failed, Toast.LENGTH_SHORT).show();
                    return;
                }
                setResult(RESULT_OK, new Intent()
                        .putExtra(EXTRA_IMAGE_PATH, imagePath)
                        .putExtra(EXTRA_CAPTION, caption));
                finish();
            });
        });
    }

    /**
     * Bakes the chosen rotation into the original (full resolution) file.
     */
    private boolean saveRotatedImage() {
        Bitmap full = decode(imagePath, 0, rotation);
        if (full == null) {
            return false;
        }
        // Write atomically so the original capture stays intact if encoding fails.
        AtomicFile atomicFile = new AtomicFile(new File(imagePath));
        FileOutputStream out = null;
        try {
            out = atomicFile.startWrite();
            if (!full.compress(Bitmap.CompressFormat.JPEG, 95, out)) {
                atomicFile.failWrite(out);
                return false;
            }
            atomicFile.finishWrite(out);
            return true;
        } catch (Exception e) {
            if (out != null) {
                atomicFile.failWrite(out);
            }
            e.printStackTrace();
            return false;
        } finally {
            full.recycle();
        }
    }

    /**
     * Decodes the file, applying its EXIF orientation plus an extra rotation.
     *
     * @param maxSize longest side to downsample to, or 0 for full resolution
     */
    private static Bitmap decode(String path, int maxSize, int extraRotation) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            if (maxSize > 0) {
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(path, options);
                int sample = 1;
                while (Math.max(options.outWidth, options.outHeight) / sample > maxSize) {
                    sample *= 2;
                }
                options = new BitmapFactory.Options();
                options.inSampleSize = sample;
            }
            Bitmap bitmap = BitmapFactory.decodeFile(path, options);
            if (bitmap == null) {
                return null;
            }
            int degrees = extraRotation;
            switch (new ExifInterface(path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degrees += 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degrees += 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degrees += 270;
                    break;
                default:
                    break;
            }
            degrees %= 360;
            if (degrees == 0) {
                return bitmap;
            }
            Matrix matrix = new Matrix();
            matrix.postRotate(degrees);
            Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (rotated != bitmap) {
                bitmap.recycle();
            }
            return rotated;
        } catch (Exception | OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }
}
