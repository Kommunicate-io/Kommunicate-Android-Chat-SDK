package com.applozic.mobicomkit.uiwidgets.conversation.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;
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

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.SyncCallService;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.DimensionsUtils;
import com.applozic.mobicomkit.uiwidgets.KmLinearLayoutManager;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.MobiComKitActivityInterface;
import com.applozic.mobicomkit.uiwidgets.conversation.adapter.QuickConversationAdapter;
import com.applozic.mobicomkit.uiwidgets.kommunicate.KmPrefSettings;
import com.applozic.mobicomkit.uiwidgets.kommunicate.utils.KmHelper;
import com.applozic.mobicomkit.uiwidgets.kommunicate.utils.KmThemeHelper;
import com.applozic.mobicomkit.uiwidgets.kommunicate.views.KmToast;
import com.applozic.mobicomkit.uiwidgets.uilistener.KmActionCallback;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.commons.core.utils.DateUtils;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.SearchListFragment;
import com.applozic.mobicommons.people.contact.Contact;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    AlCustomizationSettings alCustomizationSettings;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String jsonString = FileUtils.loadSettingsJsonFile(ApplozicService.getContext(getContext()));
        if (!TextUtils.isEmpty(jsonString)) {
            alCustomizationSettings = (AlCustomizationSettings) GsonUtils.getObjectFromJson(jsonString, AlCustomizationSettings.class);
        } else {
            alCustomizationSettings = new AlCustomizationSettings();
        }
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
        View list = inflater.inflate(R.layout.mobicom_message_list, container, false);

        recyclerView = (RecyclerView) list.findViewById(R.id.messageList);
        recyclerView.setBackgroundColor(getResources().getColor(R.color.conversation_list_all_background));
        recyclerView.setPadding(0, 0, 0, (int) DimensionsUtils.convertDpToPixel(95));


        if (messageList != null && !messageList.contains(null)) {
            messageList.add(null);
        }
        recyclerAdapter = new QuickConversationAdapter(getContext(), messageList, null);
        recyclerAdapter.setAlCustomizationSettings(alCustomizationSettings);

        faqButtonLayout = getActivity().findViewById(R.id.faqButtonLayout);

        if (alCustomizationSettings.isFaqOptionEnabled() || KmPrefSettings.getInstance(getContext()).isFaqOptionEnabled() || alCustomizationSettings.isFaqOptionEnabled(1)) {
            faqButtonLayout.setVisibility(View.VISIBLE);
            TextView textView = faqButtonLayout.findViewById(R.id.kmFaqOption);

            if (textView != null) {
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ConversationActivity.openFaq(getActivity(), new KmClientService(getContext()).getHelpCenterUrl());
                    }
                });
            }
        } else {
            faqButtonLayout.setVisibility(View.GONE);
        }

        linearLayoutManager = new KmLinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setAdapter(recyclerAdapter);
        toolbar = (Toolbar) getActivity().findViewById(R.id.my_toolbar);
        toolbar.setClickable(false);
        RelativeLayout toolbarCustomLayout = toolbar.findViewById(R.id.custom_toolbar_root_layout);
        if (toolbarCustomLayout != null) {
            toolbarCustomLayout.setVisibility(View.GONE);
        }

        loading = true;
        LinearLayout individualMessageSendLayout = (LinearLayout) list.findViewById(R.id.individual_message_send_layout);
        LinearLayout extendedSendingOptionLayout = (LinearLayout) list.findViewById(R.id.extended_sending_option_layout);

        startNewConv = list.findViewById(R.id.start_new_conversation);
        KmUtils.setGradientSolidColor(startNewConv, KmThemeHelper.getInstance(getContext(), alCustomizationSettings).getPrimaryColor());

        if (alCustomizationSettings != null && alCustomizationSettings.isShowStartNewConversation() && User.RoleType.USER_ROLE.getValue().equals(MobiComUserPreference.getInstance(getContext()).getUserRoleType())) {
            startNewConv.setVisibility(View.VISIBLE);
        }

        startNewConv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ApplozicService.getContext(getContext()) instanceof KmActionCallback) {
                    ((KmActionCallback) ApplozicService.getContext(getContext())).onReceive(getContext(), null, "startNewChat");
                } else {
                    KmHelper.setStartNewChat(getActivity());
                }
                LocalBroadcastManager.getInstance(getContext()).sendBroadcastSync(new Intent("KmStartNewConversation"));
            }
        });

        individualMessageSendLayout.setVisibility(View.GONE);
        extendedSendingOptionLayout.setVisibility(View.GONE);

        emptyTextView = (TextView) list.findViewById(R.id.noConversations);
        emptyTextView.setTextColor(Color.parseColor(alCustomizationSettings.getNoConversationLabelTextColor().trim()));

        swipeLayout = (SwipeRefreshLayout) list.findViewById(R.id.swipe_container);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        recyclerView.setLongClickable(true);
        registerForContextMenu(recyclerView);

        return list;
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if (alCustomizationSettings.isRefreshOption()) {
            menu.findItem(R.id.refresh).setVisible(true);
        }
        if (alCustomizationSettings.isProfileOption()) {
            menu.findItem(R.id.applozicUserProfile).setVisible(true);
        }
        if (alCustomizationSettings.isMessageSearchOption()) {
            menu.findItem(R.id.menu_search).setVisible(true);
        }
        if (alCustomizationSettings.isLogoutOption()) {
            menu.findItem(R.id.logout).setVisible(true);
        }
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
                emptyTextView.setText(!TextUtils.isEmpty(alCustomizationSettings.getNoConversationLabel()) ? alCustomizationSettings.getNoConversationLabel() : ApplozicService.getContext(context).getResources().getString(R.string.no_conversation));
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
        if ("success".equals(response)) {
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
                KmToast.error(ApplozicService.getContext(getContext()), ApplozicService.getContext(getContext()).getString(R.string.you_need_network_access_for_delete), Toast.LENGTH_SHORT).show();
            } else {
                KmToast.error(ApplozicService.getContext(getContext()), ApplozicService.getContext(getContext()).getString(R.string.delete_conversation_failed), Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void checkForEmptyConversations() {
        boolean isLoadingConversation = (downloadConversation != null && downloadConversation.getStatus() == AsyncTask.Status.RUNNING);
        if (latestMessageForEachContact.isEmpty() && !isLoadingConversation) {
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setText(!TextUtils.isEmpty(alCustomizationSettings.getNoConversationLabel()) ? alCustomizationSettings.getNoConversationLabel() : ApplozicService.getContext(getContext()).getResources().getString(R.string.no_conversation));
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
        toolbar.setTitle(ApplozicService.getContext(getContext()).getResources().getString(R.string.conversations));
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

        if (alCustomizationSettings == null) {
            return;
        }

        if (!alCustomizationSettings.isOnlineStatusMasterList()) {
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
                nextMessageList = syncCallService.getLatestMessagesGroupByPeople(createdAt, searchString, MobiComUserPreference.getInstance(ApplozicService.getContextFromWeak(context)).getParentGroupKey());
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
                            emptyTextView.setText(!TextUtils.isEmpty(alCustomizationSettings.getNoSearchFoundForChatMessages()) ? alCustomizationSettings.getNoSearchFoundForChatMessages() : getResources().getString(R.string.search_not_found_for_messages));
                        } else if (TextUtils.isEmpty(searchString) && messageList.isEmpty()) {
                            emptyTextView.setText(!TextUtils.isEmpty(alCustomizationSettings.getNoConversationLabel()) ? alCustomizationSettings.getNoConversationLabel() : getResources().getString(R.string.no_conversation));
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
