package com.applozic.mobicomkit.uiwidgets.conversation.fragment;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.applozic.mobicomkit.api.conversation.MessageIntentService;
import com.applozic.mobicomkit.api.conversation.MobiComConversationService;
import com.applozic.mobicomkit.api.conversation.SyncCallService;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.kommunicate.views.KmToast;
import com.applozic.mobicommons.commons.core.utils.LocationUtils;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.people.SearchListFragment;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;

import io.kommunicate.utils.KmConstants;

public class ConversationFragment extends MobiComConversationFragment implements SearchListFragment {

    private static final String TAG = "ConversationFragment";
    protected static final String CONTACT = "CONTACT";
    protected static final String CHANNEL = "CHANNEL";
    protected static final String CONVERSATION_ID = "CONVERSATION_ID";
    protected static final String SEARCH_STRING = "SEARCH_STRING";
    protected InputMethodManager inputMethodManager;
    protected Bundle bundle;

    public static ConversationFragment newInstance(Contact contact, Channel channel, Integer conversationId, String searchString, String messageSearchString, String preFilledMessage) {
        ConversationFragment f = new ConversationFragment();
        Bundle args = new Bundle();
        if (contact != null) {
            args.putSerializable(CONTACT, contact);
        }
        if (channel != null) {
            args.putSerializable(CHANNEL, channel);
        }
        if (conversationId != null) {
            args.putInt(CONVERSATION_ID, conversationId);
        }
        if (!TextUtils.isEmpty(preFilledMessage)) {
            args.putString(KmConstants.KM_PREFILLED_MESSAGE, preFilledMessage);
        }
        args.putString(SEARCH_STRING, searchString);
        args.putString(ConversationUIService.MESSAGE_SEARCH_STRING, messageSearchString);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.messageIntentClass = MessageIntentService.class;
        bundle = getArguments();
        if (bundle != null) {
            contact = (Contact) bundle.getSerializable(CONTACT);
            channel = (Channel) bundle.getSerializable(CHANNEL);
            currentConversationId = bundle.getInt(CONVERSATION_ID);
            searchString = bundle.getString(SEARCH_STRING);
            messageSearchString = bundle.getString(ConversationUIService.MESSAGE_SEARCH_STRING);
            preFilledMessage = bundle.getString(KmConstants.KM_PREFILLED_MESSAGE);
            if (searchString != null) {
                SyncCallService.refreshView = true;
            }
        }
    }

    public void attachLocation(Location currentLocation) {
        String address = LocationUtils.getAddress(getActivity(), currentLocation);
        if (!TextUtils.isEmpty(address)) {
            address = "Address: " + address + "\n";
        } else {
            address = "";
        }
        this.messageEditText.setText(address + "http://maps.google.com/?q=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.title = getResources().getString(R.string.chats);
        this.conversationService = new MobiComConversationService(getActivity());
        hideExtendedSendingOptionLayout = true;

        View view = super.onCreateView(inflater, container, savedInstanceState);

        if (alCustomizationSettings.isHideAttachmentButton()) {

            attachButton.setVisibility(View.GONE);
            messageEditText.setPadding(20, 0, 0, 0);
        }
        sendType.setSelection(1);

        messageEditText.setHint(R.string.enter_message_hint);

        if (!TextUtils.isEmpty(preFilledMessage)) {
            messageEditText.setText(preFilledMessage);
        }

        multimediaPopupGrid.setVisibility(View.GONE);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.secret_message_timer_array, R.layout.mobiframework_custom_spinner);
        adapter.setDropDownViewResource(R.layout.mobiframework_custom_spinner);

        inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);

        messageEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                multimediaPopupGrid.setVisibility(View.GONE);
                emoticonsFrameLayout.setVisibility(View.GONE);
            }
        });


        attachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emoticonsFrameLayout.setVisibility(View.GONE);
                if (contact != null && !contact.isBlocked() || channel != null) {
                    if (attachmentLayout.getVisibility() == View.VISIBLE) {
                        KmToast.error(getActivity(), R.string.select_file_count_limit, Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                if (channel != null) {
                    if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                        String userId = ChannelService.getInstance(getActivity()).getGroupOfTwoReceiverUserId(channel.getKey());
                        if (!TextUtils.isEmpty(userId)) {
                            Contact withUserContact = appContactService.getContactById(userId);
                            if (withUserContact.isBlocked()) {
                                userBlockDialog(false, withUserContact, true);
                            }
                        }
                    }
                } else if (contact != null) {
                    if (contact.isBlocked()) {
                        userBlockDialog(false, contact, false);
                    }
                }
            }
        });
        return view;
    }

    @Override
    protected void processMobiTexterUserCheck() {

    }

    public void handleAttachmentToggle() {
        if ((multimediaPopupGrid.getVisibility() == View.VISIBLE)) {
            multimediaPopupGrid.setVisibility(View.GONE);
        } else if (emoticonsFrameLayout.getVisibility() == View.VISIBLE) {
            emoticonsFrameLayout.setVisibility(View.GONE);
        }
    }

    public boolean isAttachmentOptionsOpen() {
        return (multimediaPopupGrid.getVisibility() == View.VISIBLE || emoticonsFrameLayout.getVisibility() == View.VISIBLE);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            conversationAdapter.getFilter().filter(null);
        } else {
            conversationAdapter.getFilter().filter(newText);
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStartLoading(boolean loadingStarted) {

    }

    public void reload() {

        try {
            StringBuffer stringBufferTitle = new StringBuffer();
            if (contact != null) {
                Contact updatedInfoContact = appContactService.getContactById(contact.getContactIds());
                if (updatedInfoContact.isDeleted()) {
                    Utils.toggleSoftKeyBoard(getActivity(), true);
                    bottomlayoutTextView.setText(R.string.user_has_been_deleted_text);
                    userNotAbleToChatLayout.setVisibility(View.VISIBLE);
                    individualMessageSendLayout.setVisibility(View.GONE);
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("");
                }
                if (updatedInfoContact != null && (!TextUtils.isEmpty(contact.getDisplayName())) && (!contact.getDisplayName().equals(updatedInfoContact.getDisplayName()))) {
                    stringBufferTitle.append(updatedInfoContact.getDisplayName());
                }
            } else if (channel != null) {
                if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                    String userId = ChannelService.getInstance(getActivity()).getGroupOfTwoReceiverUserId(channel.getKey());
                    if (!TextUtils.isEmpty(userId)) {
                        Contact withUserContact = appContactService.getContactById(userId);
                        if (withUserContact != null && (!TextUtils.isEmpty(contact.getDisplayName())) && (!contact.getDisplayName().equals(withUserContact.getDisplayName()))) {
                            stringBufferTitle.append(withUserContact.getDisplayName());
                        }
                    }
                }
            }
            if (!TextUtils.isEmpty(stringBufferTitle)) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(stringBufferTitle.toString());
            }
            conversationAdapter.refreshContactData();
            conversationAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUserActivated(boolean isActivated) {

    }

    @Override
    public void onGroupMute(Integer groupId) {

    }
}