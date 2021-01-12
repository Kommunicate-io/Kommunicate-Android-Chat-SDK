package com.applozic.mobicomkit.uiwidgets.conversation.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.broadcast.ConnectivityReceiver;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.KommunicateSetting;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.adapter.MobiComAttachmentGridViewAdapter;
import com.applozic.mobicomkit.uiwidgets.kommunicate.KmAttachmentsController;
import com.applozic.mobicomkit.uiwidgets.kommunicate.callbacks.PrePostUIMethods;
import com.applozic.mobicomkit.uiwidgets.kommunicate.utils.KmThemeHelper;
import com.applozic.mobicomkit.uiwidgets.kommunicate.views.KmToast;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.json.GsonUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import io.kommunicate.utils.KmUtils;

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
    AlCustomizationSettings alCustomizationSettings;
    FileClientService fileClientService;
    Uri imageUri;
    String userId, displayName, groupName;
    Integer groupId;
    MobiComUserPreference userPreferences;
    private String TAG = "MultiAttActivity";
    private Button sendAttachment;
    private Button cancelAttachment;
    private EditText messageEditText;
    private ConnectivityReceiver connectivityReceiver;
    private GridView galleryImagesGridView;
    List<String> restrictedWords;
    private ArrayList<Uri> attachmentFileList = new ArrayList<>();
    private MobiComAttachmentGridViewAdapter imagesAdapter;

    KmAttachmentsController kmAttachmentsController;
    PrePostUIMethods prePostUIMethodsFileAsyncTask;

    /**
     * will open either the general file attachment chooser
     */
    void openFileChooser() {
        Intent contentChooserIntent = FileUtils.createGetContentIntent(kmAttachmentsController.getFilterOptions(alCustomizationSettings), getPackageManager());
        contentChooserIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        Intent intentPick = Intent.createChooser(contentChooserIntent, getString(R.string.select_file));
        startActivityForResult(intentPick, REQUEST_CODE_ATTACH_PHOTO);
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
    }

    /**
     * initialize the views
     */
    private void initViews() {
        sendAttachment = (Button) findViewById(R.id.mobicom_attachment_send_btn);
        cancelAttachment = (Button) findViewById(R.id.mobicom_attachment_cancel_btn);
        galleryImagesGridView = (GridView) findViewById(R.id.mobicom_attachment_grid_View);
        messageEditText = (EditText) findViewById(R.id.mobicom_attachment_edit_text);

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
            ProgressDialog progressDialog;

            @Override
            public void preTaskUIMethod() {
                progressDialog = ProgressDialog.show(MobiComAttachmentSelectorActivity.this, MobiComAttachmentSelectorActivity.this.getString(R.string.wait),
                        MobiComAttachmentSelectorActivity.this.getString(R.string.km_contacts_loading_info), true);
            }

            @Override
            public void postTaskUIMethod(boolean completed, File file) {
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
        imagesAdapter = new MobiComAttachmentGridViewAdapter(MobiComAttachmentSelectorActivity.this, attachmentFileList, alCustomizationSettings, imageUri != null, kmAttachmentsController.getFilterOptions(alCustomizationSettings));
        galleryImagesGridView.setAdapter(imagesAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
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
            e.printStackTrace();
        }

        if (resultCode == Activity.RESULT_OK) {
            Uri selectedFileUri = (intent == null ? null : intent.getData());
            Utils.printLog(MobiComAttachmentSelectorActivity.this, TAG, "selectedFileUri :: " + selectedFileUri);
            int returnCode = kmAttachmentsController.processFile(selectedFileUri, alCustomizationSettings, prePostUIMethodsFileAsyncTask);

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
        super.onActivityResult(requestCode, resultCode, intent);
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
