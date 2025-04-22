package io.kommunicate.ui.conversation.fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static io.kommunicate.ui.conversation.richmessaging.models.v2.KmCustomInputModel.UpdateUserDetails;
import static io.kommunicate.ui.utils.KmViewHelper.setDocumentIcon;
import static java.util.Collections.disjoint;

import static io.kommunicate.utils.KmConstants.KM_SUMMARY;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import io.kommunicate.devkit.KommunicateSettings;
import io.kommunicate.devkit.SettingsSharedPreference;
import io.kommunicate.devkit.api.MobiComKitConstants;
import io.kommunicate.devkit.api.account.user.MobiComUserPreference;
import io.kommunicate.devkit.api.account.user.User;
import io.kommunicate.devkit.api.account.user.UserService;
import io.kommunicate.devkit.api.attachment.AttachmentView;
import io.kommunicate.devkit.api.attachment.FileClientService;
import io.kommunicate.devkit.api.attachment.FileMeta;
import io.kommunicate.devkit.api.conversation.MqttIntentService;
import io.kommunicate.devkit.api.conversation.Message;
import io.kommunicate.devkit.api.conversation.MessageClientService;
import io.kommunicate.devkit.api.conversation.MessageIntentService;
import io.kommunicate.devkit.api.conversation.MobiComConversationService;
import io.kommunicate.devkit.api.conversation.SyncCallService;
import io.kommunicate.devkit.api.conversation.database.MessageDatabaseService;
import io.kommunicate.devkit.api.conversation.selfdestruct.DisappearingMessageTask;
import io.kommunicate.devkit.api.conversation.service.ConversationService;
import io.kommunicate.devkit.api.notification.MuteNotificationRequest;
import io.kommunicate.devkit.api.notification.NotificationService;
import io.kommunicate.devkit.api.people.UserIntentService;
import io.kommunicate.devkit.broadcast.EventManager;
import io.kommunicate.devkit.broadcast.BroadcastService;
import io.kommunicate.devkit.channel.service.ChannelService;
import io.kommunicate.devkit.contact.AppContactService;
import io.kommunicate.devkit.contact.MobiComVCFParser;
import io.kommunicate.devkit.contact.VCFContactData;
import io.kommunicate.devkit.contact.database.ContactDatabase;
import io.kommunicate.devkit.feed.ApiResponse;
import io.kommunicate.devkit.feed.GroupInfoUpdate;
import io.kommunicate.devkit.listners.ResultCallback;
import io.kommunicate.devkit.listners.UIEventListener;
import io.kommunicate.ui.CustomizationSettings;
import io.kommunicate.ui.KmFontManager;
import io.kommunicate.ui.KmLinearLayoutManager;
import io.kommunicate.ui.KmSpeechSetting;
import io.kommunicate.ui.KommunicateSetting;
import io.kommunicate.ui.R;
import io.kommunicate.ui.alphanumbericcolor.AlphaNumberColorUtil;
import io.kommunicate.ui.async.KmMessageMetadataUpdateTask;
import io.kommunicate.ui.attachmentview.KmAudioRecordManager;
import io.kommunicate.ui.attachmentview.KommunicateAudioManager;
import io.kommunicate.ui.conversation.ConversationUIService;
import io.kommunicate.ui.conversation.DeleteConversationAsyncTask;
import io.kommunicate.ui.conversation.KmBotTypingDelayManager;
import io.kommunicate.ui.conversation.KmCustomDialog;
import io.kommunicate.ui.conversation.MessageCommunicator;
import io.kommunicate.ui.conversation.MobicomMessageTemplate;
import io.kommunicate.ui.conversation.activity.ConversationActivity;
import io.kommunicate.ui.conversation.activity.MobiComKitActivityInterface;
import io.kommunicate.ui.conversation.activity.RecyclerViewPositionHelper;
import io.kommunicate.ui.conversation.adapter.DetailedConversationAdapter;
import io.kommunicate.ui.conversation.adapter.KmContextSpinnerAdapter;
import io.kommunicate.ui.conversation.adapter.MobicomMessageTemplateAdapter;
import io.kommunicate.ui.conversation.richmessaging.KmRichMessage;
import io.kommunicate.ui.conversation.richmessaging.RichMessageActionProcessor;
import io.kommunicate.ui.conversation.richmessaging.adapters.KmAutoSuggestionArrayAdapter;
import io.kommunicate.ui.conversation.richmessaging.callbacks.KmRichMessageListener;
import io.kommunicate.ui.conversation.richmessaging.models.v2.KmAutoSuggestion;
import io.kommunicate.ui.conversation.richmessaging.models.v2.KmCustomInputModel;
import io.kommunicate.ui.conversation.richmessaging.webview.KmWebViewActivity;
import io.kommunicate.ui.conversation.stt.KmSpeechToText;
import io.kommunicate.ui.conversation.stt.KmTextToSpeech;
import io.kommunicate.ui.instruction.InstructionUtil;
import io.kommunicate.ui.kommunicate.KmPrefSettings;
import io.kommunicate.ui.kommunicate.activities.LeadCollectionActivity;
import io.kommunicate.ui.kommunicate.adapters.KmAutoSuggestionAdapter;
import io.kommunicate.ui.kommunicate.animators.OnBasketAnimationEndListener;
import io.kommunicate.ui.kommunicate.callbacks.KmToolbarClickListener;
import io.kommunicate.ui.kommunicate.settings.KmSpeechToTextSetting;
import io.kommunicate.ui.kommunicate.utils.DimensionsUtils;
import io.kommunicate.ui.kommunicate.utils.KmThemeHelper;
import io.kommunicate.ui.kommunicate.views.KmAwayView;
import io.kommunicate.ui.kommunicate.views.KmConversationInfoView;
import io.kommunicate.ui.kommunicate.views.KmFeedbackView;
import io.kommunicate.ui.kommunicate.views.KmInQueueView;
import io.kommunicate.ui.kommunicate.views.KmLanguageSlideView;
import io.kommunicate.ui.kommunicate.views.KmRecordButton;
import io.kommunicate.ui.kommunicate.views.KmRecordView;
import io.kommunicate.ui.kommunicate.views.KmRecyclerView;
import io.kommunicate.ui.kommunicate.views.KmToast;
import io.kommunicate.ui.kommunicate.views.KmTypingView;
import io.kommunicate.ui.uilistener.ContextMenuClickListener;
import io.kommunicate.ui.uilistener.CustomToolbarListener;
import io.kommunicate.ui.uilistener.KmOnMessageListener;
import io.kommunicate.ui.uilistener.KmOnRecordListener;
import io.kommunicate.ui.uilistener.KmStoragePermission;
import io.kommunicate.ui.uilistener.KmStoragePermissionListener;
import io.kommunicate.ui.usecase.BusinessHoursDetailUseCase;
import io.kommunicate.ui.usecase.UserDetailUseCase;
import io.kommunicate.ui.utils.BusinessHoursUtil;
import io.kommunicate.ui.utils.InsetHelper;
import io.kommunicate.ui.utils.KmViewHelper;
import io.kommunicate.commons.AppContextService;
import io.kommunicate.commons.commons.core.utils.DateUtils;
import io.kommunicate.commons.commons.core.utils.LocationUtils;
import io.kommunicate.commons.commons.core.utils.Utils;
import io.kommunicate.commons.commons.image.ImageCache;
import io.kommunicate.commons.commons.image.ImageLoader;
import io.kommunicate.commons.commons.image.ImageUtils;
import io.kommunicate.commons.emoticon.EmojiconHandler;
import io.kommunicate.commons.file.FileUtils;
import io.kommunicate.commons.json.GsonUtils;
import io.kommunicate.commons.people.channel.Channel;
import io.kommunicate.commons.people.channel.ChannelUserMapper;
import io.kommunicate.commons.people.channel.ChannelUtils;
import io.kommunicate.commons.people.channel.Conversation;
import io.kommunicate.commons.people.contact.Contact;

import io.kommunicate.ui.data.BusinessSettingsResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import annotations.CleanUpRequired;
import de.hdodenhof.circleimageview.CircleImageView;
import io.kommunicate.KmSettings;
import io.kommunicate.Kommunicate;
import io.kommunicate.async.AgentGetStatusTask;
import io.kommunicate.callbacks.KmAwayMessageHandler;
import io.kommunicate.callbacks.KmCharLimitCallback;
import io.kommunicate.callbacks.TaskListener;
import io.kommunicate.database.KmAutoSuggestionDatabase;
import io.kommunicate.models.FeedbackDetailsData;
import io.kommunicate.models.KmApiResponse;
import io.kommunicate.models.KmAutoSuggestionModel;
import io.kommunicate.models.KmFeedback;
import io.kommunicate.preference.KmBotPreference;
import io.kommunicate.preference.KmConversationInfoSetting;
import io.kommunicate.services.KmClientService;
import io.kommunicate.services.KmService;
import io.kommunicate.usecase.GetBotTypeUseCase;
import io.kommunicate.usecase.GetDataUseCase;
import io.kommunicate.usecase.MessageDeleteUseCase;
import io.kommunicate.usecase.MuteGroupNotificationUseCase;
import io.kommunicate.usecase.MuteUserNotificationUseCase;
import io.kommunicate.usecase.UserBlockUseCase;
import io.kommunicate.usecase.WaitingQueueStatusUseCase;
import io.kommunicate.utils.KMAgentStatusHelper;
import io.kommunicate.utils.KmAppSettingPreferences;
import io.kommunicate.utils.KmInputTextLimitUtil;
import io.kommunicate.utils.KmUtils;
import io.sentry.Sentry;

/**
 * reg
 * Created by devashish on 10/2/15.
 */
public abstract class MobiComConversationFragment extends Fragment implements View.OnClickListener, ContextMenuClickListener, KmRichMessageListener, KmOnRecordListener, OnBasketAnimationEndListener, LoaderManager.LoaderCallbacks<Cursor>, FeedbackInputFragment.FeedbackFragmentListener, FeedbackInputFragmentv2.FeedbackSubmissionListener, UIEventListener, KmSpeechToText.KmTextListener, KmBotTypingDelayManager.MessageDispatcher, KMAgentStatusHelper.DynamicAgentStatusChangeListener {

    public FrameLayout emoticonsFrameLayout,
            contextFrameLayout;
    public GridView multimediaPopupGrid;

    private static final String TAG = "MobiComConversation";
    private static final int CHAR_LIMIT_FOR_DIALOG_FLOW_BOT = 256;
    private static final int CHAR_LIMIT_WARNING = 55;

    protected List<Conversation> conversations;
    protected String title = "Conversations";
    protected DownloadConversation downloadConversation;
    protected MobiComConversationService conversationService;
    protected TextView infoBroadcast;
    protected Class messageIntentClass;
    protected TextView emptyTextView;
    protected boolean loadMore = true;
    protected Contact contact;
    protected Channel channel;
    protected Integer currentConversationId;
    protected EditText messageEditText;
    protected ImageButton languageChangeButton;
    protected KmRecordButton recordButton;
    protected ImageButton sendButton;
    protected ImageButton attachButton;
    protected Spinner sendType;
    protected LinearLayout individualMessageSendLayout, mainEditTextLinearLayout;
    protected LinearLayout extendedSendingOptionLayout, attachmentIconLayout;
    protected LinearLayout kmMessageLinearLayout;
    protected RelativeLayout attachmentLayout;
    protected ProgressBar mediaUploadProgressBar;
    protected View spinnerLayout;
    protected SwipeRefreshLayout swipeLayout;
    protected Button scheduleOption;
    protected Spinner selfDestructMessageSpinner;
    protected ImageView mediaContainer;
    protected TextView attachedFile, userNotAbleToChatTextView;
    protected String filePath;
    protected boolean firstTimeMTexterFriend;
    protected MessageCommunicator messageCommunicator;
    protected List<Message> messageList = new ArrayList<Message>();
    protected DetailedConversationAdapter conversationAdapter = null;
    protected Drawable sentIcon;
    protected Drawable deliveredIcon;
    protected Drawable readIcon;
    protected Drawable pendingIcon;
    protected ImageButton emoticonsBtn;
    protected MultimediaOptionFragment multimediaOptionFragment = new MultimediaOptionFragment();
    protected boolean hideExtendedSendingOptionLayout;
    protected SyncCallService syncCallService;
    protected KmContextSpinnerAdapter kmContextSpinnerAdapter;
    protected Message messageToForward;
    protected String searchString;
    protected CustomizationSettings customizationSettings;
    protected String preFilledMessage;
    protected LinearLayout userNotAbleToChatLayout;
    protected List<ChannelUserMapper> channelUserMapperList;
    protected AdapterView.OnItemSelectedListener adapterView;
    protected MessageDatabaseService messageDatabaseService;
    protected AppContactService appContactService;
    protected ConversationUIService conversationUIService;
    protected long millisecond;
    protected MuteNotificationRequest muteNotificationRequest;
    protected List<String> restrictedWords;
    protected RelativeLayout replayRelativeLayout;
    protected ImageButton attachReplyCancelLayout;
    protected TextView nameTextView, messageTextView;
    protected ImageView galleryImageView;
    protected FileClientService fileClientService;
    protected ImageLoader imageThumbnailLoader, messageImageLoader;
    protected ImageView imageViewForAttachmentType;
    protected RelativeLayout imageViewRLayout;
    protected Map<String, String> messageMetaData = new HashMap<>();
    protected KmAudioRecordManager kmAudioRecordManager;
    protected ImageView slideImageView;
    protected EmojiconHandler emojiIconHandler;
    protected Bitmap previewThumbnail;
    protected TextView isTyping, bottomlayoutTextView;
    protected String defaultText;
    protected boolean typingStarted;
    protected Integer channelKey;
    protected Toolbar toolbar;
    protected Menu menu;
    protected Spinner contextSpinner;
    protected boolean onSelected;
    protected ImageCache imageCache;
    protected RecyclerView messageTemplateView;
    protected ImageButton cameraButton, locationButton, fileAttachmentButton, multiSelectGalleryButton, videoButton;
    protected WeakReference<KmRecordButton> recordButtonWeakReference;
    protected RecyclerView recyclerView;
    protected RecyclerViewPositionHelper recyclerViewPositionHelper;
    protected LinearLayoutManager linearLayoutManager;
    protected DetailedConversationAdapter recyclerDetailConversationAdapter;
    protected MobicomMessageTemplate messageTemplate;
    protected MobicomMessageTemplateAdapter templateAdapter;
    protected KmAwayView kmAwayView;
    protected KmInQueueView kmInQueueView;
    protected KmTypingView kmTypingView;
    protected TextView applozicLabel;
    protected RelativeLayout customToolbarLayout;
    protected CircleImageView toolbarImageView;
    protected TextView toolbarTitleText;
    protected TextView toolbarSubtitleText;
    protected TextView toolbarOnlineColorDot;
    protected TextView toolbarOfflineColorDot;
    protected TextView toolbarAwayColorDot;
    protected TextView toolbarAlphabeticImage;
    protected String geoApiKey;
    protected FrameLayout emailReplyReminderLayout;
    protected Contact conversationAssignee;
    protected Boolean agentStatus;
    public static final int TYPING_STOP_TIME = 30;
    public static final String KM_CONVERSATION_SUBJECT = "KM_CONVERSATION_SUBJECT";
    public Map<String, CountDownTimer> typingTimerMap;
    public int loggedInUserRole;
    public static final String AUDIO_RECORD_OPTION = ":audio";
    public static final String MULTI_SELECT_GALLERY_OPTION = ":multiSelectGalleryItems";
    protected KmRecordView recordView;
    protected FrameLayout recordLayout;
    protected boolean isRecording = false;
    protected KmFontManager fontManager;
    protected RelativeLayout takeOverFromBotLayout;
    protected KmRecyclerView kmAutoSuggestionRecycler;
    protected KmAutoSuggestionAdapter kmAutoSuggestionAdapter;
    protected View kmAutoSuggestionDivider;
    protected String loggedInUserId;
    protected String messageSearchString;
    protected FeedbackInputFragment feedBackFragment;

    protected FeedbackInputFragmentv2 feedbackInputFragmentv2;
    protected KmFeedbackView kmFeedbackView;
    protected View mainDivider;
    protected FrameLayout frameLayoutProgressbar;
    protected RichMessageActionProcessor richMessageActionProcessor;
    protected boolean isTextToSpeechEnabled;
    protected boolean isSpeechToTextEnabled;
    protected boolean isSendOnSpeechEnd;
    protected KmTextToSpeech textToSpeech;
    protected KmSpeechToText speechToText;
    protected KmThemeHelper themeHelper;
    protected TextView textViewCharLimitMessage;
    protected TextWatcher messageCharacterLimitTextWatcher;
    protected boolean isAssigneeDialogFlowBot;
    protected int botMessageDelayInterval;
    protected KmBotTypingDelayManager botTypingDelayManager;
    protected boolean isRecordOptionEnabled;
    protected boolean isUserGivingEmail;
    protected RelativeLayout conversationRootLayout;
    private TextView businessSettingsTextView;
    private LinearLayout businessConversationLL;
    protected KmConversationInfoView kmConversationInfoView;
    public static final int STANDARD_HEX_COLOR_CODE_LENGTH = 7;
    public static final int STANDARD_HEX_COLOR_CODE_WITH_OPACITY_LENGTH = 9;
    public boolean isCustomToolbarSubtitleDesign;
    private KmCustomInputModel customInputField;
    private boolean isCustomFieldMessage = false;
    protected boolean isHideAssigneeStatus = false;
    protected boolean isApiAutoSuggest = false;
    protected Map<String, String> autoSuggestHeaders;
    protected String autoSuggestUrl;
    private Set<Message> botDelayMessageList;
    private List<String> localTeams;
    private RelativeLayout faqButtonLayout;
    private Button startNewConv;
    private static final String KMEVENT = "KM_EVENT";
    private static final String QUICK_REPLY_CLICK = "QUICKREPLY_CLICK";
    private static final String SKIP_BOT = "skipBot";
    private static final String CATEGORY = "category";
    private static final String KM_QUICK_REPLY = "KM_EVENT_QUICKREPLY_CLICK";
    private static final String BOT = "bot";
    private static final String HIDDEN = "HIDDEN";
    private static final String VIDEO = "video";
    private static final String AUDIO = "audio";
    private static final String CAMERA = "camera";
    private static final String text_plain = "text/plain";
    private static final String text_card = "text/x-vcard";
    private static final String IMAGE = "image";
    private static final String GALLERY = "gallery";
    private static final String LOCATION = "location";
    private static final String CONTACT = "contact";
    private static final String NAME = "[name]";
    private static final String IMPLEMENT_INTERFACE_COMMUNICATOR = " must implement interfaceDataCommunicator";
    private static final String IS_DONE_WITH_CLICKING = "isDoneWithClicking";
    private static final String APPLOZIC_TEMPLATE_MSG = "io.kommunicate.devkit.TemplateMessage";
    private static final String TEMPLATE_MSG = "templateMessage";
    private static final String APPLI_JSON = "application/json";

    public void setEmojiIconHandler(EmojiconHandler emojiIconHandler) {
        this.emojiIconHandler = emojiIconHandler;
    }

    protected Message lastUserMessage;
    private static final String[] WHATSAPP_SOURCE = {"WHATSAPPCLOUDAPI", "WHATSAPPTWILIO", "WHATSAPPDIALOG360"};
    ;
    private static final String CONVERSATION_SOURCE = "source";

    private LinearLayout messageListLinearLayout;
    private boolean isCurrentlyInDarkMode;

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean newDarkModeStatus = themeHelper.isDarkModeEnabledForSDK();
        if (isCurrentlyInDarkMode != newDarkModeStatus) {
            isCurrentlyInDarkMode = newDarkModeStatus;
            setupModes(newDarkModeStatus);
        }
    }

    public void fetchBotType(Contact contact, TaskListener<String> kmCallback) {
        if (contact != null) {
            String botTypeLocal = KmBotPreference.getInstance(getContext()).getBotType(contact.getUserId());
            if (!TextUtils.isEmpty(botTypeLocal)) {
                kmCallback.onSuccess(botTypeLocal);
            } else {
                GetBotTypeUseCase.executeWithExecutor(getContext(), contact.getUserId(), kmCallback);
            }
        }
    }

    protected DetailedConversationAdapter getConversationAdapter(Activity activity,
                                                                 int rowViewId, List<Message> messageList, Contact contact, Channel channel, Class messageIntentClass, EmojiconHandler emojiIconHandler, CustomizationSettings customizationSettings) {
        return new DetailedConversationAdapter(activity, rowViewId, messageList, contact, channel, messageIntentClass, emojiIconHandler, customizationSettings);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String jsonString = FileUtils.loadSettingsJsonFile(AppContextService.getContext(getContext()));
        geoApiKey = KommunicateSettings.getInstance(getContext()).getGeoApiKey();
        if (!TextUtils.isEmpty(jsonString)) {
            customizationSettings = (CustomizationSettings) GsonUtils.getObjectFromJson(jsonString, CustomizationSettings.class);
        } else {
            customizationSettings = new CustomizationSettings();
        }

        themeHelper = KmThemeHelper.getInstance(getContext(), customizationSettings);
        isSpeechToTextEnabled = customizationSettings.getSpeechToText().isEnabled() || KmPrefSettings.getInstance(getContext()).isSpeechToTextEnabled() || KmSpeechToTextSetting.getInstance(getContext()).isMultipleSpeechToTextEnabled();
        isTextToSpeechEnabled = customizationSettings.getTextToSpeech().isEnabled() || KmPrefSettings.getInstance(getContext()).isTextToSpeechEnabled();
        isSendOnSpeechEnd = customizationSettings.getSpeechToText().isSendMessageOnSpeechEnd() || KmPrefSettings.getInstance(getContext()).isSendMessageOnSpeechEnd();
        botMessageDelayInterval = KmAppSettingPreferences.getInstance().getKmBotMessageDelayInterval();
        botTypingDelayManager = new KmBotTypingDelayManager(getContext(), this);

        if (isTextToSpeechEnabled) {
            textToSpeech = new KmTextToSpeech(getContext(), KmSpeechSetting.getTextToSpeechLanguageCode(getContext(), customizationSettings));
        }

        richMessageActionProcessor = new RichMessageActionProcessor(this);

        fontManager = new KmFontManager(getContext(), customizationSettings);

        restrictedWords = FileUtils.loadRestrictedWordsFile(AppContextService.getContext(getContext()));

        feedBackFragment = new FeedbackInputFragment();
        feedBackFragment.setFeedbackFragmentListener(this);
        //Five Star
        feedbackInputFragmentv2 = new FeedbackInputFragmentv2();
        feedbackInputFragmentv2.setRatingSubmitListener(this);

        conversationUIService = new ConversationUIService(getActivity());
        syncCallService = SyncCallService.getInstance(getActivity());
        appContactService = new AppContactService(getActivity());
        messageDatabaseService = new MessageDatabaseService(getActivity());
        fileClientService = new FileClientService(getActivity());
        setHasOptionsMenu(true);
        imageThumbnailLoader = new ImageLoader(AppContextService.getContext(getContext()), ImageUtils.getLargestScreenDimension((Activity) getContext())) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return fileClientService.loadThumbnailImage(getContext(), (Message) data, getImageLayoutParam(false).width, getImageLayoutParam(false).height);
            }
        };

        imageCache = ImageCache.getInstance((getActivity()).getSupportFragmentManager(), 0.1f);
        imageThumbnailLoader.setImageFadeIn(false);
        imageThumbnailLoader.addImageCache((getActivity()).getSupportFragmentManager(), 0.1f);
        messageImageLoader = new ImageLoader(AppContextService.getContext(getContext()), ImageUtils.getLargestScreenDimension((Activity) getContext())) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return fileClientService.loadMessageImage(getContext(), (String) data);
            }
        };
        messageImageLoader.setImageFadeIn(false);
        messageImageLoader.addImageCache((getActivity()).getSupportFragmentManager(), 0.1f);
        kmAudioRecordManager = new KmAudioRecordManager(getActivity());
        KMAgentStatusHelper.setAgentStatusLister(this);
    }

    private void setupModes(boolean isDarkModeEnabled) {
        int currentModeColor = isDarkModeEnabled ? getResources().getColor(R.color.dark_mode_default) : Color.WHITE;
        int currentModePrimaryColor = themeHelper.parseColorWithDefault(KmAppSettingPreferences.getInstance().getPrimaryColor(), getResources().getColor(R.color.dark_mode_default));
        if (getView() != null) {
            kmTypingView = getView().findViewById(R.id.idKmTypingView);
        }

        int iconColor = themeHelper.parseColorWithDefault(customizationSettings.getAttachmentIconsBackgroundColor().get(isDarkModeEnabled ? 1 : 0),
                themeHelper.parseColorWithDefault(customizationSettings.getToolbarColor().get(isDarkModeEnabled ? 1 : 0), currentModePrimaryColor));
        fileAttachmentButton.getDrawable().setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
        emoticonsBtn.getDrawable().setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
        cameraButton.getDrawable().setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
        videoButton.getDrawable().setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
        locationButton.getDrawable().setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
        recordButton.getDrawable().setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);

        messageEditText.setTextColor(themeHelper.parseColorWithDefault(customizationSettings.getMessageEditTextTextColor().get(isDarkModeEnabled ? 1 : 0),
                getResources().getColor(R.color.chatbar_text_color)));
        messageEditText.setHintTextColor(themeHelper.parseColorWithDefault(customizationSettings.getMessageEditTextHintTextColor().get(isDarkModeEnabled ? 1 : 0),
                getResources().getColor(R.color.chatbar_text_color)));

        kmAwayView.setupTheme(isDarkModeEnabled, customizationSettings);
        kmInQueueView.setupTheme(isDarkModeEnabled, customizationSettings);
        kmAwayView.setBackgroundColor(isDarkModeEnabled ? getResources().getColor(R.color.dark_mode_default) : Color.WHITE);
        kmInQueueView.setBackgroundColor(isDarkModeEnabled ? getResources().getColor(R.color.dark_mode_default) : Color.WHITE);
        kmAwayView.getAwayMessageTv().setTextColor(Color.parseColor(isDarkModeEnabled ? customizationSettings.getAwayMessageTextColor().get(1) : customizationSettings.getAwayMessageTextColor().get(0)));
        kmInQueueView.getAwayMessageTv().setTextColor(Color.parseColor(isDarkModeEnabled ? customizationSettings.getAwayMessageTextColor().get(1) : customizationSettings.getAwayMessageTextColor().get(0)));

        individualMessageSendLayout.setBackgroundColor(themeHelper.parseColorWithDefault(
                customizationSettings.getMessageEditTextBackgroundColor().get(isDarkModeEnabled ? 1 : 0), currentModeColor
        ));
        setupDotColorStatus();
        if (recyclerDetailConversationAdapter != null) {
            recyclerDetailConversationAdapter.setupDarkMode(isDarkModeEnabled);
            recyclerDetailConversationAdapter.notifyDataSetChanged();
        }
        setupChatBackground();
    }

    private void setupChatBackground() {

        if (!TextUtils.isEmpty(isCurrentlyInDarkMode ? customizationSettings.getChatBackgroundColorOrDrawable().get(1) : customizationSettings.getChatBackgroundColorOrDrawable().get(0))) {
            String customChatBackground = isCurrentlyInDarkMode ? customizationSettings.getChatBackgroundColorOrDrawable().get(1) : customizationSettings.getChatBackgroundColorOrDrawable().get(0);
            if (customChatBackground.contains("#")) {
                if (customChatBackground.length() == STANDARD_HEX_COLOR_CODE_LENGTH || customChatBackground.length() == STANDARD_HEX_COLOR_CODE_WITH_OPACITY_LENGTH) {
                    conversationRootLayout.setBackgroundColor(Color.parseColor(isCurrentlyInDarkMode ? customizationSettings.getChatBackgroundColorOrDrawable().get(1) : customizationSettings.getChatBackgroundColorOrDrawable().get(0)));
                }
            } else {
                Resources resources = getResources();
                int resourceId = resources.getIdentifier(customChatBackground, "drawable", getContext().getPackageName());
                if (resourceId != 0) {
                    conversationRootLayout.setBackgroundResource(resourceId);
                }
            }
        } else {
            conversationRootLayout.setBackgroundColor(isCurrentlyInDarkMode ? Color.BLACK : Color.WHITE);
        }

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View list = inflater.inflate(R.layout.message_list, container, false);
        conversationRootLayout = (RelativeLayout) list.findViewById(R.id.rl_conversation_layout);
        businessConversationLL = (LinearLayout) list.findViewById(R.id.business_conversation_ll);
        businessSettingsTextView = (TextView) list.findViewById(R.id.business_conversation);
        businessSettingsTextView.setBackgroundColor(themeHelper.getToolbarColor());
        attachmentIconLayout = (LinearLayout) list.findViewById(R.id.attachment_icon_layout);
        recyclerView = (RecyclerView) list.findViewById(R.id.messageList);
        linearLayoutManager = new KmLinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerViewPositionHelper = new RecyclerViewPositionHelper(recyclerView, linearLayoutManager);
        ((ConversationActivity) getActivity()).setChildFragmentLayoutBGToTransparent();
        messageList = new ArrayList<Message>();
        multimediaPopupGrid = (GridView) list.findViewById(R.id.multimedia_options1);
        textViewCharLimitMessage = list.findViewById(R.id.botCharLimitTextView);
        kmMessageLinearLayout = list.findViewById(R.id.km_message_linear_layout);
        loggedInUserRole = MobiComUserPreference.getInstance(AppContextService.getContext(getContext())).getUserRoleType();

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        messageListLinearLayout = list.findViewById(R.id.km_message_list_linear_layout);
        isCurrentlyInDarkMode = themeHelper.isDarkModeEnabledForSDK();

        toolbar = (Toolbar) getActivity().findViewById(R.id.my_toolbar);
        toolbar.setClickable(true);
        //customisation to change option menu icon on conversation screen
        if (!TextUtils.isEmpty(customizationSettings.getMenuIconOnConversationScreen())) {
            Drawable overflowIcon = ContextCompat.getDrawable(Objects.requireNonNull(getContext()), getResources().getIdentifier(customizationSettings.getMenuIconOnConversationScreen(), "drawable", getContext().getPackageName()));
            toolbar.setOverflowIcon(overflowIcon);
        }
        setupChatBackground();

        frameLayoutProgressbar = list.findViewById(R.id.idProgressBarLayout);
        startNewConv = list.findViewById(R.id.start_new_conversation);
        startNewConv.setVisibility(GONE);
        customToolbarLayout = toolbar.findViewById(R.id.custom_toolbar_root_layout);
        loggedInUserId = MobiComUserPreference.getInstance(getContext()).getUserId();

        faqButtonLayout = toolbar.findViewById(R.id.faqButtonLayout);
        if (faqButtonLayout != null) {
            faqButtonLayout.setVisibility(View.GONE);
        }
        //Conversation Info screen to show below Topbar
        if (!customizationSettings.isAgentApp()) {
            kmConversationInfoView = list.findViewById(R.id.km_conversation_info_view);
            KmConversationInfoSetting infoSetting = KmConversationInfoSetting.getInstance(getContext());
            if (infoSetting.isKmConversationInfoEnabled()) {
                kmConversationInfoView.setVisibility(VISIBLE);
                kmConversationInfoView.setupView(infoSetting.getInfoContent(), infoSetting.getContentColo(), infoSetting.getBackgroundColor(), infoSetting.getLeadingImageIcon(), infoSetting.getTrailingImageIcon());
            }
        }

        if (customToolbarLayout != null) {
            customToolbarLayout.setVisibility(View.GONE);
            toolbarImageView = customToolbarLayout.findViewById(R.id.conversation_contact_photo);
            toolbarSubtitleText = customToolbarLayout.findViewById(R.id.toolbar_subtitle);
            //To set new custom toolbar design
            newCustomToolbarDesign();
            if (fontManager != null && fontManager.getToolbarSubtitleFont() != null) {
                toolbarSubtitleText.setTypeface(fontManager.getToolbarSubtitleFont());
            }
            toolbarTitleText = customToolbarLayout.findViewById(R.id.toolbar_title);
            if (fontManager != null && fontManager.getToolbarTitleFont() != null) {
                toolbarTitleText.setTypeface(fontManager.getToolbarTitleFont());
            }
            toolbarOnlineColorDot = customToolbarLayout.findViewById(R.id.onlineTextView);
            toolbarOfflineColorDot = customToolbarLayout.findViewById(R.id.offlineTextView);
            toolbarAwayColorDot = customToolbarLayout.findViewById(R.id.awayTextView);
            setupDotColorStatus();

            toolbarAlphabeticImage = customToolbarLayout.findViewById(R.id.toolbarAlphabeticImage);

            TextView faqOption = customToolbarLayout.findViewById(R.id.kmFaqOption);
            TextView refreshOption = customToolbarLayout.findViewById(R.id.kmRefreshOption);
            if (KmPrefSettings.getInstance(getContext()).isFaqOptionEnabled() || customizationSettings.isFaqOptionEnabled(2)) {
                faqOption.setVisibility(View.VISIBLE);
                if (faqOption != null) {
                    faqOption.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String FaqUrl = new KmClientService(getContext(), Kommunicate.getFaqPageName()).getHelpCenterUrl(customizationSettings.isHideChatInHelpcenter());
                            EventManager.getInstance().sendOnFaqClick(FaqUrl);
                            ConversationActivity.openFaq(getActivity(), FaqUrl);
                        }
                    });
                }
            } else {
                faqOption.setVisibility(View.GONE);
            }
            if (customizationSettings.isRefreshOption()) {
                refreshOption.setVisibility(VISIBLE);
                refreshOption.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        downloadConversation = new DownloadConversation(recyclerView, false, 1, 1, 1, contact, channel, currentConversationId, messageSearchString);
                        downloadConversation.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                });
            } else {
                refreshOption.setVisibility(GONE);
            }
        }

        mainDivider = list.findViewById(R.id.idMainDividerLine);
        mainEditTextLinearLayout = (LinearLayout) list.findViewById(R.id.main_edit_text_linear_layout);
        individualMessageSendLayout = (LinearLayout) list.findViewById(R.id.individual_message_send_layout);
        if (!TextUtils.isEmpty(isCurrentlyInDarkMode ? customizationSettings.getChatBarTopLineViewColor().get(1) : customizationSettings.getChatBarTopLineViewColor().get(0))) {
            mainDivider.setBackgroundColor(Color.parseColor(isCurrentlyInDarkMode ? customizationSettings.getChatBarTopLineViewColor().get(1) : customizationSettings.getChatBarTopLineViewColor().get(0)));
        }
        if (!TextUtils.isEmpty(isCurrentlyInDarkMode ? customizationSettings.getMessageEditTextBackgroundColor().get(1) : customizationSettings.getMessageEditTextBackgroundColor().get(0))) {
            individualMessageSendLayout.setBackgroundColor(Color.parseColor(isCurrentlyInDarkMode ? customizationSettings.getMessageEditTextBackgroundColor().get(1) : customizationSettings.getMessageEditTextBackgroundColor().get(0)));
        }

        kmFeedbackView = list.findViewById(R.id.idKmFeedbackView);

        //what to do when Restart Conversation button is clicked
        kmFeedbackView.setInteractionListener(new KmFeedbackView.KmFeedbackViewCallbacks() {
            @Override
            public void onRestartConversationPressed() {
                EventManager.getInstance().sendOnConversationRestartedEvent(channel != null ? channel.getKey() : 0);
                setFeedbackDisplay(false);
            }
        });

        mainEditTextLinearLayout = (LinearLayout) list.findViewById(R.id.main_edit_text_linear_layout);

        sendButton = (ImageButton) individualMessageSendLayout.findViewById(R.id.conversation_send);

        recordLayout = list.findViewById(R.id.kmRecordLayout);

        recordView = list.findViewById(R.id.km_record_view);
        recordView.setOnBasketAnimationEndListener(this);
        recordView.setOnRecordListener(this);
        recordButton = list.findViewById(R.id.audio_record_button);
        recordButton.setRecordView(recordView);
        recordButton.setListenForRecord(true);

        if (isSpeechToTextEnabled) {
            recordView.enableSpeechToText(true);
            recordView.setLessThanSecondAllowed(true);
            speechToText = new KmSpeechToText(getActivity(), recordButton, this, customizationSettings);
        }

        mainEditTextLinearLayout = (LinearLayout) list.findViewById(R.id.main_edit_text_linear_layout);
        messageTemplateView = (RecyclerView) list.findViewById(R.id.message_template_view);
        applozicLabel = list.findViewById(R.id.app_label);
        cameraButton = list.findViewById(R.id.camera_btn);
        videoButton = list.findViewById(R.id.btn_video_capture);
        locationButton = list.findViewById(R.id.location_btn);
        fileAttachmentButton = list.findViewById(R.id.file_as_attachment_btn);
        multiSelectGalleryButton = list.findViewById(R.id.idMultiSelectGalleryButton);
        emailReplyReminderLayout = list.findViewById(R.id.emailReplyReminderView);
        processAttachmentIconsClick();
        Configuration config = getResources().getConfiguration();
        recordButtonWeakReference = new WeakReference<>(recordButton);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                sendButton.setScaleX(-1);
            }
        }

        boolean isAgentApp = customizationSettings != null && customizationSettings.isAgentApp();

        if (!isAgentApp && MobiComUserPreference.getInstance(getContext()).getPricingPackage() == 1) {
            applozicLabel.setVisibility(VISIBLE);
        }

        extendedSendingOptionLayout = (LinearLayout) list.findViewById(R.id.extended_sending_option_layout);

        attachmentLayout = (RelativeLayout) list.findViewById(R.id.attachment_layout);
        isTyping = (TextView) list.findViewById(R.id.isTyping);

        contextFrameLayout = (FrameLayout) list.findViewById(R.id.contextFrameLayout);

        contextSpinner = (Spinner) list.findViewById(R.id.spinner_show);
        adapterView = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                if (conversations != null && conversations.size() > 0) {
                    Conversation conversation = conversations.get(pos);
                    BroadcastService.currentConversationId = conversation.getId();
                    if (onSelected) {
                        currentConversationId = conversation.getId();
                        if (messageList != null) {
                            messageList.clear();
                        }
                        downloadConversation = new DownloadConversation(recyclerView, true, 1, 0, 0, contact, channel, conversation.getId(), messageSearchString);
                        downloadConversation.execute();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        };

        mediaUploadProgressBar = (ProgressBar) attachmentLayout.findViewById(R.id.media_upload_progress_bar);
        emoticonsFrameLayout = (FrameLayout) list.findViewById(R.id.emojicons_frame_layout);
        emoticonsBtn = (ImageButton) list.findViewById(R.id.emoji_btn);
        replayRelativeLayout = (RelativeLayout) list.findViewById(R.id.reply_message_layout);
        messageTextView = (TextView) list.findViewById(R.id.messageTextView);
        galleryImageView = (ImageView) list.findViewById(R.id.imageViewForPhoto);
        nameTextView = (TextView) list.findViewById(R.id.replyNameTextView);
        attachReplyCancelLayout = (ImageButton) list.findViewById(R.id.imageCancel);
        imageViewRLayout = (RelativeLayout) list.findViewById(R.id.imageViewRLayout);
        imageViewForAttachmentType = (ImageView) list.findViewById(R.id.imageViewForAttachmentType);
        spinnerLayout = inflater.inflate(R.layout.message_list_header_footer, null);
        infoBroadcast = (TextView) spinnerLayout.findViewById(R.id.info_broadcast);
        spinnerLayout.setVisibility(View.GONE);
        emptyTextView = (TextView) list.findViewById(R.id.noConversations);
        emptyTextView.setTextColor(Color.parseColor(isCurrentlyInDarkMode ? customizationSettings.getNoConversationLabelTextColor().get(1).trim() : customizationSettings.getNoConversationLabelTextColor().get(0).trim()));
        emoticonsBtn.setOnClickListener(this);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        if (customizationSettings.getInnerTimestampDesign() || !TextUtils.isEmpty(isCurrentlyInDarkMode ? customizationSettings.getMessageStatusIconColor().get(1) : customizationSettings.getMessageStatusIconColor().get(0))) {
            sentIcon = getResources().getDrawable(R.drawable.km_sent_icon_c);
            deliveredIcon = getResources().getDrawable(R.drawable.km_delivered_icon_c);
            readIcon = getResources().getDrawable(R.drawable.km_read_icon_c);
            pendingIcon = getResources().getDrawable(R.drawable.km_pending_icon_c);
        } else {
            sentIcon = AppCompatResources.getDrawable(getContext(), R.drawable.km_sent_icon);
            deliveredIcon = AppCompatResources.getDrawable(getContext(), R.drawable.km_delivered_icon);
            readIcon = AppCompatResources.getDrawable(getContext(), R.drawable.km_read_icon);
            pendingIcon = AppCompatResources.getDrawable(getContext(), R.drawable.km_pending_message_icon);
        }

        kmAwayView = list.findViewById(R.id.idKmAwayView);
        kmInQueueView = list.findViewById(R.id.km_in_queue_view);
        kmTypingView = list.findViewById(R.id.idKmTypingView);

        isRecordOptionEnabled = (customizationSettings != null
                && customizationSettings.getAttachmentOptions() != null
                && customizationSettings.getAttachmentOptions().get(AUDIO_RECORD_OPTION) != null
                && customizationSettings.getAttachmentOptions().get(AUDIO_RECORD_OPTION)) || isSpeechToTextEnabled;

        recordLayout.setVisibility(isRecordOptionEnabled ? View.VISIBLE : View.GONE);
        recordButton.setVisibility(isRecordOptionEnabled ? View.VISIBLE : View.GONE);
        sendButton.setVisibility(isRecordOptionEnabled ? View.GONE : View.VISIBLE);
        KmUtils.setGradientSolidColor(sendButton, themeHelper.getSendButtonBackgroundColor());

        attachButton = (ImageButton) individualMessageSendLayout.findViewById(R.id.attach_button);

        sendType = (Spinner) extendedSendingOptionLayout.findViewById(R.id.sendTypeSpinner);
        messageEditText = (EditText) individualMessageSendLayout.findViewById(R.id.conversation_message);

        languageChangeButton = individualMessageSendLayout.findViewById(R.id.language_change_button);
        if (KmSpeechToTextSetting.getInstance(getContext()).isMultipleSpeechToTextEnabled()) {
            languageChangeButton.setVisibility(VISIBLE);
            languageChangeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (KmSpeechToTextSetting.getInstance(getContext()).getSpeechToTextList() != null) {
                        new KmLanguageSlideView(conversationUIService, KmSpeechToTextSetting.getInstance(getContext()).getSpeechToTextList()).show(getChildFragmentManager(), KmLanguageSlideView.getFragTag());
                    }
                }
            });
        }
        if (KmUtils.isAgent(getContext())) {
            kmAutoSuggestionRecycler = list.findViewById(R.id.kmAutoSuggestionRecycler);
            kmAutoSuggestionRecycler.setmMaxHeight(240);
            KmLinearLayoutManager linearLayoutManager = new KmLinearLayoutManager(getContext());
            kmAutoSuggestionRecycler.setLayoutManager(linearLayoutManager);
            kmAutoSuggestionDivider = list.findViewById(R.id.kmAutoSuggestionDivider);
        }

        if (fontManager != null && fontManager.getMessageEditTextFont() != null) {
            messageEditText.setTypeface(fontManager.getMessageEditTextFont());
        }

        messageEditText.setTextColor(themeHelper.parseColorWithDefault(customizationSettings.getMessageEditTextTextColor().get(isCurrentlyInDarkMode ? 1 : 0),
                getResources().getColor(R.color.chatbar_text_color)));
        messageEditText.setHintTextColor(themeHelper.parseColorWithDefault(customizationSettings.getMessageEditTextHintTextColor().get(isCurrentlyInDarkMode ? 1 : 0),
                getResources().getColor(R.color.chatbar_text_color)));

        userNotAbleToChatLayout = (LinearLayout) list.findViewById(R.id.user_not_able_to_chat_layout);
        userNotAbleToChatTextView = (TextView) userNotAbleToChatLayout.findViewById(R.id.user_not_able_to_chat_textView);
        userNotAbleToChatTextView.setTextColor(Color.parseColor(isCurrentlyInDarkMode ? customizationSettings.getUserNotAbleToChatTextColor().get(1) : customizationSettings.getUserNotAbleToChatTextColor().get(0)));
        takeOverFromBotLayout = list.findViewById(R.id.kmTakeOverFromBotLayout);

        if (channel != null && channel.isDeleted()) {
            userNotAbleToChatTextView.setText(R.string.group_has_been_deleted_text);
        }

        bottomlayoutTextView = (TextView) list.findViewById(R.id.user_not_able_to_chat_textView);
        if (!TextUtils.isEmpty(defaultText)) {
            messageEditText.setText(defaultText);
            defaultText = "";
        }
        scheduleOption = (Button) extendedSendingOptionLayout.findViewById(R.id.scheduleOption);
        mediaContainer = (ImageView) attachmentLayout.findViewById(R.id.media_container);
        attachedFile = (TextView) attachmentLayout.findViewById(R.id.attached_file);
        ImageView closeAttachmentLayout = (ImageView) attachmentLayout.findViewById(R.id.close_attachment_layout);

        swipeLayout = (SwipeRefreshLayout) list.findViewById(R.id.swipe_container);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        ArrayAdapter<CharSequence> sendTypeAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.send_type_options, R.layout.custom_spinner);

        sendTypeAdapter.setDropDownViewResource(R.layout.custom_spinner);
        sendType.setAdapter(sendTypeAdapter);

        messageEditText.addTextChangedListener(new TextWatcher() {

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //TODO: write code to emoticons .....
            }

            public void afterTextChanged(Editable s) {
                try {
                    if (!TextUtils.isEmpty(s.toString()) && s.toString().trim().length() > 0) {
                        if (!typingStarted) {
                            typingStarted = true;
                            handleSendAndRecordButtonView(true);
                            Intent intent = new Intent(getActivity(), MqttIntentService.class);
                            intent.putExtra(MqttIntentService.CHANNEL, channel);
                            intent.putExtra(MqttIntentService.CONTACT, contact);
                            intent.putExtra(MqttIntentService.TYPING, typingStarted);
                            MqttIntentService.enqueueWork(getActivity(), intent);
                        }
                        if (isApiAutoSuggest) {
                            final AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) messageEditText;
                            autoCompleteTextView.setThreshold(2);
                            GetDataUseCase.executeWithExecutor(
                                    getContext(),
                                    autoSuggestUrl,
                                    APPLI_JSON,
                                    APPLI_JSON,
                                    s.toString().trim(),
                                    autoSuggestHeaders,
                                    new TaskListener<>() {
                                        @Override
                                        public void onSuccess(String message) {
                                            try {
                                                JSONObject jsonObject = new JSONObject((String) message);
                                                autoCompleteTextView.setAdapter(getAdapter((KmAutoSuggestion.Source[]) GsonUtils.getObjectFromJson((String) jsonObject.get("data").toString(), KmAutoSuggestion.Source[].class)));
                                                Utils.printLog(getContext(), TAG, "Fetching Autosuggestion from API");
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onFailure(@NonNull Exception error) {
                                            Utils.printLog(getContext(), TAG, "Failed to fetch Autosuggestion from API");
                                        }
                                    });
                        }
                        populateAutoSuggestion(true, s.toString(), null);
                    } else if (s.toString().trim().length() == 0) {
                        if (typingStarted) {
                            typingStarted = false;
                            handleSendAndRecordButtonView(!TextUtils.isEmpty(filePath));
                            Intent intent = new Intent(getActivity(), MqttIntentService.class);
                            intent.putExtra(MqttIntentService.CHANNEL, channel);
                            intent.putExtra(MqttIntentService.CONTACT, contact);
                            intent.putExtra(MqttIntentService.TYPING, typingStarted);
                            MqttIntentService.enqueueWork(getActivity(), intent);
                        }
                        populateAutoSuggestion(false, s.toString().trim(), null);
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });

        messageEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                emoticonsFrameLayout.setVisibility(View.GONE);
            }
        });

        attachReplyCancelLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageMetaData = null;
                replayRelativeLayout.setVisibility(View.GONE);
            }
        });

        messageEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (typingStarted) {
                        Intent intent = new Intent(getActivity(), MqttIntentService.class);
                        intent.putExtra(MqttIntentService.CHANNEL, channel);
                        intent.putExtra(MqttIntentService.CONTACT, contact);
                        intent.putExtra(MqttIntentService.TYPING, typingStarted);
                        MqttIntentService.enqueueWork(getActivity(), intent);

                    }
                    emoticonsFrameLayout.setVisibility(View.GONE);

                    multimediaPopupGrid.setVisibility(View.GONE);
                }
            }

        });

        sendButton.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View view) {
                                              emoticonsFrameLayout.setVisibility(View.GONE);
                                              sendMessage();
                                          }
                                      }
        );


        closeAttachmentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filePath = null;
                if (previewThumbnail != null) {
                    previewThumbnail.recycle();
                }
                attachmentLayout.setVisibility(View.GONE);

                if (messageEditText != null && TextUtils.isEmpty(messageEditText.getText().toString().trim()) && recordButton != null && sendButton != null) {
                    handleSendAndRecordButtonView(false);
                }
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (recyclerDetailConversationAdapter != null) {
                    recyclerDetailConversationAdapter.contactImageLoader.setPauseWork(newState == RecyclerView.SCROLL_STATE_DRAGGING);
                }
            }

            @Override
            public void onScrolled(final RecyclerView recyclerView, int dx, int dy) {
                //super.onScrolled(recyclerView, dx, dy);
                if (loadMore) {
                    int topRowVerticalPosition =
                            (recyclerView == null || recyclerView.getChildCount() == 0) ?
                                    0 : recyclerView.getChildAt(0).getTop();
                    swipeLayout.setEnabled(topRowVerticalPosition >= 0);
                }
            }
        });

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppContextService.getContext(getContext()) != null && AppContextService.getContext(getContext()) instanceof KmToolbarClickListener) {
                    ((KmToolbarClickListener) AppContextService.getContext(getContext())).onClick(getActivity(), channel, contact);
                }
            }
        });
        recyclerView.setLongClickable(true);
        messageTemplate = customizationSettings.getMessageTemplate();

        if (messageTemplate != null && messageTemplate.isEnabled()) {
            messageTemplateView.setVisibility(View.VISIBLE);
            if (messageTemplate.isLeftAligned()) {
                ViewGroup.LayoutParams layoutParams = messageTemplateView.getLayoutParams();
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                messageTemplateView.setLayoutParams(layoutParams);
            }
            templateAdapter = new MobicomMessageTemplateAdapter(messageTemplate);
            MobicomMessageTemplateAdapter.MessageTemplateDataListener listener = new MobicomMessageTemplateAdapter.MessageTemplateDataListener() {
                @Override
                public void onItemSelected(String message) {

                    final Message lastMessage = !messageList.isEmpty() ? messageList.get(messageList.size() - 1) : null;

                    if ((messageTemplate.getTextMessageList() != null && !messageTemplate.getTextMessageList().getMessageList().isEmpty() && messageTemplate.getTextMessageList().isSendMessageOnClick() && "text".equals(getMessageType(lastMessage)))
                            || (messageTemplate.getImageMessageList() != null && !messageTemplate.getImageMessageList().getMessageList().isEmpty() && messageTemplate.getImageMessageList().isSendMessageOnClick() && IMAGE.equals(getMessageType(lastMessage)))
                            || (messageTemplate.getVideoMessageList() != null && !messageTemplate.getVideoMessageList().getMessageList().isEmpty() && messageTemplate.getVideoMessageList().isSendMessageOnClick() && VIDEO.equals(getMessageType(lastMessage)))
                            || (messageTemplate.getLocationMessageList() != null && !messageTemplate.getLocationMessageList().getMessageList().isEmpty() && messageTemplate.getLocationMessageList().isSendMessageOnClick() && LOCATION.equals(getMessageType(lastMessage)))
                            || (messageTemplate.getContactMessageList() != null && !messageTemplate.getContactMessageList().getMessageList().isEmpty() && messageTemplate.getContactMessageList().isSendMessageOnClick() && CONTACT.equals(getMessageType(lastMessage)))
                            || (messageTemplate.getAudioMessageList() != null && !messageTemplate.getAudioMessageList().getMessageList().isEmpty() && messageTemplate.getAudioMessageList().isSendMessageOnClick() && AUDIO.equals(getMessageType(lastMessage)))
                            || messageTemplate.getSendMessageOnClick()) {
                        sendMessage(message);
                    }

                    if (messageTemplate.getHideOnSend()) {
                        KmMessageMetadataUpdateTask.MessageMetadataListener listener1 = new KmMessageMetadataUpdateTask.MessageMetadataListener() {
                            @Override
                            public void onSuccess(Context context, String message) {
                                templateAdapter.setMessageList(new HashMap<String, String>());
                                templateAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onFailure(Context context, String error) {
                            }
                        };

                        if (lastMessage != null) {
                            Map<String, String> metadata = lastMessage.getMetadata();
                            metadata.put(IS_DONE_WITH_CLICKING, "true");
                            lastMessage.setMetadata(metadata);
                            new KmMessageMetadataUpdateTask(getContext(), lastMessage.getKeyString(), lastMessage.getMetadata(), listener1).execute();
                        }
                    }

                    final Intent intent = new Intent();
                    intent.setAction(APPLOZIC_TEMPLATE_MSG);
                    intent.putExtra(TEMPLATE_MSG, message);
                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    AppContextService.getContext(getContext()).sendBroadcast(intent);
                }
            };

            templateAdapter.setOnItemSelected(listener);
            LinearLayoutManager horizontalLayoutManagaer
                    = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
            messageTemplateView.setLayoutManager(horizontalLayoutManagaer);
            messageTemplateView.setAdapter(templateAdapter);
        }

        if (channel != null
                && Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType())
                && channel.getKmStatus() != Channel.IN_QUEUE_CONVERSATION
        ) {
            showAwayMessage(true, null);
        }

        emoticonsBtn.setVisibility(View.GONE);

        boolean hideAttachmentOptionsWithBots = customizationSettings.isHideAttachmentOptionsWithBots();
        if (hideAttachmentOptionsWithBots) {
            attachmentIconLayout.setVisibility(GONE);
        }
        toggleHideChatBarWithBots();

        if (customizationSettings.getAttachmentOptions() != null && !customizationSettings.getAttachmentOptions().isEmpty()) {
            Map<String, Boolean> attachmentOptions = customizationSettings.getAttachmentOptions();

            if (attachmentOptions.containsKey(":location")) {
                locationButton.setVisibility(attachmentOptions.get(":location") ? VISIBLE : View.GONE);
            }

            if (attachmentOptions.containsKey(":camera")) {
                cameraButton.setVisibility(attachmentOptions.get(":camera") ? VISIBLE : View.GONE);
            }

            if (attachmentOptions.containsKey(":video")) {
                videoButton.setVisibility(attachmentOptions.get(":video") ? VISIBLE : GONE);
            }

            if (attachmentOptions.containsKey(":file")) {
                fileAttachmentButton.setVisibility(attachmentOptions.get(":file") ? VISIBLE : View.GONE);
            }

            if (attachmentOptions.containsKey(MULTI_SELECT_GALLERY_OPTION)) {
                multiSelectGalleryButton.setVisibility(attachmentOptions.get(MULTI_SELECT_GALLERY_OPTION) ? VISIBLE : View.GONE);
            }
        }

        messageEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_DONE == actionId && getActivity() != null) {
                    Utils.toggleSoftKeyBoard(getActivity(), true);
                    return true;
                }
                return false;
            }
        });

        messageCharacterLimitTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                toggleCharLimitExceededMessage(isAssigneeDialogFlowBot, charSequence.length());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };

        setupModes(isCurrentlyInDarkMode);
        setupInsets();
        return list;
    }

    private void setupInsets() {
        InsetHelper.configureSystemInsets(
                startNewConv,
                0,
                -1,
                false
        );
        InsetHelper.configureSystemInsetsWithKeyboard(
                kmMessageLinearLayout,
                0,
                -1,
                true,
                (isVisible, keyboardHeight) -> {
                    if(isVisible
                            && recyclerView != null
                            && recyclerView.getAdapter() != null
                            && recyclerView.getAdapter().getItemCount() > 0
                    ) {
                        recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                    }
                    return null;
                }
        );
    }

    private void setupDotColorStatus() {
        KmUtils.setGradientStrokeColor(toolbarOnlineColorDot, DimensionsUtils.convertDpToPx(1), themeHelper.getToolbarColor());
        KmUtils.setGradientStrokeColor(toolbarOfflineColorDot, DimensionsUtils.convertDpToPx(1), themeHelper.getToolbarColor());
        KmUtils.setGradientStrokeColor(toolbarAwayColorDot, DimensionsUtils.convertDpToPx(1), themeHelper.getToolbarColor());
    }

    public void newCustomToolbarDesign() {
        isCustomToolbarSubtitleDesign = KmConversationInfoSetting.getInstance(getContext()).isCustomToolbarSubtitleDesign();

        if (isCustomToolbarSubtitleDesign) {
            TextView ratingTextview = customToolbarLayout.findViewById(R.id.toolbar_agent_rating);
            TextView experienceTextview = customToolbarLayout.findViewById(R.id.toolbar_agent_experience);
            Drawable starDrawable = getResources().getDrawable(R.drawable.star);
            starDrawable.setBounds(0, 0, 40, 40);
            ratingTextview.setCompoundDrawables(starDrawable, null, null, null);
            String experienceText = KmConversationInfoSetting.getInstance(getContext()).getToolbarAgentExperience();
            Float agentRating = KmConversationInfoSetting.getInstance(getContext()).getToolbarSubtitleRating();
            if (!TextUtils.isEmpty(experienceText)) {
                experienceTextview.setVisibility(VISIBLE);
                StringBuilder stringBuilder = new StringBuilder(experienceText.trim());
                stringBuilder.append(agentRating == -1.0F ? "" : " | ");
                experienceTextview.setText(stringBuilder);
            }
            if (agentRating != -1.0F) {
                ratingTextview.setVisibility(VISIBLE);
                ratingTextview.setText(agentRating.toString());
            }
        }
    }

    public void handleSendAndRecordButtonView(boolean isSendButtonVisible) {
        boolean showRecordButton = (customizationSettings != null
                && customizationSettings.getAttachmentOptions() != null
                && customizationSettings.getAttachmentOptions().get(AUDIO_RECORD_OPTION) != null
                && customizationSettings.getAttachmentOptions().get(AUDIO_RECORD_OPTION)) || isSpeechToTextEnabled;

        sendButton.setVisibility(showRecordButton ? (isSendButtonVisible ? View.VISIBLE : View.GONE) : View.VISIBLE);
        recordButton.setVisibility(showRecordButton ? (isSendButtonVisible ? View.GONE : View.VISIBLE) : View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (textToSpeech != null) {
            textToSpeech.initialize();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (textToSpeech != null) {
            textToSpeech.destroy();
        }
    }

    @Override
    public void onMessageSent(Message message) {
    }

    @Override
    public void onMessageReceived(Message message) {
    }

    @Override
    public void onLoadMore(boolean loadMore) {
    }

    @Override
    public void onMessageSync(Message message, String key) {
    }

    @Override
    public void onMessageDeleted(String messageKey, String userId) {
    }

    @Override
    public void onMessageDelivered(Message message, String userId) {
    }

    @Override
    public void onAllMessagesDelivered(String userId) {
    }

    @Override
    public void onAllMessagesRead(String userId) {
    }

    @Override
    public void onConversationDeleted(String userId, Integer channelKey, String response) {
    }

    @Override
    public void onUpdateTypingStatus(String userId, String isTyping) {
    }

    @Override
    public void onUpdateLastSeen(String userId) {
    }

    @Override
    public void onMqttDisconnected() {
        KommunicateSettings.unSubscribeToTyping(getContext(), channel, contact);
    }

    @Override
    public void onMqttConnected() {
        KommunicateSettings.subscribeToTyping(getContext(), channel, contact);
    }

    @Override
    public void onUserOnline() {
    }

    @Override
    public void onUserOffline() {
    }

    @Override
    public void onChannelUpdated() {
        if (channel == null) {
            return;
        }

        final String existingAssignee = channel.getConversationAssignee();
        channel = ChannelService.getInstance(getActivity()).getChannelByChannelKey(channel.getKey());
        //conversation is open
        //if the conversation is opened from the dashboard while the feedback input fragment is open, the feedback fragment will be closed
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setFeedbackDisplay(channel != null && channel.getKmStatus() == Channel.CLOSED_CONVERSATIONS && !KmUtils.isAgent(getContext()));
                if (existingAssignee != null
                        && (!existingAssignee.equals(channel.getConversationAssignee()) ||  kmInQueueView.getVisibility() == VISIBLE)
                        && channel.getKmStatus() != Channel.IN_QUEUE_CONVERSATION
                ) {
                    showAwayMessage(true, null);
                }
            }
        });
    }

    @Override
    public void onConversationRead(String userId, boolean isGroup) {
    }

    @Override
    public void onUserDetailUpdated(String userId) {
    }

    @Override
    public void onMessageMetadataUpdated(String keyString) {
    }

    @Override
    public void onUserMute(boolean mute, String userId) {
    }

    public void openFiveStarRatingFragment() {
        if (feedbackInputFragmentv2 == null) {
            feedbackInputFragmentv2 = new FeedbackInputFragmentv2();
            feedbackInputFragmentv2.setRatingSubmitListener(this);
        }

        getActivity().getSupportFragmentManager().executePendingTransactions();
        if (!feedbackInputFragmentv2.isVisible() && !feedbackInputFragmentv2.isAdded()) {
            feedbackInputFragmentv2.show(getActivity().getSupportFragmentManager(), FeedbackInputFragment.getFragTag());
        }
    }

    public void openEmojiFeedbackFragment() {
        if (feedBackFragment == null) {
            feedBackFragment = new FeedbackInputFragment();
            feedBackFragment.setFeedbackFragmentListener(this);
        }

        getActivity().getSupportFragmentManager().executePendingTransactions();
        if (!feedBackFragment.isVisible() && !feedBackFragment.isAdded()) {
            feedBackFragment.show(getActivity().getSupportFragmentManager(), FeedbackInputFragment.getFragTag());
        }
        EventManager.getInstance().sendOnRateConversationClick();

    }

    public void openFeedbackFragment() {
        if (KmAppSettingPreferences.getRatingBase() == 5) {
             openFiveStarRatingFragment();
        } else {
            openEmojiFeedbackFragment();
        }
    }

    @SuppressLint("MissingPermission")
    protected void vibrate() {
        try {
            if (getActivity() != null) {
                Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null) {
                    vibrator.vibrate(200);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        isApiAutoSuggest = false;
        if (isCustomFieldMessage) {
            validateCustomInputRegex(message);
        } else {
            messageEditText.setText("");
            sendMessage(message, null, null, null, Message.ContentType.DEFAULT.getValue());
        }
    }

    protected void sendMessage() {
        if (channel != null) {
            if (isUserGivingEmail && kmAwayView.isUserAnonymous() && kmAwayView.isCollectEmailOnAwayEnabled()) {
                if (messageEditText != null && !TextUtils.isEmpty(messageEditText.getText().toString().trim())) {

                    String inputMessage = messageEditText.getText().toString();
                    handleSendAndRecordButtonView(true);
                    if (!Pattern.compile(LeadCollectionActivity.EMAIL_VALIDATION_REGEX).matcher(inputMessage).matches()) {
                        kmAwayView.showInvalidEmail();
                        return;
                    }
                    isUserGivingEmail = false;
                    kmAwayView.handleUserEmail(inputMessage);

                }
            }
            if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                String userId = ChannelService.getInstance(getActivity()).getGroupOfTwoReceiverUserId(channel.getKey());
                if (!TextUtils.isEmpty(userId)) {
                    Contact withUserContact = appContactService.getContactById(userId);
                    if (withUserContact.isBlocked()) {
                        userBlockDialog(false, withUserContact, true);
                    } else {
                        processSendMessage();
                    }
                }
            } else if (Channel.GroupType.OPEN.getValue().equals(channel.getType())) {
                if (Utils.isInternetAvailable(getActivity())) {
                    processSendMessage();
                } else {
                    KmToast.error(AppContextService.getContext(getContext()), AppContextService.getContext(getContext()).getString(R.string.internet_connection_not_available), Toast.LENGTH_SHORT).show();
                }
            } else {
                processSendMessage();
            }
            if (kmAwayView.isUserAnonymous() && kmAwayView.isCollectEmailOnAwayEnabled() && customizationSettings.isEnableAwayMessage()) {
                kmAwayView.askForEmail();
                isUserGivingEmail = true;
            }
        } else if (contact != null) {
            if (contact.isBlocked()) {
                userBlockDialog(false, contact, false);
            } else {
                processSendMessage();
            }
        }
    }

    public void sendMessage(short messageContentType, String filePath) {
        this.filePath = filePath;
        sendMessage("", messageContentType);
    }

    public void sendMessage(String message, short messageContentType, String filePath) {
        this.filePath = filePath;
        sendMessage(message, null, null, null, messageContentType);
    }

    public void sendMessage(String message, short messageContentType) {
        sendMessage(message, null, null, null, messageContentType);
    }

    public void sendMessage(String message, Map<String, String> messageMetaData, short messageContentType) {
        sendMessage(message, messageMetaData, null, null, messageContentType);
    }

    public void sendMessage(String message, Map<String, String> messageMetaData, FileMeta fileMetas, String fileMetaKeyStrings, short messageContentType) {
        MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(getActivity());
        Message messageToSend = new Message();

        // check for conversation closed or not.
        if (channel.getKmStatus() == Channel.CLOSED_CONVERSATIONS
                && !customizationSettings.isRestartConversationButtonVisibility()
        ) {
            return;
        }

        if (channel != null) {
            messageToSend.setGroupId(channel.getKey());
            if (!TextUtils.isEmpty(channel.getClientGroupId())) {
                messageToSend.setClientGroupId(channel.getClientGroupId());
            }
        } else {
            messageToSend.setTo(contact.getContactIds());
            messageToSend.setContactIds(contact.getContactIds());
        }
        messageToSend.setRead(Boolean.TRUE);
        messageToSend.setStoreOnDevice(Boolean.TRUE);
        if (messageToSend.getCreatedAtTime() == null) {
            messageToSend.setCreatedAtTime(System.currentTimeMillis() + userPreferences.getDeviceTimeOffset());
        }
        if (currentConversationId != null && currentConversationId != 0) {
            messageToSend.setConversationId(currentConversationId);
        }
        messageToSend.setSendToDevice(Boolean.FALSE);
        messageToSend.setType(sendType.getSelectedItemId() == 1 ? Message.MessageType.MT_OUTBOX.getValue() : Message.MessageType.OUTBOX.getValue());
        messageToSend.setTimeToLive(getTimeToLive());
        messageToSend.setMessage(message);
        messageToSend.setDeviceKeyString(userPreferences.getDeviceKeyString());
        messageToSend.setSource(Message.Source.MT_MOBILE_APP.getValue());
        if (!TextUtils.isEmpty(filePath)) {
            List<String> filePaths = new ArrayList<String>();
            filePaths.add(filePath);
            messageToSend.setFilePaths(filePaths);
            if (messageContentType == Message.ContentType.AUDIO_MSG.getValue() || messageContentType == Message.ContentType.CONTACT_MSG.getValue() || messageContentType == Message.ContentType.VIDEO_MSG.getValue()) {
                messageToSend.setContentType(messageContentType);
            } else {
                messageToSend.setContentType(Message.ContentType.ATTACHMENT.getValue());
            }
        } else {
            messageToSend.setContentType(messageContentType);
        }

        if (messageContentType == Message.ContentType.LOCATION.getValue()) {
            messageToSend.setContentType(Message.ContentType.LOCATION.getValue());
            messageToSend.setFilePaths(null);
        }

        messageToSend.setFileMetaKeyStrings(fileMetaKeyStrings);
        messageToSend.setFileMetas(fileMetas);

        messageToSend.setMetadata(getMessageMetadata(messageMetaData));

        conversationService.sendMessage(messageToSend, messageIntentClass);
        if (replayRelativeLayout != null) {
            replayRelativeLayout.setVisibility(View.GONE);
        }
        if (selfDestructMessageSpinner != null) {
            selfDestructMessageSpinner.setSelection(0);
        }
        attachmentLayout.setVisibility(View.GONE);
        if (channel != null && channel.getType() != null && Channel.GroupType.BROADCAST_ONE_BY_ONE.getValue().equals(channel.getType())) {
            sendBroadcastMessage(message, filePath);
        }
        this.messageMetaData = null;
        filePath = null;
    }

    protected void processSendMessage() {
        if (!TextUtils.isEmpty(messageEditText.getText().toString().trim()) || !TextUtils.isEmpty(filePath)) {
            String inputMessage = messageEditText.getText().toString();
            String[] inputMsg = inputMessage.toLowerCase().split(" ");
            List<String> userInputList = Arrays.asList(inputMsg);

            boolean disjointResult = (restrictedWords == null) || disjoint(restrictedWords, userInputList);
            boolean restrictedWordMatches;

            try {
                String dynamicRegex = KommunicateSetting.getInstance(getContext()).getRestrictedWordsRegex();
                String pattern = !TextUtils.isEmpty(dynamicRegex) ? dynamicRegex : (customizationSettings != null
                        && !TextUtils.isEmpty(customizationSettings.getRestrictedWordRegex()) ? customizationSettings.getRestrictedWordRegex() : "");

                restrictedWordMatches = !TextUtils.isEmpty(pattern) && Pattern.compile(pattern).matcher(inputMessage.trim()).matches();
            } catch (PatternSyntaxException e) {
                e.printStackTrace();
                createInvalidPatternExceptionDialog();
                return;
            }

            if (disjointResult && !restrictedWordMatches) {
                sendMessage(messageEditText.getText().toString().trim());
            } else {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity()).
                        setPositiveButton(R.string.ok_alert, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                handleSendAndRecordButtonView(true);
                            }
                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                handleSendAndRecordButtonView(true);
                            }
                        });
                alertDialog.setTitle(customizationSettings.getRestrictedWordMessage());
                alertDialog.setCancelable(true);
                alertDialog.create().show();
            }
        }
    }

    protected void createInvalidPatternExceptionDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity()).
                setPositiveButton(R.string.ok_alert, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        handleSendAndRecordButtonView(true);
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        handleSendAndRecordButtonView(true);
                    }
                });
        alertDialog.setTitle(AppContextService.getContext(getContext()).getString(R.string.invalid_message_matching_pattern));
        alertDialog.setCancelable(true);
        alertDialog.create().show();
    }

    public void populateAutoSuggestion(boolean show, String typedText, String message) {
        if (kmAutoSuggestionRecycler != null) {
            if (show) {
                if (!TextUtils.isEmpty(typedText.trim()) && typedText.startsWith("/") && !typedText.startsWith("/ ")) {
                    Bundle bundle = new Bundle();
                    bundle.putString(KmAutoSuggestionAdapter.KM_AUTO_SUGGESTION_TYPED_TEXT, typedText.substring(1));
                    if (kmAutoSuggestionAdapter != null) {
                        getLoaderManager().restartLoader(1, bundle, MobiComConversationFragment.this);
                    } else {
                        getLoaderManager().initLoader(1, bundle, this);
                    }
                } else {
                    getLoaderManager().destroyLoader(1);
                    kmAutoSuggestionRecycler.setVisibility(View.GONE);
                    if (kmAutoSuggestionDivider != null) {
                        kmAutoSuggestionDivider.setVisibility(View.GONE);
                    }
                }
            } else {
                getLoaderManager().destroyLoader(1);
                kmAutoSuggestionRecycler.setVisibility(View.GONE);
                if (!TextUtils.isEmpty(message) && messageEditText != null) {
                    messageEditText.setText(message);
                    messageEditText.setSelection(message.length());
                }
                if (kmAutoSuggestionDivider != null) {
                    kmAutoSuggestionDivider.setVisibility(View.GONE);
                }
            }
        }
    }

    public void deleteMessageFromDeviceList(String messageKeyString) {
        try {
            int position;
            boolean updateQuickConversation = false;
            int index;
            for (Message message : messageList) {
                boolean value = message.getKeyString() != null ? message.getKeyString().equals(messageKeyString) : false;
                if (value) {
                    index = messageList.indexOf(message);
                    if (index != -1) {
                        int aboveIndex = index - 1;
                        int belowIndex = index + 1;
                        Message aboveMessage = messageList.get(aboveIndex);
                        if (belowIndex != messageList.size()) {
                            Message belowMessage = messageList.get(belowIndex);
                            if (aboveMessage.isTempDateType() && belowMessage.isTempDateType()) {
                                messageList.remove(aboveMessage);
                                recyclerDetailConversationAdapter.notifyItemRemoved(aboveIndex);
                            }
                        } else if (belowIndex == messageList.size() && aboveMessage.isTempDateType()) {
                            messageList.remove(aboveMessage);
                            recyclerDetailConversationAdapter.notifyItemRemoved(aboveIndex);
                        }
                    }
                }
                if (message.getKeyString() != null && message.getKeyString().equals(messageKeyString)) {
                    position = messageList.indexOf(message);

                    if (position == messageList.size() - 1) {
                        updateQuickConversation = true;
                    }
                    if (message.getScheduledAt() != null && message.getScheduledAt() != 0) {
                        messageDatabaseService.deleteScheduledMessage(messageKeyString);
                    }
                    messageList.remove(position);
                    recyclerDetailConversationAdapter.notifyItemRemoved(position);
                    if (messageList.isEmpty()) {
                        emptyTextView.setVisibility(VISIBLE);
                        if (getActivity() != null) {
                            ((MobiComKitActivityInterface) getActivity()).removeConversation(message, channel != null ? String.valueOf(channel.getKey()) : contact.getUserId());
                        }
                    }
                    break;
                }
            }
            int messageListSize = messageList.size();
            if (messageListSize > 0 && updateQuickConversation && getActivity() != null) {
                ((MobiComKitActivityInterface) getActivity()).updateLatestMessage(messageList.get(messageListSize - 1), channel != null ? String.valueOf(channel.getKey()) : contact.getUserId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getCurrentUserId() {
        if (contact == null) {
            return "";
        }
        return contact.getUserId() != null ? contact.getUserId() : "";
    }

    public Contact getContact() {
        return contact;
    }

    protected void setContact(Contact contact) {
        this.contact = contact;
    }

    public boolean hasMultiplePhoneNumbers() {
        return contact != null && contact.hasMultiplePhoneNumbers();
    }

    public MultimediaOptionFragment getMultimediaOptionFragment() {
        return multimediaOptionFragment;
    }

    public Spinner getSendType() {
        return sendType;
    }

    public Spinner getSelfDestructMessageSpinner() {
        return selfDestructMessageSpinner;
    }

    public Button getScheduleOption() {
        return scheduleOption;
    }

    public void setFirstTimeMTexterFriend(boolean firstTimeMTexterFriend) {
        this.firstTimeMTexterFriend = firstTimeMTexterFriend;
    }

    //    public EmojiconEditText getMessageEditText() {
    //        return messageEditText;
    //    }

    public void clearList() {
        if (getActivity() == null) {
            return;
        }
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (recyclerDetailConversationAdapter != null) {
                    messageList.clear();
                    if (messageList.isEmpty()) {
                        emptyTextView.setVisibility(View.VISIBLE);
                    }
                    recyclerDetailConversationAdapter.notifyDataSetChanged();
                }
                if (kmContextSpinnerAdapter != null) {
                    contextFrameLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    public void updateMessage(final Message message) {
        if (getActivity() == null) {
            return;
        }
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Note: Removing and adding the same message again as the new sms object will contain the keyString.
                messageList.remove(message);
                messageList.add(message);
                recyclerDetailConversationAdapter.notifyDataSetChanged();
            }
        });
    }

    public void updateMessageMetadata(String keyString) {
        int i = -1;
        if (!messageList.isEmpty()) {
            for (Message message : messageList) {
                if (keyString.equals(message.getKeyString())) {
                    i = messageList.indexOf(message);
                }
            }
        }
        if (i != -1) {
            messageList.get(i).setMetadata(messageDatabaseService.getMessage(keyString).getMetadata());
            if (conversationAdapter != null) {
                if (messageList.get(i).isDeletedForAll()) {
                    messageList.remove(i);
                }
                conversationAdapter.notifyDataSetChanged();
            }
            if (messageList.get(messageList.size() - 1).getMetadata().containsKey(IS_DONE_WITH_CLICKING)) {
                templateAdapter.setMessageList(new HashMap<String, String>());
                templateAdapter.notifyDataSetChanged();
            }
        }
    }

    public void addMessage(final Message message) {
        hideAwayMessage(message);
        if (messageList.contains(message)) {
            updateMessage(message);
        }
        if (AppContextService.getContext(getContext()) instanceof KmOnMessageListener) {
            ((KmOnMessageListener) AppContextService.getContext(getContext())).onNewMessage(message, channel, contact);
        }

        if (botMessageDelayInterval > 0 && message.getGroupId() != null && message.getGroupId() != 0 && !TextUtils.isEmpty(message.getTo())) {
            Contact contact = appContactService.getContactById(message.getTo());
            if (contact != null && User.RoleType.BOT.getValue().equals(contact.getRoleType())) {
                if (botDelayMessageList == null || !botDelayMessageList.contains(message)) {
                    if (botDelayMessageList == null) {
                        botDelayMessageList = new HashSet<>();
                    }
                    botDelayMessageList.add(message);
                    botTypingDelayManager.addMessage(message);
                    emptyTextView.setVisibility(GONE);
                }
                return;
            }
        }

        handleAddMessage(message);
    }

    protected void handleAddMessage(final Message message) {

        if (Objects.equals(message.getType(), Message.MessageType.MT_INBOX.getValue()) &&
                !Message.ContentType.CHANNEL_CUSTOM_MESSAGE.getValue().equals(message.getContentType()) &&
                textToSpeech != null &&
                !TextUtils.isEmpty(message.getMessage())) {
            textToSpeech.speak(message.getMessage());
        }
        if (message.getGroupId() != null) {
            if (channel != null && channel.getKey().equals(message.getGroupId())) {
                if (message.getTo() != null) {
                    updateTypingStatus(message.getTo(), false);
                }
            }
        } else if (contact != null && contact.getContactIds().equals(message.getTo())) {
            updateTypingStatus(message.getTo(), false);
        }

        if (getActivity() == null) {
            return;
        }
        if (!messageDatabaseService.isMessagePresent(message.getKeyString())) {
            messageDatabaseService.createMessage(message);
        }
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Todo: Handle disappearing messages.
                boolean added = updateMessageList(message, false);
                if (added) {
                    //Todo: update unread count
                    recyclerDetailConversationAdapter.updateLastSentMessage(message);
                    recyclerDetailConversationAdapter.notifyDataSetChanged();
                    linearLayoutManager.scrollToPositionWithOffset(messageList.size() - 1, 0);
                    emptyTextView.setVisibility(View.GONE);
                    currentConversationId = message.getConversationId();
                    channelKey = message.getGroupId();
                    if (Message.MessageType.MT_INBOX.getValue().equals(message.getType())) {
                        try {
                            messageDatabaseService.updateReadStatusForKeyString(message.getKeyString());
                            Intent intent = new Intent(getActivity(), UserIntentService.class);
                            intent.putExtra(UserIntentService.PAIRED_MESSAGE_KEY_STRING, message.getKeyString());
                            intent.putExtra(UserIntentService.CONTACT, contact);
                            intent.putExtra(UserIntentService.CHANNEL, channel);
                            UserIntentService.enqueueWork(getActivity(), intent);
                        } catch (Exception e) {
                            Utils.printLog(getContext(), TAG, "Got exception while read");
                        }
                    }
                }
                selfDestructMessage(message);
                checkForAutoSuggestions();
                checkForCustomInput();
            }
        });
    }

    protected abstract void processMobiTexterUserCheck();

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        this.menu = menu;
        if (menu instanceof MenuBuilder) {
            MenuBuilder menuBuilder = (MenuBuilder) menu;
            menuBuilder.setOptionalIconsVisible(true);
        }

        if (contact != null && contact.isDeleted()) {
            menu.findItem(R.id.dial).setVisible(false);
            menu.findItem(R.id.refresh).setVisible(false);
            menu.findItem(R.id.userBlock).setVisible(false);
            menu.findItem(R.id.userUnBlock).setVisible(false);
            menu.findItem(R.id.dial).setVisible(false);
            return;
        }

        String contactNumber = contact != null ? contact.getContactNumber() : null;
        SettingsSharedPreference setting = SettingsSharedPreference.getInstance(getActivity());

        if ((setting.isHandleDial() && !TextUtils.isEmpty(contactNumber) && contactNumber.length() > 2)
                || (setting.isIPCallEnabled())) {
            if (setting.isIPCallEnabled()) {
                menu.findItem(R.id.dial).setVisible(true);
                menu.findItem(R.id.video_call).setVisible(true);
            }
            if (setting.isHandleDial()) {
                menu.findItem(R.id.dial).setVisible(true);
            }
        } else {
            menu.findItem(R.id.video_call).setVisible(false);
            menu.findItem(R.id.dial).setVisible(false);
        }
        if (channel != null) {
            menu.findItem(R.id.dial).setVisible(false);
            menu.findItem(R.id.video_call).setVisible(false);
            menu.findItem(R.id.share_conversation).setVisible(Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType()) && customizationSettings.isEnableShareConversation() && !channel.isDeleted());

            if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                String userId = ChannelService.getInstance(getActivity()).getGroupOfTwoReceiverUserId(channel.getKey());
                if (!TextUtils.isEmpty(userId) && customizationSettings.isBlockOption()) {
                    Contact withUserContact = appContactService.getContactById(userId);
                    if (withUserContact.isBlocked()) {
                        menu.findItem(R.id.userUnBlock).setVisible(true);
                    } else {
                        menu.findItem(R.id.userBlock).setVisible(true);
                    }
                }
            } else {
                menu.findItem(R.id.userBlock).setVisible(false);
                menu.findItem(R.id.userUnBlock).setVisible(false);
                if (customizationSettings.isMuteOption() && !Channel.GroupType.BROADCAST.getValue().equals(channel.getType())) {
                    menu.findItem(R.id.unmuteGroup).setVisible(!channel.isDeleted() && channel.isNotificationMuted());
                    menu.findItem(R.id.muteGroup).setVisible(!channel.isDeleted() && !channel.isNotificationMuted());
                }
            }
        } else if (customizationSettings.isMuteUserChatOption() && contact != null) {
            menu.findItem(R.id.userBlock).setVisible(false);
            menu.findItem(R.id.userUnBlock).setVisible(false);
            menu.findItem(R.id.unmuteGroup).setVisible(!contact.isDeleted() && contact.isNotificationMuted());
            menu.findItem(R.id.muteGroup).setVisible(!contact.isDeleted() && !contact.isNotificationMuted());

        } else if (contact != null && customizationSettings.isBlockOption()) {
            if (contact.isBlocked()) {
                menu.findItem(R.id.userUnBlock).setVisible(true);
            } else {
                menu.findItem(R.id.userBlock).setVisible(true);
            }
        }

        menu.removeItem(R.id.menu_search);

        if (channel != null && channel.isDeleted()) {
            menu.findItem(R.id.refresh).setVisible(false);
            menu.findItem(R.id.deleteConversation).setVisible(false);
        } else {
            menu.findItem(R.id.refresh).setVisible(customizationSettings.isRefreshOption());
            menu.findItem(R.id.deleteConversation).setVisible(customizationSettings.isDeleteOption());
        }

        menu.findItem(R.id.dial).setVisible(false);
        menu.findItem(R.id.refresh).setVisible(false);
        menu.findItem(R.id.userBlock).setVisible(false);
        menu.findItem(R.id.userUnBlock).setVisible(false);
        menu.findItem(R.id.dial).setVisible(false);
        menu.findItem(R.id.muteGroup).setVisible(false);
        menu.findItem(R.id.unmuteGroup).setVisible(false);
        menu.findItem(R.id.deleteConversation).setVisible(false);
        menu.findItem(R.id.logout).setVisible(customizationSettings.isLogoutOptionFromConversation());
        menu.findItem(R.id.rate_conversation).setVisible(themeHelper.isCollectFeedback() && customizationSettings.isRateConversationMenuOption());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.userBlock) {
            if (channel != null) {
                if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                    String userId = ChannelService.getInstance(getActivity()).getGroupOfTwoReceiverUserId(channel.getKey());
                    if (!TextUtils.isEmpty(userId)) {
                        userBlockDialog(true, appContactService.getContactById(userId), true);
                    }
                }
            } else if (contact != null) {
                userBlockDialog(true, contact, false);
            }
        }
        if (id == R.id.userUnBlock) {
            if (channel != null) {
                if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                    String userId = ChannelService.getInstance(getActivity()).getGroupOfTwoReceiverUserId(channel.getKey());
                    if (!TextUtils.isEmpty(userId)) {
                        userBlockDialog(false, appContactService.getContactById(userId), true);
                    }
                }
            } else if (contact != null) {
                userBlockDialog(false, contact, false);
            }
        }
        if (id == R.id.dial) {
            if (contact != null) {
                if (contact.isBlocked()) {
                    userBlockDialog(false, contact, false);
                } else {
                    ((ConversationActivity) getActivity()).processCall(contact, currentConversationId);
                }
            }
        }
        if (id == R.id.deleteConversation) {
            deleteConversationThread();
            return true;
        }

        if (id == R.id.share_conversation) {
            if (channel != null && Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType())) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType(text_plain);
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, new KmClientService(getContext()).getConversationShareUrl() + channel.getKey());
                startActivity(Intent.createChooser(sharingIntent, AppContextService.getContext(getContext()).getResources().getString(R.string.share_using)));
            }
        }

        if (id == R.id.video_call) {
            if (contact != null) {
                if (contact.isBlocked()) {
                    userBlockDialog(false, contact, false);
                } else {
                    ((ConversationActivity) getActivity()).processVideoCall(contact, currentConversationId);
                }
            }
        }
        if (id == R.id.muteGroup) {
            if (channel != null) {
                muteGroupChat();
            } else if (contact != null) {
                muteUserChat();
            }
        }
        if (id == R.id.unmuteGroup) {
            if (channel != null) {
                umuteGroupChat();
            } else if (contact != null) {
                unMuteUserChat();
            }
        }
        if (id == R.id.rate_conversation) {
            openFeedbackFragment();
        }
        return false;
    }

    protected void setSendButtonState(boolean enabled) {
        sendButton.setEnabled(enabled);
        sendButton.setClickable(enabled);
        KmUtils.setGradientSolidColor(sendButton, enabled ? themeHelper.getSendButtonBackgroundColor() : requireActivity().getResources().getColor(R.color.km_disabled_view_color));
    }

    protected void showCharLimitMessage(boolean isDialogFlowLimitMessage, boolean exceeded, int deltaCharacterCount, int charLimit) {
        textViewCharLimitMessage.setText(requireActivity().getString(isDialogFlowLimitMessage ? R.string.bot_char_limit : R.string.char_limit,
                charLimit,
                requireActivity().getString(exceeded ? R.string.remove_char_message : R.string.remaining_char_message, deltaCharacterCount)));
        textViewCharLimitMessage.setVisibility(VISIBLE);
        setSendButtonState(!exceeded);
    }

    protected void hideCharLimitMessage() {
        textViewCharLimitMessage.setVisibility(GONE);
        setSendButtonState(true);
    }

    protected void setVisibilityOfCharLimitMessage(final boolean isDialogFlowLimitMessage, final int messageCharacterLimit, int characterCount) {
        new KmInputTextLimitUtil(messageCharacterLimit, CHAR_LIMIT_WARNING).checkCharacterLimit(characterCount, new KmCharLimitCallback() {
            @Override
            public void onCrossed(boolean exceeded, boolean warning, int deltaCharacterCount) {
                showCharLimitMessage(isDialogFlowLimitMessage, exceeded, deltaCharacterCount, messageCharacterLimit);
            }

            @Override
            public void onNormal() {
                hideCharLimitMessage();
            }
        });
    }

    protected void toggleCharLimitExceededMessage(boolean isDialogFlowBot, int characterCount) {
        if (textViewCharLimitMessage == null || sendButton == null || messageEditText == null) {
            return;
        }

        int settingsMessageCharacterLimit = customizationSettings != null ? customizationSettings.getMessageCharacterLimit() : CustomizationSettings.DEFAULT_MESSAGE_CHAR_LIMIT;
        int messageCharacterLimit = isDialogFlowBot ? Math.min(CHAR_LIMIT_FOR_DIALOG_FLOW_BOT, settingsMessageCharacterLimit) : settingsMessageCharacterLimit;

        setVisibilityOfCharLimitMessage(isDialogFlowBot, messageCharacterLimit, characterCount);
    }

    protected void fetchBotTypeAndToggleCharLimitExceededMessage(Contact assignee, Channel channel, AppContactService appContactService, int loggedInUserRole) {
        if (assignee == null) {
            assignee = KmService.getSupportGroupContact(getContext(), channel, appContactService, loggedInUserRole);
        }

        if (assignee != null) {
            if (!User.RoleType.BOT.getValue().equals(assignee.getRoleType())) {
                isAssigneeDialogFlowBot = false;
                toggleCharLimitExceededMessage(false, messageEditText.getText().length());
            } else {
                fetchBotType(assignee, new TaskListener<String>() {
                    @Override
                    public void onSuccess(String botTypeResponseString) {
                        if (messageEditText != null) {
                            isAssigneeDialogFlowBot = "dialogflow".equals(botTypeResponseString);
                            toggleCharLimitExceededMessage(isAssigneeDialogFlowBot, messageEditText.getText().length());
                        }
                    }

                    @Override
                    public void onFailure(Exception error) {
                    }
                });
            }
        }
    }

    public void fetchBotTypeAndToggleCharLimitExceededMessage() {
        if (channel != null && appContactService != null) {
            fetchBotTypeAndToggleCharLimitExceededMessage(conversationAssignee, channel, appContactService, loggedInUserRole);
        }
    }

    public void loadConversation(Channel channel, Integer conversationId) {
        loadConversation(null, channel, conversationId, messageSearchString);
    }

    public void loadConversation(Contact contact, Integer conversationId) {
        loadConversation(contact, null, conversationId, messageSearchString);
    }

    //With search
    public void loadConversation(Contact contact, Integer conversationId, String messageSearchString) {
        loadConversation(contact, null, conversationId, messageSearchString);
    }

    public void loadConversation(Channel channel, Integer conversationId, String messageSearchString) {
        loadConversation(null, channel, conversationId, messageSearchString);
    }

    public void loadConversation(final Contact contact, final Channel channel, final Integer conversationId, final String searchString) {
        if (downloadConversation != null) {
            downloadConversation.cancel(true);
        }

        setContact(contact);
        setChannel(channel);
        KommunicateSettings.subscribeToTyping(getContext(), channel, contact);

        BroadcastService.currentUserId = contact != null ? contact.getContactIds() : String.valueOf(channel.getKey());
        typingStarted = false;
        onSelected = false;
        messageMetaData = null;

        if (userNotAbleToChatLayout != null) {
            if (contact != null && contact.isDeleted()) {
                userNotAbleToChatLayout.setVisibility(VISIBLE);
                handleSendAndRecordButtonView(true);
                individualMessageSendLayout.setVisibility(View.GONE);
            } else {
                userNotAbleToChatLayout.setVisibility(View.GONE);
                toggleMessageSendLayoutVisibility();
                toggleHideChatBarWithBots();
                toggleAttachmentLayoutVisibility();
            }
        }

        if (contact != null && channel != null) {
            if (getActivity() != null) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("");
            }
            if (menu != null) {
                menu.findItem(R.id.unmuteGroup).setVisible(false);
                menu.findItem(R.id.muteGroup).setVisible(false);
            }
        }
        if (replayRelativeLayout != null) {
            replayRelativeLayout.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(filePath) && attachmentLayout != null) {
            attachmentLayout.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(defaultText) && messageEditText != null) {
            messageEditText.setText(defaultText);
            defaultText = "";
        }

        extendedSendingOptionLayout.setVisibility(VISIBLE);

        unregisterForContextMenu(recyclerView);
        if (SettingsSharedPreference.getInstance(getActivity()).isNotificationStacking()) {
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(AppContextService.getContext(getContext()));
            notificationManagerCompat.cancel(NotificationService.NOTIFICATION_ID);
        } else {
            if (contact != null) {
                if (!TextUtils.isEmpty(contact.getContactIds())) {
                    NotificationManager notificationManager =
                            (NotificationManager) AppContextService.getContext(getContext()).getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager != null) {
                        notificationManager.cancel(contact.getContactIds().hashCode());
                    }
                }
            }

            if (channel != null) {
                NotificationManager notificationManager =
                        (NotificationManager) AppContextService.getContext(getContext()).getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.cancel(String.valueOf(channel.getKey()).hashCode());
                }
            }
        }

        clearList();
        updateTitle(contact, channel);
        swipeLayout.setEnabled(true);
        loadMore = true;
        if (selfDestructMessageSpinner != null) {
            selfDestructMessageSpinner.setSelection(0);
        }
        recyclerDetailConversationAdapter = getConversationAdapter(getActivity(),
                R.layout.message_row_view, messageList, contact, channel, messageIntentClass, emojiIconHandler, customizationSettings);
        recyclerDetailConversationAdapter.setAlCustomizationSettings(customizationSettings);
        recyclerDetailConversationAdapter.setupDarkMode(isCurrentlyInDarkMode);
        recyclerDetailConversationAdapter.setContextMenuClickListener(this);
        recyclerDetailConversationAdapter.setRichMessageCallbackListener(richMessageActionProcessor.getRichMessageListener());
        recyclerDetailConversationAdapter.setFontManager(fontManager);
        if (getActivity() instanceof KmStoragePermissionListener) {
            recyclerDetailConversationAdapter.setStoragePermissionListener((KmStoragePermissionListener) getActivity());
        } else {
            recyclerDetailConversationAdapter.setStoragePermissionListener(new KmStoragePermissionListener() {
                @Override
                public boolean isPermissionGranted() {
                    return false;
                }

                @Override
                public void checkPermission(KmStoragePermission storagePermission) {
                }
            });
        }

        linearLayoutManager.setSmoothScrollbarEnabled(true);
        recyclerView.setAdapter(recyclerDetailConversationAdapter);
        registerForContextMenu(recyclerView);

        processMobiTexterUserCheck();


        downloadConversation = new DownloadConversation(recyclerView, true, 1, 0, 0, contact, channel, conversationId, messageSearchString);
        downloadConversation.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        if (hideExtendedSendingOptionLayout) {
            extendedSendingOptionLayout.setVisibility(View.GONE);
        }
        emoticonsFrameLayout.setVisibility(View.GONE);

        if (contact != null) {
            Intent intent = new Intent(getActivity(), UserIntentService.class);
            intent.putExtra(UserIntentService.USER_ID, contact.getUserId());
            UserIntentService.enqueueWork(getActivity(), intent);
        }

        //initialize code for text limit on messageEditText
        messageEditText.addTextChangedListener(messageCharacterLimitTextWatcher);
        toggleCharLimitExceededMessage(false, messageEditText.getText().length());

        if (channel != null) {
            if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                String userId = ChannelService.getInstance(getActivity()).getGroupOfTwoReceiverUserId(channel.getKey());
                if (!TextUtils.isEmpty(userId)) {
                    Intent intent = new Intent(getActivity(), UserIntentService.class);
                    intent.putExtra(UserIntentService.USER_ID, userId);
                    UserIntentService.enqueueWork(getActivity(), intent);
                }
            } else if (!Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType())) {
                updateChannelSubTitle(channel);
            }

            //for char limit for the message sent to a dialog flow bot
            fetchBotTypeAndToggleCharLimitExceededMessage(conversationAssignee, channel, appContactService, loggedInUserRole);
        }

        InstructionUtil.showInstruction(getActivity(), R.string.instruction_go_back_to_recent_conversation_list, MobiComKitActivityInterface.INSTRUCTION_DELAY, BroadcastService.INTENT_ACTIONS.INSTRUCTION.toString());

        // check if conversation is a resolved one, and display the respective feedback layouts
        // also open the feedback input fragment if feedback isn't set
        if (channel != null && channel.getKmStatus() == Channel.CLOSED_CONVERSATIONS && !KmUtils.isAgent(getContext())) {
            setFeedbackDisplay(true);
        } else {
            setFeedbackDisplay(false);
        }
    }


    public void updateLastSeenStatus() {
        if (this.getActivity() == null) {
            return;
        }
        if (contact != null) {
            contact = appContactService.getContactById(contact.getContactIds());
        }

        if (contact != null) {
            processUpdateLastSeenStatus(contact);
        } else if (channel != null && Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
            String userId = ChannelService.getInstance(getActivity()).getGroupOfTwoReceiverUserId(channel.getKey());
            if (!TextUtils.isEmpty(userId)) {
                Contact withUserContact = appContactService.getContactById(userId);
                processUpdateLastSeenStatus(withUserContact);
            }

        }

    }

    protected void processUpdateLastSeenStatus(final Contact withUserContact) {
        if (withUserContact == null) {
            return;
        }

        if (this.getActivity() == null) {
            return;
        }
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (userNotAbleToChatLayout != null && individualMessageSendLayout != null) {
                    userNotAbleToChatLayout.setVisibility(withUserContact.isDeleted() ? VISIBLE : View.GONE);
                    individualMessageSendLayout.setVisibility(withUserContact.
                            isDeleted() ? View.GONE : VISIBLE);
                    if (withUserContact.isDeleted()) {
                        handleSendAndRecordButtonView(withUserContact.isDeleted());
                    }
                    bottomlayoutTextView.setText(R.string.user_has_been_deleted_text);
                }

                if (menu != null) {
                    menu.findItem(R.id.userBlock).setVisible(customizationSettings.isBlockOption() ? !withUserContact.isDeleted() && !withUserContact.isBlocked() : customizationSettings.isBlockOption());
                    menu.findItem(R.id.userUnBlock).setVisible(customizationSettings.isBlockOption() ? !withUserContact.isDeleted() && withUserContact.isBlocked() : customizationSettings.isBlockOption());
                    menu.findItem(R.id.refresh).setVisible(customizationSettings.isRefreshOption() ? !withUserContact.isDeleted() : customizationSettings.isRefreshOption());
                }

                if (withUserContact.isBlocked() || withUserContact.isBlockedBy() || withUserContact.isDeleted()) {
                    if (getActivity() != null) {
                        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("");
                    }
                    return;
                }
                if (withUserContact != null) {
                    if (withUserContact.isConnected()) {
                        typingStarted = false;
                        if (getActivity() != null) {
                            ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(AppContextService.getContext(getContext()).getString(R.string.user_online));
                        }
                    } else if (withUserContact.getLastSeenAt() != 0) {
                        if (getActivity() != null) {
                            ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(AppContextService.getContext(getContext()).getString(R.string.subtitle_last_seen_at_time) + " " + DateUtils.getDateAndTimeForLastSeen(AppContextService.getContext(getContext()), withUserContact.getLastSeenAt(), R.string.JUST_NOW, R.plurals.MINUTES_AGO, R.plurals.HOURS_AGO, R.string.YESTERDAY));
                        }
                    } else {
                        if (getActivity() != null) {
                            ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("");
                        }
                    }
                }
            }

        });
    }

    public void updateChannelSubTitle(final Channel channel) {
        if (channel.getGroupUsers() != null) {
            channelUserMapperList = channel.getGroupUsers();
        } else {
            channelUserMapperList = ChannelService.getInstance(getActivity()).getListOfUsersFromChannelUserMapper(channel.getKey());

        }
        if (channelUserMapperList != null && channelUserMapperList.size() > 0) {
            if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                String userId = ChannelService.getInstance(getActivity()).getGroupOfTwoReceiverUserId(channel.getKey());
                if (!TextUtils.isEmpty(userId)) {
                    final Contact withUserContact = appContactService.getContactById(userId);
                    if (withUserContact != null) {
                        if (getActivity() == null) {
                            return;
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (withUserContact.isBlocked()) {
                                    if (getActivity() != null) {
                                        setToolbarSubtitle("");
                                    }
                                } else {
                                    if (withUserContact.isConnected() && getActivity() != null) {
                                        setToolbarSubtitle(AppContextService.getContext(getContext()).getString(R.string.user_online));
                                    } else if (withUserContact.getLastSeenAt() != 0 && getActivity() != null) {
                                        setToolbarSubtitle(AppContextService.getContext(getContext()).getString(R.string.subtitle_last_seen_at_time) + " " + DateUtils.getDateAndTimeForLastSeen(getContext(), withUserContact.getLastSeenAt(), R.string.JUST_NOW, R.plurals.MINUTES_AGO, R.plurals.HOURS_AGO, R.string.YESTERDAY));
                                    } else if (getActivity() != null) {
                                        setToolbarSubtitle("");
                                    }
                                }
                            }
                        });
                    }
                }

            } else {
                final StringBuffer stringBuffer = new StringBuffer();
                Contact contactDisplayName;
                String youString = "";
                int i = 0;
                for (ChannelUserMapper channelUserMapper : channelUserMapperList) {
                    i++;
                    if (i > 20) {
                        break;
                    }
                    contactDisplayName = appContactService.getContactById(channelUserMapper.getUserKey());
                    if (!TextUtils.isEmpty(channelUserMapper.getUserKey())) {
                        if (loggedInUserId.equals(channelUserMapper.getUserKey())) {
                            youString = AppContextService.getContext(getContext()).getString(R.string.you_string);
                        } else {
                            stringBuffer.append(contactDisplayName.getDisplayName()).append(",");
                        }
                    }
                }

                final String finalYouString = youString;
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (!TextUtils.isEmpty(stringBuffer)) {
                            if (channelUserMapperList.size() <= 20) {
                                if (!TextUtils.isEmpty(finalYouString)) {
                                    stringBuffer.append(finalYouString).append(",");
                                }
                                int lastIndex = stringBuffer.lastIndexOf(",");
                                String userIds = stringBuffer.replace(lastIndex, lastIndex + 1, "").toString();
                                if (getActivity() != null) {
                                    setToolbarSubtitle(userIds);
                                }
                            } else {
                                if (getActivity() != null) {
                                    setToolbarSubtitle(stringBuffer.toString());
                                }
                            }
                        } else {
                            if (getActivity() != null) {
                                setToolbarSubtitle(finalYouString);
                            }
                        }
                    }
                });
            }

        }
    }

    protected void setToolbarTitle(String title) {
        if (getActivity() == null) {
            return;
        }
        ((CustomToolbarListener) getActivity()).setToolbarTitle(title);
    }

    protected void setToolbarSubtitle(String subtitle) {
        if (getActivity() == null) {
            return;
        }
        if ((customizationSettings.isGroupSubtitleHidden() || KommunicateSetting.getInstance(getContext()).isGroupSubtitleHidden()) && channel != null && !subtitle.contains(AppContextService.getContext(getContext()).getString(R.string.is_typing))) {
            ((CustomToolbarListener) getActivity()).setToolbarSubtitle("");
            return;
        }
        ((CustomToolbarListener) getActivity()).setToolbarSubtitle(subtitle);
    }

    public boolean isBroadcastedToChannel(Integer channelKey) {
        return getChannel() != null && getChannel().getKey().equals(channelKey);
    }

    public boolean getCurrentChannelKey(Integer channelKey) {
        return channel != null && channel.getKey().equals(channelKey);
    }

    public Channel getChannel() {
        return channel;
    }

    protected void setChannel(Channel channel) {
        if (channel == null) {
            return;
        }
        this.channel = channel;
        boolean isUserPresent = true;
        if (channel.getGroupUsers() != null) {
            isUserPresent = channel.isUserPresentInChannel(MobiComUserPreference.getInstance(getContext()).getUserId());
        } else {
            isUserPresent = ChannelService.getInstance(getContext()).isUserAlreadyPresentInChannel(channel.getKey(), MobiComUserPreference.getInstance(getContext()).getUserId());
        }
        if (!isUserPresent && messageTemplate != null && messageTemplate.isEnabled() && templateAdapter != null) {
            templateAdapter.setMessageList(new HashMap<String, String>());
            templateAdapter.notifyDataSetChanged();
        }
    }

    public boolean isMsgForConversation(Message message) {

        if (BroadcastService.isContextBasedChatEnabled() && message.getConversationId() != null) {
            return isMessageForCurrentConversation(message) && compareConversationId(message);
        }
        return isMessageForCurrentConversation(message);
    }

    public boolean isMessageForCurrentConversation(Message message) {
        return (message.getGroupId() != null && channel != null && message.getGroupId().equals(channel.getKey())) ||
                (!TextUtils.isEmpty(message.getContactIds()) && contact != null && message.getContactIds().equals(contact.getContactIds())) && message.getGroupId() == null;
    }

    public boolean compareConversationId(Message message) {
        return message.getConversationId() != null && currentConversationId != null && message.getConversationId().equals(currentConversationId);
    }

    public void updateUploadFailedStatus(final Message message) {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int i = messageList.indexOf(message);
                if (i != -1) {
                    messageList.get(i).setCanceled(true);
                    recyclerDetailConversationAdapter.notifyDataSetChanged();
                }
            }
        });

    }

    public void downloadFailed(final Message message) {
        if (getActivity() == null) {
            return;
        }
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int index = messageList.indexOf(message);
                if (index != -1) {
                    View view = recyclerView.getChildAt(index -
                            linearLayoutManager.findFirstVisibleItemPosition());

                    if (view != null) {
                        final LinearLayout attachmentDownloadLayout = (LinearLayout) view.findViewById(R.id.attachment_download_layout);
                        attachmentDownloadLayout.setVisibility(VISIBLE);
                    }

                }
            }

        });
    }

    public abstract void attachLocation(Location currentLocation);

    public void updateDeliveryStatusForAllMessages(final boolean markRead) {
        if (getActivity() == null) {
            return;
        }

        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Drawable statusIcon = deliveredIcon;
                    if (markRead) {
                        statusIcon = readIcon;
                    }
                    for (int index = 0; index < messageList.size(); index++) {
                        Message message = messageList.get(index);
                        if ((message.getStatus() == Message.Status.DELIVERED_AND_READ.getValue()) || message.isTempDateType() || message.isCustom() || !message.isTypeOutbox() || message.isChannelCustomMessage()) {
                            continue;
                        }
                        if (messageList.get(index) != null) {
                            messageList.get(index).setDelivered(true);
                        }
                        message.setDelivered(true);
                        if (markRead) {
                            if (messageList.get(index) != null) {
                                messageList.get(index).setStatus(Message.Status.DELIVERED_AND_READ.getValue());
                            }
                            message.setStatus(Message.Status.DELIVERED_AND_READ.getValue());
                        }
                        View view = recyclerView.getChildAt(index -
                                linearLayoutManager.findFirstVisibleItemPosition());
                        if (view != null && !message.isCustom() && !message.isChannelCustomMessage()) {
                            TextView statusImage = view.findViewById(R.id.statusImage);
                            ImageView statusImageView = view.findViewById(R.id.statusImageView);
                            if (customizationSettings.getInnerTimestampDesign()) {
                                statusImage.setCompoundDrawablesWithIntrinsicBounds(null, null, statusIcon, null);
                            } else {
                                statusImageView.setImageDrawable(statusIcon);
                            }
                        }
                    }
                } catch (Exception ex) {
                    Utils.printLog(getContext(), TAG, "Exception while updating delivery status in UI.");
                }
            }
        });
    }

    public void updateDeliveryStatus(final Message message) {
        if (getActivity() != null) {
            this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int index = messageList.indexOf(message);
                        if (index != -1) {
                            if (messageList.get(index).getStatus() == Message.Status.DELIVERED_AND_READ.getValue()
                                    || messageList.get(index).isTempDateType()
                                    || messageList.get(index).isCustom()
                                    || messageList.get(index).isChannelCustomMessage()) {
                                return;
                            }
                            messageList.get(index).setDelivered(true);
                            messageList.get(index).setStatus(message.getStatus());
                            showBotTypingIndicator();
                            View view = recyclerView.getChildAt(index -
                                    linearLayoutManager.findFirstVisibleItemPosition());
                            if (view != null && !messageList.get(index).isCustom()) {
                                Drawable statusIcon = deliveredIcon;
                                if (message.getStatus() == Message.Status.DELIVERED_AND_READ.getValue()) {
                                    statusIcon = readIcon;
                                    messageList.get(index).setStatus(Message.Status.DELIVERED_AND_READ.getValue());
                                }
                                TextView statusImage = view.findViewById(R.id.statusImage);
                                ImageView statusImageView = view.findViewById(R.id.statusImageView);
                                if (customizationSettings.getInnerTimestampDesign()) {
                                    statusImage.setCompoundDrawablesWithIntrinsicBounds(null, null, statusIcon, null);
                                } else {
                                    statusImageView.setImageDrawable(statusIcon);
                                }
                            }
                        } else if (!message.isVideoNotificationMessage() && !message.isHidden()) {
                            messageList.add(message);
                            linearLayoutManager.scrollToPositionWithOffset(messageList.size() - 1, 0);
                            emptyTextView.setVisibility(View.GONE);
                            recyclerDetailConversationAdapter.notifyDataSetChanged();
                        }
                    } catch (Exception ex) {
                        Utils.printLog(getContext(), TAG, "Exception while updating delivery status in UI.");
                    }
                }
            });
        }
    }

    private void showBotTypingIndicator() {
        int botIntervalDelay = KmAppSettingPreferences.INSTANCE.getBotTypingIndicatorInterval();
        if (botIntervalDelay == 0) {
            return;
        }

        // check conversation is assigned to bot
        String assigneeId = channel.getConversationAssignee();
        if (assigneeId == null) {
            return;
        }
        Contact assigneeInfo = new AppContactService(getContext()).getContactById(assigneeId);
        if (assigneeInfo == null || !assigneeInfo.getRoleType().equals(User.RoleType.BOT.getValue())) {
            return;
        }

        handleTypingMessage(true);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            handleTypingMessage(false);
        }, botIntervalDelay);
    }

    public void loadFile(Uri uri, File file, String mimeType) {
        if (uri == null || file == null) {
            KmToast.error(AppContextService.getContext(getContext()), AppContextService.getContext(getContext()).getString(R.string.file_not_selected), Toast.LENGTH_LONG).show();
            return;
        }
        handleSendAndRecordButtonView(true);
        filePath = Uri.parse(file.getAbsolutePath()).toString();
        if (TextUtils.isEmpty(filePath)) {
            Utils.printLog(getContext(), TAG, "Error while fetching filePath");
            attachmentLayout.setVisibility(View.GONE);
            KmToast.error(AppContextService.getContext(getContext()), AppContextService.getContext(getContext()).getString(R.string.info_file_attachment_error), Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(mimeType)) {
            mimeType = AppContextService.getContext(getContext()).getContentResolver().getType(uri);
        }
        Cursor returnCursor =
                AppContextService.getContext(getContext()).getContentResolver().query(uri, null, null, null, null);
        if (returnCursor != null) {
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            long fileSize = returnCursor.getLong(sizeIndex);
            long maxFileSize = customizationSettings.getMaxAttachmentSizeAllowed() * 1024 * 1024;
            if (fileSize > maxFileSize) {
                KmToast.makeText(AppContextService.getContext(getContext()), AppContextService.getContext(getContext()).getString(R.string.info_attachment_max_allowed_file_size), Toast.LENGTH_LONG).show();
                return;
            }
            attachedFile.setText(returnCursor.getString(returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)));
            returnCursor.close();
        }
        attachmentLayout.setVisibility(VISIBLE);
        if (mimeType != null && (mimeType.startsWith(IMAGE) || mimeType.startsWith(VIDEO))) {
            sendMessage();
        } else {
            attachedFile.setVisibility(VISIBLE);
            mediaContainer.setImageBitmap(null);
        }
    }

    public synchronized boolean updateMessageList(Message message, boolean update) {
        boolean toAdd = !messageList.contains(message);
        loadMore = true;
        if (update) {
            messageList.remove(message);
            messageList.add(message);
        } else if (toAdd) {
            Message firstDateMessage = new Message();
            firstDateMessage.setTempDateType(Short.valueOf("100"));
            firstDateMessage.setCreatedAtTime(message.getCreatedAtTime());
            if (!messageList.contains(firstDateMessage)) {
                messageList.add(firstDateMessage);
            }

            messageList.add(message);
        }
        return toAdd;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            messageCommunicator = (MessageCommunicator) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + IMPLEMENT_INTERFACE_COMMUNICATOR);
        }
    }

    protected AlertDialog showInviteDialog(int titleId, int messageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(AppContextService.getContext(getContext()).getString(messageId).replace(NAME, getNameForInviteDialog()))
                .setTitle(titleId);
        builder.setPositiveButton(R.string.invite, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent share = new Intent(Intent.ACTION_SEND);
                startActivity(Intent.createChooser(share, "Share Via"));
                sendType.setSelection(0);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                sendType.setSelection(0);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }


    public String getNameForInviteDialog() {
        if (contact != null) {
            return contact.getDisplayName();
        } else if (channel != null) {
            if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                String userId = ChannelService.getInstance(getActivity()).getGroupOfTwoReceiverUserId(channel.getKey());
                if (!TextUtils.isEmpty(userId)) {
                    Contact withUserContact = appContactService.getContactById(userId);
                    return withUserContact.getDisplayName();
                }
            } else {
                return ChannelUtils.getChannelTitleName(channel, MobiComUserPreference.getInstance(getActivity()).getUserId());
            }
        }
        return "";
    }

    public void onClickOnMessageReply(Message message) {
        if (message != null) {
            if (recyclerView != null) {
                int height = recyclerView.getHeight();
                int itemHeight = recyclerView.getChildAt(0).getHeight();
                int index = messageList.indexOf(message);
                if (index != -1) {
                    recyclerView.requestFocusFromTouch();
                    linearLayoutManager.scrollToPositionWithOffset(index, height / 2 - itemHeight / 2);
                    recyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (recyclerView != null) {
                                try {
                                    if (recyclerView.isFocused()) {
                                        recyclerView.clearFocus();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }, 800);
                }
            }

        }
    }

    public void forwardMessage(Message messageToForward, Contact contact, Channel channel) {
        this.contact = contact;
        this.channel = channel;
        if (messageToForward.isAttachmentDownloaded()) {
            filePath = messageToForward.getFilePaths().get(0);
        }
        this.messageToForward = messageToForward;
        loadConversation(contact, channel, currentConversationId, null);

    }

    protected void sendForwardMessage(Message messageToForward) {
        MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(getActivity());

        if (channel != null) {
            if (!ChannelService.getInstance(getContext()).processIsUserPresentInChannel(channel.getKey())) {
                return;
            }
            messageToForward.setGroupId(channel.getKey());
            messageToForward.setClientGroupId(null);
            messageToForward.setContactIds(null);
            messageToForward.setTo(null);
        } else {
            if (contact.isBlocked()) {
                return;
            }
            messageToForward.setGroupId(null);
            messageToForward.setClientGroupId(null);
            messageToForward.setTo(contact.getContactIds());
            messageToForward.setContactIds(contact.getContactIds());
        }

        messageToForward.setKeyString(null);
        messageToForward.setMessageId(null);
        messageToForward.setDelivered(false);
        messageToForward.setRead(Boolean.TRUE);
        messageToForward.setStoreOnDevice(Boolean.TRUE);
        messageToForward.setCreatedAtTime(System.currentTimeMillis() + userPreferences.getDeviceTimeOffset());
        if (currentConversationId != null && currentConversationId != 0) {
            messageToForward.setConversationId(currentConversationId);
        }
        Map<String, String> metaDataMapForward = messageToForward.getMetadata();
        if (metaDataMapForward != null && !metaDataMapForward.isEmpty() && metaDataMapForward.get(Message.MetaDataType.AL_REPLY.getValue()) != null) {
            messageToForward.setMetadata(null);
        }
        messageToForward.setSendToDevice(Boolean.FALSE);
        messageToForward.setType(sendType.getSelectedItemId() == 1 ? Message.MessageType.MT_OUTBOX.getValue() : Message.MessageType.OUTBOX.getValue());
        messageToForward.setTimeToLive(getTimeToLive());
        messageToForward.setSentToServer(false);
        messageToForward.setStatus(Message.Status.READ.getValue());

        if (!TextUtils.isEmpty(filePath)) {
            List<String> filePaths = new ArrayList<String>();
            filePaths.add(filePath);
            messageToForward.setFilePaths(filePaths);
        }
        conversationService.sendMessage(messageToForward, messageIntentClass);
        if (selfDestructMessageSpinner != null) {
            selfDestructMessageSpinner.setSelection(0);
        }
        attachmentLayout.setVisibility(View.GONE);
        filePath = null;
    }

    protected Map<String, String> getMessageMetadata(Map<String, String> newMetadata) {
        Map<String, String> mergedMetaData = new HashMap<>();
        Map<String, String> existingMetadata = null;

        if (!TextUtils.isEmpty(SettingsSharedPreference.getInstance(getActivity()).getMessageMetaData())) {
            Type mapType = new TypeToken<Map<String, String>>() {
            }.getType();
            try {
                existingMetadata = new Gson().fromJson(SettingsSharedPreference.getInstance(getActivity()).getMessageMetaData(), mapType);
                if (existingMetadata != null && !existingMetadata.isEmpty()) {
                    mergedMetaData.putAll(existingMetadata);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (newMetadata != null && !newMetadata.isEmpty()) {
            if (existingMetadata != null && !existingMetadata.isEmpty()) {
                for (String key : existingMetadata.keySet()) {
                    if (newMetadata.containsKey(key)) {
                        try {
                            Map<String, String> existingMetadataValueMap = getDataMap(existingMetadata.get(key));

                            if (existingMetadataValueMap != null) {
                                Map<String, String> newMetadataValueMap = getDataMap(newMetadata.get(key));
                                if (newMetadataValueMap != null) {
                                    existingMetadataValueMap.putAll(newMetadataValueMap);
                                    mergedMetaData.put(key, GsonUtils.getJsonFromObject(existingMetadataValueMap, Map.class));
                                    newMetadata.remove(key);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            mergedMetaData.putAll(newMetadata);
        }

        if (this.messageMetaData != null && !this.messageMetaData.isEmpty()) {
            mergedMetaData.putAll(this.messageMetaData);
        }

        return mergedMetaData;
    }

    protected Map<String, String> getDataMap(String data) {
        if (!TextUtils.isEmpty(data)) {
            try {
                return (Map<String, String>) GsonUtils.getObjectFromJson(data, Map.class);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    public void sendProductMessage(final String messageToSend, final FileMeta fileMeta, final Contact contact, final short messageContentType) {
        final Message message = new Message();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String topicId;
                MobiComConversationService conversationService = new MobiComConversationService(getActivity());
                MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(getActivity());
                topicId = new MessageClientService(getActivity()).getTopicId(currentConversationId);
                if (getChannel() != null) {
                    message.setGroupId(channelKey);
                } else {
                    message.setContactIds(contact.getUserId());
                    message.setTo(contact.getUserId());
                }
                message.setMessage(messageToSend);
                message.setRead(Boolean.TRUE);
                message.setStoreOnDevice(Boolean.TRUE);
                message.setSendToDevice(Boolean.FALSE);
                message.setContentType(messageContentType);
                message.setType(Message.MessageType.MT_OUTBOX.getValue());
                message.setDeviceKeyString(userPreferences.getDeviceKeyString());
                message.setSource(Message.Source.MT_MOBILE_APP.getValue());
                message.setTopicId(messageToSend);
                message.setCreatedAtTime(System.currentTimeMillis() + userPreferences.getDeviceTimeOffset());
                message.setTopicId(topicId);
                message.setConversationId(currentConversationId);
                message.setFileMetas(fileMeta);
                conversationService.sendMessage(message, MessageIntentService.class);
            }
        }).start();

    }

    public void sendBroadcastMessage(String message, String path) {
        MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(getActivity());
        if (channelUserMapperList != null && channelUserMapperList.size() > 0) {
            for (ChannelUserMapper channelUserMapper : channelUserMapperList) {
                if (!userPreferences.getUserId().equals(channelUserMapper.getUserKey())) {
                    Message messageToSend = new Message();
                    messageToSend.setTo(channelUserMapper.getUserKey());
                    messageToSend.setContactIds(channelUserMapper.getUserKey());
                    messageToSend.setRead(Boolean.TRUE);
                    messageToSend.setStoreOnDevice(Boolean.TRUE);
                    if (messageToSend.getCreatedAtTime() == null) {
                        messageToSend.setCreatedAtTime(System.currentTimeMillis() + userPreferences.getDeviceTimeOffset());
                    }
                    if (currentConversationId != null && currentConversationId != 0) {
                        messageToSend.setConversationId(currentConversationId);
                    }
                    messageToSend.setSendToDevice(Boolean.FALSE);
                    messageToSend.setType(sendType.getSelectedItemId() == 1 ? Message.MessageType.MT_OUTBOX.getValue() : Message.MessageType.OUTBOX.getValue());
                    messageToSend.setTimeToLive(getTimeToLive());
                    messageToSend.setMessage(message);
                    messageToSend.setDeviceKeyString(userPreferences.getDeviceKeyString());
                    messageToSend.setSource(Message.Source.MT_MOBILE_APP.getValue());
                    if (!TextUtils.isEmpty(path)) {
                        List<String> filePaths = new ArrayList<String>();
                        filePaths.add(path);
                        messageToSend.setFilePaths(filePaths);
                    }
                    conversationService.sendMessage(messageToSend, MessageIntentService.class);

                    if (selfDestructMessageSpinner != null) {
                        selfDestructMessageSpinner.setSelection(0);
                    }
                    attachmentLayout.setVisibility(View.GONE);
                }
            }
        }
    }

    protected Integer getTimeToLive() {
        if (selfDestructMessageSpinner == null || selfDestructMessageSpinner.getSelectedItemPosition() <= 1) {
            return null;
        }
        return Integer.parseInt(selfDestructMessageSpinner.getSelectedItem().toString().replace("mins", "").replace("min", "").trim());
    }

    public void updateMessageKeyString(final Message message) {
        if (getActivity() == null) {
            return;
        }
        //Showing typing indicator when bot is fetching response from API
        if (customizationSettings.isShowTypingIndicatorWhileFetchingResponse()) {
            if (Objects.equals(conversationAssignee.getRoleType(), User.RoleType.BOT.getValue())) {
                updateTypingStatus(message.getTo(), true);
            }
        }

        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int index = messageList.indexOf(message);
                if (index != -1) {
                    Message messageListItem = messageList.get(index);
                    messageListItem.setKeyString(message.getKeyString());
                    messageListItem.setSentToServer(true);
                    messageListItem.setCreatedAtTime(message.getSentMessageTimeAtServer());
                    messageListItem.setFileMetaKeyStrings(message.getFileMetaKeyStrings());
                    messageListItem.setFileMetas(message.getFileMetas());
                    if (messageList.get(index) != null) {
                        messageList.get(index).setKeyString(message.getKeyString());
                        messageList.get(index).setSentToServer(true);
                        messageList.get(index).setCreatedAtTime(message.getSentMessageTimeAtServer());
                        messageList.get(index).setFileMetaKeyStrings(message.getFileMetaKeyStrings());
                        messageList.get(index).setFileMetas(message.getFileMetas());
                    }
                    View view = recyclerView.getChildAt(index - linearLayoutManager.findFirstVisibleItemPosition());
                    if (view != null) {
                        ProgressBar mediaUploadProgressBarIndividualMessage = (ProgressBar) view.findViewById(R.id.media_upload_progress_bar);
                        RelativeLayout downloadInProgressLayout = (RelativeLayout) view.findViewById(R.id.doc_download_progress_rl);
                        if (mediaUploadProgressBarIndividualMessage != null) {
                            mediaUploadProgressBarIndividualMessage.setVisibility(View.GONE);
                        }
                        if (downloadInProgressLayout != null) {
                            downloadInProgressLayout.setVisibility(View.GONE);
                        }
                        if (message.getFileMetas() != null && !IMAGE.contains(message.getFileMetas().getContentType()) && !VIDEO.contains(message.getFileMetas().getContentType())) {
                            RelativeLayout applozicDocRelativeLayout = (RelativeLayout) view.findViewById(R.id.doc_downloaded);
                            ImageView imageViewDoc = (ImageView) applozicDocRelativeLayout.findViewById(R.id.doc_icon);
                            if (message.getFileMetas() != null) {
                                if (message.getFileMetas().getContentType().contains(AUDIO)) {
                                    imageViewDoc.setImageResource(R.drawable.ic_play_circle_outline);
                                } else {
                                    setDocumentIcon(null, imageViewDoc);
                                    setDocumentIcon(message.getFileMetas().getContentType(), imageViewDoc);
                                }
                                applozicDocRelativeLayout.setVisibility(VISIBLE);
                            } else if (message.getFilePaths() != null) {
                                String filePath = message.getFilePaths().get(0);
                                final String mimeType = FileUtils.getMimeType(filePath);
                                if (mimeType.contains(AUDIO)) {
                                    imageViewDoc.setImageResource(R.drawable.ic_play_circle_outline);
                                } else {
                                    setDocumentIcon(mimeType, imageViewDoc);
                                }
                                applozicDocRelativeLayout.setVisibility(VISIBLE);
                            }
                        }
                        TextView createdAtTime = (TextView) view.findViewById(R.id.createdAtTime);
                        TextView statusTextView = view.findViewById(R.id.statusImage);
                        ImageView statusImageView = view.findViewById(R.id.statusImageView);

                        if (statusTextView != null && messageListItem.getKeyString() != null && messageListItem.isTypeOutbox() && !messageListItem.isCall() && !messageListItem.getDelivered() && !messageListItem.isCustom() && !messageListItem.isChannelCustomMessage() && messageListItem.getScheduledAt() == null) {
                            if (customizationSettings.getInnerTimestampDesign()) {
                                statusTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, sentIcon, null);
                            } else {
                                statusImageView.setImageDrawable(sentIcon);
                            }
                        }
                    }
                }
            }
        });
    }

    public void updateDownloadStatus(final Message message) {
        if (getActivity() == null) {
            return;
        }

        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    int index = messageList.indexOf(message);
                    if (index != -1) {
                        Message smListItem = messageList.get(index);
                        smListItem.setKeyString(message.getKeyString());
                        smListItem.setFileMetaKeyStrings(message.getFileMetaKeyStrings());
                        if (messageList.get(index) != null) {
                            messageList.get(index).setKeyString(message.getKeyString());
                            messageList.get(index).setFileMetaKeyStrings(message.getFileMetaKeyStrings());
                        }
                        View view = recyclerView.getChildAt(index - linearLayoutManager.findFirstVisibleItemPosition());
                        if (view != null) {
                            final RelativeLayout attachmentDownloadProgressLayout = (RelativeLayout) view.findViewById(R.id.attachment_download_progress_layout);
                            final AttachmentView attachmentView = (AttachmentView) view.findViewById(R.id.main_attachment_view);
                            final ImageView preview = (ImageView) view.findViewById(R.id.preview);
                            TextView audioDurationTextView = (TextView) view.findViewById(R.id.audio_duration_textView);
                            final ImageView videoIcon = (ImageView) view.findViewById(R.id.video_icon);
                            String audioDuration;
                            if (message.getFileMetas() != null && message.getFileMetas().getContentType().contains(IMAGE)) {
                                attachmentView.setVisibility(VISIBLE);
                                preview.setVisibility(View.GONE);
                                attachmentView.setMessage(smListItem);
                                attachmentDownloadProgressLayout.setVisibility(View.GONE);
                            } else if (message.getFileMetas() != null && message.getFileMetas().getContentType().contains(VIDEO)) {
                                FileClientService fileClientService = new FileClientService(getContext());
                                attachedFile.setVisibility(View.GONE);
                                preview.setVisibility(VISIBLE);
                                videoIcon.setVisibility(VISIBLE);
                                preview.setImageBitmap(fileClientService.createAndSaveVideoThumbnail(message.getFilePaths().get(0)));
                            } else if (message.getFileMetas() != null) {
                                //Hide Attachment View...
                                RelativeLayout applozicDocRelativeLayout = (RelativeLayout) view.findViewById(R.id.doc_downloaded);
                                ImageView imageViewDoc = (ImageView) applozicDocRelativeLayout.findViewById(R.id.doc_icon);
                                if (message.getFileMetas() != null && message.getFilePaths() == null) {
                                    String filePath = message.getFilePaths().get(0);
                                    final String mimeType = FileUtils.getMimeType(filePath);
                                    if (message.getFileMetas().getContentType().contains(AUDIO)) {
                                        imageViewDoc.setImageResource(R.drawable.ic_play_circle_outline);
                                    } else {
                                        setDocumentIcon(mimeType, imageViewDoc);
                                    }
                                    applozicDocRelativeLayout.setVisibility(VISIBLE);
                                } else if (message.getFilePaths() != null) {
                                    String filePath = message.getFilePaths().get(0);
                                    final String mimeType = FileUtils.getMimeType(filePath);
                                    if (mimeType.contains(AUDIO)) {
                                        if (message.isAttachmentDownloaded()) {
                                            audioDuration = KommunicateAudioManager.getInstance(getContext()).refreshAudioDuration(filePath);
                                            audioDurationTextView.setVisibility(View.VISIBLE);
                                            audioDurationTextView.setText(audioDuration);
                                        } else {
                                            audioDurationTextView.setVisibility(View.VISIBLE);
                                            audioDurationTextView.setText("00:00");
                                        }
                                        imageViewDoc.setImageResource(R.drawable.ic_play_circle_outline);
                                    } else {
                                        setDocumentIcon(mimeType, imageViewDoc);
                                    }
                                    applozicDocRelativeLayout.setVisibility(VISIBLE);
                                }
                                view.findViewById(R.id.doc_download_progress_rl).setVisibility(View.GONE);
                            }
                        }

                    }
                } catch (Exception ex) {
                    Utils.printLog(getContext(), TAG, "Exception while updating download status: " + ex.getMessage());
                }
            }
        });
    }

    public void setDefaultText(String defaultText) {
        this.defaultText = defaultText;
    }

    public void setConversationId(Integer conversationId) {
        this.currentConversationId = conversationId;
    }

    public void updateUserTypingStatus(final String typingUserId, final String isTypingStatus) {
        updateTypingStatus(typingUserId, "1".equals(isTypingStatus));
    }

    public void updateTypingStatus(final String typingUserId, final boolean start) {
        if (contact != null) {
            if (contact.isBlocked() || contact.isBlockedBy()) {
                return;
            }
        }

        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (start) {
                        CountDownTimer timer = getCountDownTimer(typingUserId);
                        if (timer != null) {
                            timer.start();
                        }

                        if (channel != null) {
                            if (!MobiComUserPreference.getInstance(getActivity()).getUserId().equals(typingUserId)) {
                                Contact displayNameContact = appContactService.getContactById(typingUserId);
                                if (displayNameContact.isBlocked() || displayNameContact.isBlockedBy()) {
                                    return;
                                }
                                if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                                    ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(AppContextService.getContext(getContext()).getString(R.string.is_typing));
                                } else if (Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType())) {
                                    handleTypingMessage(true);
                                } else {
                                    ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(displayNameContact.getDisplayName() + " " + AppContextService.getContext(getContext()).getString(R.string.is_typing));
                                }
                            }
                        } else {
                            ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(AppContextService.getContext(getContext()).getString(R.string.is_typing));
                        }
                    } else {
                        if (typingTimerMap != null) {
                            CountDownTimer timer = typingTimerMap.get(typingUserId);
                            if (timer != null) {
                                typingTimerMap.remove(typingUserId);
                                timer.cancel();
                            }
                        }

                        if (channel != null) {
                            if (!MobiComUserPreference.getInstance(getActivity()).getUserId().equals(typingUserId)) {
                                Contact displayNameContact = appContactService.getContactById(typingUserId);
                                if (displayNameContact.isBlocked() || displayNameContact.isBlockedBy()) {
                                    return;
                                }
                                if (Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType())) {
                                    handleTypingMessage(false);
                                    if (conversationAssignee != null) {
                                        switchContactStatus(conversationAssignee, null);
                                    } else {
                                        processSupportGroupDetails(channel);
                                    }
                                } else {
                                    updateChannelSubTitle(channel);
                                }
                            }
                        } else {
                            updateLastSeenStatus();
                        }
                    }
                }
            });
        }
    }

    private void handleTypingMessage(boolean show) {
        if (show) {
            for (int i = messageList.size() - 1; i > 0; i--) {
                if (messageList.get(i).isTypingMessage()) {
                    messageList.remove(i);
                    break;
                }
            }
            Message typingMessage = new Message();
            typingMessage.setTypingMessage();
            messageList.add(typingMessage);
            linearLayoutManager.scrollToPosition(messageList.size() - 1);
            recyclerDetailConversationAdapter.onItemInserted(messageList.size() - 1);
        } else {
            try {
                int position;
                for (Message message : messageList) {
                    if (message.getType() != null && message.isTypingMessage()) {
                        position = messageList.indexOf(message);
                        messageList.remove(position);
                        recyclerDetailConversationAdapter.notifyItemRemoved(position);
                        if (messageList.isEmpty()) {
                            emptyTextView.setVisibility(VISIBLE);
                            if (getActivity() != null) {
                                ((MobiComKitActivityInterface) getActivity()).removeConversation(message, channel != null ? String.valueOf(channel.getKey()) : contact.getUserId());
                            }
                        }
                        break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //    public void onEmojiconClicked(Emojicon emojicon) {
    //        //TODO: Move OntextChangeListiner to EmojiEditableTExt
    //        int currentPos = messageEditText.getSelectionStart();
    //        messageEditText.setTextKeepState(messageEditText.getText().
    //                insert(currentPos, emojicon.getEmoji()));
    //    }

    //TODO: Please add onclick events here...  anonymous class are
    // TODO :hard to read and suggested if we have very few event view
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.emoji_btn) {
            if (emoticonsFrameLayout.getVisibility() == VISIBLE) {
                emoticonsFrameLayout.setVisibility(View.GONE);
                Utils.toggleSoftKeyBoard(getActivity(), false);
            } else {
                Utils.toggleSoftKeyBoard(getActivity(), true);
                emoticonsFrameLayout.setVisibility(VISIBLE);
                multimediaPopupGrid.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        populateAutoSuggestion(false, null, null);

        EventManager.getInstance().unregisterUIListener(TAG);

        if (isRecording) {
            onLessThanSecond();
            if (recordButton != null) {
                recordButton.stopScale();
            }
            if (recordView != null) {
                recordView.hideViews(true);
            }
            onAnimationEnd();
        }

        BroadcastService.currentUserId = null;
        BroadcastService.currentConversationId = null;
        if (typingStarted) {
            if (contact != null || (channel != null && !Channel.GroupType.OPEN.getValue().equals(channel.getType()))) {
                KommunicateSettings.publishTypingStatus(getContext(), channel, contact, false);
            }
            typingStarted = false;
        }
        KommunicateSettings.unSubscribeToTyping(getContext(), channel, contact);
        if (recyclerDetailConversationAdapter != null) {
            recyclerDetailConversationAdapter.contactImageLoader.setPauseWork(false);
        }

        if (typingTimerMap != null) {
            typingTimerMap.clear();
        }
    }

    public void updateTitle(Contact contact, Channel channel) {
        if (customToolbarLayout != null) {
            customToolbarLayout.setVisibility(View.GONE);
        }

        StringBuilder titleBuilder = new StringBuilder();
        if (channel != null) {
            if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                String userId = ChannelService.getInstance(getActivity()).getGroupOfTwoReceiverUserId(channel.getKey());
                if (!TextUtils.isEmpty(userId)) {
                    Contact withUserContact = appContactService.getContactById(userId);
                    titleBuilder.append(withUserContact.getDisplayName());
                }
            } else if (Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType())) {
                if (customToolbarLayout != null) {
                    customToolbarLayout.setVisibility(VISIBLE);
                }
            } else {
                titleBuilder.append(ChannelUtils.getChannelTitleName(channel, loggedInUserId));
            }
        } else if (contact != null) {
            titleBuilder.append(contact.getDisplayName());
        }
        if (getActivity() != null) {
            setToolbarTitle(titleBuilder.toString());
        }
    }

    public void deleteConversationThread() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity()).
                setPositiveButton(R.string.delete_conversation, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new DeleteConversationAsyncTask(new MobiComConversationService(getActivity()), contact, channel, currentConversationId, getActivity()).execute();
                    }
                });
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        alertDialog.setTitle(AppContextService.getContext(getContext()).getString(R.string.dialog_delete_conversation_title).replace(NAME, getNameForInviteDialog()));
        alertDialog.setMessage(AppContextService.getContext(getContext()).getString(R.string.dialog_delete_conversation_confir).replace(NAME, getNameForInviteDialog()));
        alertDialog.setCancelable(true);
        alertDialog.create().show();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventManager.getInstance().registerUIListener(TAG, this);

        if (MobiComUserPreference.getInstance(getActivity()).isChannelDeleted()) {
            MobiComUserPreference.getInstance(getActivity()).setDeleteChannel(false);
            if (getActivity().getSupportFragmentManager() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
            return;
        }

        ((ConversationActivity) getActivity()).setChildFragmentLayoutBGToTransparent();
        if (contact != null || channel != null) {
            BroadcastService.currentUserId = contact != null ? contact.getContactIds() : String.valueOf(channel.getKey());
            BroadcastService.currentConversationId = currentConversationId;
            if (BroadcastService.currentUserId != null) {
                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getActivity());
                if (SettingsSharedPreference.getInstance(getActivity()).isNotificationStacking()) {
                    notificationManagerCompat.cancel(NotificationService.NOTIFICATION_ID);
                } else {
                    if (contact != null) {
                        if (!TextUtils.isEmpty(contact.getContactIds())) {
                            notificationManagerCompat.cancel(contact.getContactIds().hashCode());
                        }
                    }
                    if (channel != null) {
                        notificationManagerCompat.cancel(String.valueOf(channel.getKey()).hashCode());
                    }
                }
            }

            if (downloadConversation != null) {
                downloadConversation.cancel(true);
            }

            if (channel != null) {
                if (channel.getType() != null && (!Channel.GroupType.OPEN.getValue().equals(channel.getType()) && !Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType()))) {
                    boolean present = true;
                    if (channel.getGroupUsers() != null) {
                        present = channel.isUserPresentInChannel(MobiComUserPreference.getInstance(getContext()).getUserId());
                    } else {
                        present = ChannelService.getInstance(getActivity()).processIsUserPresentInChannel(channel.getKey());
                    }

                    hideSendMessageLayout(channel.isDeleted() || !present);
                } else {
                    hideSendMessageLayout(channel.isDeleted());
                }
                if (ChannelService.isUpdateTitle && !Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType())) {
                    updateChannelSubTitle(channel);
                    ChannelService.isUpdateTitle = false;
                }

                if (channel != null && channel.getKmStatus() != Channel.IN_QUEUE_CONVERSATION) {
                    loadAwayMessage();
                }
                processSupportGroupDetails(channel);
                processBusinessHourDetails(channel);
                KommunicateSettings.subscribeToTyping(getContext(), channel, contact);
            }

            if (appContactService != null && contact != null) {
                updateLastSeenStatus();
            }

            if (SyncCallService.refreshView) {
                messageList.clear();
                SyncCallService.refreshView = false;
            }

            if (channel != null && !channel.isUserPresentInChannel(MobiComUserPreference
                    .getInstance(getContext()).getUserId())) {
                Channel newChannel = ChannelService.getInstance(getActivity()).getChannelByChannelKey(channel.getKey());
                if (newChannel != null && newChannel.getType() != null && Channel.GroupType.OPEN.getValue().equals(newChannel.getType())) {
                    MobiComUserPreference.getInstance(getActivity()).setNewMessageFlag(true);
                }
            }

            if (channel != null && Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType()) && customToolbarLayout != null) {
                customToolbarLayout.setVisibility(VISIBLE);
            }

            if (messageList.isEmpty()) {
                loadConversation(contact, channel, currentConversationId, messageSearchString);
            } else if (MobiComUserPreference.getInstance(getActivity()).getNewMessageFlag()) {
                loadnewMessageOnResume(contact, channel, currentConversationId);
            }

            MobiComUserPreference.getInstance(getActivity()).setNewMessageFlag(false);
        }
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                downloadConversation = new DownloadConversation(recyclerView, false, 1, 1, 1, contact, channel, currentConversationId, messageSearchString);
                downloadConversation.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        if (isEmailConversation(channel)) {
            emailReplyReminderLayout.setVisibility(VISIBLE);
        }

        if (kmTypingView != null) {
            kmTypingView.setVisibility(GONE);
        }
    }

    private void processBusinessHourDetails(Channel channel) {
        String subscription = KmAppSettingPreferences.getCurrentSubscriptionDetails();
        Set<String> avlblPlans = new HashSet<>(Arrays.asList(
                "trial",
                "business_monthly_v7",
                "business_yearly_v7",
                "business_monthly_v7_inr",
                "business_yearly_v7_inr",
                "business_monthly_v8",
                "business_yearly_v8",
                "business_monthly_v8_inr",
                "business_yearly_v8_inr"
        ));
        if (subscription == null
                || !(subscription.contains("business_") || subscription.contains("trial"))
                || !avlblPlans.contains(subscription)
        ) {
            return;
        }

        BusinessHoursDetailUseCase.executeWithExecutor(
                requireContext(),
                channel.getTeamId(),
                new TaskListener<BusinessSettingsResponse>() {
                    @Override
                    public void onSuccess(BusinessSettingsResponse businessSettingsResponse) {
                        if (!BusinessHoursUtil.isWithinBusinessHours(
                                businessSettingsResponse.getBusinessHourMap(),
                                businessSettingsResponse.getTimezone()
                        )) {
                            businessConversationLL.setVisibility(VISIBLE);
                            businessSettingsTextView.setText(businessSettingsResponse.getMessage());
                        } else  {
                            businessConversationLL.setVisibility(GONE);
                        }
                   }

                    @Override
                    public void onFailure(@NonNull Exception error) {
                        businessConversationLL.setVisibility(GONE);
                        Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void showTakeOverFromBotLayout(boolean show, final Contact assigneeBot) {
        if (takeOverFromBotLayout != null) {
            if (show) {
                if (assigneeBot == null) {
                    return;
                }
                takeOverFromBotLayout.setVisibility(VISIBLE);
                TextView takeOverFromBotButton = takeOverFromBotLayout.findViewById(R.id.kmTakeOverFromBotButton);
                TextView takeOverFromBotName = takeOverFromBotLayout.findViewById(R.id.kmAssignedBotNameTv);

                if (takeOverFromBotName != null) {
                    takeOverFromBotName.setText(assigneeBot.getDisplayName());
                }
                takeOverFromBotButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new KmCustomDialog().showDialog(getActivity(), assigneeBot.getDisplayName(), new KmCustomDialog.KmDialogClickListener() {
                            @Override
                            public void onClickNegativeButton(Dialog dialog) {
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                            }

                            @Override
                            public void onClickPositiveButton(Dialog dialog) {
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                                if (channel.isUserPresentInChannel(assigneeBot.getUserId())) {
                                    processTakeOverFromBot(getContext(), channel);
                                } else {
                                    takeOverFromBotLayout.setVisibility(View.GONE);
                                }
                            }
                        });
                    }
                });
            } else {
                takeOverFromBotLayout.setVisibility(View.GONE);
            }
        }
    }

    protected void hideSendMessageLayout(boolean hide) {
        if (hide) {
            individualMessageSendLayout.setVisibility(View.GONE);
            userNotAbleToChatLayout.setVisibility(VISIBLE);
            handleSendAndRecordButtonView(true);
        } else {
            userNotAbleToChatLayout.setVisibility(View.GONE);
        }

    }

    public void updateSupportGroupTitleAndImageAndHideSubtitle(Channel channel) {
        if (getActivity() == null) {
            return;
        }
        toolbarAlphabeticImage.setVisibility(VISIBLE);
        toolbarImageView.setVisibility(GONE);
        if (customizationSettings.isAgentApp() && channel != null && !TextUtils.isEmpty(channel.getImageUrl())) {
            KmViewHelper.loadImage(getContext(), toolbarImageView, toolbarAlphabeticImage, channel.getImageUrl(), R.drawable.km_ic_contact_picture_holo_light);
        } else if (!customizationSettings.isAgentApp() && channel != null && channel.getConversationAssignee() != null) {
            Contact withUserContact = appContactService.getContactById(channel.getConversationAssignee());

            KmViewHelper.loadContactImage(getContext(), toolbarImageView, toolbarAlphabeticImage, withUserContact, R.drawable.km_ic_contact_picture_holo_light);
        } else {
            toolbarAlphabeticImage.setVisibility(VISIBLE);
            toolbarImageView.setVisibility(View.GONE);

            String contactNumber = "";
            char firstLetter = 0;

            if (channel.getName() == null) {
                return;
            }
            contactNumber = channel.getName().toUpperCase();
            firstLetter = channel.getName().toUpperCase().charAt(0);

            if (firstLetter != '+') {
                toolbarAlphabeticImage.setText(String.valueOf(firstLetter));
            } else if (contactNumber.length() >= 2) {
                toolbarAlphabeticImage.setText(String.valueOf(contactNumber.charAt(1)));
            }

            Character colorKey = AlphaNumberColorUtil.alphabetBackgroundColorMap.containsKey(firstLetter) ? firstLetter : null;
            GradientDrawable bgShape = (GradientDrawable) toolbarAlphabeticImage.getBackground();
            if (getContext() != null) {
                bgShape.setColor(getContext().getResources().getColor(AlphaNumberColorUtil.alphabetBackgroundColorMap.get(colorKey)));
            }
        }

        if (channel.getKmStatus() == Channel.IN_QUEUE_CONVERSATION) {
            toolbarTitleText.setText(R.string.in_queue);
            toolbarAwayColorDot.setVisibility(GONE);
            toolbarSubtitleText.setVisibility(GONE);
            updateWaitingStatus();
        } else if (!TextUtils.isEmpty(channel.getName())) {
            toolbarTitleText.setText(channel.getName());
            toolbarAwayColorDot.setVisibility(VISIBLE);
            toolbarSubtitleText.setVisibility(VISIBLE);
            kmInQueueView.setVisibility(GONE);
        }

        if (customizationSettings.isAgentApp()) {
            setStatusDots(false, true); //setting the status dot as offline
            if (toolbarSubtitleText != null) {
                toolbarSubtitleText.setVisibility(View.GONE);
            }
        }
    }

    private void updateWaitingStatus() {
        if (channel != null && channel.getKmStatus() == Channel.IN_QUEUE_CONVERSATION) {
            WaitingQueueStatusUseCase.executeWithExecutor(
                    requireContext(),
                    Long.parseLong(channel.getTeamId()),
                    new TaskListener<List<Long>>() {
                        @Override
                        public void onSuccess(List<Long> status) {
                            kmInQueueView.setVisibility(VISIBLE);
                            int position = status.indexOf(Long.parseLong(channel.getClientGroupId())) + 1;
                            kmInQueueView.setupInQueueMessage(position);
                        }

                        @Override
                        public void onFailure(@NonNull Exception error) {
                            Toast.makeText(requireContext(), "Unable to fetch waiting status", Toast.LENGTH_SHORT).show();
                            Sentry.captureException(error);
                            kmInQueueView.setVisibility(GONE);
                        }
                    });
        }
    }

    public void retrieveAgentStatusAndSwitchContactStatusUI(final Contact contact) {
        new AgentGetStatusTask(getContext(), contact.getUserId(), new AgentGetStatusTask.KmAgentGetStatusHandler() {
            @Override
            public void onFinished(boolean agentStatus) {
                switchContactStatus(contact, agentStatus);
            }

            @Override
            public void onError(String error) {
                Utils.printLog(getContext(), TAG, "Couldn't get agent status.");
                switchContactStatus(contact, null);
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void processSupportGroupDetails(final Channel channel) {
        KmService.getSupportGroupContact(getContext(), channel, appContactService, loggedInUserRole);

        if (loggedInUserRole == User.RoleType.AGENT.getValue()) {
            Contact assigneeContact = KmService.getAssigneeContact(channel, appContactService);
            showTakeOverFromBotLayout(assigneeContact != null && User.RoleType.BOT.getValue().equals(assigneeContact.getRoleType()) && !BOT.equals(assigneeContact.getUserId()), assigneeContact);
        }
        updateSupportGroupTitleAndImageAndHideSubtitle(channel);

        UserDetailUseCase.executeWithExecutor(
                requireContext(),
                channel.getConversationAssignee(),
                contactCallback -> {
                    conversationAssignee = contactCallback;
                    updateSupportGroupTitleAndImageAndHideSubtitle(channel);
                    switchContactStatus(contactCallback, contactCallback.isUserOnline());
                });
    }

    //connected is for online/offline, agentStatus is online/away
    protected void setStatusDots(boolean connected, boolean agentStatus) {
        boolean onlineDotVisibility = connected && agentStatus;
        boolean offlineDotVisibility = !connected;
        boolean awayDotVisibility = connected && !agentStatus;

        if (toolbarOnlineColorDot != null && toolbarOfflineColorDot != null && toolbarAwayColorDot != null) {
            if (isHideAssigneeStatus) {
                toolbarAwayColorDot.setVisibility(GONE);
                toolbarOfflineColorDot.setVisibility(GONE);
                toolbarOnlineColorDot.setVisibility(GONE);
                toolbarSubtitleText.setVisibility(GONE);
            } else {
                toolbarAwayColorDot.setVisibility(awayDotVisibility ? VISIBLE : View.GONE);
                toolbarOfflineColorDot.setVisibility(offlineDotVisibility ? VISIBLE : View.GONE);
                toolbarOnlineColorDot.setVisibility(onlineDotVisibility ? VISIBLE : View.GONE);
            }
        }
    }

    public void updateAssigneeStatus(String assigneeId, KMAgentStatusHelper.KMAgentStatus status) {
        Contact assigneeContact = KmService.getAssigneeContact(channel, appContactService);
        if (!TextUtils.isEmpty(assigneeId) && !Objects.equals(assigneeContact.getUserId(), assigneeId))
            return;

        if (fontManager != null && fontManager.getToolbarSubtitleFont() != null) {
            toolbarSubtitleText.setTypeface(fontManager.getToolbarSubtitleFont());
        } else {
            toolbarSubtitleText.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        }

        if (status == KMAgentStatusHelper.KMAgentStatus.ONLINE) {
            toolbarSubtitleText.setText(AppContextService.getContext(getContext()).getString(R.string.online));
            setStatusDots(true, true);
            showAwayMessage(false, null);
        } else if (status == KMAgentStatusHelper.KMAgentStatus.OFFLINE) {
            toolbarSubtitleText.setText(R.string.offline);
            setStatusDots(false, true);
            showAwayMessage(false, null);
        } else if (status == KMAgentStatusHelper.KMAgentStatus.AWAY) {
            toolbarSubtitleText.setText(R.string.away);
            setStatusDots(true, false);
            showAwayMessage(true, null);
        }
        toolbarSubtitleText.setVisibility(isCustomToolbarSubtitleDesign ? View.GONE : View.VISIBLE);
    }

    public void switchContactStatus(Contact contact, Boolean agentStatus) {
        if (contact == null) {
            return;
        }

        if (channel != null && channel.getKmStatus() == Channel.IN_QUEUE_CONVERSATION) {
            toolbarAwayColorDot.setVisibility(GONE);
            toolbarOfflineColorDot.setVisibility(GONE);
            toolbarOnlineColorDot.setVisibility(GONE);
            toolbarSubtitleText.setVisibility(GONE);
            return;
        }

        if (KMAgentStatusHelper.status != KMAgentStatusHelper.KMAgentStatus.DefaultStatus && (TextUtils.isEmpty(KMAgentStatusHelper.assigneeID) || Objects.equals(contact.getUserId(), KMAgentStatusHelper.assigneeID))) {
            updateAssigneeStatus(KMAgentStatusHelper.assigneeID, KMAgentStatusHelper.status);
            return;
        }

        if (User.RoleType.BOT.getValue().equals(contact.getRoleType()) || !contact.isConnected()) {
            kmAwayView.setVisibility(GONE);
        }

        if (agentStatus == null) {
            agentStatus = this.agentStatus != null ? this.agentStatus : true; //default to true
        } else {
            this.agentStatus = agentStatus;
        }

        if (toolbarSubtitleText != null) {
            if (fontManager != null && fontManager.getToolbarSubtitleFont() != null) {
                toolbarSubtitleText.setTypeface(fontManager.getToolbarSubtitleFont());
            } else {
                toolbarSubtitleText.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            }
            if (User.RoleType.BOT.getValue().equals(contact.getRoleType())) {
                if (isHideAssigneeStatus) {
                    toolbarSubtitleText.setVisibility(GONE);
                    toolbarOnlineColorDot.setVisibility(GONE);
                    toolbarOfflineColorDot.setVisibility(GONE);
                } else {
                    toolbarSubtitleText.setText(AppContextService.getContext(getContext()).getString(R.string.online));
                    toolbarSubtitleText.setVisibility(isCustomToolbarSubtitleDesign ? View.GONE : View.VISIBLE);
                    setStatusDots(true, true);
                }
                return;
            }
            if (contact.isConnected()) {
                if (agentStatus) {
                    toolbarSubtitleText.setText(R.string.online);
                } else {
                    toolbarSubtitleText.setText(R.string.away);
                }
                toolbarSubtitleText.setVisibility(isCustomToolbarSubtitleDesign ? View.GONE : View.VISIBLE);
            } else {
                if (User.RoleType.USER_ROLE.getValue().equals(contact.getRoleType())) {
                    if (contact.getLastSeenAt() > 0) {
                        if (getActivity() != null) {
                            toolbarSubtitleText.setVisibility(isCustomToolbarSubtitleDesign ? View.GONE : View.VISIBLE);
                            toolbarSubtitleText.setText(AppContextService.getContext(getContext()).getString(R.string.subtitle_last_seen_at_time) + " " + DateUtils.getDateAndTimeForLastSeen(AppContextService.getContext(getContext()), contact.getLastSeenAt(), R.string.JUST_NOW, R.plurals.MINUTES_AGO, R.plurals.HOURS_AGO, R.string.YESTERDAY));
                        }
                    } else {
                        toolbarSubtitleText.setVisibility(View.GONE);
                    }
                } else {
                    toolbarSubtitleText.setVisibility(isCustomToolbarSubtitleDesign ? View.GONE : View.VISIBLE);
                    toolbarSubtitleText.setText(R.string.offline);
                }
            }
        }

        setStatusDots(contact.isConnected(), agentStatus);
    }

    public void updateChannelTitleAndSubTitle() {
        if (channel != null) {
            Channel channelInfo = ChannelService.getInstance(getActivity()).getChannelInfo(channel.getKey());
            boolean isUserPresent = true;
            if (channel.getGroupUsers() != null) {
                isUserPresent = channel.isUserPresentInChannel(MobiComUserPreference.getInstance(getContext()).getUserId());
            } else {
                isUserPresent = ChannelService.getInstance(getContext()).isUserAlreadyPresentInChannel(channel.getKey(), MobiComUserPreference.getInstance(getContext()).getUserId());
            }
            if (channelInfo.isDeleted()) {
                channel.setDeletedAtTime(channelInfo.getDeletedAtTime());
                individualMessageSendLayout.setVisibility(View.GONE);
                userNotAbleToChatLayout.setVisibility(VISIBLE);
                handleSendAndRecordButtonView(true);
                userNotAbleToChatTextView.setText(AppContextService.getContext(getContext()).getString(R.string.group_has_been_deleted_text));
                if (channel != null && !isUserPresent
                        && messageTemplate != null && messageTemplate.isEnabled() && templateAdapter != null) {
                    templateAdapter.setMessageList(new HashMap<String, String>());
                    templateAdapter.notifyDataSetChanged();
                }
                if (getActivity() != null) {
                    getActivity().invalidateOptionsMenu();
                }
            } else {

                if ((!isUserPresent
                        && userNotAbleToChatLayout != null
                        && (!Channel.GroupType.OPEN.getValue().equals(channel.getType())) && !Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType()))) {
                    individualMessageSendLayout.setVisibility(View.GONE);
                    userNotAbleToChatLayout.setVisibility(VISIBLE);
                    handleSendAndRecordButtonView(true);
                    if (channel != null && !isUserPresent
                            && messageTemplate != null && messageTemplate.isEnabled() && templateAdapter != null) {
                        templateAdapter.setMessageList(new HashMap<String, String>());
                        templateAdapter.notifyDataSetChanged();
                    }
                }
            }

            if (Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType())) {
                processSupportGroupDetails(channel);
            } else {
                updateChannelTitle(channelInfo);
                updateChannelSubTitle(channelInfo);
            }
        }
    }

    public void updateChannelTitle(Channel newChannel) {
        if (customToolbarLayout != null) {
            customToolbarLayout.setVisibility(Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType()) ? VISIBLE : View.GONE);
        }

        if (Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType())) {
            processSupportGroupDetails(channel);
        } else if (!Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
            if (newChannel != null && !TextUtils.isEmpty(channel.getName()) && !channel.getName().equals(newChannel.getName())) {
                title = ChannelUtils.getChannelTitleName(newChannel, loggedInUserId);
                channel = newChannel;
                setToolbarTitle(title);
            }
        }
    }

    public void updateTitleForOpenGroup() {
        try {
            if (channel != null) {
                Channel newChannel = ChannelService.getInstance(getActivity()).getChannelByChannelKey(channel.getKey());
                setToolbarTitle(newChannel.getName());
                updateChannelSubTitle(newChannel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void selfDestructMessage(Message message) {
        if (Message.MessageType.MT_INBOX.getValue().equals(message.getType()) &&
                message.getTimeToLive() != null && message.getTimeToLive() != 0) {
            new Timer().schedule(new DisappearingMessageTask(getActivity(), conversationService, message), message.getTimeToLive() * 60 * 1000);
        }
    }

    public void loadnewMessageOnResume(Contact contact, Channel channel, Integer conversationId) {
        downloadConversation = new DownloadConversation(recyclerView, true, 1, 0, 0, contact, channel, conversationId, messageSearchString);
        downloadConversation.execute();
    }

    public int scrollToFirstSearchIndex() {

        int position = 0;
        if (searchString != null) {

            for (position = messageList.size() - 1; position >= 0; position--) {
                Message message = messageList.get(position);
                if (!TextUtils.isEmpty(message.getMessage()) && message.getMessage().toLowerCase(Locale.getDefault()).indexOf(
                        searchString.toString().toLowerCase(Locale.getDefault())) != -1) {
                    return position;
                }
            }
        } else {
            position = messageList.size();
        }
        return position;
    }

    public void blockUserProcess(final String userId, final boolean block, final boolean isFromChannel) {

        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "",
                AppContextService.getContext(getContext()).getString(R.string.please_wait_info), true);

        UserBlockUseCase.TaskListener listener = new UserBlockUseCase.TaskListener() {

            @Override
            public void onSuccess(ApiResponse apiResponse) {
                if (block && typingStarted) {
                    if (getActivity() != null) {
                        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("");
                    }
                    Intent intent = new Intent(getActivity(), MqttIntentService.class);
                    intent.putExtra(MqttIntentService.CONTACT, contact);
                    intent.putExtra(MqttIntentService.STOP_TYPING, true);
                    MqttIntentService.enqueueWork(getActivity(), intent);
                }
                menu.findItem(R.id.userBlock).setVisible(!block);
                menu.findItem(R.id.userUnBlock).setVisible(block);
            }

            @Override
            public void onFailure(ApiResponse apiResponse, Exception exception) {
                String error = AppContextService.getContext(getContext()).getString(Utils.isInternetAvailable(getActivity()) ? R.string.km_server_error : R.string.you_need_network_access_for_block_or_unblock);
                Toast toast = KmToast.error(getActivity(), error, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }

            @Override
            public void onCompletion() {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                if (!isFromChannel) {
                    contact = appContactService.getContactById(userId);
                }
            }

        };

        UserBlockUseCase.executeWithExecutor(getActivity(), userId, block, listener);
    }

    public void userBlockDialog(final boolean block, final Contact withUserContact, final boolean isFromChannel) {
        if (withUserContact == null) {
            return;
        }
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity()).
                setPositiveButton(R.string.ok_alert, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        blockUserProcess(withUserContact.getUserId(), block, isFromChannel);
                    }
                });
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        String name = withUserContact.getDisplayName();
        alertDialog.setMessage(getString(block ? R.string.user_block_info : R.string.user_un_block_info).replace("[name]", name));
        alertDialog.setCancelable(true);
        alertDialog.create().show();
    }

    public void muteGroupChat() {
        final CharSequence[] items = {AppContextService.getContext(getContext()).getString(R.string.eight_Hours), AppContextService.getContext(getContext()).getString(R.string.one_week), AppContextService.getContext(getContext()).getString(R.string.one_year)};
        Date date = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime();
        millisecond = date.getTime();

        final TaskListener taskListener = new TaskListener<String>() {
            @Override
            public void onFailure(@NonNull Exception error) {
                Log.e(TAG, "Failed to mute group notifications", error);
                Toast.makeText(getContext(), "Failed to mute group notifications", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(@NonNull String status) {
                if (menu != null) {
                    menu.findItem(R.id.muteGroup).setVisible(false);
                    menu.findItem(R.id.unmuteGroup).setVisible(true);
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle(AppContextService.getContext(getContext()).getResources().getString(R.string.mute_group_for))
                .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, final int selectedItem) {
                        if (selectedItem == 0) {
                            millisecond = millisecond + 28800000;
                        } else if (selectedItem == 1) {
                            millisecond = millisecond + 604800000;

                        } else if (selectedItem == 2) {
                            millisecond = millisecond + 31558000000L;
                        }

                        muteNotificationRequest = new MuteNotificationRequest(channel.getKey(), millisecond);
                        MuteGroupNotificationUseCase.executeWithExecutor(
                                getContext(),
                                muteNotificationRequest,
                                taskListener
                        );
                        dialog.dismiss();

                    }
                });
        AlertDialog alertdialog = builder.create();
        alertdialog.show();
    }

    public void umuteGroupChat() {
        Date date = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime();
        millisecond = date.getTime();

        final TaskListener taskListener = new TaskListener<String>() {
            @Override
            public void onFailure(@NonNull Exception error) {
                Log.e(TAG, "Failed to unmute group notifications", error);
                Toast.makeText(getContext(), "Failed to group user notifications", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(@NonNull String status) {
                if (menu != null) {
                    menu.findItem(R.id.unmuteGroup).setVisible(false);
                    menu.findItem(R.id.muteGroup).setVisible(true);
                }
            }
        };

        muteNotificationRequest = new MuteNotificationRequest(channel.getKey(), millisecond);
        MuteGroupNotificationUseCase.executeWithExecutor(
                getContext(),
                muteNotificationRequest,
                taskListener
        );
    }

    public void muteUserChat() {
        final CharSequence[] items = {AppContextService.getContext(getContext()).getString(R.string.eight_Hours), AppContextService.getContext(getContext()).getString(R.string.one_week), AppContextService.getContext(getContext()).getString(R.string.one_year)};
        Date date = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime();
        millisecond = date.getTime();

        final TaskListener listener = new TaskListener<String>() {
            @Override
            public void onSuccess(String status) {
                if (menu != null) {
                    menu.findItem(R.id.muteGroup).setVisible(false);
                    menu.findItem(R.id.unmuteGroup).setVisible(true);
                }
            }

            @Override
            public void onFailure(Exception error) {
                Log.e(TAG, "Failed to mute user notifications", error);
                Toast.makeText(getContext(), "Failed to mute user notifications", Toast.LENGTH_SHORT).show();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle(AppContextService.getContext(getContext()).getResources().getString(R.string.mute_user_for))
                .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, final int selectedItem) {
                        if (selectedItem == 0) {
                            millisecond = millisecond + 28800000;
                        } else if (selectedItem == 1) {
                            millisecond = millisecond + 604800000;
                        } else if (selectedItem == 2) {
                            millisecond = millisecond + 31558000000L;
                        }

                        MuteUserNotificationUseCase.executeWithExecutor(getContext(), contact.getUserId(), millisecond, listener);
                        dialog.dismiss();

                    }
                });
        AlertDialog alertdialog = builder.create();
        alertdialog.show();
    }

    public void unMuteUserChat() {
        Date date = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime();
        millisecond = date.getTime();

        final TaskListener taskListener = new TaskListener<String>() {
            @Override
            public void onSuccess(String status) {
                if (menu != null) {
                    menu.findItem(R.id.unmuteGroup).setVisible(false);
                    menu.findItem(R.id.muteGroup).setVisible(true);
                }
            }

            @Override
            public void onFailure(Exception error) {
                Log.e(TAG, "Failed to unmute user notifications", error);
                Toast.makeText(getContext(), "Failed to unmute user notifications", Toast.LENGTH_SHORT).show();
            }
        };
        MuteUserNotificationUseCase.executeWithExecutor(
                getContext(),
                contact.getUserId(),
                millisecond,
                taskListener
        );
    }

    public void muteUser(boolean mute) {
        if (menu != null) {
            menu.findItem(R.id.unmuteGroup).setVisible(mute);
            menu.findItem(R.id.muteGroup).setVisible(!mute);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null) {
            ((ConversationActivity) getActivity()).setChildFragmentLayoutBG();
        }
        if (KommunicateAudioManager.getInstance(getContext()) != null) {
            KommunicateAudioManager.getInstance(getContext()).audiostop();
        }
    }

    public ViewGroup.LayoutParams getImageLayoutParam(boolean outBoxType) {
        if (getActivity() != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
            if (wm != null) {
                wm.getDefaultDisplay().getMetrics(metrics);
                float wtPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getActivity().getResources().getDisplayMetrics());
                ViewGroup.MarginLayoutParams params;
                if (outBoxType) {
                    params = new RelativeLayout.LayoutParams(metrics.widthPixels + (int) wtPx * 2, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins((int) wtPx, 0, (int) wtPx, 0);
                } else {
                    params = new RelativeLayout.LayoutParams(metrics.widthPixels - (int) wtPx * 2, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 0, 0);
                }
                return params;
            }
        }
        return null;
    }

    public String getMessageType(Message lastMessage) {
        String type = null;

        if (lastMessage == null) {
            return null;
        }

        if (lastMessage.getContentType() == Message.ContentType.LOCATION.getValue()) {
            type = LOCATION;
        } else if (lastMessage.getContentType() == Message.ContentType.AUDIO_MSG.getValue()) {
            type = AUDIO;
        } else if (lastMessage.getContentType() == Message.ContentType.VIDEO_MSG.getValue()) {
            type = VIDEO;
        } else if (lastMessage.getContentType() == Message.ContentType.ATTACHMENT.getValue()) {
            if (lastMessage.getFilePaths() != null) {
                String filePath = lastMessage.getFilePaths().get(lastMessage.getFilePaths().size() - 1);
                String mimeType = FileUtils.getMimeType(filePath);

                if (mimeType != null) {
                    if (mimeType.startsWith(IMAGE)) {
                        type = IMAGE;
                    } else if (mimeType.startsWith(AUDIO)) {
                        type = AUDIO;
                    } else if (mimeType.startsWith(VIDEO)) {
                        type = VIDEO;
                    }
                }
            } else if (lastMessage.getFileMetas() != null) {
                if (lastMessage.getFileMetas().getContentType().contains(IMAGE)) {
                    type = IMAGE;
                } else if (lastMessage.getFileMetas().getContentType().contains(AUDIO)) {
                    type = AUDIO;
                } else if (lastMessage.getFileMetas().getContentType().contains(VIDEO)) {
                    type = VIDEO;
                }
            }
        } else if (lastMessage.getContentType() == Message.ContentType.CONTACT_MSG.getValue()) {
            type = CONTACT;
        } else {
            type = "text";
        }
        return type;
    }

    public class DownloadConversation extends AsyncTask<Void, Integer, Long> implements KmBotTypingDelayManager.MessageDispatcher {

        private RecyclerView recyclerView;
        private int firstVisibleItem;
        private boolean initial;
        private Contact contact;
        private Channel channel;
        private Integer conversationId;
        private List<Conversation> conversationList;
        private String messageSearchString;
        private List<Message> nextMessageList = new ArrayList<Message>();
        private boolean isNewConversation;

        public DownloadConversation(RecyclerView recyclerView, boolean initial, int firstVisibleItem, int amountVisible, int totalItems, Contact contact, Channel channel, Integer conversationId, String messageSearchString) {
            this.recyclerView = recyclerView;
            this.initial = initial;
            this.firstVisibleItem = firstVisibleItem;
            this.contact = contact;
            this.channel = channel;
            this.conversationId = conversationId;
            this.messageSearchString = messageSearchString;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            emptyTextView.setVisibility(View.GONE);

            onStartLoading(true);
            if (swipeLayout != null) {
                swipeLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeLayout.setRefreshing(true);
                    }
                });
            }

            if (initial) {
                if (recordButtonWeakReference != null) {
                    KmRecordButton recordButton = recordButtonWeakReference.get();
                    if (recordButton != null) {
                        recordButton.setEnabled(false);
                    }
                }
                sendButton.setEnabled(false);
                messageEditText.setEnabled(false);
            }

            if (!initial && messageList.isEmpty()) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity()).
                        setPositiveButton(R.string.ok_alert, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        loadMore = false;
                    }
                });
                //Todo: Move this to mobitexter app
                alertDialog.setTitle(AppContextService.getContext(getContext()).getString(R.string.sync_older_messages));
                alertDialog.setCancelable(true);
                alertDialog.create().show();
            }
        }

        @Override
        protected Long doInBackground(Void... voids) {
            try {
                if (initial) {
                    Long lastConversationloadTime = 1L;
                    if (!messageList.isEmpty()) {
                        for (int i = messageList.size() - 1; i >= 0; i--) {
                            if (messageList.get(i).isTempDateType()) {
                                continue;
                            }
                            lastConversationloadTime = messageList.get(i).getCreatedAtTime();
                            break;
                        }
                    }


                    nextMessageList = conversationService.getMessages(this::ignoreSummaryMessageForUser, lastConversationloadTime + 1L, null, contact, channel, conversationId, false, !TextUtils.isEmpty(messageSearchString));
                    isNewConversation = isNewConversation(nextMessageList);
                } else if (firstVisibleItem == 1 && loadMore && !messageList.isEmpty()) {
                    loadMore = false;
                    Long endTime = null;
                    for (Message message : messageList) {
                        if (message.isTempDateType()) {
                            continue;
                        }
                        endTime = messageList.get(!TextUtils.isEmpty(customizationSettings.getStaticTopMessage()) ? 1 : 0).getCreatedAtTime();
                        break;
                    }
                    nextMessageList = conversationService.getMessages(this::ignoreSummaryMessageForUser, null, endTime, contact, channel, conversationId, false, !TextUtils.isEmpty(messageSearchString));
                    isNewConversation = isNewConversation(nextMessageList);
                }
                if (BroadcastService.isContextBasedChatEnabled()) {
                    conversations = ConversationService.getInstance(getActivity()).getConversationList(channel, contact);
                }

                List<Message> createAtMessage = new ArrayList<Message>();
                if (nextMessageList != null && !nextMessageList.isEmpty()) {
                    Message firstDateMessage = new Message();
                    firstDateMessage.setTempDateType(Short.valueOf("100"));
                    firstDateMessage.setCreatedAtTime(nextMessageList.get(0).getCreatedAtTime());

                    Message firstMessage = new Message();
                    firstMessage.setInitialFirstMessage();
                    if (initial && !TextUtils.isEmpty(customizationSettings.getStaticTopMessage()) && !messageList.contains(firstMessage)) {
                        createAtMessage.add(firstMessage);
                    } else if (!initial && !TextUtils.isEmpty(customizationSettings.getStaticTopMessage())) {
                        createAtMessage.add(firstMessage);
                        messageList.remove(firstMessage);
                    }
                    if (initial && !messageList.contains(firstDateMessage)) {
                        createAtMessage.add(firstDateMessage);
                    } else if (!initial) {
                        createAtMessage.add(firstDateMessage);
                        messageList.remove(firstDateMessage);
                    }
                    if (!createAtMessage.contains(nextMessageList.get(0))) {
                        createAtMessage.add(nextMessageList.get(0));
                    }

                    for (int i = 1; i <= nextMessageList.size() - 1; i++) {
                        long dayDifference = DateUtils.daysBetween(new Date(nextMessageList.get(i - 1).getCreatedAtTime()), new Date(nextMessageList.get(i).getCreatedAtTime()));

                        if (dayDifference >= 1) {
                            Message message = new Message();
                            message.setTempDateType(Short.valueOf("100"));
                            message.setCreatedAtTime(nextMessageList.get(i).getCreatedAtTime());
                            if (initial && !messageList.contains(message)) {
                                createAtMessage.add(message);
                            } else if (!initial) {
                                createAtMessage.add(message);
                                messageList.remove(message);
                            }
                        }
                        if (!createAtMessage.contains(nextMessageList.get(i))) {
                            createAtMessage.add(nextMessageList.get(i));
                        }
                    }
                }
                nextMessageList = createAtMessage;
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return 0L;
        }

        private Boolean ignoreSummaryMessageForUser(Message message) {
            // Check if message is conversation summary.
            boolean isKmSummary = message.getMetadata().containsKey(KM_SUMMARY)
                    && Boolean.parseBoolean(message.getMetaDataValueForKey(KM_SUMMARY));

            // Ignore message if app is not agent and message type is not summary message.
            return !(customizationSettings.isAgentApp() && isKmSummary);
        }

        private boolean isNewConversation(List<Message> nextMessageList) {
            if (nextMessageList.size() == 0) {
                return false;
            }
            long createdTimeInMilliSec = nextMessageList.get(0).getCreatedAtTime();
            long currentTimeInMilliSec = System.currentTimeMillis();
            long diff = currentTimeInMilliSec - createdTimeInMilliSec;
            return diff < 5000;
        }

        @Override
        protected void onPostExecute(Long result) {
            super.onPostExecute(result);

            if (nextMessageList.isEmpty()) {
                linearLayoutManager.setStackFromEnd(false);
            }

            if (!messageList.isEmpty() && !nextMessageList.isEmpty() &&
                    messageList.get(0).equals(nextMessageList.get(nextMessageList.size() - 1))) {
                nextMessageList.remove(nextMessageList.size() - 1);
            }

            if (!messageList.isEmpty() && !nextMessageList.isEmpty() &&
                    messageList.get(0).getCreatedAtTime().equals(nextMessageList.get(nextMessageList.size() - 1).getCreatedAtTime())) {
                nextMessageList.remove(nextMessageList.size() - 1);
            }

            Contact assignee = new ContactDatabase(getContext()).getContactById(channel.getConversationAssignee());

            if (isNewConversation && assignee != null && assignee.getRoleType().equals(User.RoleType.BOT.getValue()) && botMessageDelayInterval > 0) {
                KmBotTypingDelayManager kmBotTypingDelayManager = new KmBotTypingDelayManager(getContext(), this);
                handleTypingMessage(false);
                for (Message message : nextMessageList) {
                    if (initial && !messageList.contains(message)) {
                        if (TextUtils.isEmpty(message.getKeyString())) {
                            messageList.add(0, message);
                            selfDestructMessage(message);
                            setupConversationScreen();
                            handleTypingMessage(true);
                        } else if (botDelayMessageList == null || !botDelayMessageList.contains(message)) {
                            if (botDelayMessageList == null) {
                                botDelayMessageList = new HashSet<>();
                            }
                            botDelayMessageList.add(message);
                            kmBotTypingDelayManager.addMessage(message);
                        }
                    }
                }
            } else {
                for (Message message : nextMessageList) {
                    if (initial && !messageList.contains(message)) {
                        messageList.add(message);
                    }
                    selfDestructMessage(message);
                }
                setupConversationScreen();
            }
        }

        private void setupConversationScreen() {
            if (initial) {
                recyclerDetailConversationAdapter.searchString = searchString;
                emptyTextView.setVisibility(messageList.isEmpty() && (botDelayMessageList == null || botDelayMessageList.isEmpty()) ? VISIBLE : View.GONE);
                if (messageList.isEmpty()) {
                    linearLayoutManager.setStackFromEnd(false);

                } else if (!messageList.isEmpty()) {
                    if (!TextUtils.isEmpty(searchString)) {
                        int height = recyclerView.getHeight();
                        int itemHeight = recyclerView.getChildAt(0).getHeight();
                        recyclerView.requestFocusFromTouch();
                        recyclerView.scrollTo(scrollToFirstSearchIndex() + 1, height / 2 - itemHeight / 2);
                    } else {
                        if (!recyclerView.canScrollVertically(1) || !recyclerView.canScrollVertically(-1)) {
                            linearLayoutManager.setStackFromEnd(false);
                        }
                        linearLayoutManager.scrollToPositionWithOffset(messageList.size() - 1, 0);
                    }
                }
            } else if (!nextMessageList.isEmpty()) {
                messageList.addAll(0, nextMessageList);
                linearLayoutManager.scrollToPositionWithOffset(nextMessageList.size() - 1, 50);
            }

            conversationService.read(contact, channel);
            Message lastSentMessage = null;

            if (!messageList.isEmpty()) {
                if (customizationSettings.isAgentApp()) {
                    for (int i = messageList.size() - 1; i >= 0; i--) {
                        Message userLastmessage = messageList.get(i);
                        if (userLastmessage.getType().equals(Message.MessageType.MT_INBOX.getValue()) && !Objects.equals(userLastmessage.getTo(), BOT)) {
                            lastUserMessage = userLastmessage;
                            break;
                        }
                    }
                }

                List<Long> readListToBeUpdated = new ArrayList<>();

                for (int i = messageList.size() - 1; i >= 0; i--) {
                    Message message = messageList.get(i);
                    if (lastSentMessage == null && message.isTypeOutbox()) {
                        lastSentMessage = message;
                    }
                    if (!message.isRead() && !message.isTempDateType() && !message.isCustom()) {
                        if (message.getMessageId() != null) {
                            message.setRead(Boolean.TRUE);
                            readListToBeUpdated.add(message.getMessageId());
                        }
                    }
                }

                try {
                    new Thread(() -> {
                        for (Long messageId : readListToBeUpdated) {
                            messageDatabaseService.updateMessageReadFlag(messageId, true);
                        }
                    }).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            restrictWhatsappConversation(lastUserMessage);
            if (customizationSettings.isAgentApp()) {
                restrictNonTeamMemberConversation(null);
            }
            if (recyclerDetailConversationAdapter != null) {
                recyclerDetailConversationAdapter.setLastSentMessage(lastSentMessage);
            }

            if (conversations != null && conversations.size() > 0) {
                conversationList = conversations;
            }
            if (conversationList != null && conversationList.size() > 0 && !onSelected) {
                onSelected = true;
                kmContextSpinnerAdapter = new KmContextSpinnerAdapter(getActivity(), conversationList);
                if (kmContextSpinnerAdapter != null && contextSpinner != null) {
                    contextSpinner.setAdapter(kmContextSpinnerAdapter);
                    contextFrameLayout.setVisibility(VISIBLE);
                    int i = 0;
                    for (Conversation c : conversationList) {
                        i++;
                        if (c.getId().equals(conversationId)) {
                            break;
                        }
                    }
                    contextSpinner.setSelection(i - 1, false);
                    contextSpinner.setOnItemSelectedListener(adapterView);
                }
            }
            if (recyclerDetailConversationAdapter != null) {
                recyclerDetailConversationAdapter.refreshWebView();
                recyclerDetailConversationAdapter.notifyDataSetChanged();
            }
            onStartLoading(false);
            if (swipeLayout != null) {
                swipeLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeLayout.setRefreshing(false);
                    }
                });
            }

            if (messageToForward != null) {
                sendForwardMessage(messageToForward);
                messageToForward = null;
            }

            if (!messageList.isEmpty()) {
                channelKey = messageList.get(messageList.size() - 1).getGroupId();
            }
            if (initial) {
                if (recordButtonWeakReference != null) {
                    KmRecordButton recordButton = recordButtonWeakReference.get();
                    if (recordButton != null) {
                        recordButton.setEnabled(true);
                    }
                }
                if (sendButton != null) {
                    sendButton.setEnabled(true);
                }
                if (messageEditText != null) {
                    messageEditText.setEnabled(true);
                }
            }
            loadMore = !nextMessageList.isEmpty();
            checkForAutoSuggestions();
            checkForCustomInput();
        }

        @Override
        public void onMessageQueued(Message message) {
            updateTypingStatus(message.getTo(), true);
        }

        @Override
        public void onMessageDispatched(Message message) {
            handleAddMessage(message);
        }
    }

    public void checkForAutoSuggestions() {
        if (User.RoleType.USER_ROLE.getValue() == loggedInUserRole) {
            final AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) messageEditText;
            if (messageList != null && !messageList.isEmpty() && messageList.get(messageList.size() - 1).isAutoSuggestion()) {
                KmAutoSuggestion autoSuggestion = KmAutoSuggestion.parseAutoSuggestion(messageList.get(messageList.size() - 1));
                if (autoSuggestion == null) {
                    return;
                }
                if (!TextUtils.isEmpty(autoSuggestion.getPlaceholder())) {
                    messageEditText.setHint(autoSuggestion.getPlaceholder());
                }

                autoCompleteTextView.setThreshold(2);//will start working from first character

                String payloadJson = GsonUtils.getJsonFromObject(autoSuggestion.getSource(), Object.class);
                try {
                    //Data is of type String Array
                    autoCompleteTextView.setAdapter(getAdapter((String[]) GsonUtils.getObjectFromJson(payloadJson, String[].class)));
                } catch (Exception sourceParseException) {
                    try {
                        //Data is of type Source Array
                        KmAutoSuggestion.Source sourceData = (KmAutoSuggestion.Source) GsonUtils.getObjectFromJson(payloadJson, KmAutoSuggestion.Source.class);
                        //Data should be fetched from API
                        if (!TextUtils.isEmpty(sourceData.getUrl())) {
                            isApiAutoSuggest = true;
                            autoSuggestUrl = sourceData.getUrl();
                            autoSuggestHeaders = sourceData.getHeaders();
                        } else {
                            autoCompleteTextView.setAdapter(getAdapter((KmAutoSuggestion.Source[]) GsonUtils.getObjectFromJson(payloadJson, KmAutoSuggestion.Source[].class)));
                        }
                    } catch (Exception stringParseException) {
                        autoCompleteTextView.setAdapter(getAdapter((KmAutoSuggestion.Source[]) GsonUtils.getObjectFromJson(payloadJson, KmAutoSuggestion.Source[].class)));
                    }
                }
                autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Object data = adapterView.getItemAtPosition(i);
                        messageEditText.setText(data instanceof KmAutoSuggestion.Source ? ((KmAutoSuggestion.Source) data).getMessage() : (String) data);
                        messageEditText.post(new Runnable() {
                            @Override
                            public void run() {
                                messageEditText.setSelection(messageEditText.getText().length());
                            }
                        });
                    }
                });
            } else {
                autoCompleteTextView.setAdapter(null);
                messageEditText.setHint(!TextUtils.isEmpty(customizationSettings.getEditTextHintText()) ? customizationSettings.getEditTextHintText() : AppContextService.getContext(getContext()).getString(R.string.enter_message_hint));
            }
        }
    }

    private void checkForCustomInput() {
        if (User.RoleType.USER_ROLE.getValue() == loggedInUserRole) {
            if (messageList != null && !messageList.isEmpty() && messageList.get(messageList.size() - 1).isCustomInputField()) {
                customInputField = KmCustomInputModel.parseCustomInputModel(messageList.get(messageList.size() - 1));
                if (customInputField == null) {
                    return;
                }
                isCustomFieldMessage = true;
                if (!TextUtils.isEmpty(customInputField.getKM_FIELD().getPlaceholder())) {
                    messageEditText.setHint(customInputField.getKM_FIELD().getPlaceholder());
                }
            } else {
                customInputField = null;
                isCustomFieldMessage = false;
            }
        }
    }

    private void toggleAttachmentLayoutVisibility() {
        if (customizationSettings.isHideAttachmentOptionsWithBots() && attachmentIconLayout != null) {
            attachmentIconLayout.setVisibility(
                    getCurrentConversationAssignee() == User.RoleType.BOT
                            ? GONE : VISIBLE
            );
        }
    }

    private void toggleHideChatBarWithBots() {
        if (!customizationSettings.isHideChatBarWithBots() || kmMessageLinearLayout == null) {
            return;
        }
        kmMessageLinearLayout.setVisibility(
                getCurrentConversationAssignee() == User.RoleType.BOT ? GONE : VISIBLE
        );
    }

    private User.RoleType getCurrentConversationAssignee() {
        Contact assigneeContact = appContactService.getContactById(channel.getConversationAssignee());
        return User.RoleType.fromValue(assigneeContact.getRoleType());
    }

    private void updateUserFromCustomInput(String message) {
        String fieldType = customInputField.getKM_FIELD().getFieldType();
        String field = customInputField.getKM_FIELD().getField();
        if (customInputField == null || TextUtils.isEmpty(fieldType)) {
            return;
        }
        User user = new User();
        switch (fieldType) {
            case (KmCustomInputModel.EMAIL): {
                user.setEmail(message);
                break;
            }
            case (KmCustomInputModel.NAME): {
                user.setDisplayName(message);
                break;
            }
            case (KmCustomInputModel.PHONE_NUMBER): {
                user.setContactNumber(message);
                break;
            }
            default: {
                Map<String, String> inputMetadata = new HashMap<>();
                inputMetadata.put(field, message);
                user.setMetadata(inputMetadata);
            }
        }

        UserService.getInstance(getContext()).updateUser(user, fieldType.equals(KmCustomInputModel.EMAIL), new ResultCallback() {
            @Override
            public void onSuccess(Object response) {
                Utils.printLog(getContext(), TAG, "Updated User through Custom Input Field");
            }

            @Override
            public void onError(Object error) {
                Utils.printLog(getContext(), TAG, "Failed to update user through Custom Input Field " + error.toString());


            }
        });
    }

    private void validateCustomInputRegex(String message) {
        if (customInputField == null) {
            return;
        }
        if (customInputField.getKM_FIELD().getValidation() != null
                && !TextUtils.isEmpty(customInputField.getKM_FIELD().getValidation().getRegex())) {
            try {
                if (!Pattern.compile(customInputField.getKM_FIELD().getValidation().getRegex()).matcher(messageEditText.getText().toString().trim()).find()) {
                    kmAwayView.showInvalidEmail();
                    if (!TextUtils.isEmpty(customInputField.getKM_FIELD().getValidation().getErrorText())) {
                        KmToast.error(getContext(), customInputField.getKM_FIELD().getValidation().getErrorText(), Toast.LENGTH_SHORT).show();
                    } else {
                        KmToast.error(getContext(), getResources().getString(R.string.invalid_regex_error), Toast.LENGTH_SHORT).show();
                    }
                    handleSendAndRecordButtonView(true);
                    return;
                }
            } catch (PatternSyntaxException e) {
                e.printStackTrace();
                boolean isRegexMatching = true;
                switch (customInputField.getKM_FIELD().getFieldType()) {
                    case (KmCustomInputModel.EMAIL): {
                        isRegexMatching = Pattern.compile(LeadCollectionActivity.EMAIL_VALIDATION_REGEX).matcher(messageEditText.getText().toString().trim()).matches();
                        break;
                    }
                    case (KmCustomInputModel.PHONE_NUMBER): {
                        isRegexMatching = Pattern.compile(LeadCollectionActivity.PHONE_NUMBER_VALIDATION_REGEX).matcher(messageEditText.getText().toString().trim()).matches();
                        break;
                    }
                }
                if (!isRegexMatching) {
                    if (!TextUtils.isEmpty(customInputField.getKM_FIELD().getValidation().getErrorText())) {
                        KmToast.error(getContext(), customInputField.getKM_FIELD().getValidation().getErrorText(), Toast.LENGTH_SHORT).show();
                    } else {
                        KmToast.error(getContext(), getResources().getString(R.string.invalid_regex_error), Toast.LENGTH_SHORT).show();
                    }
                    handleSendAndRecordButtonView(true);
                    return;
                }
            }
        }
        if (customInputField.getKM_FIELD().getAction().containsKey(UpdateUserDetails) && customInputField.getKM_FIELD().getAction().get(UpdateUserDetails).equals(true)) {
            updateUserFromCustomInput(message);
        }
        messageEditText.setText("");
        sendMessage(message, customInputField.getReplyMetadata() != null ? customInputField.getReplyMetadata() : null, null, null, Message.ContentType.DEFAULT.getValue());
    }

    protected <T> KmAutoSuggestionArrayAdapter<T> getAdapter(T[] data) {
        return new KmAutoSuggestionArrayAdapter<>(getContext(), R.layout.km_auto_suggestion_row_layout, data, customizationSettings);
    }

    @Override
    public boolean onItemClick(int position, MenuItem item) {
        if (messageList.size() <= position || position == -1) {
            return true;
        }
        Message message = messageList.get(position);
        if (message.isTempDateType() || message.isCustom()) {
            return true;
        }

        switch (item.getItemId()) {
            case 0:
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) AppContextService.getContext(getContext()).getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText(AppContextService.getContext(getContext()).getString(R.string.copied_message), message.getMessage());
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                }
                break;
            case 1:
                deleteForAll(message, position);
                break;
            case 2:
                messageDatabaseService.deleteMessageFromDb(message);
                deleteMessageFromDeviceList(message.getKeyString());
                recyclerDetailConversationAdapter.notifyItemRangeChanged(position - 1, messageList.size());
                Message messageToResend = new Message(message);
                messageToResend.setCreatedAtTime(System.currentTimeMillis() + MobiComUserPreference.getInstance(getActivity()).getDeviceTimeOffset());
                conversationService.sendMessage(messageToResend, messageIntentClass);
                break;
            case 3:
                if (customizationSettings.isAgentApp() && message.isTypeOutbox()) {
                    deleteForAll(message, position);
                } else {
                    String messageKeyString = message.getKeyString();
                    new DeleteConversationAsyncTask(conversationService, message, contact).execute();
                    deleteMessageFromDeviceList(messageKeyString);
                    recyclerDetailConversationAdapter.notifyItemRangeChanged(position - 1, messageList.size());
                }
                break;
            case 4:
                String messageJson = GsonUtils.getJsonFromObject(message, Message.class);
                conversationUIService.startMessageInfoFragment(messageJson);
                break;
            case 5:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                if (message.getFilePaths() != null) {
                    Uri shareUri = null;
                    if (Utils.hasNougat()) {
                        shareUri = FileProvider.getUriForFile(AppContextService.getContext(getContext()), Utils.getMetaDataValue(getActivity(), MobiComKitConstants.PACKAGE_NAME) + ".provider", new File(message.getFilePaths().get(0)));
                    } else {
                        shareUri = Uri.fromFile(new File(message.getFilePaths().get(0)));
                    }
                    shareIntent.setDataAndType(shareUri, text_card);
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, shareUri);
                    if (!TextUtils.isEmpty(message.getMessage())) {
                        shareIntent.putExtra(Intent.EXTRA_TEXT, message.getMessage());
                    }
                    shareIntent.setType(FileUtils.getMimeType(new File(message.getFilePaths().get(0))));
                } else {
                    shareIntent.putExtra(Intent.EXTRA_TEXT, message.getMessage());
                    shareIntent.setType(text_plain);
                }
                startActivity(Intent.createChooser(shareIntent, AppContextService.getContext(getContext()).getString(R.string.send_message_to)));
                break;

            case 6:
                try {
                    Configuration config = AppContextService.getContext(getContext()).getResources().getConfiguration();
                    messageMetaData = new HashMap<>();
                    String displayName;
                    if (message.getGroupId() != null) {
                        if (MobiComUserPreference.getInstance(getActivity()).getUserId().equals(message.getContactIds()) || TextUtils.isEmpty(message.getContactIds())) {
                            displayName = AppContextService.getContext(getContext()).getString(R.string.you_string);
                        } else {
                            displayName = appContactService.getContactById(message.getContactIds()).getDisplayName();
                        }
                    } else {
                        if (message.isTypeOutbox()) {
                            displayName = AppContextService.getContext(getContext()).getString(R.string.you_string);
                        } else {
                            displayName = appContactService.getContactById(message.getContactIds()).getDisplayName();
                        }
                    }
                    nameTextView.setText(displayName);
                    if (message.hasAttachment()) {
                        FileMeta fileMeta = message.getFileMetas();
                        imageViewForAttachmentType.setVisibility(VISIBLE);
                        if (fileMeta.getContentType().contains(IMAGE)) {
                            imageViewForAttachmentType.setImageResource(R.drawable.km_ic_image_camera_alt);
                            if (TextUtils.isEmpty(message.getMessage())) {
                                messageTextView.setText(AppContextService.getContext(getContext()).getString(R.string.photo_string));
                            } else {
                                messageTextView.setText(message.getMessage());
                            }
                            galleryImageView.setVisibility(VISIBLE);
                            imageViewRLayout.setVisibility(VISIBLE);
                            imageThumbnailLoader.loadImage(message, galleryImageView);
                        } else if (fileMeta.getContentType().contains(VIDEO)) {
                            imageViewForAttachmentType.setImageResource(R.drawable.km_ic_action_video);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                if (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                                    imageViewForAttachmentType.setScaleX(-1);
                                }
                            }
                            if (TextUtils.isEmpty(message.getMessage())) {
                                messageTextView.setText(ApplozicService.getContext(getContext()).getString(R.string.video_string));
                            } else {
                                messageTextView.setText(message.getMessage());
                            }
                            if (message.getFilePaths() != null && message.getFilePaths().size() > 0) {
                                if (imageCache.getBitmapFromMemCache(message.getKeyString()) != null) {
                                    galleryImageView.setImageBitmap(imageCache.getBitmapFromMemCache(message.getKeyString()));
                                } else {
                                    imageCache.addBitmapToCache(message.getKeyString(), fileClientService.createAndSaveVideoThumbnail(message.getFilePaths().get(0)));
                                    galleryImageView.setImageBitmap(fileClientService.createAndSaveVideoThumbnail(message.getFilePaths().get(0)));
                                }
                            }
                            galleryImageView.setVisibility(VISIBLE);
                            imageViewRLayout.setVisibility(VISIBLE);
                        } else if (fileMeta.getContentType().contains(AUDIO)) {
                            imageViewForAttachmentType.setImageResource(R.drawable.km_ic_music_note);
                            if (TextUtils.isEmpty(message.getMessage())) {
                                messageTextView.setText(AppContextService.getContext(getContext()).getString(R.string.audio_string));
                            } else {
                                messageTextView.setText(message.getMessage());
                            }
                            galleryImageView.setVisibility(View.GONE);
                            imageViewRLayout.setVisibility(View.GONE);
                        } else if (message.isContactMessage()) {
                            MobiComVCFParser parser = new MobiComVCFParser();
                            imageViewForAttachmentType.setImageResource(R.drawable.km_ic_person_white);
                            try {
                                VCFContactData data = parser.parseCVFContactData(message.getFilePaths().get(0));
                                if (data != null) {
                                    messageTextView.setText(AppContextService.getContext(getContext()).getString(R.string.contact_string));
                                    messageTextView.append(" " + data.getName());
                                }
                            } catch (Exception e) {
                                imageViewForAttachmentType.setImageResource(R.drawable.km_ic_person_white);
                                messageTextView.setText(AppContextService.getContext(getContext()).getString(R.string.contact_string));
                            }
                            galleryImageView.setVisibility(View.GONE);
                            imageViewRLayout.setVisibility(View.GONE);
                        } else {
                            imageViewForAttachmentType.setImageResource(R.drawable.km_ic_action_attachment);
                            if (TextUtils.isEmpty(message.getMessage())) {
                                messageTextView.setText(AppContextService.getContext(getContext()).getString(R.string.attachment_string));
                            } else {
                                messageTextView.setText(message.getMessage());
                            }
                            galleryImageView.setVisibility(View.GONE);
                            imageViewRLayout.setVisibility(View.GONE);
                        }
                        imageViewForAttachmentType.setColorFilter(ContextCompat.getColor(AppContextService.getContext(getContext()), R.color.core_lite_gray_color));
                    } else if (message.getContentType() == Message.ContentType.LOCATION.getValue()) {
                        imageViewForAttachmentType.setVisibility(VISIBLE);
                        galleryImageView.setVisibility(VISIBLE);
                        imageViewRLayout.setVisibility(VISIBLE);
                        messageTextView.setText(AppContextService.getContext(getContext()).getString(R.string.al_location_string));
                        imageViewForAttachmentType.setImageResource(R.drawable.km_ic_location_on_white_24dp);
                        imageViewForAttachmentType.setColorFilter(ContextCompat.getColor(AppContextService.getContext(getContext()), R.color.core_lite_gray_color));
                        messageImageLoader.setLoadingImage(R.drawable.km_map_offline_thumbnail);
                        messageImageLoader.loadImage(LocationUtils.loadStaticMap(message.getMessage(), geoApiKey), galleryImageView);
                    } else {
                        imageViewForAttachmentType.setVisibility(View.GONE);
                        imageViewRLayout.setVisibility(View.GONE);
                        galleryImageView.setVisibility(View.GONE);
                        messageTextView.setText(message.getMessage());
                    }
                    messageMetaData.put(Message.MetaDataType.AL_REPLY.getValue(), message.getKeyString());
                    if (messageMetaData != null && !messageMetaData.isEmpty()) {
                        String replyMessageKey = messageMetaData.get(Message.MetaDataType.AL_REPLY.getValue());
                        if (!TextUtils.isEmpty(replyMessageKey)) {
                            messageDatabaseService.updateMessageReplyType(replyMessageKey, Message.ReplyMessage.REPLY_MESSAGE.getValue());
                        }
                    }
                    attachReplyCancelLayout.setVisibility(VISIBLE);
                    replayRelativeLayout.setVisibility(VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                Utils.printLog(getContext(), TAG, "Default switch case.");
        }
        return true;
    }

    private void deleteForAll(Message message, int position) {
        MessageDeleteUseCase.executeWithExecutor(
                getContext(),
                message.getKeyString(),
                true,
                new TaskListener<String>() {
                    @Override
                    public void onSuccess(String response) {
                        if (getContext() != null) {
                            deleteMessageFromDeviceList(message.getKeyString());
                            recyclerDetailConversationAdapter.notifyItemRangeChanged(position - 1, messageList.size());
                            KmToast.makeText(getContext(), "Message Deleted", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Exception error) {
                        KmToast.error(AppContextService.getContext(getContext()), "Error while deleting Message", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void processAttachmentIconsClick() {

        if (cameraButton != null) {
            cameraButton.setOnClickListener(v -> {
                EventManager.getInstance().sendOnAttachmentClick(CAMERA);
                emoticonsFrameLayout.setVisibility(View.GONE);
                if (getActivity() != null) {
                    if (((KmStoragePermissionListener) getActivity()).isPermissionGranted()) {
                        ((ConversationActivity) getActivity()).processCameraAction();
                    } else {
                        ((KmStoragePermissionListener) getActivity()).checkPermission(new KmStoragePermission() {
                            @Override
                            public void onAction(boolean didGrant) {
                                if (didGrant) {
                                    ((ConversationActivity) getActivity()).processCameraAction();
                                }
                            }
                        });
                    }
                }
            });
        }

        if (videoButton != null) {
            videoButton.setOnClickListener(v -> {
                EventManager.getInstance().sendOnAttachmentClick(VIDEO);
                emoticonsFrameLayout.setVisibility(View.GONE);
                if (getActivity() != null) {
                    if (((KmStoragePermissionListener) getActivity()).isPermissionGranted()) {
                        ((ConversationActivity) getActivity()).processVideoRecording();
                    } else {
                        ((KmStoragePermissionListener) getActivity()).checkPermission(new KmStoragePermission() {
                            @Override
                            public void onAction(boolean didGrant) {
                                if (didGrant) {
                                    ((ConversationActivity) getActivity()).processVideoRecording();
                                }
                            }
                        });
                    }
                }
            });
        }

        if (multiSelectGalleryButton != null) {
            multiSelectGalleryButton.setOnClickListener(view -> {
                EventManager.getInstance().sendOnAttachmentClick(GALLERY);
                emoticonsFrameLayout.setVisibility(View.GONE);
                if (getActivity() != null) {
                    if (!((KmStoragePermissionListener) getActivity()).isPermissionGranted()) {
                        //get permission
                        ((KmStoragePermissionListener) getActivity()).checkPermission(new KmStoragePermission() {
                            @Override
                            public void onAction(boolean didGrant) {
                                //did not get permission
                                if (!didGrant) {
                                    return;
                                } else {
                                    ((ConversationActivity) getActivity()).processMultiSelectGallery();
                                }
                            }
                        });
                    } else {
                        ((ConversationActivity) getActivity()).processMultiSelectGallery();
                    }
                }
            });
        }

        if (fileAttachmentButton != null) {
            fileAttachmentButton.setOnClickListener(v -> {
                EventManager.getInstance().sendOnAttachmentClick("file");
                emoticonsFrameLayout.setVisibility(View.GONE);
                if (getActivity() != null) {
                    if (((KmStoragePermissionListener) getActivity()).isPermissionGranted()) {
                        ((ConversationActivity) getActivity()).processAttachment();
                    } else {
                        ((KmStoragePermissionListener) getActivity()).checkPermission(new KmStoragePermission() {
                            @Override
                            public void onAction(boolean didGrant) {
                                if (didGrant) {
                                    ((ConversationActivity) getActivity()).processAttachment();
                                }
                            }
                        });
                    }
                }
            });
        }


        if (locationButton != null) {
            locationButton.setOnClickListener(v -> {
                emoticonsFrameLayout.setVisibility(View.GONE);
                EventManager.getInstance().sendOnLocationClick();
                if (getActivity() != null) {
                    ((ConversationActivity) getActivity()).processLocation();
                }
            });
        }
    }

    /**
     * displays/hides the away layout
     *
     * @param show true to display/ false to not, if response is null, then fetch from cache
     */
    public void showAwayMessage(boolean show, KmApiResponse.KmDataResponse response) {
        if (response != null) {
            kmAwayView.setupAwayMessage(response, channel);
        } else if (show && kmAwayView.getAwayMessage() != null) {
            kmAwayView.handleAwayMessage(true);
        } else if (show) {
            loadAwayMessage();
        }
        if (customizationSettings.getAwayMessageTextColor() != null) {
            kmAwayView.getAwayMessageTv().setTextColor(Color.parseColor(isCurrentlyInDarkMode ? customizationSettings.getAwayMessageTextColor().get(1) : customizationSettings.getAwayMessageTextColor().get(0)));
        }
        if (isHideAssigneeStatus) {
            kmAwayView.setVisibility(GONE);
        } else {
            kmAwayView.setVisibility(show && customizationSettings.isEnableAwayMessage() ? VISIBLE : GONE);
        }
        //don't hide any message if away view is visible
        if (show && customizationSettings.isEnableAwayMessage()) {
            linearLayoutManager.scrollToPosition(messageList.size() - 1);
        }
    }

    public boolean isAwayMessageVisible() {
        return kmAwayView.isAwayMessageVisible();
    }

    /**
     * displays/hides the feedback display layout, along with the feedback received from the server.
     *
     * @param display true to display/ false to not
     */
    public void setFeedbackDisplay(boolean display) {
        hideMessageTemplate();
        if (channel != null && !channel.isDeleted()) {
            if (display) {
                kmFeedbackView.setVisibility(VISIBLE);
                TextView restartConversationTextQuestion = kmFeedbackView.findViewById(R.id.idTextQuestion);
                TextView textViewRestartConversation = kmFeedbackView.findViewById(R.id.idFeedbackRestartConversation);
                if (!customizationSettings.isRestartConversationButtonVisibility()) {
                    textViewRestartConversation.setVisibility(View.INVISIBLE);
                    textViewRestartConversation.setPadding(0, 0, 0, 0);
                    restartConversationTextQuestion.setVisibility(View.INVISIBLE);
                    restartConversationTextQuestion.setPadding(0, 0, 0, 0);
                }
                individualMessageSendLayout.setVisibility(View.GONE);
                mainDivider.setVisibility(View.GONE);


                if (themeHelper.isCollectFeedback()) {
                    frameLayoutProgressbar.setVisibility(VISIBLE);
                    KmService.getConversationFeedback(getActivity(), new FeedbackDetailsData(String.valueOf(channel.getKey()), null, null, null), new TaskListener<KmApiResponse<KmFeedback>>() {
                        @Override
                        public void onSuccess(KmApiResponse<KmFeedback> response) {
                            frameLayoutProgressbar.setVisibility(View.GONE);
                            if (response.getData() != null) { //i.e if feedback found
                                //show the feedback based on the data given
                                Message latestStatusMessage = messageDatabaseService.getLatestStatusMessage(channel.getKey());
                                Message latestMessage = messageList.get(messageList.size() - 1);
                                //If Conversation is resolved and feedback is submitted
                                if (latestStatusMessage != null && latestStatusMessage.isTypeResolved() && response.getData().isLatestFeedbackSubmitted(latestStatusMessage.getCreatedAtTime())) {
//                                   kmFeedbackView.showFeedback(context, response.getData());
                                } else {
//                                  //Don't open feedback input fragment when feedback is already submitted
                                    if (latestMessage.isFeedbackMessage()) {
                                        return;
                                    }
                                    if (!customizationSettings.isOneTimeRating())
                                        openFeedbackFragment();
                                }
                            } else {
                                //if feedback not found (null)
                                //open the feedback input fragment
                                openFeedbackFragment();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Exception error) {
                            frameLayoutProgressbar.setVisibility(View.GONE);
                            Utils.printLog(getContext(), TAG, "Feedback get failed: " + error.toString());
                        }
                    });
                }
            } else {
                kmFeedbackView.setVisibility(View.GONE);
                toggleMessageSendLayoutVisibility();
                toggleHideChatBarWithBots();
                toggleAttachmentLayoutVisibility();
                mainDivider.setVisibility(VISIBLE);
            }
        }
    }

    public void hideAwayMessage(Message message) {
        if (message.isTypeOutbox()) {
            return;
        }

        if (isAwayMessageVisible() && message.getMetadata() != null) {
            if (message.getMetadata().isEmpty()) {
                showAwayMessage(false, null);
                return;
            }
            boolean isValidMetadata = message.getMetadata().containsKey(CATEGORY) && !HIDDEN.equals(message.getMetadata().get(CATEGORY))
                    && message.getMetadata().containsKey("hide") && !"true".equals(message.getMetadata().get("hide"));

            boolean isSentByBot = message.getMetadata().containsKey(SKIP_BOT) && "true".equals(message.getMetadata().get(SKIP_BOT));
            if (isValidMetadata || isSentByBot) {
                showAwayMessage(false, null);
                return;
            }
        }

        if (isAwayMessageVisible() && message.getMetadata() == null) {
            showAwayMessage(false, null);
        }
    }

    @Override
    public void onAction(Context context, String action, Message message, Object object, Map<String, Object> replyMetadata) {
        switch (action) {
            case KmRichMessage.OPEN_WEB_VIEW_ACTIVITY:
                if (getActivity() != null) {
                    Bundle bundle = (Bundle) object;
                    boolean isDeepLink = bundle.getBoolean(KmRichMessage.IS_DEEP_LINK, false);
                    Intent intent;
                    if (isDeepLink) {
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(bundle.getString(KmRichMessage.LINK_URL)));
                    } else {
                        intent = new Intent(getActivity(), KmWebViewActivity.class);
                        intent.putExtra(KmWebViewActivity.Al_WEB_VIEW_BUNDLE, bundle);
                    }
                    getActivity().startActivity(intent);
                }
                break;
            case KmRichMessage.SEND_MESSAGE:
                if (message != null) {
                    sendMessage(message.getMessage(), message.getMetadata(), message.getContentType());
                }
                break;
            case KmAutoSuggestionAdapter.KM_AUTO_SUGGESTION_ACTION:
                List<KmAutoSuggestionModel> autoSuggestionList = KmService.getAutoSuggestionList();
                if (autoSuggestionList != null && !autoSuggestionList.isEmpty()) {
                    for (KmAutoSuggestionModel kmAutoSuggestion : autoSuggestionList) {
                        if (kmAutoSuggestion.isSupportsRichMessage() && object.equals(kmAutoSuggestion.getContent())) {

                            Map<String, String> autoSuggestionMetadata = new HashMap<>();
                            try {
                                JSONObject jsonDictionary = new JSONObject((String) object);
                                JSONObject contentMetaData = jsonDictionary.optJSONObject("metadata");

                                if (contentMetaData != null) {
                                    Iterator<String> keys = contentMetaData.keys();
                                    while (keys.hasNext()) {
                                        String key = keys.next();
                                        String value = contentMetaData.optString(key);
                                        autoSuggestionMetadata.put(key, value);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            autoSuggestionMetadata.put(KMEVENT, QUICK_REPLY_CLICK);
                            autoSuggestionMetadata.put(SKIP_BOT, "true");
                            autoSuggestionMetadata.put(CATEGORY, HIDDEN);
                            sendMessage(KM_QUICK_REPLY, autoSuggestionMetadata, null, null, Message.ContentType.DEFAULT.getValue());
                            messageEditText.setText("");
                            getLoaderManager().destroyLoader(1);
                            kmAutoSuggestionRecycler.setVisibility(View.GONE);
                            if (kmAutoSuggestionDivider != null) {
                                kmAutoSuggestionDivider.setVisibility(View.GONE);
                            }
                        }
                        if (!kmAutoSuggestion.isSupportsRichMessage() && object.equals(kmAutoSuggestion.getContent())) {
                            populateAutoSuggestion(false, null, (String) object);
                        }
                    }
                }
                break;
            case RichMessageActionProcessor.NOTIFY_ITEM_CHANGE:
                if (messageList != null && recyclerDetailConversationAdapter != null && message != null) {
                    int index = messageList.indexOf(message);

                    if (index != -1) {
                        recyclerDetailConversationAdapter.notifyItemChanged(index);
                    }
                }
        }
    }

    @Deprecated
    @CleanUpRequired(reason = "Migrated from KMUserDetailTask to UserDetailUseCase ")
    public static class KMUserDetailTask extends AsyncTask<Void, Void, Boolean> {

        WeakReference<Context> context;
        String userId;
        KmUserDetailsCallback callback;
        AppContactService appContactService;

        public KMUserDetailTask(Context context, String userId, KmUserDetailsCallback callback) {
            this.context = new WeakReference<>(context);
            this.userId = userId;
            this.callback = callback;
            appContactService = new AppContactService(this.context.get());
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                Set<String> userIdSet = new HashSet<>();
                userIdSet.add(userId);
                UserService.getInstance(context.get()).processUserDetailsByUserIds(userIdSet);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean executed) {
            super.onPostExecute(executed);
            if (executed && callback != null) {
                callback.hasFinished(appContactService.getContactById(userId));
            }
        }
    }

    public interface KmUserDetailsCallback {

        void hasFinished(Contact contact);
    }

    public static boolean isEmailConversation(Channel channel) {
        return channel != null && channel.getMetadata() != null && channel.getMetadata().containsKey(KM_CONVERSATION_SUBJECT);
    }

    public CountDownTimer getCountDownTimer(final String userId) {

        if (typingTimerMap == null) {
            typingTimerMap = new HashMap<>();
        }

        if (typingTimerMap.containsKey(userId)) {
            return typingTimerMap.get(userId);
        }

        CountDownTimer timer = new CountDownTimer(TYPING_STOP_TIME * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if (typingTimerMap != null) {
                    typingTimerMap.remove(userId);
                }
                updateTypingStatus(userId, false);
            }
        };

        typingTimerMap.put(userId, timer);
        return timer;
    }

    @Override
    public void onRecordStart() {
        vibrate();
        if (speechToText != null) {
            if (isRecording) {
                speechToText.stopListening();
                if (recordButton != null) {
                    recordButton.stopScale();
                }
                toggleRecordViews(true);

                if (messageEditText != null && !TextUtils.isEmpty(messageEditText.getText().toString().trim())) {
                    handleSendAndRecordButtonView(true);
                }
            } else {
                toggleRecordViews(false);
                speechToText.startListening();
            }
        } else {
            if (kmAudioRecordManager != null) {
                kmAudioRecordManager.recordAudio();
            }
        }
        toggleRecordViews(false);
    }

    @Override
    public void onRecordCancel() {
        isRecording = false;
        if (recordButton != null && getContext() != null) {
            KmUtils.setBackground(getContext(), recordButton, R.drawable.km_audio_button_background);
        }
        if (kmAudioRecordManager != null) {
            kmAudioRecordManager.cancelAudio();
        }
    }

    @Override
    public void onRecordFinish(long recordTime) {
        toggleRecordViews(true);
        if (kmAudioRecordManager != null) {
            kmAudioRecordManager.sendAudio();
        }
    }

    @Override
    public void onLessThanSecond() {
        toggleRecordViews(true);
        if (getContext() != null && !isSpeechToTextEnabled) {
            KmToast.makeText(getContext(), getContext().getString(R.string.km_audio_record_toast_message), Toast.LENGTH_SHORT).show();
        }
        if (kmAudioRecordManager != null) {
            kmAudioRecordManager.cancelAudio();
        }
    }

    @Override
    public void onSpeechToTextResult(String text) {
        if (messageEditText != null && !TextUtils.isEmpty(text)) {
            messageEditText.setText(text);
            messageEditText.setSelection(text.length());
            handleSendAndRecordButtonView(!isSendOnSpeechEnd);

            if (isSendOnSpeechEnd && sendButton != null) {
                sendButton.callOnClick();
            }
        }
    }

    @Override
    public void onSpeechToTextPartialResult(String text) {
        if (messageEditText != null && !TextUtils.isEmpty(text)) {
            messageEditText.setText(text);
            messageEditText.setSelection(text.length());
            handleSendAndRecordButtonView(speechToText.isStopped());
        }
    }

    @Override
    public void onSpeechEnd(int errorCode) {
        toggleRecordViews(true);
    }


    public void toggleRecordViews(boolean stopRecording) {
        isRecording = !stopRecording;
        if (recordButton != null && getContext() != null) {
            KmUtils.setBackground(getContext(), recordButton, stopRecording ? R.drawable.km_audio_button_background : R.drawable.km_audio_button_pressed_background);
        }

        if (messageEditText != null) {
            messageEditText.setVisibility((stopRecording || isSpeechToTextEnabled) ? View.VISIBLE : View.GONE);
            if (stopRecording) {
                messageEditText.requestFocus();
            }
        }
    }

    @Override
    public void onAnimationEnd() {
        if (messageEditText != null) {
            messageEditText.setVisibility(View.VISIBLE);
            messageEditText.requestFocus();
        }
    }

    public void processTakeOverFromBot(Context context, final Channel channel) {
        if (context == null || channel == null) {
            return;
        }

        final String loggedInUserId = MobiComUserPreference.getInstance(context).getUserId();

        Set<String> botIds = channel.getListOfUsersByRole(2);
        if (botIds != null) {
            botIds.remove(BOT);
        }

        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(AppContextService.getContext(context).getString(R.string.processing_take_over_from_bot));
        progressDialog.setCancelable(false);
        progressDialog.show();

        KmService.removeMembersFromConversation(
                context,
                channel.getKey(),
                botIds,
                new TaskListener<String>() {
                    @Override
                    public void onSuccess(String response) {
                        GroupInfoUpdate groupInfoUpdate = new GroupInfoUpdate(channel);
                        Map<String, String> metadata = channel.getMetadata();
                        if (metadata == null) {
                            metadata = new HashMap<>();
                        }
                        metadata.put(Channel.CONVERSATION_ASSIGNEE, loggedInUserId);
                        groupInfoUpdate.setMetadata(metadata);
                        KmSettings.updateConversation(AppContextService.getContext(getContext()), groupInfoUpdate, new TaskListener<Context>() {
                            @Override
                            public void onSuccess(Context status) {
                                Message message = new Message();

                                message.setMessage(Utils.getString(context, R.string.km_assign_to_message) + new ContactDatabase(context).getContactById(loggedInUserId).getDisplayName());

                                Map<String, String> metadata = new HashMap<>();
                                metadata.put(KmService.KM_SKIP_BOT, Message.GroupMessageMetaData.TRUE.getValue());
                                metadata.put(Message.BOT_ASSIGN, MobiComUserPreference.getInstance(context).getUserId());
                                metadata.put(KmService.KM_NO_ALERT, Message.GroupMessageMetaData.TRUE.getValue());
                                metadata.put(KmService.KM_BADGE_COUNT, Message.GroupMessageMetaData.FALSE.getValue());
                                metadata.put(Message.MetaDataType.KEY.getValue(), Message.MetaDataType.ARCHIVE.getValue());
                                message.setMetadata(metadata);
                                message.setContentType(Message.ContentType.CHANNEL_CUSTOM_MESSAGE.getValue());
                                message.setGroupId(channel.getKey());
                                new MobiComConversationService(context).sendMessage(message);

                                showTakeOverFromBotLayout(false, null);
                                if (progressDialog != null) {
                                    progressDialog.dismiss();
                                }
                            }

                            @Override
                            public void onFailure(Exception error) {
                                if (progressDialog != null) {
                                    progressDialog.dismiss();
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }
                    }
                });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return KmAutoSuggestionDatabase.getInstance(AppContextService.getContext(getContext())).getAutoSuggestionCursorLoader(args != null ? args.getString(KmAutoSuggestionAdapter.KM_AUTO_SUGGESTION_TYPED_TEXT) : "");
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor c) {
        if (kmAutoSuggestionAdapter == null) {
            kmAutoSuggestionAdapter = new KmAutoSuggestionAdapter(getContext(), this);
            kmAutoSuggestionRecycler.setAdapter(kmAutoSuggestionAdapter);
        }

        if (c.getCount() > 0) {
            if (kmAutoSuggestionDivider != null) {
                kmAutoSuggestionDivider.setVisibility(VISIBLE);
            }
            if (kmAutoSuggestionRecycler != null) {
                kmAutoSuggestionRecycler.setVisibility(VISIBLE);
            }
        } else {
            if (kmAutoSuggestionDivider != null) {
                kmAutoSuggestionDivider.setVisibility(View.GONE);
            }
            if (kmAutoSuggestionRecycler != null) {
                kmAutoSuggestionRecycler.setVisibility(View.GONE);
            }
        }
        kmAutoSuggestionAdapter.swapCursor(c);
    }


    /**
     * set the feed back of the given conversation when the submit button on feedback fragment is pressed.
     *
     * @param ratingValue the rating
     * @param feedback    the feedback comment
     */
    @Override
    public void onFeedbackFragmentSubmitButtonPressed(int ratingValue, String feedback) {
        final KmFeedback kmFeedback = new KmFeedback();
        kmFeedback.setGroupId(channel.getKey());

        if (!TextUtils.isEmpty(feedback)) {
            String[] feedbackArray = new String[1];
            feedbackArray[0] = feedback;
            kmFeedback.setComments(feedbackArray);
        }

        kmFeedback.setRating(ratingValue);
        EventManager.getInstance().sendOnSubmitRatingClicked(channel.getKey(), ratingValue, feedback);
        Contact user = new AppContactService(this.getActivity()).getContactById(MobiComUserPreference.getInstance(this.getContext()).getUserId());
        KmService.setConversationFeedback(getActivity(), kmFeedback, new FeedbackDetailsData(null, user.getDisplayName(), user.getUserId(), conversationAssignee.getUserId()), new TaskListener<KmApiResponse<KmFeedback>>() {
            @Override
            public void onSuccess(KmApiResponse<KmFeedback> status) {
                //kmFeedbackView.showFeedback(context, kmFeedback);
            }

            @Override
            public void onFailure(@NonNull Exception error) {
                KmToast.error(getActivity(), R.string.feedback_update_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onFeedbackFragmentV2SubmitButtonPressed(int ratingValue, String feedback) {
        final KmFeedback kmFeedback = new KmFeedback();
        kmFeedback.setGroupId(channel.getKey());

        if (!TextUtils.isEmpty(feedback)) {
            String[] feedbackArray = new String[1];
            feedbackArray[0] = feedback;
            kmFeedback.setComments(feedbackArray);
        }

        kmFeedback.setRating(ratingValue);
        Contact user = new AppContactService(this.getActivity()).getContactById(MobiComUserPreference.getInstance(this.getContext()).getUserId());
        KmService.setConversationFeedback(getActivity(), kmFeedback, new FeedbackDetailsData(null, user.getDisplayName(), user.getUserId(), conversationAssignee.getUserId()), new TaskListener<KmApiResponse<KmFeedback>>() {
            @Override
            public void onSuccess(KmApiResponse<KmFeedback> status) {
                //kmFeedbackView.showFeedback(context, kmFeedback);
            }

            @Override
            public void onFailure(@NonNull Exception error) {
                KmToast.error(getActivity(), R.string.feedback_update_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onLoaderReset(Loader loader) {
        kmAutoSuggestionAdapter.swapCursor(null);
    }

    @Override
    public void onMessageQueued(Message message) {
        updateTypingStatus(message.getTo(), true);
    }

    @Override
    public void onMessageDispatched(Message message) {
        handleAddMessage(message);
    }

    public abstract void onStartLoading(boolean loadingStarted);

    public void loadAwayMessage() {
        if (loggedInUserRole == User.RoleType.USER_ROLE.getValue()) {
            Kommunicate.loadAwayMessage(getContext(), channel.getKey(), new KmAwayMessageHandler() {
                @Override
                public void onSuccess(Context context, KmApiResponse.KmDataResponse response) {
                    showAwayMessage(true, response);

                }

                @Override
                public void onFailure(Context context, Exception e, String response) {
                    showAwayMessage(false, null);
                    Utils.printLog(context, TAG, "Response: " + response + "\nException : " + e);
                }
            });
        }

        toggleMessageSendLayoutVisibility();
        toggleHideChatBarWithBots();
        toggleAttachmentLayoutVisibility();
    }

    protected void toggleMessageSendLayoutVisibility() {
        if (loggedInUserRole == User.RoleType.USER_ROLE.getValue()) {
            Contact assigneeContact = appContactService.getContactById(channel.getConversationAssignee());

            boolean hideLayout = customizationSettings.isRestrictMessageTypingWithBots()
                    && assigneeContact != null
                    && User.RoleType.BOT.getValue().equals(assigneeContact.getRoleType());

            individualMessageSendLayout.setVisibility(hideLayout ? GONE : VISIBLE);
            recordLayout.setVisibility(hideLayout || !isRecordOptionEnabled ? GONE : VISIBLE);
            restrictWhatsappConversation(lastUserMessage);
        } else {
            individualMessageSendLayout.setVisibility(VISIBLE);
            restrictWhatsappConversation(lastUserMessage);

        }
    }

    public void hideMessageTemplate() {
        if (messageTemplate != null && messageTemplate.isEnabled()) {
            if (!customizationSettings.isRestartConversationButtonVisibility() && channel != null && channel.getKmStatus() == Channel.CLOSED_CONVERSATIONS) {
                messageTemplateView.setVisibility(GONE);
            } else {
                messageTemplateView.setVisibility(View.VISIBLE);
            }
        }
    }

    protected double twentyFourHoursAgoTimeStamp() {
        Date currentDate = new Date();
        long twentyFourHoursAgoMillis = currentDate.getTime() - (24 * 60 * 60 * 1000);
        return twentyFourHoursAgoMillis;
    }

    protected void restrictWhatsappConversation(Message lastMessage) {
        if (customizationSettings.isAgentApp() && channel.getMetadata() != null) {
            String conversationSource = channel.getMetadata().get(CONVERSATION_SOURCE);
            if (conversationSource != null) {
                for (String source : WHATSAPP_SOURCE) {
                    if (source.equals(conversationSource)) {
                        if (lastMessage != null) {
                            long lastMessageTime = lastMessage.getCreatedAtTime();
                            if ((double) lastMessageTime <= twentyFourHoursAgoTimeStamp()) {
                                individualMessageSendLayout.setVisibility(View.GONE);
                                recordLayout.setVisibility(GONE);
                                userNotAbleToChatTextView.setText(R.string.km_whatsapp_restriction);
                                userNotAbleToChatLayout.setVisibility(VISIBLE);
                            }
                        }

                    }
                }
            }

        }
    }

    protected void restrictNonTeamMemberConversation(ArrayList<String> teams) {
        if (getActivity() != null) {
            if (teams != null) {
                localTeams = teams;
            }
            if (messageList.isEmpty() || (localTeams != null && localTeams.isEmpty())) {
                return;
            }
            Channel messageChannel = ChannelService.getInstance(getActivity()).getChannel(messageList.get(messageList.size() - 1).getGroupId());
            String messageTeamId = messageChannel.getTeamId();
            if (localTeams != null && !localTeams.contains(messageTeamId) &&
                   channel.getMetadata() != null) {
                individualMessageSendLayout.setVisibility(View.GONE);
                recordLayout.setVisibility(GONE);
                userNotAbleToChatTextView.setText(R.string.km_teammode_restriction);
                userNotAbleToChatLayout.setVisibility(VISIBLE);
            }
        }
    }
}
