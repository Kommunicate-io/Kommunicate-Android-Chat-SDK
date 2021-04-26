package com.applozic.mobicomkit.uiwidgets.conversation.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.api.conversation.SyncCallService;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.webview.KmWebViewActivity;
import com.applozic.mobicomkit.uiwidgets.kommunicate.utils.KmThemeHelper;
import com.applozic.mobicomkit.uiwidgets.kommunicate.views.KmToast;
import com.google.android.material.snackbar.Snackbar;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.account.register.RegisterUserClientService;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MessageIntentService;
import com.applozic.mobicomkit.api.conversation.MobiComMessageService;
import com.applozic.mobicomkit.api.conversation.service.ConversationService;
import com.applozic.mobicomkit.api.people.UserIntentService;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.broadcast.ConnectivityReceiver;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.KommunicateSetting;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.MessageCommunicator;
import com.applozic.mobicomkit.uiwidgets.conversation.MobiComKitBroadcastReceiver;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.ConversationFragment;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.MobiComQuickConversationFragment;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.MultimediaOptionFragment;
import com.applozic.mobicomkit.uiwidgets.instruction.KmPermissions;
import com.applozic.mobicomkit.uiwidgets.instruction.InstructionUtil;
import com.applozic.mobicomkit.uiwidgets.kommunicate.KmAttachmentsController;
import com.applozic.mobicomkit.uiwidgets.kommunicate.callbacks.PrePostUIMethods;

import io.kommunicate.async.KmAutoSuggestionsAsyncTask;
import io.kommunicate.utils.KmConstants;
import io.kommunicate.utils.KmUtils;

import com.applozic.mobicomkit.uiwidgets.kommunicate.utils.KmHelper;
import com.applozic.mobicomkit.uiwidgets.uilistener.CustomToolbarListener;
import com.applozic.mobicomkit.uiwidgets.uilistener.KmActionCallback;
import com.applozic.mobicomkit.uiwidgets.uilistener.KmStoragePermission;
import com.applozic.mobicomkit.uiwidgets.uilistener.KmStoragePermissionListener;
import com.applozic.mobicomkit.uiwidgets.uilistener.MobicomkitUriListener;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.commons.core.utils.PermissionsUtils;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.SearchListFragment;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.channel.Conversation;
import com.applozic.mobicommons.people.contact.Contact;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.applozic.mobicomkit.uiwidgets.conversation.fragment.MultimediaOptionFragment.REQUEST_CODE_MULTI_SELECT_GALLERY;


/**
 * Created by devashish on 6/25/2015.
 */
public class ConversationActivity extends AppCompatActivity implements MessageCommunicator, MobiComKitActivityInterface, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ActivityCompat.OnRequestPermissionsResultCallback, MobicomkitUriListener, SearchView.OnQueryTextListener, OnClickReplyInterface, KmStoragePermissionListener, CustomToolbarListener {

    public static final int LOCATION_SERVICE_ENABLE = 1001;
    public static final String TAKE_ORDER = "takeOrder";
    public static final String CONTACT = "contact";
    public static final String CHANNEL = "channel";
    public static final String CONVERSATION_ID = "conversationId";
    public static final String GOOGLE_API_KEY_META_DATA = "com.google.android.geo.API_KEY";
    public static final String ACTIVITY_TO_OPEN_ONCLICK_OF_CALL_BUTTON_META_DATA = "activity.open.on.call.button.click";
    protected static final long UPDATE_INTERVAL = 500;
    protected static final long FASTEST_INTERVAL = 1;
    private static final String LOAD_FILE = "loadFile";
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final String API_KYE_STRING = "YOUR_GEO_API_KEY";
    private static final String CAPTURED_IMAGE_URI = "capturedImageUri";
    private static final String CAPTURED_VIDEO_URI = "capturedVideoUri";
    private static final String SHARE_TEXT = "share_text";
    public static final String CONTACTS_GROUP_ID = "CONTACTS_GROUP_ID";
    private static final String TAG = "ConversationActivity";
    private static Uri capturedImageUri;
    private static String inviteMessage;
    private static int retry;
    public Contact contact;
    public LinearLayout layout;
    public Integer currentConversationId;
    public Snackbar snackbar;
    protected ConversationFragment conversation;
    protected MobiComQuickConversationFragment quickConversationFragment;
    protected MobiComKitBroadcastReceiver mobiComKitBroadcastReceiver;
    protected ActionBar mActionBar;
    protected GoogleApiClient googleApiClient;
    String geoApiKey;
    String activityToOpenOnClickOfCallButton;
    int resourceId;
    RelativeLayout childFragmentLayout;
    MobiComMessageService mobiComMessageService;
    AlCustomizationSettings alCustomizationSettings;
    ConnectivityReceiver connectivityReceiver;
    File mediaFile;
    File profilePhotoFile;
    SyncAccountStatusAsyncTask accountStatusAsyncTask;
    String contactsGroupId;
    private LocationRequest locationRequest;
    private Channel channel;
    private BaseContactService baseContactService;
    private KmPermissions applozicPermission;
    private Uri videoFileUri;
    private Uri imageUri;
    private ConversationUIService conversationUIService;
    private SearchView searchView;
    private String searchTerm;
    private SearchListFragment searchListFragment;
    private LinearLayout serviceDisconnectionLayout;
    private KmStoragePermission alStoragePermission;
    private RelativeLayout customToolbarLayout;
    KmAttachmentsController kmAttachmentsController;
    PrePostUIMethods prePostUIMethods;

    public ConversationActivity() {

    }

    public static void addFragment(FragmentActivity fragmentActivity, Fragment fragmentToAdd, String fragmentTag) {
        FragmentManager supportFragmentManager = fragmentActivity.getSupportFragmentManager();

        // Fragment activeFragment = UIService.getActiveFragment(fragmentActivity);
        FragmentTransaction fragmentTransaction = supportFragmentManager
                .beginTransaction();
        fragmentTransaction.replace(R.id.layout_child_activity, fragmentToAdd,
                fragmentTag);

        if (supportFragmentManager.getBackStackEntryCount() > 1
                && !ConversationUIService.MESSGAE_INFO_FRAGMENT.equalsIgnoreCase(fragmentTag) && !ConversationUIService.USER_PROFILE_FRAMENT.equalsIgnoreCase(fragmentTag)) {
            supportFragmentManager.popBackStackImmediate();
        }

        fragmentTransaction.addToBackStack(fragmentTag);
        fragmentTransaction.commitAllowingStateLoss();
        supportFragmentManager.executePendingTransactions();
        //Log.i(TAG, "BackStackEntryCount: " + supportFragmentManager.getBackStackEntryCount());
    }

    public static Uri getCapturedImageUri() {
        return capturedImageUri;
    }

    public static void setCapturedImageUri(Uri capturedImageUri) {
        ConversationActivity.capturedImageUri = capturedImageUri;
    }

    @Override
    public void showErrorMessageView(String message) {
        try {
            layout.setVisibility(View.VISIBLE);
            snackbar = Snackbar.make(layout, message, Snackbar.LENGTH_LONG);
            snackbar.setAction(this.getString(R.string.ok_alert), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    snackbar.dismiss();
                }
            });
            snackbar.setDuration(Snackbar.LENGTH_LONG);
            ViewGroup group = (ViewGroup) snackbar.getView();
            TextView textView = (TextView) group.findViewById(R.id.snackbar_action);
            textView.setTextColor(Color.YELLOW);
            group.setBackgroundColor(getResources().getColor(R.color.error_background_color));
            TextView txtView = (TextView) group.findViewById(R.id.snackbar_text);
            txtView.setMaxLines(5);
            snackbar.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void retry() {
        retry++;
    }

    @Override
    public int getRetryCount() {
        return retry;
    }

    public void dismissErrorMessage() {
        if (snackbar != null) {
            snackbar.dismiss();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Applozic.disconnectPublish(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Applozic.connectPublishWithVerifyToken(this, getString(R.string.please_wait_info));
        syncMessages();
        if (!Utils.isInternetAvailable(getApplicationContext())) {
            String errorMessage = getResources().getString(R.string.internet_connection_not_available);
            showErrorMessageView(errorMessage);
        }
    }

    protected void syncMessages() {
        SyncCallService.getInstance(this).syncMessages(null);
    }

    @Override
    protected void onPause() {
        //ApplozicMqttService.getInstance(this).unSubscribe();
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable(CONTACT, contact);
        savedInstanceState.putSerializable(CHANNEL, channel);
        savedInstanceState.putSerializable(CONVERSATION_ID, currentConversationId);

        if (capturedImageUri != null) {
            savedInstanceState.putString(CAPTURED_IMAGE_URI, capturedImageUri.toString());
        }
        if (videoFileUri != null) {
            savedInstanceState.putString(CAPTURED_VIDEO_URI, videoFileUri.toString());
        }
        if (mediaFile != null) {
            savedInstanceState.putSerializable(LOAD_FILE, mediaFile);
        }

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                Intent upIntent = KommunicateSetting.getInstance(this).getParentActivityIntent(this);
                if (upIntent != null && isTaskRoot()) {
                    TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
                }
                ConversationActivity.this.finish();
                return true;
            }
            boolean takeOrder = getIntent().getBooleanExtra(TAKE_ORDER, false);
            if (takeOrder && getSupportFragmentManager().getBackStackEntryCount() == 2) {
                try {
                    String parentActivity = KommunicateSetting.getInstance(this).getParentActivityName(this);
                    if (parentActivity != null) {
                        Intent intent = new Intent(this, Class.forName(parentActivity));
                        startActivity(intent);
                    }
                    ConversationActivity.this.finish();
                    return true;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                getSupportFragmentManager().popBackStack();
            }
            Utils.toggleSoftKeyBoard(this, true);
            return true;
        } else if (serviceDisconnectionLayout != null && serviceDisconnectionLayout.getVisibility() == View.VISIBLE) {
            ConversationActivity.this.finish();
        } else {
            super.onSupportNavigateUp();
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApplozicService.initWithContext(this);
        String jsonString = FileUtils.loadSettingsJsonFile(getApplicationContext());
        if (!TextUtils.isEmpty(jsonString)) {
            alCustomizationSettings = (AlCustomizationSettings) GsonUtils.getObjectFromJson(jsonString, AlCustomizationSettings.class);
        } else {
            alCustomizationSettings = new AlCustomizationSettings();
        }
        if (!TextUtils.isEmpty(alCustomizationSettings.getChatBackgroundImageName())) {
            resourceId = getResources().getIdentifier(alCustomizationSettings.getChatBackgroundImageName(), "drawable", getPackageName());
        }
        if (resourceId != 0) {
            getWindow().setBackgroundDrawableResource(resourceId);
        }
        setContentView(R.layout.quickconversion_activity);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);

        customToolbarLayout = myToolbar.findViewById(R.id.custom_toolbar_root_layout);
        myToolbar.setBackgroundColor(KmThemeHelper.getInstance(this, alCustomizationSettings).getToolbarColor());
        customToolbarLayout.setBackgroundColor(KmThemeHelper.getInstance(this, alCustomizationSettings).getToolbarColor());
        KmUtils.setStatusBarColor(this, KmThemeHelper.getInstance(this, alCustomizationSettings).getStatusBarColor());

        setSupportActionBar(myToolbar);
        setToolbarTitleSubtitleColorFromSettings();

        baseContactService = new AppContactService(this);
        conversationUIService = new ConversationUIService(this);
        mobiComMessageService = new MobiComMessageService(this, MessageIntentService.class);
        quickConversationFragment = new MobiComQuickConversationFragment();
        connectivityReceiver = new ConnectivityReceiver();
        geoApiKey = Applozic.getInstance(this).getGeoApiKey();
        activityToOpenOnClickOfCallButton = Utils.getMetaDataValue(getApplicationContext(), ACTIVITY_TO_OPEN_ONCLICK_OF_CALL_BUTTON_META_DATA);
        layout = (LinearLayout) findViewById(R.id.footerAd);
        applozicPermission = new KmPermissions(this, layout);
        childFragmentLayout = (RelativeLayout) findViewById(R.id.layout_child_activity);
        contactsGroupId = MobiComUserPreference.getInstance(this).getContactsGroupId();
        serviceDisconnectionLayout = findViewById(R.id.serviceDisconnectionLayout);
        if (Utils.hasMarshmallow() && !alCustomizationSettings.isGlobalStoragePermissionDisabled()) {
            applozicPermission.checkRuntimePermissionForStorage();
        }

        mActionBar = getSupportActionBar();

        inviteMessage = Utils.getMetaDataValue(getApplicationContext(), SHARE_TEXT);
        retry = 0;

        if (KmUtils.isServiceDisconnected(this, alCustomizationSettings != null && alCustomizationSettings.isAgentApp(), customToolbarLayout)) {
            serviceDisconnectionLayout.setVisibility(View.VISIBLE);
        } else {
            if (savedInstanceState != null) {
                capturedImageUri = savedInstanceState.getString(CAPTURED_IMAGE_URI) != null ?
                        Uri.parse(savedInstanceState.getString(CAPTURED_IMAGE_URI)) : null;
                videoFileUri = savedInstanceState.getString(CAPTURED_VIDEO_URI) != null ?
                        Uri.parse(savedInstanceState.getString(CAPTURED_VIDEO_URI)) : null;
                mediaFile = savedInstanceState.getSerializable(LOAD_FILE) != null ? (File) savedInstanceState.getSerializable(LOAD_FILE) : null;

                contact = (Contact) savedInstanceState.getSerializable(CONTACT);
                channel = (Channel) savedInstanceState.getSerializable(CHANNEL);
                currentConversationId = savedInstanceState.getInt(CONVERSATION_ID);
                if (contact != null || channel != null) {
                    if (channel != null) {
                        conversation = ConversationUIService.getConversationFragment(this, null, channel, currentConversationId, null, null, null);
                    } else {
                        conversation = ConversationUIService.getConversationFragment(this, contact, null, currentConversationId, null, null, null);
                    }
                    addFragment(this, conversation, ConversationUIService.CONVERSATION_FRAGMENT);
                }
            } else {
                setSearchListFragment(quickConversationFragment);
                addFragment(this, quickConversationFragment, ConversationUIService.QUICK_CONVERSATION_FRAGMENT);
            }
        }

        mobiComKitBroadcastReceiver = new MobiComKitBroadcastReceiver(this);
        InstructionUtil.showInfo(this, R.string.info_message_sync, BroadcastService.INTENT_ACTIONS.INSTRUCTION.toString());

        mActionBar.setTitle(R.string.conversations);

        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);

        kmAttachmentsController = new KmAttachmentsController(this);

        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        onNewIntent(getIntent());

        Boolean takeOrder = getIntent().getBooleanExtra(TAKE_ORDER, false);

        if (!takeOrder) {
            Intent lastSeenStatusIntent = new Intent(this, UserIntentService.class);
            lastSeenStatusIntent.putExtra(UserIntentService.USER_LAST_SEEN_AT_STATUS, true);
            UserIntentService.enqueueWork(this, lastSeenStatusIntent);
        }

        if (ApplozicClient.getInstance(this).isAccountClosed() || ApplozicClient.getInstance(this).isNotAllowed()) {
            accountStatusAsyncTask = new SyncAccountStatusAsyncTask(this, layout, snackbar);
            accountStatusAsyncTask.execute();
        }
        registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        if (getIntent() != null) {
            Set<String> userIdLists = new HashSet<String>();
            if (getIntent().getStringArrayListExtra(ConversationUIService.GROUP_NAME_LIST_CONTACTS) != null) {
                MobiComUserPreference.getInstance(this).setIsContactGroupNameList(true);
                userIdLists.addAll(getIntent().getStringArrayListExtra(ConversationUIService.GROUP_NAME_LIST_CONTACTS));
            } else if (getIntent().getStringArrayListExtra(ConversationUIService.GROUP_ID_LIST_CONTACTS) != null) {
                MobiComUserPreference.getInstance(this).setIsContactGroupNameList(false);
                userIdLists.addAll(getIntent().getStringArrayListExtra(ConversationUIService.GROUP_ID_LIST_CONTACTS));
            }

            if (!userIdLists.isEmpty()) {
                MobiComUserPreference.getInstance(this).setContactGroupIdList(userIdLists);
            }
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(mobiComKitBroadcastReceiver, BroadcastService.getIntentFilter());

        if (KmUtils.isAgent(this)) {
            new KmAutoSuggestionsAsyncTask(this, null).execute();
        }

        //the pre and post attachment write async task callbacks
        prePostUIMethods = new PrePostUIMethods() {
            @Override
            public void preTaskUIMethod() {
                //TODO: add progress bar
            }

            @Override
            public void postTaskUIMethod(boolean completed, File file) {
                //TODO: add the progress bar in the feedback fragment commit to here
                //TODO: conversationUIService.getConversationFragment().hideProgressBar();
                conversationUIService.sendAttachments(new ArrayList<>(Arrays.asList(Uri.parse(file.getAbsolutePath()))), "");
            }
        };
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //setIntent(intent);
        if (!MobiComUserPreference.getInstance(this).isLoggedIn()) {
            //user is not logged in
            Utils.printLog(this, "AL", "user is not logged in yet.");
            return;
        }

        if (customToolbarLayout != null) {
            customToolbarLayout.setVisibility(View.GONE);
        }

        try {
            if (KmUtils.isServiceDisconnected(this, alCustomizationSettings != null && alCustomizationSettings.isAgentApp(), customToolbarLayout)) {
                serviceDisconnectionLayout.setVisibility(View.VISIBLE);
            } else {
                if (intent.getExtras() != null) {
                    BroadcastService.setContextBasedChat(intent.getExtras().getBoolean(ConversationUIService.CONTEXT_BASED_CHAT));
                    if (BroadcastService.isIndividual() && intent.getExtras().getBoolean(MobiComKitConstants.QUICK_LIST)) {
                        setSearchListFragment(quickConversationFragment);
                        addFragment(this, quickConversationFragment, ConversationUIService.QUICK_CONVERSATION_FRAGMENT);
                    } else {
                        conversationUIService.checkForStartNewConversation(intent);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showActionBar(boolean show) {
        mActionBar.setDisplayShowTitleEnabled(show);
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        showActionBar(true);
        //return false;
        getMenuInflater().inflate(R.menu.mobicom_basic_menu_for_normal_message, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        if (Utils.hasICS()) {
            searchItem.collapseActionView();
        }
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(true);
        searchView.setIconified(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            conversationUIService.onActivityResult(requestCode, resultCode, data);

            if (requestCode == LOCATION_SERVICE_ENABLE) {
                if (((LocationManager) getSystemService(Context.LOCATION_SERVICE))
                        .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    googleApiClient.connect();
                } else {
                    KmToast.error(ConversationActivity.this, R.string.unable_to_fetch_location, Toast.LENGTH_LONG).show();
                }
                return;
            }

            if (requestCode == REQUEST_CODE_MULTI_SELECT_GALLERY) {
                try {
                    final ClipData clipData = data.getClipData();
                    if (clipData == null) {
                        final Uri singleFileUri = (data == null ? null : data.getData());
                        int returnCode = kmAttachmentsController.processFile(singleFileUri, alCustomizationSettings, prePostUIMethods);
                        doReturnCodeActions(returnCode);
                    } else {
                        int itemCount = clipData.getItemCount();
                        if (itemCount > KmAttachmentsController.NO_OF_MULTI_SELECTIONS_ALLOWED) {
                            KmToast.error(this, R.string.mobicom_max_attachment_warning, Toast.LENGTH_SHORT).show();
                        } else {
                            for (int i = 0; i < itemCount; i++) {
                                int returnCode = kmAttachmentsController.processFile(clipData.getItemAt(i).getUri(), alCustomizationSettings, prePostUIMethods);
                                doReturnCodeActions(returnCode);
                            }
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doReturnCodeActions(int returnCode) {
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

    public void setToolbarTitleSubtitleColorFromSettings() {
        if (customToolbarLayout == null) {
            return;
        }

        KmThemeHelper kmThemeHelper = KmThemeHelper.getInstance(this, alCustomizationSettings);
        ((TextView) customToolbarLayout.findViewById(R.id.toolbar_title)).setTextColor(kmThemeHelper.getToolbarTitleColor());
        ((TextView) customToolbarLayout.findViewById(R.id.toolbar_subtitle)).setTextColor(kmThemeHelper.getToolbarSubtitleColor());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PermissionsUtils.REQUEST_STORAGE) {
            if (alStoragePermission != null) {
                alStoragePermission.onAction(PermissionsUtils.verifyPermissions(grantResults));
            }
            if (PermissionsUtils.verifyPermissions(grantResults)) {
                showSnackBar(R.string.storage_permission_granted);
            } else {
                showSnackBar(R.string.storage_permission_not_granted);
            }
        } else if (requestCode == KmPermissions.REQUEST_STORAGE_ATTACHMENT) {
            if (alStoragePermission != null) {
                alStoragePermission.onAction(PermissionsUtils.verifyPermissions(grantResults));
            }
            if (PermissionsUtils.verifyPermissions(grantResults)) {
                showSnackBar(R.string.storage_permission_granted);
                processAttachment();
            } else {
                showSnackBar(R.string.storage_permission_not_granted);
            }
        } else if (requestCode == KmPermissions.REQUEST_STORAGE_MULTI_SELECT_GALLERY) {
            if (alStoragePermission != null) {
                alStoragePermission.onAction(PermissionsUtils.verifyPermissions(grantResults));
            }
            if (PermissionsUtils.verifyPermissions(grantResults)) {
                showSnackBar(R.string.storage_permission_granted);
                processMultiSelectGallery();
            } else {
                showSnackBar(R.string.storage_permission_not_granted);
            }
        } else if (requestCode == PermissionsUtils.REQUEST_LOCATION) {
            if (PermissionsUtils.verifyPermissions(grantResults)) {
                showSnackBar(R.string.location_permission_granted);
                processingLocation();
            } else {
                showSnackBar(R.string.location_permission_not_granted);
            }
        } else if (requestCode == PermissionsUtils.REQUEST_PHONE_STATE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSnackBar(R.string.phone_state_permission_granted);
            } else {
                showSnackBar(R.string.phone_state_permission_not_granted);
            }
        } else if (requestCode == PermissionsUtils.REQUEST_AUDIO_RECORD) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSnackBar(R.string.record_audio_permission_granted);
            } else {
                showSnackBar(R.string.record_audio_permission_not_granted);
            }
        } else if (requestCode == KmPermissions.REQUEST_CAMERA_PHOTO) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSnackBar(R.string.phone_camera_permission_granted);
                processCameraAction();
            } else {
                showSnackBar(R.string.phone_camera_permission_not_granted);
            }
        } else if (requestCode == KmPermissions.REQUEST_CAMERA_VIDEO) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSnackBar(R.string.phone_camera_permission_granted);
                processVideoRecording();
            } else {
                showSnackBar(R.string.phone_camera_permission_not_granted);
            }
        } else if (requestCode == PermissionsUtils.REQUEST_CAMERA_AUDIO) {
            if (PermissionsUtils.verifyPermissions(grantResults)) {
                showSnackBar(R.string.phone_camera_and_audio_permission_granted);
            } else {
                showSnackBar(R.string.audio_or_camera_permission_not_granted);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void processingLocation() {
        if (alCustomizationSettings.isLocationShareViaMap() && !TextUtils.isEmpty(geoApiKey) && !API_KYE_STRING.equals(geoApiKey)) {
            Intent toMapActivity = new Intent(this, MobicomLocationActivity.class);
            startActivityForResult(toMapActivity, MultimediaOptionFragment.REQUEST_CODE_SEND_LOCATION);
        } else {
            //================= START GETTING LOCATION WITHOUT LOADING MAP AND SEND LOCATION AS TEXT===============

            if (!((LocationManager) getSystemService(Context.LOCATION_SERVICE))
                    .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.location_services_disabled_title)
                        .setMessage(R.string.location_services_disabled_message)
                        .setCancelable(false)
                        .setPositiveButton(R.string.location_service_settings, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivityForResult(intent, LOCATION_SERVICE_ENABLE);
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                KmToast.error(ConversationActivity.this, R.string.location_sending_cancelled, Toast.LENGTH_LONG).show();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                googleApiClient.disconnect();
                googleApiClient.connect();
            }

            //=================  END ===============

        }
    }

    @Override
    public boolean isPermissionGranted() {
        return !PermissionsUtils.checkSelfForStoragePermission(this);
    }

    @Override
    public void checkPermission(KmStoragePermission storagePermission) {
        PermissionsUtils.requestPermissions(this, PermissionsUtils.PERMISSIONS_STORAGE, PermissionsUtils.REQUEST_STORAGE);
        this.alStoragePermission = storagePermission;
    }

    public void processLocation() {
        if (Utils.hasMarshmallow()) {
            new KmPermissions(ConversationActivity.this, layout).checkRuntimePermissionForLocation();
        } else {
            processingLocation();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.refresh) {
            KmToast.success(this, getString(R.string.info_message_sync), Toast.LENGTH_LONG).show();
            new SyncMessagesAsyncTask(this).execute();
        } else if (id == R.id.shareOptions) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setAction(Intent.ACTION_SEND)
                    .setType("text/plain").putExtra(Intent.EXTRA_TEXT, inviteMessage);
            startActivity(Intent.createChooser(intent, "Share Via"));
            return super.onOptionsItemSelected(item);
        } else if (id == R.id.logout) {
            LocalBroadcastManager.getInstance(this).sendBroadcastSync(new Intent("KmLogoutOption"));
            try {
                if (!TextUtils.isEmpty(alCustomizationSettings.getLogoutPackage())) {
                    Class loginActivity = Class.forName(alCustomizationSettings.getLogoutPackage().trim());
                    if (loginActivity != null) {
                        if (getApplication() instanceof KmActionCallback) {
                            ((KmActionCallback) getApplication()).onReceive(this, alCustomizationSettings.getLogoutPackage().trim(), "logoutCall");
                        } else {
                            KmHelper.performLogout(this, alCustomizationSettings.getLogoutPackage().trim());
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (ClassCastException e) {

            }
        }
        return false;
    }

    @Override
    public void onQuickConversationFragmentItemClick(View view, Contact contact, Channel channel, Integer conversationId, String searchString) {
        conversation = ConversationUIService.getConversationFragment(this, contact, channel, conversationId, searchString, null, null);
        addFragment(this, conversation, ConversationUIService.CONVERSATION_FRAGMENT);
        this.channel = channel;
        this.contact = contact;
        this.currentConversationId = conversationId;
    }

    @Override
    public void startContactActivityForResult() {

    }

    @Override
    public void addFragment(ConversationFragment conversationFragment) {
        addFragment(this, conversationFragment, ConversationUIService.CONVERSATION_FRAGMENT);
        conversation = conversationFragment;
    }


    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            try {
                Intent upIntent = KommunicateSetting.getInstance(this).getParentActivityIntent(this);
                if (upIntent != null && isTaskRoot()) {
                    TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.finish();
            return;
        }
        Boolean takeOrder = getIntent().getBooleanExtra(TAKE_ORDER, false);
        ConversationFragment conversationFragment = (ConversationFragment) getSupportFragmentManager().findFragmentByTag(ConversationUIService.CONVERSATION_FRAGMENT);
        if (conversationFragment != null && conversationFragment.isVisible() && conversationFragment.isAttachmentOptionsOpen()) {
            conversationFragment.handleAttachmentToggle();
            return;
        }

        if (takeOrder && getSupportFragmentManager().getBackStackEntryCount() == 2) {
            Intent upIntent = KommunicateSetting.getInstance(this).getParentActivityIntent(this);
            if (upIntent != null && isTaskRoot()) {
                TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
            }
            ConversationActivity.this.finish();
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void updateLatestMessage(Message message, String formattedContactNumber) {
        conversationUIService.updateLatestMessage(message, formattedContactNumber);

    }

    @Override
    public void removeConversation(Message message, String formattedContactNumber) {
        conversationUIService.removeConversation(message, formattedContactNumber);
    }

    @Override
    public void onConnected(Bundle bundle) {
        try {
            Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (currentLocation == null) {
                KmToast.success(this, R.string.waiting_for_current_location, Toast.LENGTH_SHORT).show();
                locationRequest = new LocationRequest();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setInterval(UPDATE_INTERVAL);
                locationRequest.setFastestInterval(FASTEST_INTERVAL);
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            }
            if (currentLocation != null && conversation != null) {
                conversation.attachLocation(currentLocation);
            }
        } catch (Exception e) {
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w(((Object) this).getClass().getSimpleName(),
                "onConnectionSuspended() called.");

    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            if (conversation != null && location != null) {
                conversation.attachLocation(location);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            showErrorDialog(connectionResult.getErrorCode());
        }

    }

    public void setChildFragmentLayoutBG() {

        childFragmentLayout.setBackgroundResource(R.color.conversation_list_all_background);
    }

    public void setChildFragmentLayoutBGToTransparent() {

        childFragmentLayout.setBackgroundResource(android.R.color.transparent);
    }

    void showErrorDialog(int code) {
        GooglePlayServicesUtil.getErrorDialog(code, this,
                CONNECTION_FAILURE_RESOLUTION_REQUEST).show();
    }

    public Contact getContact() {
        return contact;
    }

    public Channel getChannel() {
        return channel;
    }

    public Integer getConversationId() {
        return currentConversationId;
    }

    public void showSnackBar(int resId) {
        snackbar = Snackbar.make(layout, resId,
                Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    public Uri getVideoFileUri() {
        return videoFileUri;
    }

    public void setVideoFileUri(Uri videoFileUri) {
        this.videoFileUri = videoFileUri;
    }

    public File getFileObject() {
        return mediaFile;
    }

    public void processVideoCall(Contact contactObj, Integer conversationId) {
        this.contact = baseContactService.getContactById(contactObj.getContactIds());
        if (ApplozicClient.getInstance(getApplicationContext()).isIPCallEnabled()) {
            try {
                if (Utils.hasMarshmallow() && !PermissionsUtils.checkPermissionForCameraAndMicrophone(this)) {
                    applozicPermission.checkRuntimePermissionForCameraAndAudioRecording();
                    return;
                }
                String activityName = KommunicateSetting.getInstance(this).getActivityCallback(KommunicateSetting.RequestCode.VIDEO_CALL);
                Class activityToOpen = Class.forName(activityName);
                Intent intent = new Intent(this, activityToOpen);
                intent.putExtra("CONTACT_ID", contact.getUserId());
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void processCall(Contact contactObj, Integer conversationId) {
        this.contact = baseContactService.getContactById(contactObj.getContactIds());
        this.currentConversationId = conversationId;
        try {

            if (ApplozicClient.getInstance(getApplicationContext()).isIPCallEnabled()) {
                if (Utils.hasMarshmallow() && !PermissionsUtils.checkPermissionForCameraAndMicrophone(this)) {
                    applozicPermission.checkRuntimePermissionForCameraAndAudioRecording();
                    return;
                }
                //Audio Call
                String activityName = KommunicateSetting.getInstance(this).getActivityCallback(KommunicateSetting.RequestCode.AUDIO_CALL);
                Class activityToOpen = Class.forName(activityName);
                Intent intent = new Intent(this, activityToOpen);
                intent.putExtra("CONTACT_ID", contact.getUserId());
                startActivity(intent);
                return;
            }

            if (activityToOpenOnClickOfCallButton != null) {
                Intent callIntent = new Intent(this, Class.forName(activityToOpenOnClickOfCallButton));
                if (currentConversationId != null) {
                    Conversation conversation = ConversationService.getInstance(this).getConversationByConversationId(currentConversationId);
                    callIntent.putExtra(ConversationUIService.TOPIC_ID, conversation.getTopicId());
                }
                callIntent.putExtra(ConversationUIService.CONTACT, contact);
                startActivity(callIntent);
            }
        } catch (Exception e) {
            Utils.printLog(this, "ConversationActivity", "Call permission is not added in androidManifest");
        }
    }


    public void processCameraAction() {
        try {
            if (PermissionsUtils.isCameraPermissionGranted(this)) {
                imageCapture();
            } else {
                if (Utils.hasMarshmallow() && PermissionsUtils.checkSelfForCameraPermission(this)) {
                    applozicPermission.requestCameraPermission(KmPermissions.REQUEST_CAMERA_PHOTO);
                } else {
                    imageCapture();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processVideoRecording() {
        try {
            if (PermissionsUtils.isCameraPermissionGranted(this)) {
                showVideoCapture();
            } else {
                if (Utils.hasMarshmallow() && PermissionsUtils.checkSelfForCameraPermission(this)) {
                    applozicPermission.requestCameraPermission(KmPermissions.REQUEST_CAMERA_VIDEO);
                } else {
                    showVideoCapture();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void imageCapture() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_" + ".jpeg";

            mediaFile = FileClientService.getFilePath(imageFileName, getApplicationContext(), "image/jpeg");

            capturedImageUri = FileProvider.getUriForFile(this, Utils.getMetaDataValue(this, MobiComKitConstants.PACKAGE_NAME) + ".provider", mediaFile);

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ClipData clip =
                        ClipData.newUri(getContentResolver(), "a Photo", capturedImageUri);

                cameraIntent.setClipData(clip);
                cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            } else {
                List<ResolveInfo> resInfoList =
                        getPackageManager()
                                .queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    grantUriPermission(packageName, capturedImageUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    grantUriPermission(packageName, capturedImageUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            }

            if (cameraIntent.resolveActivity(getApplicationContext().getPackageManager()) != null) {
                if (mediaFile != null) {
                    startActivityForResult(cameraIntent, MultimediaOptionFragment.REQUEST_CODE_TAKE_PHOTO);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processAttachment() {
        if (Utils.hasMarshmallow() && PermissionsUtils.checkSelfForStoragePermission(this)) {
            applozicPermission.requestStoragePermissions(KmPermissions.REQUEST_STORAGE_ATTACHMENT);
        } else {
            Intent intentPick = new Intent(this, MobiComAttachmentSelectorActivity.class);
            startActivityForResult(intentPick, MultimediaOptionFragment.REQUEST_MULTI_ATTCAHMENT);
        }
    }

    public void processMultiSelectGallery() {
        if (Utils.hasMarshmallow() && PermissionsUtils.checkSelfForStoragePermission(this)) {
            applozicPermission.requestStoragePermissions(KmPermissions.REQUEST_STORAGE_MULTI_SELECT_GALLERY);
        } else {
            Intent contentChooserIntent = FileUtils.createGetContentIntent(FileUtils.GalleryFilterOptions.IMAGE_VIDEO, getPackageManager());
            contentChooserIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            contentChooserIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            Intent intentPick = Intent.createChooser(contentChooserIntent, getString(R.string.select_file));
            startActivityForResult(intentPick, REQUEST_CODE_MULTI_SELECT_GALLERY);
        }
    }

    public void showVideoCapture() {

        try {
            Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "VID_" + timeStamp + "_" + ".mp4";

            mediaFile = FileClientService.getFilePath(imageFileName, getApplicationContext(), "video/mp4");

            videoFileUri = FileProvider.getUriForFile(this, Utils.getMetaDataValue(this, MobiComKitConstants.PACKAGE_NAME) + ".provider", mediaFile);

            videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoFileUri);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                videoIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                videoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ClipData clip =
                        ClipData.newUri(getContentResolver(), "a Video", videoFileUri);

                videoIntent.setClipData(clip);
                videoIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                videoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            } else {
                List<ResolveInfo> resInfoList =
                        getPackageManager()
                                .queryIntentActivities(videoIntent, PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    grantUriPermission(packageName, videoFileUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    grantUriPermission(packageName, videoFileUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);

                }
            }

            if (videoIntent.resolveActivity(getApplicationContext().getPackageManager()) != null) {
                if (mediaFile != null) {
                    videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                    startActivityForResult(videoIntent, MultimediaOptionFragment.REQUEST_CODE_CAPTURE_VIDEO_ACTIVITY);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public Uri getCurrentImageUri() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_" + ".jpeg";
        profilePhotoFile = FileClientService.getFilePath(imageFileName, getApplicationContext(), "image/jpeg");
        imageUri = FileProvider.getUriForFile(this, Utils.getMetaDataValue(this, MobiComKitConstants.PACKAGE_NAME) + ".provider", profilePhotoFile);
        return imageUri;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        this.searchTerm = query;
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        this.searchTerm = query;
        if (getSearchListFragment() != null) {
            getSearchListFragment().onQueryTextChange(query);
        }
        return true;
    }

    public SearchListFragment getSearchListFragment() {
        return searchListFragment;
    }

    public void setSearchListFragment(SearchListFragment searchListFragment) {
        this.searchListFragment = searchListFragment;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mobiComKitBroadcastReceiver != null) {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(mobiComKitBroadcastReceiver);
            }
            if (connectivityReceiver != null) {
                unregisterReceiver(connectivityReceiver);
            }
            if (accountStatusAsyncTask != null) {
                accountStatusAsyncTask.cancel(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClickOnMessageReply(Message message) {
        if (message != null && conversation != null) {
            conversation.onClickOnMessageReply(message);
        }
    }

    @Override
    public void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    public void setToolbarSubtitle(String subtitle) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(subtitle);
        }
    }

    @Override
    public void setToolbarImage(Contact contact, Channel channel) {

    }

    @Override
    public void hideSubtitleAndProfilePic() {

    }

    private class SyncMessagesAsyncTask extends AsyncTask<Boolean, Void, Void> {
        MobiComMessageService messageService;

        public SyncMessagesAsyncTask(Context context) {
            messageService = new MobiComMessageService(context, MessageIntentService.class);
        }

        protected Void doInBackground(Boolean... parms) {
            messageService.syncMessages();
            return null;
        }
    }

    public class SyncAccountStatusAsyncTask extends AsyncTask<Void, Void, Boolean> {
        Context context;
        RegisterUserClientService registerUserClientService;
        String loggedInUserId;
        ApplozicClient applozicClient;
        WeakReference<Snackbar> snackBarWeakReference;
        WeakReference<LinearLayout> linearLayoutWeakReference;

        public SyncAccountStatusAsyncTask(Context context, LinearLayout linearLayout, Snackbar snackbar) {
            this.context = context;
            this.registerUserClientService = new RegisterUserClientService(context);
            this.linearLayoutWeakReference = new WeakReference<LinearLayout>(linearLayout);
            this.snackBarWeakReference = new WeakReference<Snackbar>(snackbar);
            this.applozicClient = ApplozicClient.getInstance(context);
            this.loggedInUserId = MobiComUserPreference.getInstance(context).getUserId();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            User applozicUser = new User();
            applozicUser.setUserId(loggedInUserId);
            try {
                registerUserClientService.updateRegisteredAccount(applozicUser);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }


        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (applozicClient.isAccountClosed() || applozicClient.isNotAllowed()) {
                LinearLayout linearLayout = null;
                Snackbar snackbar = null;
                if (snackBarWeakReference != null) {
                    snackbar = snackBarWeakReference.get();
                }
                if (linearLayoutWeakReference != null) {
                    linearLayout = linearLayoutWeakReference.get();
                }
                if (snackbar != null && linearLayout != null) {
                    snackbar = Snackbar.make(linearLayout, applozicClient.isAccountClosed() ?
                                    R.string.applozic_account_closed : R.string.applozic_free_version_not_allowed_on_release_build,
                            Snackbar.LENGTH_INDEFINITE);
                    snackbar.show();
                }
            }
        }
    }

    public static void openFaq(Activity activity, String url) {
        if (activity != null) {
            Intent faqIntent = new Intent(activity, KmWebViewActivity.class);
            Bundle urlBundle = new Bundle();
            urlBundle.putString(KmConstants.KM_HELPCENTER_URL, url);
            faqIntent.putExtra(KmWebViewActivity.Al_WEB_VIEW_BUNDLE, urlBundle);
            activity.startActivity(faqIntent);
        }
    }
}
