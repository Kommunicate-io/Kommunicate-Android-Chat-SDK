package com.applozic.mobicomkit.uiwidgets.kommunicate.views;

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

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.KmConversationStatus;
import com.applozic.mobicomkit.uiwidgets.conversation.KmResolve;
import com.applozic.mobicomkit.uiwidgets.conversation.adapter.KmConversationStatusListAdapter;
import com.applozic.mobicomkit.uiwidgets.databinding.KmConversationStatusListLayoutBinding;
import com.applozic.mobicomkit.uiwidgets.kommunicate.callbacks.KmClickHandler;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;

import java.util.ArrayList;
import java.util.List;


public class KmBottomSlideFragment extends Fragment implements KmClickHandler<KmResolve> {
    private static final String TAG = "KmBottomSlideFragment";
    private int status;
    private Channel channel;

    public static KmBottomSlideFragment newInstance(int status, Channel channel) {
        KmBottomSlideFragment bottomSlideFragment = new KmBottomSlideFragment();
        Bundle args = new Bundle();
        args.putInt(Channel.CONVERSATION_STATUS, status);
        args.putString(ConversationUIService.GROUP, GsonUtils.getJsonFromObject(channel, Channel.class));
        bottomSlideFragment.setArguments(args);
        return bottomSlideFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            status = bundle.getInt(Channel.CONVERSATION_STATUS, 0);
            channel = (Channel) GsonUtils.getObjectFromJson(bundle.getString(ConversationUIService.GROUP), Channel.class);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.km_conversation_status_list_layout, container, false);

        KmConversationStatusListLayoutBinding binding = DataBindingUtil.bind(view.findViewById(R.id.kmStatusListLayout));

        if (binding != null) {
            binding.setBottomSlideFragment(this);
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
        statusList.add(new KmResolve(3, KmConversationStatus.MARK_AS_SPAM, status == 3));
        return statusList;
    }

    @Override
    public void onItemClicked(View view, KmResolve data) {
        KmConversationStatus.updateConversationStatus(data, channel);
        dismissFragment();
    }

    public void dismissFragment() {
        if (getFragmentManager() != null) {
            getFragmentManager().popBackStackImmediate();
        }
    }
}
