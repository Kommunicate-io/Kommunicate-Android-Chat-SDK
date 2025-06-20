package io.kommunicate.ui.conversation.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import io.kommunicate.devkit.api.account.user.MobiComUserPreference;
import io.kommunicate.devkit.api.account.user.User;
import io.kommunicate.devkit.api.conversation.Message;
import io.kommunicate.devkit.api.conversation.SyncCallService;
import io.kommunicate.devkit.api.conversation.database.MessageDatabaseService;
import io.kommunicate.devkit.broadcast.EventManager;
import io.kommunicate.devkit.broadcast.BroadcastService;
import io.kommunicate.devkit.contact.AppContactService;
import io.kommunicate.devkit.contact.BaseContactService;
import io.kommunicate.ui.CustomizationSettings;
import io.kommunicate.ui.DimensionsUtils;
import io.kommunicate.ui.KmLinearLayoutManager;
import io.kommunicate.ui.R;
import io.kommunicate.ui.conversation.ConversationUIService;
import io.kommunicate.ui.conversation.activity.ConversationActivity;
import io.kommunicate.ui.conversation.activity.MobiComKitActivityInterface;
import io.kommunicate.ui.conversation.adapter.QuickConversationAdapter;
import io.kommunicate.ui.kommunicate.KmPrefSettings;
import io.kommunicate.ui.kommunicate.utils.KmHelper;
import io.kommunicate.ui.kommunicate.utils.KmThemeHelper;
import io.kommunicate.ui.kommunicate.views.KmToast;
import io.kommunicate.ui.uilistener.KmActionCallback;
import io.kommunicate.ui.utils.InsetHelper;
import io.kommunicate.commons.AppContextService;
import io.kommunicate.commons.commons.core.utils.DateUtils;
import io.kommunicate.commons.commons.core.utils.Utils;
import io.kommunicate.commons.file.FileUtils;
import io.kommunicate.commons.json.GsonUtils;
import io.kommunicate.commons.people.SearchListFragment;
import io.kommunicate.commons.people.contact.Contact;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.kommunicate.Kommunicate;
import io.kommunicate.services.KmClientService;
import io.kommunicate.utils.KmUtils;

/**
 * Created by devashish on 10/2/15.
 */
public class MobiComQuickConversationFragment extends Fragment implements SearchListFragment {

    protected RecyclerView recyclerView = null;
    protected TextView emptyTextView;
    protected SwipeRefreshLayout swipeLayout;
    protected int listIndex;
    protected Map<String, Message> latestMessageForEachContact = new HashMap<String, Message>();
    protected List<Message> messageList = new ArrayList<Message>();
    protected QuickConversationAdapter recyclerAdapter = null;
    protected boolean loadMore = false;
    protected SyncCallService syncCallService;
    ConversationUIService conversationUIService;
    CustomizationSettings customizationSettings;
    String searchString;
    private DownloadConversation downloadConversation;
    private BaseContactService baseContactService;
    private Toolbar toolbar;
    private MessageDatabaseService messageDatabaseService;
    private int previousTotalItemCount = 0;
    private boolean loading = true;
    LinearLayoutManager linearLayoutManager;
    boolean isAlreadyLoading = false;
    int pastVisiblesItems, visibleItemCount, totalItemCount;
    Button startNewConv;
    RelativeLayout faqButtonLayout;
    KmThemeHelper themeHelper;
    boolean isCurrentlyInDarkMode;
    private static final String SUCCESS = "success";
    private static final String KM_START_NEW_CONVERSATION = "KmStartNewConversation";
    private static final String START_NEW_CHAT = "startNewChat";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String jsonString = FileUtils.loadSettingsJsonFile(AppContextService.getContext(getContext()));
        if (!TextUtils.isEmpty(jsonString)) {
            customizationSettings = (CustomizationSettings) GsonUtils.getObjectFromJson(jsonString, CustomizationSettings.class);
        } else {
            customizationSettings = new CustomizationSettings();
        }
        themeHelper = KmThemeHelper.getInstance(getContext(), customizationSettings);
        syncCallService = SyncCallService.getInstance(getActivity());
        conversationUIService = new ConversationUIService(getActivity());
        baseContactService = new AppContactService(getActivity());
        messageDatabaseService = new MessageDatabaseService(getActivity());
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                MobiComUserPreference.getInstance(getActivity()).setDeviceTimeOffset(DateUtils.getTimeDiffFromUtc());
            }
        });
        thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        setHasOptionsMenu(true);
        BroadcastService.lastIndexForChats = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View list = inflater.inflate(R.layout.message_list, container, false);
        isCurrentlyInDarkMode = themeHelper.isDarkModeEnabledForSDK();
        if (!customizationSettings.isAgentApp()) {
            LinearLayout kmMessageLinearLayout = list.findViewById(R.id.km_message_linear_layout);
            if (kmMessageLinearLayout != null) {
                kmMessageLinearLayout.setVisibility(View.GONE);
            }
        }

        recyclerView = (RecyclerView) list.findViewById(R.id.messageList);
//        recyclerView.setBackgroundColor(getResources().getColor(isCurrentlyInDarkMode ? R.color.dark_mode_default : R.color.conversation_list_all_background));
        recyclerView.setPadding(0, 0, 0, (int) DimensionsUtils.convertDpToPixel(95));


        if (messageList != null && !messageList.contains(null)) {
            messageList.add(null);
        }
        recyclerAdapter = new QuickConversationAdapter(getContext(), messageList, null);
        recyclerAdapter.setDarkMode(isCurrentlyInDarkMode);
        recyclerAdapter.setAlCustomizationSettings(customizationSettings);

        faqButtonLayout = getActivity().findViewById(R.id.faqButtonLayout);
        faqButtonLayout.setVisibility(View.VISIBLE);

        if (customizationSettings.isFaqOptionEnabled() || KmPrefSettings.getInstance(getContext()).isFaqOptionEnabled() || customizationSettings.isFaqOptionEnabled(1)) {
            TextView textView = faqButtonLayout.findViewById(R.id.kmFaqOption);
            TextView conversationTextView = faqButtonLayout.findViewById(R.id.km_conversation_text_view);
            if (textView != null) {
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String FaqUrl = new KmClientService(getContext(), Kommunicate.getFaqPageName()).getHelpCenterUrl(customizationSettings.isHideChatInHelpcenter());
                        EventManager.getInstance().sendOnFaqClick(FaqUrl);
                        ConversationActivity.openFaq(getActivity(), FaqUrl);
                    }
                });
            }
            conversationTextView.setTextColor(themeHelper.getToolbarTitleColor());
        } else {
            TextView textView = faqButtonLayout.findViewById(R.id.kmFaqOption);
            textView.setVisibility(View.GONE);
        }

        linearLayoutManager = new KmLinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setAdapter(recyclerAdapter);
        toolbar = (Toolbar) getActivity().findViewById(R.id.my_toolbar);
        toolbar.setClickable(false);
        TextView conversationTextView = toolbar.findViewById(R.id.km_conversation_text_view);
        if (conversationTextView != null) {
            conversationTextView.setTextColor(themeHelper.getToolbarTitleColor());
        }
        if (!TextUtils.isEmpty(customizationSettings.getMenuIconOnConversationScreen())) {
            Drawable overflowIcon = ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.km_baseline_more_vert);
            toolbar.setOverflowIcon(overflowIcon);
        }
        RelativeLayout toolbarCustomLayout = toolbar.findViewById(R.id.custom_toolbar_root_layout);
        if (toolbarCustomLayout != null) {
            toolbarCustomLayout.setVisibility(View.GONE);
        }

        loading = true;
        LinearLayout individualMessageSendLayout = (LinearLayout) list.findViewById(R.id.individual_message_send_layout);
        LinearLayout extendedSendingOptionLayout = (LinearLayout) list.findViewById(R.id.extended_sending_option_layout);

        startNewConv = list.findViewById(R.id.start_new_conversation);
//        KmUtils.setGradientSolidColor(startNewConv, !TextUtils.isEmpty(isCurrentlyInDarkMode ? alCustomizationSettings.getStartNewConversationButtonBackgroundColor().get(1) : alCustomizationSettings.getStartNewConversationButtonBackgroundColor().get(0)) ? Color.parseColor(isCurrentlyInDarkMode ? alCustomizationSettings.getStartNewConversationButtonBackgroundColor().get(1) : alCustomizationSettings.getStartNewConversationButtonBackgroundColor().get(0)) : KmThemeHelper.getInstance(getContext(), alCustomizationSettings).getPrimaryColor());

        if (customizationSettings != null && customizationSettings.isShowStartNewConversation() && User.RoleType.USER_ROLE.getValue().equals(MobiComUserPreference.getInstance(getContext()).getUserRoleType())) {
            startNewConv.setVisibility(View.VISIBLE);
        } else {
            startNewConv.setVisibility(View.GONE);
        }

        startNewConv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppContextService.getContext(getContext()) instanceof KmActionCallback) {
                    ((KmActionCallback) AppContextService.getContext(getContext())).onReceive(getContext(), null, START_NEW_CHAT);
                } else {
                    KmHelper.setStartNewChat(getActivity());
                }
                LocalBroadcastManager.getInstance(getContext()).sendBroadcastSync(new Intent(KM_START_NEW_CONVERSATION));
            }
        });

        individualMessageSendLayout.setVisibility(View.GONE);
        extendedSendingOptionLayout.setVisibility(View.GONE);

        emptyTextView = (TextView) list.findViewById(R.id.noConversations);
//        emptyTextView.setTextColor(Color.parseColor(isCurrentlyInDarkMode ? alCustomizationSettings.getNoConversationLabelTextColor().get(1).trim() : alCustomizationSettings.getNoConversationLabelTextColor().get(0).trim()));

        swipeLayout = (SwipeRefreshLayout) list.findViewById(R.id.swipe_container);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        recyclerView.setLongClickable(true);
        setupModes(isCurrentlyInDarkMode);
        registerForContextMenu(recyclerView);
        setupInsets();
        return list;
    }

    private void setupInsets() {
        InsetHelper.configureSystemInsets(
                startNewConv,
                0,
                25,
                false
        );
        InsetHelper.configureSystemInsets(
                recyclerView,
                0,
                200,
                true
        );
    }

    protected View.OnClickListener startNewConversation() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null) {
                    ((MobiComKitActivityInterface) getActivity()).startContactActivityForResult();
                }
            }
        };
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean newDarkModeStatus = themeHelper.isDarkModeEnabledForSDK();
        if (isCurrentlyInDarkMode != newDarkModeStatus) {
            isCurrentlyInDarkMode = newDarkModeStatus;
            setupModes(newDarkModeStatus);
        }
    }

    private void setupModes(boolean isDarkModeEnabled) {
        ((TextView) toolbar.findViewById(R.id.km_conversation_text_view)).setTextColor(themeHelper.getToolbarTitleColor());
        emptyTextView.setTextColor(Color.parseColor(isDarkModeEnabled ? customizationSettings.getNoConversationLabelTextColor().get(1).trim() : customizationSettings.getNoConversationLabelTextColor().get(0).trim()));
        KmUtils.setGradientSolidColor(startNewConv, themeHelper.parseColorWithDefault(customizationSettings.getStartNewConversationButtonBackgroundColor().get(isDarkModeEnabled ? 1 : 0),
                themeHelper.parseColorWithDefault(customizationSettings.getToolbarColor().get(isDarkModeEnabled ? 1 : 0), themeHelper.getPrimaryColor())));
        recyclerView.setBackgroundColor(getResources().getColor(isCurrentlyInDarkMode ? R.color.dark_mode_default : R.color.conversation_list_all_background));
        recyclerAdapter.setDarkMode(isDarkModeEnabled);
        recyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        //Removing Refresh option from conversation list screen because we have swipe down refresh
//        if (alCustomizationSettings.isRefreshOption()) {
//            menu.findItem(R.id.refresh).setVisible(true);
//        }
        if (customizationSettings.isProfileOption()) {
            menu.findItem(R.id.user_profile).setVisible(true);
        }
        if (customizationSettings.isMessageSearchOption()) {
            menu.findItem(R.id.menu_search).setVisible(true);
        }
        if (customizationSettings.isLogoutOption()) {
            menu.findItem(R.id.logout).setVisible(true);
        }

        if (!customizationSettings.isAgentApp() && customizationSettings.isToolbarTitleCenterAligned()) {
            if (customizationSettings.isProfileOption() || customizationSettings.isMessageSearchOption() || customizationSettings.isLogoutOption()) {
                centerToolbarTitle(toolbar, true);
            } else {
                centerToolbarTitle(toolbar, false);
            }
        }
    }

    private void centerToolbarTitle(Toolbar myToolbar, boolean isOverflowMenuVisible) {
        TextView toolbarTitle = myToolbar.findViewById(R.id.km_conversation_text_view);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) toolbarTitle.getLayoutParams();
        toolbarTitle.setGravity(Gravity.CENTER);

        Resources resources = getResources();
        TypedValue typedValue = new TypedValue();
        Objects.requireNonNull(getActivity()).getTheme().resolveAttribute(
                android.R.attr.actionBarSize, typedValue, true);

        int actionBarWidth = (int) typedValue.getDimension(resources.getDisplayMetrics());

        int marginEnd;
        if (isOverflowMenuVisible) {
            marginEnd = 10;
        } else {
            marginEnd = actionBarWidth - 30;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            layoutParams.setMarginEnd(marginEnd);
        } else {
            layoutParams.setMargins(0, 0, marginEnd, 0);
        }
        toolbarTitle.setLayoutParams(layoutParams);
    }

    public void addMessage(final Message message) {
        if (getActivity() == null) {
            return;
        }
        final Context context = getActivity();
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                message.processContactIds(context);
                Message recentMessage;
                if (message.getGroupId() != null) {
                    recentMessage = latestMessageForEachContact.get(ConversationUIService.GROUP + message.getGroupId());
                } else {
                    recentMessage = latestMessageForEachContact.get(message.getContactIds());
                }

                if (recentMessage != null && message.getCreatedAtTime() >= recentMessage.getCreatedAtTime()) {
                    messageList.remove(recentMessage);
                } else if (recentMessage != null) {
                    return;
                }
                if (message.getGroupId() != null) {
                    latestMessageForEachContact.put(ConversationUIService.GROUP + message.getGroupId(), message);
                } else {
                    latestMessageForEachContact.put(message.getContactIds(), message);
                }
                messageList.add(0, message);
                recyclerAdapter.notifyDataSetChanged();
                emptyTextView.setVisibility(View.GONE);
                emptyTextView.setText(!TextUtils.isEmpty(customizationSettings.getNoConversationLabel()) ? customizationSettings.getNoConversationLabel() : AppContextService.getContext(context).getResources().getString(R.string.no_conversation));
            }
        });
    }

    public void refreshView() {
        if (!getUserVisibleHint()) {
            return;
        }
        if (getActivity() == null) {
            return;
        }
        try {
            this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() != null) {
                        if (recyclerAdapter != null) {
                            recyclerAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });

        } catch (Exception e) {
            Utils.printLog(getActivity(), "AL", "Exception while updating view .");
        }
    }


    public void updateUserInfo(final String userId) {
        if (!getUserVisibleHint()) {
            return;
        }
        if (getActivity() == null) {
            return;
        }
        if (getActivity() != null) {
            this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Message message = latestMessageForEachContact.get(userId);
                        if (message != null) {
                            int index = messageList.indexOf(message);
                            View view = recyclerView.getChildAt(index - linearLayoutManager.findFirstVisibleItemPosition());
                            Contact contact = baseContactService.getContactById(userId);
                            if (view != null && contact != null) {
                                ImageView contactImage = (ImageView) view.findViewById(R.id.contactImage);
                                TextView displayNameTextView = (TextView) view.findViewById(R.id.smReceivers);
                                displayNameTextView.setText(contact.getDisplayName());
                                recyclerAdapter.contactImageLoader.loadImage(contact, contactImage);
                                recyclerAdapter.notifyDataSetChanged();
                            }
                        }
                    } catch (Exception ex) {
                        Utils.printLog(getActivity(), "AL", "Exception while updating view .");
                    }
                }
            });
        }
    }

    public void updateLastMessage(String keyString, String userId) {
        if (messageDatabaseService == null) {
            return;
        }
        for (Message message : messageList) {
            if (message.getKeyString() != null && message.getKeyString().equals(keyString)) {
                List<Message> lastMessage;
                if (message.getGroupId() != null) {
                    lastMessage = messageDatabaseService.getLatestMessageByChannelKey(message.getGroupId());
                } else {
                    lastMessage = messageDatabaseService.getLatestMessage(message.getContactIds());
                }
                if (lastMessage.isEmpty()) {
                    removeConversation(message, userId);
                } else {
                    deleteMessage(lastMessage.get(0), userId);
                }
                break;
            }
        }
    }

    public void updateLastMessage(Message message) {
        if (message == null || messageDatabaseService == null) {
            return;
        }
        List<Message> lastMessage = new ArrayList<>();
        if (message.getGroupId() != null) {
            lastMessage = messageDatabaseService.getLatestMessageByChannelKey(message.getGroupId());
        } else {
            lastMessage = messageDatabaseService.getLatestMessage(message.getContactIds());
        }
        if (lastMessage.isEmpty()) {
            removeConversation(message, message.getContactIds());
        } else {
            deleteMessage(lastMessage.get(0), message.getContactIds());
        }
    }

    public void updateChannelName() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        recyclerAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void deleteMessage(final Message message, final String userId) {
        if (getActivity() == null) {
            return;
        }
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Message recentMessage;
                if (message.getGroupId() != null) {
                    recentMessage = latestMessageForEachContact.get(ConversationUIService.GROUP + message.getGroupId());
                } else {
                    recentMessage = latestMessageForEachContact.get(message.getContactIds());
                }
                if (recentMessage != null && message.getCreatedAtTime() <= recentMessage.getCreatedAtTime()) {
                    if (message.getGroupId() != null) {
                        latestMessageForEachContact.put(ConversationUIService.GROUP + message.getGroupId(), message);
                    } else {
                        latestMessageForEachContact.put(message.getContactIds(), message);
                    }

                    messageList.set(messageList.indexOf(recentMessage), message);

                    recyclerAdapter.notifyDataSetChanged();
                    if (messageList.isEmpty()) {
                        emptyTextView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    public void updateLatestMessage(Message message, String userId) {
        deleteMessage(message, userId);
    }

    public void removeConversation(final Message message, final String userId) {
        if (getActivity() == null) {
            return;
        }
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (message.getGroupId() != null && message.getGroupId() != 0) {
                    latestMessageForEachContact.remove(ConversationUIService.GROUP + message.getGroupId());
                } else {
                    latestMessageForEachContact.remove(message.getContactIds());
                }
                messageList.remove(message);
                if (recyclerAdapter != null) {
                    recyclerAdapter.notifyDataSetChanged();
                }
                checkForEmptyConversations();
            }
        });
    }

    public void removeConversation(final Contact contact, final Integer channelKey, String response) {
        if (getActivity() == null) {
            return;
        }
        if (SUCCESS.equals(response)) {
            this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Message message = null;
                    if (channelKey != null && channelKey != 0) {
                        message = latestMessageForEachContact.get(ConversationUIService.GROUP + channelKey);
                    } else {
                        message = latestMessageForEachContact.get(contact.getUserId());
                    }
                    if (message == null) {
                        return;
                    }
                    messageList.remove(message);
                    if (channelKey != null && channelKey != 0) {
                        latestMessageForEachContact.remove(ConversationUIService.GROUP + channelKey);
                    } else {
                        latestMessageForEachContact.remove(contact.getUserId());
                    }
                    recyclerAdapter.notifyDataSetChanged();
                    checkForEmptyConversations();
                }
            });
        } else {
            if (!Utils.isInternetAvailable(getActivity())) {
                KmToast.error(AppContextService.getContext(getContext()), AppContextService.getContext(getContext()).getString(R.string.you_need_network_access_for_delete), Toast.LENGTH_SHORT).show();
            } else {
                KmToast.error(AppContextService.getContext(getContext()), AppContextService.getContext(getContext()).getString(R.string.delete_conversation_failed), Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void checkForEmptyConversations() {
        boolean isLoadingConversation = (downloadConversation != null && downloadConversation.getStatus() == AsyncTask.Status.RUNNING);
        if (latestMessageForEachContact.isEmpty() && !isLoadingConversation) {
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setText(!TextUtils.isEmpty(customizationSettings.getNoConversationLabel()) ? customizationSettings.getNoConversationLabel() : AppContextService.getContext(getContext()).getResources().getString(R.string.no_conversation));
        } else {
            emptyTextView.setVisibility(View.GONE);
        }
    }

    public void setLoadMore(boolean loadMore) {
        this.loadMore = loadMore;
    }

    @Override
    public void onPause() {
        super.onPause();
        listIndex = linearLayoutManager.findFirstVisibleItemPosition();
        BroadcastService.currentUserId = null;
        if (recyclerView != null) {
            BroadcastService.lastIndexForChats = linearLayoutManager.findFirstVisibleItemPosition();
        }
        if (recyclerAdapter != null) {
            recyclerAdapter.contactImageLoader.setPauseWork(false);
            recyclerAdapter.channelImageLoader.setPauseWork(false);
        }
    }


    @Override
    public void onResume() {
        //Assigning to avoid notification in case if quick conversation fragment is opened....
        toolbar.setTitle(AppContextService.getContext(getContext()).getResources().getString(R.string.conversations));
        toolbar.setTitleTextColor(KmThemeHelper.getInstance(getActivity(), customizationSettings).getToolbarTitleColor());
        toolbar.setSubtitle("");
        BroadcastService.selectMobiComKitAll();
        super.onResume();
        if (recyclerView != null) {
            if (recyclerView.getChildCount() > listIndex) {
                recyclerView.scrollToPosition(listIndex);
            }
        }
        if (!isAlreadyLoading) {
            latestMessageForEachContact.clear();
            messageList.clear();
            downloadConversations(false, searchString);
        }
        if (swipeLayout != null) {
            swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                public void onRefresh() {
                    SyncMessages syncMessages = new SyncMessages();
                    syncMessages.execute();
                }
            });
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (recyclerAdapter != null) {
                    recyclerAdapter.contactImageLoader.setPauseWork(newState == RecyclerView.SCROLL_STATE_DRAGGING);
                    recyclerAdapter.channelImageLoader.setPauseWork(newState == RecyclerView.SCROLL_STATE_DRAGGING);
                }

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {
                    visibleItemCount = linearLayoutManager.getChildCount();
                    totalItemCount = linearLayoutManager.getItemCount();
                    pastVisiblesItems = linearLayoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        if (totalItemCount > previousTotalItemCount) {
                            if (!messageList.isEmpty()) {
                                loading = false;
                            }
                            previousTotalItemCount = totalItemCount;
                        }
                    }

                    if ((totalItemCount - visibleItemCount) == 0) {
                        return;
                    }
                    if (totalItemCount <= 5) {
                        return;
                    }
                    if (loadMore && !loading && (visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        DownloadConversation downloadConversation = new DownloadConversation(getContext(), false, listIndex);
                        downloadConversation.setTextViewWeakReference(emptyTextView);
                        downloadConversation.setSwipeRefreshLayoutWeakReference(swipeLayout);
                        downloadConversation.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        loading = true;
                        loadMore = false;
                    }
                }
            }
        });
    }

    public void downloadConversations(boolean showInstruction, String searchString) {
        downloadConversation = new DownloadConversation(getContext(), true, 1, searchString);
        downloadConversation.setTextViewWeakReference(emptyTextView);
        downloadConversation.setSwipeRefreshLayoutWeakReference(swipeLayout);
        downloadConversation.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        if (recyclerAdapter != null) {
            recyclerAdapter.searchString = searchString;
        }
    }

    public void updateLastSeenStatus(final String userId) {

        if (customizationSettings == null) {
            return;
        }

        if (!customizationSettings.isOnlineStatusMasterList()) {
            return;
        }
        if (getActivity() == null) {
            return;
        }
        if (MobiComUserPreference.getInstance(getContext()).getUserId().equals(userId)) {
            return;
        }
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (recyclerAdapter != null) {
                        recyclerAdapter.notifyDataSetChanged();
                    }
                } catch (Exception ex) {
                    Utils.printLog(getActivity(), "AL", "Exception while updating online status.");
                }
            }
        });
    }

    public void updateConversationRead(final String currentId, final boolean isGroup) {
        if (getActivity() == null) {
            return;
        }
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Message message = null;
                    if (isGroup) {
                        message = latestMessageForEachContact.get(ConversationUIService.GROUP + currentId);
                    } else {
                        message = latestMessageForEachContact.get(currentId);
                    }

                    if (message != null) {
                        int index = messageList.indexOf(message);
                        if (index != -1 && recyclerView != null) {
                            View view = recyclerView.getChildAt(index - linearLayoutManager.findFirstVisibleItemPosition());
                            if (view != null) {
                                TextView unreadCountTextView = (TextView) view.findViewById(R.id.unreadSmsCount);
                                unreadCountTextView.setVisibility(View.GONE);
                            }
                        }
                    }
                } catch (Exception ex) {
                    Utils.printLog(getActivity(), "AL", "Exception while updating Unread count...");
                }
            }
        });
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        this.searchString = newText;
        if (TextUtils.isEmpty(newText)) {
            downloadConversations(false, null);
        } else {
            downloadConversations(false, newText);
        }
        return true;
    }

    public class DownloadConversation extends AsyncTask<Void, Integer, Long> {

        private int firstVisibleItem;
        private boolean initial;
        private List<Message> nextMessageList = new ArrayList<Message>();
        private boolean loadMoreMessages;
        private String searchString;
        private WeakReference<Context> context;
        private WeakReference<SwipeRefreshLayout> swipeRefreshLayoutWeakReference;
        private WeakReference<TextView> textViewWeakReference;

        public void setTextViewWeakReference(TextView emptyTextViewWeakReference) {
            this.textViewWeakReference = new WeakReference<TextView>(emptyTextViewWeakReference);
        }


        public void setSwipeRefreshLayoutWeakReference(SwipeRefreshLayout swipeRefreshLayout) {
            this.swipeRefreshLayoutWeakReference = new WeakReference<SwipeRefreshLayout>(swipeRefreshLayout);
        }


        public DownloadConversation(Context context, boolean initial, int firstVisibleItem, String searchString) {
            this.context = new WeakReference<>(context);
            this.initial = initial;
            this.firstVisibleItem = firstVisibleItem;
            this.searchString = searchString;
        }

        public DownloadConversation(Context context, boolean initial, int firstVisibleItem) {
            this(context, initial, firstVisibleItem, null);
            loadMoreMessages = true;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isAlreadyLoading = true;
            if (loadMoreMessages) {
                if (!messageList.contains(null)) {
                    messageList.add(null);
                }
                if (recyclerAdapter != null) {
                    recyclerAdapter.notifyItemInserted(messageList.size() - 1);
                }
            } else {
                if (swipeRefreshLayoutWeakReference != null) {
                    final SwipeRefreshLayout swipeRefreshLayout = swipeRefreshLayoutWeakReference.get();
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setEnabled(true);
                        swipeRefreshLayout.post(new Runnable() {
                            @Override
                            public void run() {
                                swipeRefreshLayout.setRefreshing(true);
                            }
                        });
                    }
                }
            }
        }

        protected Long doInBackground(Void... voids) {
            if (initial) {
                nextMessageList = syncCallService.getLatestMessagesGroupByPeople(searchString);
            } else if (!messageList.isEmpty()) {
                listIndex = firstVisibleItem;
                Long createdAt;
                if (messageList.size() >= 2 && messageList.contains(null)) {
                    createdAt = messageList.isEmpty() ? null : messageList.get(messageList.size() - 2).getCreatedAtTime();
                } else {
                    createdAt = messageList.isEmpty() ? null : messageList.get(messageList.size() - 1).getCreatedAtTime();
                }
                nextMessageList = syncCallService.getLatestMessagesGroupByPeople(createdAt, searchString, MobiComUserPreference.getInstance(AppContextService.getContextFromWeak(context)).getParentGroupKey());
            }

            return 0L;
        }

        protected void onPostExecute(Long result) {
            if (!loadMoreMessages) {
                if (swipeRefreshLayoutWeakReference != null) {
                    final SwipeRefreshLayout swipeRefreshLayout = swipeRefreshLayoutWeakReference.get();
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setEnabled(true);
                        swipeRefreshLayout.post(new Runnable() {
                            @Override
                            public void run() {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                }
            }

            if (!TextUtils.isEmpty(searchString)) {
                messageList.clear();
                latestMessageForEachContact.clear();
            }

            for (Message currentMessage : nextMessageList) {
                if (currentMessage.isSentToMany()) {
                    continue;
                }
                Message recentSms;
                if (currentMessage.getGroupId() != null) {
                    recentSms = latestMessageForEachContact.get(ConversationUIService.GROUP + currentMessage.getGroupId());
                } else {
                    recentSms = latestMessageForEachContact.get(currentMessage.getContactIds());
                }

                if (recentSms != null) {
                    if (currentMessage.getCreatedAtTime() >= recentSms.getCreatedAtTime()) {
                        if (currentMessage.getGroupId() != null) {
                            latestMessageForEachContact.put(ConversationUIService.GROUP + currentMessage.getGroupId(), currentMessage);
                        } else {
                            latestMessageForEachContact.put(currentMessage.getContactIds(), currentMessage);
                        }
                        messageList.remove(recentSms);
                        messageList.add(currentMessage);
                    }
                } else {
                    if (currentMessage.getGroupId() != null) {
                        latestMessageForEachContact.put(ConversationUIService.GROUP + currentMessage.getGroupId(), currentMessage);
                    } else {
                        latestMessageForEachContact.put(currentMessage.getContactIds(), currentMessage);
                    }

                    messageList.add(currentMessage);
                }
            }
            if (loadMoreMessages) {
                if (messageList.contains(null)) {
                    messageList.remove(null);
                }
            }

            if (recyclerAdapter != null) {
                recyclerAdapter.notifyDataSetChanged();
            }

            if (initial) {
                if (textViewWeakReference != null) {
                    TextView emptyTextView = textViewWeakReference.get();
                    if (emptyTextView != null) {
                        emptyTextView.setVisibility(messageList.isEmpty() ? View.VISIBLE : View.GONE);
                        if (!TextUtils.isEmpty(searchString) && messageList.isEmpty()) {
                            emptyTextView.setText(!TextUtils.isEmpty(customizationSettings.getNoSearchFoundForChatMessages()) ? customizationSettings.getNoSearchFoundForChatMessages() : getResources().getString(R.string.search_not_found_for_messages));
                        } else if (TextUtils.isEmpty(searchString) && messageList.isEmpty()) {
                            emptyTextView.setText(!TextUtils.isEmpty(customizationSettings.getNoConversationLabel()) ? customizationSettings.getNoConversationLabel() : getResources().getString(R.string.no_conversation));
                        }
                    }
                }
                if (!messageList.isEmpty()) {
                    if (recyclerView != null) {
                        if (recyclerAdapter != null && recyclerAdapter.getItemCount() > BroadcastService.lastIndexForChats) {
                            recyclerView.scrollToPosition(BroadcastService.lastIndexForChats);
                            BroadcastService.lastIndexForChats = 0;
                        } else {
                            recyclerView.scrollToPosition(0);
                        }
                    }
                }
            } else {
                if (!loadMoreMessages && recyclerView != null) {
                    recyclerView.scrollToPosition(firstVisibleItem);
                }
            }
            if (!nextMessageList.isEmpty()) {
                loadMore = true;
            }
            isAlreadyLoading = false;
        }
    }

    private class SyncMessages extends AsyncTask<Void, Integer, Long> {
        SyncMessages() {
        }

        @Override
        protected Long doInBackground(Void... params) {
            if (syncCallService != null) {
                syncCallService.syncMessages(null);
            }
            return 1l;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            if (swipeLayout != null) {
                swipeLayout.setRefreshing(false);
            }
        }
    }
}
