package com.applozic.mobicomkit.uiwidgets.conversation.activity;

import static com.applozic.mobicomkit.uiwidgets.utils.SentryUtils.configureSentryWithKommunicateUI;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.NinePatchDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import dev.kommunicate.devkit.api.account.user.MobiComUserPreference;
import dev.kommunicate.devkit.api.attachment.FileClientService;
import dev.kommunicate.devkit.api.conversation.Message;
import dev.kommunicate.devkit.broadcast.ConnectivityReceiver;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.KommunicateSetting;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.adapter.MobiComAttachmentGridViewAdapter;
import com.applozic.mobicomkit.uiwidgets.kommunicate.KmAttachmentsController;
import com.applozic.mobicomkit.uiwidgets.kommunicate.callbacks.PrePostUIMethods;
import com.applozic.mobicomkit.uiwidgets.kommunicate.utils.KmThemeHelper;
import com.applozic.mobicomkit.uiwidgets.kommunicate.views.KmToast;
import com.applozic.mobicomkit.uiwidgets.utils.InsetHelper;
import dev.kommunicate.commons.commons.core.utils.Utils;
import dev.kommunicate.commons.file.FileUtils;
import dev.kommunicate.commons.file.MediaPicker;
import dev.kommunicate.commons.json.GsonUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import io.kommunicate.utils.KmUtils;
import io.sentry.Sentry;

import static java.util.Collections.disjoint;

public class MobiComAttachmentSelectorActivity extends AppCompatActivity {

    public static final String MULTISELECT_SELECTED_FILES = "multiselect.selectedFiles";
    public static final String MULTISELECT_MESSAGE = "multiselect.message";
    public static final String URI_LIST = "URI_LIST";
    public static String USER_ID = "USER_ID";
    public static String DISPLAY_NAME = "DISPLAY_NAME";
    public static String GROUP_ID = "GROUP_ID";
    public static String GROUP_NAME = "GROUP_NAME";
    private static int REQUEST_CODE_ATTACH_PHOTO = 10;
    private AlCustomizationSettings alCustomizationSettings;
    private FileClientService fileClientService;
    private Uri imageUri;
    private String userId, displayName, groupName;
    private Integer groupId;
    private MobiComUserPreference userPreferences;
    private String TAG = "MultiAttActivity";
    private Button sendAttachment;
    private Button cancelAttachment;
    private FrameLayout attachmentAction;
    private EditText messageEditText;
    private ConnectivityReceiver connectivityReceiver;
    private GridView galleryImagesGridView;
    private List<String> restrictedWords;
    private ArrayList<Uri> attachmentFileList = new ArrayList<>();
    private MobiComAttachmentGridViewAdapter imagesAdapter;
    private LinearLayout idRootLinearLayout;
    private KmAttachmentsController kmAttachmentsController;
    private PrePostUIMethods prePostUIMethodsFileAsyncTask;
    private ActivityResultLauncher<PickVisualMediaRequest> mediaPickerResult;
    private ActivityResultLauncher<Intent> attachmentAudioResult;


    /**
     * will open either the general file attachment chooser
     */
    void openFileChooser() {
        FileUtils.GalleryFilterOptions filterOptions = kmAttachmentsController.getFilterOptions(alCustomizationSettings);
        switch (filterOptions) {
            case ALL_FILES: {
                Intent contentChooserIntent = MediaPicker.INSTANCE.createAllFilesPickerIntent(
                        alCustomizationSettings.isMultipleAttachmentSelectionEnabled(),
                        getString(R.string.select_file)
                );
                attachmentAudioResult.launch(contentChooserIntent);
            }
            break;
            case AUDIO_ONLY: {
                Intent audioPickerIntent = MediaPicker.INSTANCE.createAudioFilesPickerIntent(
                        alCustomizationSettings.isMultipleAttachmentSelectionEnabled(),
                        getString(R.string.select_file)
                );
                attachmentAudioResult.launch(audioPickerIntent);
            }
            break;
            case IMAGE_VIDEO: case IMAGE_ONLY: case VIDEO_ONLY: {
                MediaPicker.INSTANCE.createMediaPickerIntent(
                        mediaPickerResult,
                        filterOptions
                );
            }
            break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobicom_multi_attachment_activity);
        String jsonString = FileUtils.loadSettingsJsonFile(getApplicationContext());
        if (!TextUtils.isEmpty(jsonString)) {
            alCustomizationSettings = (AlCustomizationSettings) GsonUtils.getObjectFromJson(jsonString, AlCustomizationSettings.class);
        } else {
            alCustomizationSettings = new AlCustomizationSettings();
        }

        configureSentryWithKommunicateUI(this, alCustomizationSettings.toString());
        setupActivityResultCallback();
        kmAttachmentsController = new KmAttachmentsController(this);

        KmUtils.setStatusBarColor(this, KmThemeHelper.getInstance(this, alCustomizationSettings).getStatusBarColor());

        restrictedWords = FileUtils.loadRestrictedWordsFile(this);
        fileClientService = new FileClientService(this);
        userPreferences = MobiComUserPreference.getInstance(this);
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            userId = intent.getExtras().getString(USER_ID);
            displayName = intent.getExtras().getString(DISPLAY_NAME);
            if (intent.hasExtra(GROUP_ID)) {
                groupId = intent.getExtras().getInt(GROUP_ID);
            }
            groupName = intent.getExtras().getString(GROUP_NAME);
            imageUri = (Uri) intent.getParcelableExtra(URI_LIST);
            if (imageUri != null) {
                attachmentFileList.add(imageUri);
            }
        }

        initViews();
        setUpGridView();
        fileClientService = new FileClientService(this);
        if (imageUri == null) {
            openFileChooser();
        }
        connectivityReceiver = new ConnectivityReceiver();
        registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        setupInsets();
    }

    private void setupInsets() {
        InsetHelper.configureSystemInsets(
                galleryImagesGridView,
                -1,
                0,
                true
        );
        InsetHelper.configureSystemInsets(
                attachmentAction,
                0,
                -1,
                false
        );
    }


    private void setupActivityResultCallback() {
        attachmentAudioResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    configureAddAttachmentButton();

                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent resultData = result.getData();

                        if (resultData.getClipData() != null) {
                            ClipData clipData = resultData.getClipData();

                            for (int index = 0; index < clipData.getItemCount(); index++) {
                                if (index == alCustomizationSettings.getMaxAttachmentAllowed()) {
                                    KmToast.error(this, R.string.mobicom_max_attachment_warning, Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                ClipData.Item item = clipData.getItemAt(index);
                                Uri selectedFileUri = item.getUri();
                                processFile(selectedFileUri);
                            }

                        } else if (resultData.getData() != null) {
                            Uri selectedFileUri = result.getData().getData();
                            processFile(selectedFileUri);
                        }
                    } else {
                        KmToast.error(
                                MobiComAttachmentSelectorActivity.this,
                                "Unable to select the file",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        mediaPickerResult = MediaPicker.INSTANCE.registerImageVideoPicker(
                MobiComAttachmentSelectorActivity.this,
                uris -> {
                    try {
                        configureAddAttachmentButton();
                        if (uris.isEmpty()) {
                            KmToast.error(this, R.string.mobicom_no_attachment_warning, Toast.LENGTH_SHORT).show();
                            return null;
                        }
                        if (uris.size() > KmAttachmentsController.NO_OF_MULTI_SELECTIONS_ALLOWED) {
                            KmToast.error(this, R.string.mobicom_max_attachment_warning, Toast.LENGTH_SHORT).show();
                            return null;
                        }

                        // Process URIs
                        for (Uri uri: uris) {
                            processFile(uri);
                        }
                    } catch (Exception exception) {
                        Sentry.captureException(exception);
                    }
                    return null;
                },
                alCustomizationSettings.isMultipleAttachmentSelectionEnabled()
        );
    }

    private void configureAddAttachmentButton() {
        try {
            if (imagesAdapter != null) {
                View view = galleryImagesGridView.getChildAt(imagesAdapter.getCount() - 1);
                if (view != null) {
                    ImageView imageView = view.findViewById(R.id.galleryImageView);
                    if (imageView != null) {
                        imageView.setEnabled(true);
                    }
                }
            }
        } catch (Exception e) {
            Sentry.captureException(e);
        }
    }

    /**
     * initialize the views
     */
    private void initViews() {
        sendAttachment = (Button) findViewById(R.id.mobicom_attachment_send_btn);
        cancelAttachment = (Button) findViewById(R.id.mobicom_attachment_cancel_btn);
        attachmentAction = (FrameLayout) findViewById(R.id.attachment_action_button);
        galleryImagesGridView = (GridView) findViewById(R.id.mobicom_attachment_grid_View);
        messageEditText = (EditText) findViewById(R.id.mobicom_attachment_edit_text);
        KmThemeHelper themeHelper = KmThemeHelper.getInstance(this, alCustomizationSettings);
        if (themeHelper.isDarkModeEnabledForSDK()) {
            idRootLinearLayout = findViewById(R.id.idRootLinearLayout);
            idRootLinearLayout.setBackgroundColor(getResources().getColor(R.color.dark_mode_default));
            NinePatchDrawable bgShape = (NinePatchDrawable) messageEditText.getBackground();
            bgShape.setColorFilter(getResources().getColor(R.color.received_message_bg_color_night), android.graphics.PorterDuff.Mode.MULTIPLY);
            messageEditText.setTextColor(getResources().getColor(R.color.chatbar_text_color));
            messageEditText.setHintTextColor(getResources().getColor(R.color.chatbar_text_color));
        }
        cancelAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        sendAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (attachmentFileList.isEmpty()) {
                    KmToast.error(getApplicationContext(), R.string.mobicom_select_attachment_text, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!validateCaptionTextAndShowDialog()) {
                    Utils.printLog(MobiComAttachmentSelectorActivity.this, TAG, "Caption Text is not valid");
                    return;
                }

                if (imageUri != null) {
                    for (Uri uri : attachmentFileList) {
                        try {
                            Message messageToSend = kmAttachmentsController.putAttachmentInfo(uri, false, groupId, userId, messageEditText.getText().toString());
                            Intent startConversationActivity = new Intent(MobiComAttachmentSelectorActivity.this, ConversationActivity.class);
                            if (groupId != 0) {
                                startConversationActivity.putExtra(ConversationUIService.GROUP_ID, groupId);
                                startConversationActivity.putExtra(ConversationUIService.GROUP_NAME, groupName);
                                startConversationActivity.putExtra(ConversationUIService.FORWARD_MESSAGE, GsonUtils.getJsonFromObject(messageToSend, messageToSend.getClass()));
                            } else {
                                startConversationActivity.putExtra(ConversationUIService.USER_ID, userId);
                                startConversationActivity.putExtra(ConversationUIService.DISPLAY_NAME, displayName);
                                startConversationActivity.putExtra(ConversationUIService.FORWARD_MESSAGE, GsonUtils.getJsonFromObject(messageToSend, messageToSend.getClass()));
                            }
                            startActivity(startConversationActivity);
                            finish();
                        } catch (Exception e) {
                            KmToast.error(MobiComAttachmentSelectorActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                } else {
                    Intent intent = new Intent();
                    intent.putParcelableArrayListExtra(MULTISELECT_SELECTED_FILES, attachmentFileList);
                    intent.putExtra(MULTISELECT_MESSAGE, messageEditText.getText().toString());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

        //what to do before and after the doInBackground of the FileTaskAsync
        prePostUIMethodsFileAsyncTask = new PrePostUIMethods() {
            ProgressDialog progressDialog = new ProgressDialog(getApplicationContext());

            @Override
            public void preTaskUIMethod() {
                if (progressDialog != null) {
                    progressDialog = ProgressDialog.show(MobiComAttachmentSelectorActivity.this, MobiComAttachmentSelectorActivity.this.getString(R.string.wait),
                            MobiComAttachmentSelectorActivity.this.getString(R.string.km_contacts_loading_info), true);
                }
            }

            @Override
            public void postTaskUIMethod(Uri selectedFileUri, boolean completed, File file) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                if (file == null) {
                    return;
                }
                Uri uri = Uri.parse(file.getAbsolutePath());
                addUri(uri);
                imagesAdapter.notifyDataSetChanged();
            }
        };
    }

    boolean validateCaptionTextAndShowDialog() {
        if (!TextUtils.isEmpty(messageEditText.getText().toString().trim())) {
            String inputMessage = messageEditText.getText().toString();
            String[] inputMsg = inputMessage.toLowerCase().split(" ");
            List<String> userInputList = Arrays.asList(inputMsg);

            boolean disjointResult = (restrictedWords == null) || disjoint(restrictedWords, userInputList);
            boolean restrictedWordMatches;

            try {
                String dynamicRegex = KommunicateSetting.getInstance(this).getRestrictedWordsRegex();
                String pattern = !TextUtils.isEmpty(dynamicRegex) ? dynamicRegex : (alCustomizationSettings != null
                        && !TextUtils.isEmpty(alCustomizationSettings.getRestrictedWordRegex()) ? alCustomizationSettings.getRestrictedWordRegex() : "");

                restrictedWordMatches = !TextUtils.isEmpty(pattern) && Pattern.compile(pattern).matcher(inputMessage.trim()).matches();
            } catch (PatternSyntaxException e) {
                Utils.printLog(this, TAG, "The Regex to match message is invalid");
                e.printStackTrace();
                return false;
            }

            if (!(disjointResult && !restrictedWordMatches)) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this).
                        setPositiveButton(R.string.ok_alert, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                            }
                        });
                alertDialog.setTitle(alCustomizationSettings.getRestrictedWordMessage());
                alertDialog.setCancelable(true);
                alertDialog.create().show();
                return false;
            }
        }
        return true;
    }

    private void addUri(Uri uri) {
        attachmentFileList.add(uri);
        Utils.printLog(MobiComAttachmentSelectorActivity.this, TAG, "attachmentFileList  :: " + attachmentFileList);
    }

    private void setUpGridView() {
        imagesAdapter = new MobiComAttachmentGridViewAdapter(
                MobiComAttachmentSelectorActivity.this,
                attachmentFileList,
                alCustomizationSettings,
                imageUri != null,
                kmAttachmentsController.getFilterOptions(alCustomizationSettings),
                () -> {
                    openFileChooser();
                    return null;
                }
        );
        galleryImagesGridView.setAdapter(imagesAdapter);
    }

    protected void processFile(Uri uri) {
        Utils.printLog(MobiComAttachmentSelectorActivity.this, TAG, "selectedFileUri :: " + uri);
        int returnCode = kmAttachmentsController.processFile(uri, alCustomizationSettings, prePostUIMethodsFileAsyncTask);

        switch (returnCode) {
            case KmAttachmentsController.MAX_SIZE_EXCEEDED:
                KmToast.error(this, R.string.info_attachment_max_allowed_file_size, Toast.LENGTH_LONG).show();
                break;
            case KmAttachmentsController.MIME_TYPE_EMPTY:
                Utils.printLog(this, TAG, "URI mime type is empty.");
                break;
            case KmAttachmentsController.MIME_TYPE_NOT_SUPPORTED:
                KmToast.error(this, R.string.info_file_attachment_mime_type_not_supported, Toast.LENGTH_LONG).show();
                break;
            case KmAttachmentsController.FORMAT_EMPTY:
                Utils.printLog(this, TAG, "URI format(extension) is empty.");
                break;
        }
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
