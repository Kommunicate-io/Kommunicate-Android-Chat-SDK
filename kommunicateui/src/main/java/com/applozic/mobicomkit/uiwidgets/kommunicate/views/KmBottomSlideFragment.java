package com.applozic.mobicomkit.uiwidgets.kommunicate.views;

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
import com.applozic.mobicomkit.feed.GroupInfoUpdate;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.async.AlChannelUpdateTask;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.KmConversationStatus;
import com.applozic.mobicomkit.uiwidgets.conversation.KmResolve;
import com.applozic.mobicomkit.uiwidgets.conversation.adapter.KmConversationStatusListAdapter;
import com.applozic.mobicomkit.uiwidgets.databinding.KmConversationStatusListLayoutBinding;
import com.applozic.mobicomkit.uiwidgets.kommunicate.callbacks.KmClickHandler;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KmBottomSlideFragment extends Fragment implements KmClickHandler<KmResolve> {
    private static final String TAG = "KmBottomSlideFragment";
    private int status;
    private Channel channel;
    private KmClickHandler<KmResolve> kmClickHandler;
    private static final String BADGE_COUNT = "BADGE_COUNT";
    private static final String NO_ALERT = "NO_ALERT";

    public static KmBottomSlideFragment newInstance(int status, Channel channel) {
        KmBottomSlideFragment bottomSlideFragment = new KmBottomSlideFragment();
        Bundle args = new Bundle();
        args.putInt(Channel.CONVERSATION_STATUS, status);
        args.putString(ConversationUIService.GROUP, GsonUtils.getJsonFromObject(channel, Channel.class));
        bottomSlideFragment.setArguments(args);
        return bottomSlideFragment;
    }

    private void onAttachToParentFragment(Fragment fragment) {
        try {
            kmClickHandler = (KmClickHandler<KmResolve>) fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException(fragment.toString() + " must implement KmClickHandler");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            status = bundle.getInt(Channel.CONVERSATION_STATUS, 0);
            channel = (Channel) GsonUtils.getObjectFromJson(bundle.getString(ConversationUIService.GROUP), Channel.class);
        }
        onAttachToParentFragment(getParentFragment());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.km_conversation_status_list_layout, container, false);

        KmConversationStatusListLayoutBinding binding = DataBindingUtil.bind(view.findViewById(R.id.kmStatusListLayout));

        if (binding != null) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            layoutManager.setOrientation(RecyclerView.VERTICAL);
            binding.statusRecyclerView.setLayoutManager(layoutManager);
            KmConversationStatusListAdapter adapter = new KmConversationStatusListAdapter(getStatusList(), this);
            binding.statusRecyclerView.setAdapter(adapter);
        }
        return view;
    }

    public static String getFragTag() {
        return TAG;
    }

    public List<KmResolve> getStatusList() {
        List<KmResolve> statusList = new ArrayList<>();
        statusList.add(new KmResolve(0, status != 2 && status != 3 && status != 4));
        statusList.add(new KmResolve(2, status == 2));
        statusList.add(new KmResolve(3, KmConversationStatus.MARK_AS_SPAM, status == 3));
        statusList.add(new KmResolve(4, KmConversationStatus.MARK_AS_DUPLICATE, status == 4));

        return statusList;
    }

    private void updateConversationStatus(final KmResolve data) {
        if (channel != null) {
            Map<String, String> metadata = new HashMap<>();

            metadata.put(Channel.CONVERSATION_STATUS, String.valueOf(KmConversationStatus.getStatusFromName(data.getStatusName())));
            channel.setMetadata(metadata);

            GroupInfoUpdate groupInfoUpdate = new GroupInfoUpdate(metadata, channel.getKey());

            new AlChannelUpdateTask(getContext(), groupInfoUpdate, new AlChannelUpdateTask.AlChannelUpdateListener() {
                @Override
                public void onSuccess(Context context) {
                    removeCurrentFragment();
                    sendConversationStatusUpdateMessage(data);
                    Utils.printLog(context, TAG, "Conversation status update success");
                }

                @Override
                public void onFailure(Context context) {
                    Utils.printLog(context, TAG, "Conversation status update failed");
                }
            }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void sendConversationStatusUpdateMessage(KmResolve data) {
        if (channel != null) {
            Map<String, String> metadata = new HashMap<>();
            metadata.put(Message.KM_STATUS, data.getStatusName());
            metadata.put(Message.KM_SKIP_BOT, "true");
            metadata.put(NO_ALERT, "false");
            metadata.put(BADGE_COUNT, "false");
            metadata.put(Message.MetaDataType.KEY.getValue(), Message.MetaDataType.ARCHIVE.getValue());

            new MessageBuilder(getContext())
                    .setContentType(Message.ContentType.CHANNEL_CUSTOM_MESSAGE.getValue())
                    .setMessage(Utils.getString(getContext(), R.string.km_change_status_to_message) + " " + KmConversationStatus.getStatus(KmConversationStatus.getStatusFromName(data.getStatusName())))
                    .setGroupId(channel.getKey())
                    .setMetadata(metadata)
                    .send();
            if (kmClickHandler != null) {
                kmClickHandler.onItemClicked(null, data);
            }
        }
    }

    @Override
    public void onItemClicked(View view, KmResolve data) {
        updateConversationStatus(data);
    }

    public void removeCurrentFragment() {
        if (getFragmentManager() != null) {
            getFragmentManager().popBackStack();
        }
    }
}
