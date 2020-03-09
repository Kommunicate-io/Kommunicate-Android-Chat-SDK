package com.applozic.mobicomkit.uiwidgets.conversation.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MessageBuilder;
import com.applozic.mobicomkit.api.notification.NotificationService;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.feed.GroupInfoUpdate;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.async.AlChannelUpdateTask;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.adapter.KmAssigneeListAdapter;
import com.applozic.mobicomkit.uiwidgets.databinding.KmUserListFragmentBinding;

import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.users.KmContact;

public class KmUserListFragment extends Fragment implements KmCallback {
    private static final String TAG = "KmUserListFragment";
    private static final String IS_AGENT_TAB = "IS_AGENT_TAB";
    private KmAssigneeListAdapter assigneeListAdapter;
    private KmUserListFragmentBinding binding;
    private int channelKey;
    private boolean isAgentTab;

    public static KmUserListFragment newInstance(String assigneeId, boolean isAgentTab, int channelId, List<KmContact> userList) {
        KmUserListFragment fragment = new KmUserListFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Channel.CONVERSATION_ASSIGNEE, assigneeId);
        bundle.putBoolean(IS_AGENT_TAB, isAgentTab);
        bundle.putInt(ConversationUIService.GROUP_ID, channelId);
        if (userList != null && !userList.isEmpty()) {
            bundle.putString(TAG, GsonUtils.getJsonFromObject(userList.toArray(), KmContact[].class));
        }
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isAgentTab = getArguments().getBoolean(IS_AGENT_TAB);
            channelKey = getArguments().getInt(ConversationUIService.GROUP_ID);
        }
    }

    public void showEmptyListText(boolean show, boolean isForSearch) {
        if (binding != null) {
            binding.kmUserListRecycler.setVisibility(show ? View.GONE : View.VISIBLE);
            binding.kmEmptyListMessage.setVisibility(show ? View.VISIBLE : View.GONE);
            binding.kmLoadingProgressBar.setVisibility(View.GONE);
            binding.kmEmptyListMessage.setText(isForSearch ? getNoSearchResultString() : getEmptyTextString());
        }
    }

    private void showInitialLoading(boolean show) {
        if (binding != null) {
            binding.kmLoadingProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            binding.kmEmptyListMessage.setVisibility(View.GONE);
        }
    }

    public void setSearchText(String searchText) {
        if (assigneeListAdapter != null) {
            assigneeListAdapter.getFilter().filter(searchText);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.km_user_list_fragment, container, false);

        binding = DataBindingUtil.bind(view);

        if (getArguments() != null && binding != null) {
            showInitialLoading(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            layoutManager.setOrientation(RecyclerView.VERTICAL);
            binding.kmUserListRecycler.setLayoutManager(layoutManager);
            assigneeListAdapter = new KmAssigneeListAdapter(getArguments().getString(Channel.CONVERSATION_ASSIGNEE, null), this);
            if (getArguments().containsKey(TAG)) {
                addUserList(Arrays.asList((KmContact[]) GsonUtils.getObjectFromJson(getArguments().getString(TAG), KmContact[].class)));
            }
            binding.kmUserListRecycler.setAdapter(assigneeListAdapter);
        }
        return view;
    }

    public void addUserList(List<KmContact> userList) {
        showInitialLoading(false);

        if (assigneeListAdapter != null) {
            assigneeListAdapter.addAssigneeList(userList);

            if (userList == null || userList.isEmpty()) {
                showEmptyListText(true, false);
            }
        }
    }

    private String getEmptyTextString() {
        return Utils.getString(getContext(), isAgentTab ? R.string.km_empty_agent_list_message : R.string.km_empty_bot_list_message);
    }

    private String getNoSearchResultString() {
        return Utils.getString(getContext(), isAgentTab ? R.string.km_no_agents_found : R.string.km_no_bots_found);
    }

    @Override
    public void onSuccess(Object message) {
        if (message instanceof KmContact) {
            updateConversationAssignee((KmContact) message);
            dismissFragment();  //Dismiss the assignee list fragment
            dismissFragment();  //Dismiss the bottom slide fragment
        } else {
            Boolean showEmptySearchText = (Boolean) message;
            showEmptyListText(showEmptySearchText, true);
        }
    }

    @Override
    public void onFailure(Object error) {

    }

    private void updateConversationAssignee(final KmContact contact) {
        if (channelKey > 0) {

            Channel channel = ChannelService.getInstance(getContext()).getChannel(channelKey);

            if (channel != null) {
                Map<String, String> metadata = channel.getMetadata();

                if (metadata == null) {
                    metadata = new HashMap<>();
                }

                metadata.put(Channel.CONVERSATION_ASSIGNEE, contact.getContactIds());
                channel.setMetadata(metadata);

                GroupInfoUpdate groupInfoUpdate = new GroupInfoUpdate(metadata, channel.getKey());

                new AlChannelUpdateTask(ApplozicService.getAppContext(), groupInfoUpdate, new AlChannelUpdateTask.AlChannelUpdateListener() {
                    @Override
                    public void onSuccess(Context context) {
                        sendConversationAssigneeUpdateMessage(contact);
                    }

                    @Override
                    public void onFailure(Context context) {

                    }
                }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    private void sendConversationAssigneeUpdateMessage(KmContact contact) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put(Message.KM_ASSIGN, contact.getContactIds());
        metadata.put(Message.KM_SKIP_BOT, "true");
        metadata.put(NotificationService.NO_ALERT, "false");
        metadata.put(NotificationService.BADGE_COUNT, "false");
        metadata.put(Message.MetaDataType.KEY.getValue(), Message.MetaDataType.ARCHIVE.getValue());

        new MessageBuilder(getContext())
                .setContentType(Message.ContentType.CHANNEL_CUSTOM_MESSAGE.getValue())
                .setMessage(Utils.getString(getContext(), R.string.km_change_assignee_to_message) + " " + contact.getDisplayName())
                .setGroupId(channelKey)
                .setMetadata(metadata)
                .send();
    }

    public void dismissFragment() {
        if (getFragmentManager() != null) {
            getFragmentManager().popBackStackImmediate();
        }
    }
}
