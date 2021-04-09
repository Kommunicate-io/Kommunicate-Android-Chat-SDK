package com.applozic.mobicomkit.uiwidgets.conversation;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.UserClientService;
import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.api.attachment.FileMeta;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MobiComConversationService;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.async.KmChannelDeleteTask;
import com.applozic.mobicomkit.uiwidgets.async.KmChannelLeaveMember;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.MobiComAttachmentSelectorActivity;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.MobiComKitActivityInterface;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.ConversationFragment;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.MessageInfoFragment;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.MobiComQuickConversationFragment;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.MultimediaOptionFragment;
import com.applozic.mobicomkit.uiwidgets.kommunicate.views.KmToast;
import com.applozic.mobicomkit.uiwidgets.uilistener.KmFragmentGetter;
import com.applozic.mobicommons.commons.core.utils.LocationInfo;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.channel.ChannelUtils;
import com.applozic.mobicommons.people.contact.Contact;

import java.io.File;
import java.util.ArrayList;

import io.kommunicate.utils.KmConstants;

public class ConversationUIService {

    public static final int REQUEST_CODE_CONTACT_GROUP_SELECTION = 1011;
    public static final String CONVERSATION_FRAGMENT = "ConversationFragment";
    public static final String MESSGAE_INFO_FRAGMENT = "messageInfoFagment";
    public static final String USER_PROFILE_FRAMENT = "userProfilefragment";
    public static final String QUICK_CONVERSATION_FRAGMENT = "QuickConversationFragment";
    public static final String FORWARD_MESSAGE = "forwardMessage";
    public static final String CLIENT_GROUP_ID = "clientGroupId";
    public static final String DISPLAY_NAME = "displayName";
    public static final String TAKE_ORDER = "takeOrder";
    public static final String USER_ID = "userId";
    public static final String GROUP_ID = "groupId";
    public static final String GROUP_ID_LIST_CONTACTS = "groupIdListContacts";
    public static final String GROUP_NAME_LIST_CONTACTS = "groupIdNameContacts";
    public static final String GROUP_NAME = "groupName";
    public static final String FIRST_TIME_MTEXTER_FRIEND = "firstTimeMTexterFriend";
    public static final String CONTACT_ID = "contactId";
    public static final String CONTEXT_BASED_CHAT = "contextBasedChat";
    public static final String CONTACT_NUMBER = "contactNumber";
    public static final String APPLICATION_ID = "applicationId";
    public static final String DEFAULT_TEXT = "defaultText";
    public static final String FINAL_PRICE_TEXT = "Final agreed price ";
    public static final String PRODUCT_TOPIC_ID = "topicId";
    public static final String PRODUCT_IMAGE_URL = "productImageUrl";
    public static final String CONTACT = "CONTACT";
    public static final String GROUP = "group-";
    public static final String SUCCESS = "success";
    public static final String SEARCH_STRING = "searchString";
    public static final String CONVERSATION_ID = "CONVERSATION_ID";
    public static final String TOPIC_ID = "TOPIC_ID";
    private static final String TAG = "ConversationUIService";
    public static final String MESSAGE_SEARCH_STRING = "MESSAGE_SEARCH_STRING";
    private FileClientService fileClientService;
    private FragmentActivity fragmentActivity;
    private BaseContactService baseContactService;
    private NotificationManager notificationManager;
    private boolean isActionMessageHidden;

    public ConversationUIService(FragmentActivity fragmentActivity) {
        this.fragmentActivity = fragmentActivity;
        this.baseContactService = new AppContactService(fragmentActivity);
        this.notificationManager = (NotificationManager) fragmentActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        this.fileClientService = new FileClientService(fragmentActivity);
        isActionMessageHidden = ApplozicClient.getInstance(fragmentActivity).isActionMessagesHidden();
    }

    public MobiComQuickConversationFragment getQuickConversationFragment() {

        MobiComQuickConversationFragment quickConversationFragment = (MobiComQuickConversationFragment) UIService.getFragmentByTag(fragmentActivity, QUICK_CONVERSATION_FRAGMENT);

        if (quickConversationFragment == null) {
            quickConversationFragment = new MobiComQuickConversationFragment();
            ConversationActivity.addFragment(fragmentActivity, quickConversationFragment, QUICK_CONVERSATION_FRAGMENT);
        }
        return quickConversationFragment;
    }

    public ConversationFragment getConversationFragment() {

        ConversationFragment conversationFragment = (ConversationFragment) UIService.getFragmentByTag(fragmentActivity, CONVERSATION_FRAGMENT);

        if (conversationFragment == null) {
            Contact contact = ((ConversationActivity) fragmentActivity).getContact();
            Channel channel = ((ConversationActivity) fragmentActivity).getChannel();
            Integer conversationId = ((ConversationActivity) fragmentActivity).getConversationId();
            conversationFragment = getConversationFragment(fragmentActivity, contact, channel, conversationId, null, null, null);
            ConversationActivity.addFragment(fragmentActivity, conversationFragment, CONVERSATION_FRAGMENT);
        }
        return conversationFragment;
    }

    public void openConversationFragment(final Contact contact, final Integer conversationId, final String searchString, final String messageSearchString) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                ConversationFragment conversationFragment = (ConversationFragment) UIService.getFragmentByTag(fragmentActivity, CONVERSATION_FRAGMENT);
                if (conversationFragment == null) {
                    conversationFragment = getConversationFragment(fragmentActivity, contact, null, conversationId, searchString, messageSearchString, null);
                    ((MobiComKitActivityInterface) fragmentActivity).addFragment(conversationFragment);
                } else {
                    MessageInfoFragment messageInfoFragment = (MessageInfoFragment) UIService.getFragmentByTag(fragmentActivity, ConversationUIService.MESSGAE_INFO_FRAGMENT);
                    if (messageInfoFragment != null) {
                        if (fragmentActivity.getSupportFragmentManager() != null) {
                            fragmentActivity.getSupportFragmentManager().popBackStackImmediate();
                        }
                    }
                    conversationFragment.loadConversation(contact, conversationId, messageSearchString);
                }
            }
        });
    }

    public void openConversationFragment(final Channel channel, final Integer conversationId, final String searchString, final String messageSearchString, final String preFilledMessage) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                ConversationFragment conversationFragment = (ConversationFragment) UIService.getFragmentByTag(fragmentActivity, CONVERSATION_FRAGMENT);
                if (conversationFragment == null) {
                    conversationFragment = getConversationFragment(fragmentActivity, null, channel, conversationId, searchString, messageSearchString, preFilledMessage);
                    ((MobiComKitActivityInterface) fragmentActivity).addFragment(conversationFragment);
                } else {
                    MessageInfoFragment messageInfoFragment = (MessageInfoFragment) UIService.getFragmentByTag(fragmentActivity, ConversationUIService.MESSGAE_INFO_FRAGMENT);
                    if (messageInfoFragment != null && fragmentActivity.getSupportFragmentManager() != null) {
                        fragmentActivity.getSupportFragmentManager().popBackStackImmediate();
                    }
                    conversationFragment.loadConversation(channel, conversationId, messageSearchString);
                }
            }
        });
    }

    /**
     * send the the attachment messages.
     *
     * @param attachmentList the list of messages
     * @param messageText    the message text
     */
    public void sendAttachments(ArrayList<Uri> attachmentList, String messageText) {
        for (Uri info : attachmentList) {
            getConversationFragment().sendMessage(messageText, Message.ContentType.ATTACHMENT.getValue(), info.toString());
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        try {
            if ((requestCode == MultimediaOptionFragment.REQUEST_CODE_ATTACH_PHOTO ||
                    requestCode == MultimediaOptionFragment.REQUEST_CODE_TAKE_PHOTO)
                    && resultCode == Activity.RESULT_OK) {
                Uri selectedFileUri = (intent == null ? null : intent.getData());
                File file = null;
                if (selectedFileUri == null) {
                    file = ((ConversationActivity) fragmentActivity).getFileObject();
                    selectedFileUri = ((ConversationActivity) fragmentActivity).getCapturedImageUri();
                }

                if (selectedFileUri != null) {
                    selectedFileUri = ((ConversationActivity) fragmentActivity).getCapturedImageUri();
                    file = ((ConversationActivity) fragmentActivity).getFileObject();
                }
                MediaScannerConnection.scanFile(fragmentActivity,
                        new String[]{file.getAbsolutePath()}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                            }
                        });
                getConversationFragment().loadFile(selectedFileUri, file);
                Utils.printLog(fragmentActivity, TAG, "File uri: " + selectedFileUri);
            }

            if (requestCode == REQUEST_CODE_CONTACT_GROUP_SELECTION && resultCode == Activity.RESULT_OK) {
                checkForStartNewConversation(intent);
            }
            if (requestCode == MultimediaOptionFragment.REQUEST_CODE_CAPTURE_VIDEO_ACTIVITY && resultCode == Activity.RESULT_OK) {

                Uri selectedFilePath = ((ConversationActivity) fragmentActivity).getVideoFileUri();

                File file = ((ConversationActivity) fragmentActivity).getFileObject();

                if (!(file != null && file.exists())) {
                    FileUtils.getLastModifiedFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera/").renameTo(file);
                }

                if (selectedFilePath != null) {
                    getConversationFragment().loadFile(selectedFilePath, file);
                    getConversationFragment().sendMessage("", Message.ContentType.VIDEO_MSG.getValue());
                }
            }

            if (requestCode == MultimediaOptionFragment.REQUEST_MULTI_ATTCAHMENT && resultCode == Activity.RESULT_OK) {
                ArrayList<Uri> attachmentList = intent.getParcelableArrayListExtra(MobiComAttachmentSelectorActivity.MULTISELECT_SELECTED_FILES);
                String messageText = intent.getStringExtra(MobiComAttachmentSelectorActivity.MULTISELECT_MESSAGE);

                //TODO: check performance, we might need to put in each posting in separate thread.
                sendAttachments(attachmentList, messageText);
            }

            if (requestCode == MultimediaOptionFragment.REQUEST_CODE_SEND_LOCATION && resultCode == Activity.RESULT_OK) {
                Double latitude = intent.getDoubleExtra("latitude", 0);
                Double longitude = intent.getDoubleExtra("longitude", 0);
                //TODO: put your location(lat/lon ) in constructor.
                LocationInfo info = new LocationInfo(latitude, longitude);
                String locationInfo = GsonUtils.getJsonFromObject(info, LocationInfo.class);
                sendLocation(locationInfo);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteConversationThread(final Contact contact, final Channel channel) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(fragmentActivity).
                setPositiveButton(R.string.delete_conversation, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new DeleteConversationAsyncTask(new MobiComConversationService(fragmentActivity), contact, channel, null, fragmentActivity).execute();

                    }
                });
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        String name = "";
        if (channel != null) {
            if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                String userId = ChannelService.getInstance(fragmentActivity).getGroupOfTwoReceiverUserId(channel.getKey());
                if (!TextUtils.isEmpty(userId)) {
                    Contact withUserContact = baseContactService.getContactById(userId);
                    name = withUserContact.getDisplayName();
                }
            } else {
                name = ChannelUtils.getChannelTitleName(channel, MobiComUserPreference.getInstance(fragmentActivity).getUserId());
            }
        } else {
            name = contact.getDisplayName();
        }
        alertDialog.setTitle(fragmentActivity.getString(R.string.dialog_delete_conversation_title).replace("[name]", name));
        alertDialog.setMessage(fragmentActivity.getString(R.string.dialog_delete_conversation_confir).replace("[name]", name));
        alertDialog.setCancelable(true);
        alertDialog.create().show();
    }

    public void deleteGroupConversation(final Channel channel) {

        if (!Utils.isInternetAvailable(fragmentActivity)) {
            showToastMessage(fragmentActivity.getString(R.string.you_dont_have_any_network_access_info));
            return;
        }

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(fragmentActivity).
                setPositiveButton(R.string.channel_deleting, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        final ProgressDialog progressDialog = ProgressDialog.show(fragmentActivity, "",
                                fragmentActivity.getString(R.string.deleting_channel_user), true);
                        KmChannelDeleteTask.TaskListener channelDeleteTask = new KmChannelDeleteTask.TaskListener() {
                            @Override
                            public void onSuccess(String response) {
                                Log.i(TAG, "Channel deleted response:" + response);

                            }

                            @Override
                            public void onFailure(String response, Exception exception) {
                                showToastMessage(fragmentActivity.getString(Utils.isInternetAvailable(fragmentActivity) ? R.string.km_server_error : R.string.you_dont_have_any_network_access_info));
                            }

                            @Override
                            public void onCompletion() {
                                if (progressDialog != null && progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }

                            }
                        };
                        KmChannelDeleteTask kmChannelDeleteTask = new KmChannelDeleteTask(fragmentActivity, channelDeleteTask, channel);
                        kmChannelDeleteTask.execute((Void) null);
                    }
                });
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        alertDialog.setMessage(fragmentActivity.getString(R.string.delete_channel_messages_and_channel_info).replace(fragmentActivity.getString(R.string.group_name_info), channel.getName()).replace(fragmentActivity.getString(R.string.groupType_info), Channel.GroupType.BROADCAST.getValue().equals(channel.getType()) ? fragmentActivity.getString(R.string.broadcast_string) : fragmentActivity.getString(R.string.group_string)));
        alertDialog.setCancelable(true);
        alertDialog.create().show();
    }

    public void channelLeaveProcess(final Channel channel) {
        if (!Utils.isInternetAvailable(fragmentActivity)) {
            showToastMessage(fragmentActivity.getString(R.string.you_dont_have_any_network_access_info));
            return;
        }
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(fragmentActivity).
                setPositiveButton(R.string.channel_exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        KmChannelLeaveMember.ChannelLeaveMemberListener applozicLeaveMemberListener = new KmChannelLeaveMember.ChannelLeaveMemberListener() {
                            @Override
                            public void onSuccess(String response, Context context) {
                            }

                            @Override
                            public void onFailure(String response, Exception e, Context context) {
                                showToastMessage(fragmentActivity.getString(Utils.isInternetAvailable(fragmentActivity) ? R.string.km_server_error : R.string.you_dont_have_any_network_access_info));
                            }
                        };
                        KmChannelLeaveMember kmChannelLeaveMember = new KmChannelLeaveMember(fragmentActivity, channel.getKey(), MobiComUserPreference.getInstance(fragmentActivity).getUserId(), applozicLeaveMemberListener);
                        kmChannelLeaveMember.setEnableProgressDialog(true);
                        kmChannelLeaveMember.execute((Void) null);

                    }
                });
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        alertDialog.setMessage(fragmentActivity.getString(R.string.exit_channel_message_info).replace(fragmentActivity.getString(R.string.group_name_info), channel.getName()).replace(fragmentActivity.getString(R.string.groupType_info), Channel.GroupType.BROADCAST.getValue().equals(channel.getType()) ? fragmentActivity.getString(R.string.broadcast_string) : fragmentActivity.getString(R.string.group_string)));
        alertDialog.setCancelable(true);
        alertDialog.create().show();
    }

    public void updateLatestMessage(Message message, String formattedContactNumber) {
        if (!BroadcastService.isQuick()) {
            return;
        }
        getQuickConversationFragment().updateLatestMessage(message, formattedContactNumber);
    }

    public void removeConversation(Message message, String formattedContactNumber) {
        if (!BroadcastService.isQuick()) {
            return;
        }
        getQuickConversationFragment().removeConversation(message, formattedContactNumber);
    }

    public void addMessage(Message message) {
        if (message.isUpdateMessage()) {
            if (!BroadcastService.isQuick()) {
                return;
            }

            MobiComQuickConversationFragment fragment = (MobiComQuickConversationFragment) UIService.getFragmentByTag(fragmentActivity, QUICK_CONVERSATION_FRAGMENT);
            if (fragment != null) {
                if (message.hasHideKey()) {
                    fragment.refreshView();
                } else {
                    fragment.addMessage(message);
                }
            }
        }
    }

    public void updateLastMessage(String keyString, String userId) {
        if (!BroadcastService.isQuick()) {
            return;
        }
        getQuickConversationFragment().updateLastMessage(keyString, userId);
    }

    public boolean isBroadcastedToGroup(Integer channelKey) {
        if (!BroadcastService.isIndividual()) {
            return false;
        }
        return getConversationFragment().isBroadcastedToChannel(channelKey);
    }

    public void syncMessages(Message message, String keyString) {
        if (!message.hasHideKey() && !message.isVideoNotificationMessage()) {
            if (BroadcastService.isIndividual()) {
                ConversationFragment conversationFragment = getConversationFragment();
                if (conversationFragment != null && conversationFragment.isMsgForConversation(message)
                        && !Message.GroupMessageMetaData.TRUE.getValue().equals(message.getMetaDataValueForKey(Message.GroupMessageMetaData.HIDE_KEY.getValue()))) {
                    conversationFragment.addMessage(message);
                }
            }

            if (!Message.MetaDataType.ARCHIVE.getValue().equals(message.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue()))
                    || !(isActionMessageHidden && message.isActionMessage())) {
                updateLastMessage(message);
            }
        }
    }

    public void updateLastMessage(Message message) {
        if (!BroadcastService.isQuick()) {
            return;
        }
        getQuickConversationFragment().updateLastMessage(message);
    }

    public void downloadConversations(boolean showInstruction) {
        if (!BroadcastService.isQuick()) {
            return;
        }
        getQuickConversationFragment().downloadConversations(showInstruction, null);
    }

    public void setLoadMore(boolean loadMore) {
        if (!BroadcastService.isQuick()) {
            return;
        }
        getQuickConversationFragment().setLoadMore(loadMore);
    }

    public void updateMessageKeyString(Message message) {
        if (!BroadcastService.isIndividual()) {
            return;
        }
        String userId = message.getContactIds();
        ConversationFragment conversationFragment = getConversationFragment();
        if (!TextUtils.isEmpty(userId) && conversationFragment.getContact() != null && userId.equals(conversationFragment.getContact().getUserId()) ||
                conversationFragment.getCurrentChannelKey(message.getGroupId())) {
            conversationFragment.updateMessageKeyString(message);
        }
    }

    public void deleteMessage(String keyString, String userId) {
        updateLastMessage(keyString, userId);
        if (BroadcastService.isIndividual()) {
            getConversationFragment().deleteMessageFromDeviceList(keyString);
        }
    }

    public void updateLastSeenStatus(String contactId) {
        if (BroadcastService.isQuick()) {
            getQuickConversationFragment().updateLastSeenStatus(contactId);
            return;
        }
        if (BroadcastService.isIndividual()) {
            ConversationFragment conversationFragment = getConversationFragment();
            if (conversationFragment.getContact() != null && contactId.equals(conversationFragment.getContact().getContactIds()) || conversationFragment.getChannel() != null) {
                conversationFragment.updateLastSeenStatus();
            }
        }
    }

    public void updateDeliveryStatusForContact(String contactId) {
        updateStatus(contactId, false);
    }

    public void updateReadStatusForContact(String contactId) {
        updateStatus(contactId, true);
    }

    private void updateStatus(String contactId, boolean markRead) {
        if (!BroadcastService.isIndividual()) {
            return;
        }
        ConversationFragment conversationFragment = getConversationFragment();
        if (!TextUtils.isEmpty(contactId) && conversationFragment.getContact() != null && contactId.equals(conversationFragment.getContact().getContactIds())) {
            conversationFragment.updateDeliveryStatusForAllMessages(markRead);
        }
    }

    public void updateDeliveryStatus(Message message, String formattedContactNumber) {
        if (!BroadcastService.isIndividual()) {
            return;
        }
        ConversationFragment conversationFragment = getConversationFragment();
        if (conversationFragment.isMessageForCurrentConversation(message)) {
            conversationFragment.updateDeliveryStatus(message);
        }
    }

    public void deleteConversation(Contact contact, Integer channelKey, String response) {
        if (BroadcastService.isIndividual()) {
            if ("success".equals(response)) {
                getConversationFragment().clearList();
            } else {
                if (!Utils.isInternetAvailable(fragmentActivity)) {
                    KmToast.error(fragmentActivity, fragmentActivity.getString(R.string.you_need_network_access_for_delete), Toast.LENGTH_SHORT).show();
                } else {
                    KmToast.error(fragmentActivity, fragmentActivity.getString(R.string.delete_conversation_failed), Toast.LENGTH_SHORT).show();
                }
            }

        }
        if (BroadcastService.isQuick()) {
            getQuickConversationFragment().removeConversation(contact, channelKey, response);
        }
    }

    public void updateUploadFailedStatus(Message message) {
        if (!BroadcastService.isIndividual()) {
            return;
        }
        getConversationFragment().updateUploadFailedStatus(message);
    }

    public void updateDownloadFailed(Message message) {
        if (!BroadcastService.isIndividual()) {
            return;
        }
        getConversationFragment().downloadFailed(message);
    }

    public void updateDownloadStatus(Message message) {
        if (!BroadcastService.isIndividual()) {
            return;
        }
        getConversationFragment().updateDownloadStatus(message);
    }

    public void updateChannelName() {
        if (BroadcastService.isQuick()) {
            getQuickConversationFragment().updateChannelName();
        }
    }

    public void updateTypingStatus(String userId, String isTypingStatus) {
        if (!BroadcastService.isIndividual()) {
            return;
        }
        ConversationFragment conversationFragment = getConversationFragment();
        Utils.printLog(fragmentActivity, TAG, "Received typing status for: " + userId);
        if (conversationFragment.getContact() != null && userId.equals(conversationFragment.getContact().getContactIds()) || conversationFragment.getChannel() != null) {
            conversationFragment.updateUserTypingStatus(userId, isTypingStatus);
        }

    }

    public void updateChannelSync() {
        if (BroadcastService.isChannelInfo()) {
            BroadcastService.sendUpdateGroupInfoBroadcast(fragmentActivity, BroadcastService.INTENT_ACTIONS.UPDATE_GROUP_INFO.toString());
        }
        if (BroadcastService.isQuick() && getQuickConversationFragment() != null) {
            getQuickConversationFragment().refreshView();
        }
        if (BroadcastService.isIndividual()) {
            getConversationFragment().updateChannelTitleAndSubTitle();
            getConversationFragment().fetchBotTypeAndToggleCharLimitExceededMessage();
        }
    }

    public void updateTitleAndSubtitle() {
        if (!BroadcastService.isIndividual()) {
            return;
        }
        if (BroadcastService.isIndividual()) {
            getConversationFragment().updateTitleForOpenGroup();
        }
    }

    public void updateUserInfo(String userId) {
        if (TextUtils.isEmpty(userId)) {
            return;
        }

        if (BroadcastService.isQuick()) {
            getQuickConversationFragment().updateUserInfo(userId);
            return;
        }
        if (BroadcastService.isIndividual()) {
            ConversationFragment conversationFragment = getConversationFragment();
            if (conversationFragment.getContact() != null && userId.equals(conversationFragment.getContact().getContactIds()) || conversationFragment.getChannel() != null) {
                conversationFragment.reload();
            }
        }

    }


    public void updateConversationRead(String currentId, boolean isGroup) {
        if (TextUtils.isEmpty(currentId)) {
            return;
        }
        if (!BroadcastService.isIndividual()) {
            notificationManager.cancel(currentId.hashCode());
        }
        if (BroadcastService.isQuick()) {
            getQuickConversationFragment().updateConversationRead(currentId, isGroup);
        }
    }

    public void sendAudioMessage(String selectedFilePath) {

        Utils.printLog(fragmentActivity, "ConversationUIService:", "Send audio message ...");

        getConversationFragment().sendMessage(Message.ContentType.AUDIO_MSG.getValue(), selectedFilePath);

    }

    public void updateMessageMetadata(String keyString) {
        getConversationFragment().updateMessageMetadata(keyString);
    }

    public void muteUserChat(boolean mute, String userId) {
        if (getConversationFragment() != null && getConversationFragment().getContact() != null && getConversationFragment().getContact().getUserId().equals(userId)) {
            getConversationFragment().muteUser(mute);
        }
    }


    public void startMessageInfoFragment(String messageJson) {

        MessageInfoFragment messageInfoFragment = (MessageInfoFragment) UIService.getFragmentByTag(fragmentActivity, MESSGAE_INFO_FRAGMENT);
        if (messageInfoFragment == null) {
            messageInfoFragment = new MessageInfoFragment();
            Bundle bundle = new Bundle();
            bundle.putString(MessageInfoFragment.MESSAGE_ARGUMENT_KEY, messageJson);
            messageInfoFragment.setArguments(bundle);
            ConversationActivity.addFragment(fragmentActivity, messageInfoFragment, MESSGAE_INFO_FRAGMENT);
        }
    }


    public void checkForStartNewConversation(Intent intent) {
        Contact contact = null;
        Channel channel = null;
        Integer conversationId = null;

        final Uri uri = intent.getData();
        if (uri != null) {
            //Note: This is used only for the device contacts
            Long contactId = intent.getLongExtra(CONTACT_ID, 0);
            if (contactId == 0) {
                //Todo: show warning that the user doesn't have any number stored.
                return;
            }
            contact = baseContactService.getContactById(String.valueOf(contactId));
        }

        Integer channelKey = intent.getIntExtra(GROUP_ID, -1);
        String clientGroupId = intent.getStringExtra(CLIENT_GROUP_ID);
        String channelName = intent.getStringExtra(GROUP_NAME);

        if (!TextUtils.isEmpty(clientGroupId)) {
            channel = ChannelService.getInstance(fragmentActivity).getChannelByClientGroupId(clientGroupId);
            if (channel == null) {
                return;
            }
        } else if (channelKey != -1 && channelKey != null && channelKey != 0) {
            channel = ChannelService.getInstance(fragmentActivity).getChannel(channelKey);
        }

        if (channel != null && !TextUtils.isEmpty(channelName) && TextUtils.isEmpty(channel.getName())) {
            channel.setName(channelName);
            ChannelService.getInstance(fragmentActivity).updateChannel(channel);
        }

        String contactNumber = intent.getStringExtra(CONTACT_NUMBER);

        boolean firstTimeMTexterFriend = intent.getBooleanExtra(FIRST_TIME_MTEXTER_FRIEND, false);
        if (!TextUtils.isEmpty(contactNumber)) {
            contact = baseContactService.getContactById(contactNumber);
            if (BroadcastService.isIndividual()) {
                getConversationFragment().setFirstTimeMTexterFriend(firstTimeMTexterFriend);
            }
        }

        String userId = intent.getStringExtra(USER_ID);
        if (TextUtils.isEmpty(userId)) {
            userId = intent.getStringExtra("contactId");
        }

        if (!TextUtils.isEmpty(userId)) {
            contact = baseContactService.getContactById(userId);
        }
        String searchString = intent.getStringExtra(SEARCH_STRING);
        String applicationId = intent.getStringExtra(APPLICATION_ID);
        if (contact != null) {
            contact.setApplicationId(applicationId);
            baseContactService.upsert(contact);
        }
        String fullName = intent.getStringExtra(DISPLAY_NAME);
        if (contact != null && TextUtils.isEmpty(contact.getFullName()) && !TextUtils.isEmpty(fullName)) {
            contact.setFullName(fullName);
            baseContactService.upsert(contact);
            new UserClientService(fragmentActivity).updateUserDisplayName(userId, fullName);
        }
        String messageJson = intent.getStringExtra(MobiComKitConstants.MESSAGE_JSON_INTENT);
        if (!TextUtils.isEmpty(messageJson)) {
            Message message = (Message) GsonUtils.getObjectFromJson(messageJson, Message.class);
            if (message.getGroupId() != null) {
                channel = ChannelService.getInstance(fragmentActivity).getChannelByChannelKey(message.getGroupId());
            } else {
                contact = baseContactService.getContactById(message.getContactIds());
            }
            conversationId = message.getConversationId();
        }
        if (conversationId == null) {
            conversationId = intent.getIntExtra(CONVERSATION_ID, 0);
        }
        if (conversationId != 0 && conversationId != null) {
            getConversationFragment().setConversationId(conversationId);
        } else {
            conversationId = null;
        }

        String defaultText = intent.getStringExtra(ConversationUIService.DEFAULT_TEXT);
        if (!TextUtils.isEmpty(defaultText)) {
            getConversationFragment().setDefaultText(defaultText);
        }

        if (contact != null) {
            openConversationFragment(contact, conversationId, searchString, intent.getStringExtra(MESSAGE_SEARCH_STRING));
        }

        String preFilledMessage = intent.getStringExtra(KmConstants.KM_PREFILLED_MESSAGE);
        if (channel != null) {
            openConversationFragment(channel, conversationId, searchString, intent.getStringExtra(MESSAGE_SEARCH_STRING), preFilledMessage);
        }
        String productTopicId = intent.getStringExtra(ConversationUIService.PRODUCT_TOPIC_ID);
        String productImageUrl = intent.getStringExtra(ConversationUIService.PRODUCT_IMAGE_URL);
        if (!TextUtils.isEmpty(productTopicId) && !TextUtils.isEmpty(productImageUrl)) {
            try {
                FileMeta fileMeta = new FileMeta();
                fileMeta.setContentType("image");
                fileMeta.setBlobKeyString(productImageUrl);
                getConversationFragment().sendProductMessage(productTopicId, fileMeta, contact, Message.ContentType.TEXT_URL.getValue());
            } catch (Exception e) {
            }
        }
    }

    void showToastMessage(final String messageToShow) {
        Toast toast = KmToast.error(fragmentActivity, messageToShow, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void reconnectMQTT() {
        try {
            if (((MobiComKitActivityInterface) fragmentActivity).getRetryCount() <= 3) {
                if (Utils.isInternetAvailable(fragmentActivity)) {
                    Utils.printLog(fragmentActivity, TAG, "Reconnecting to mqtt.");
                    ((MobiComKitActivityInterface) fragmentActivity).retry();
                    Applozic.connectPublish(fragmentActivity);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendLocation(String position) {
        getConversationFragment().sendMessage(position, Message.ContentType.LOCATION.getValue());
    }

    public static ConversationFragment getConversationFragment(Context context, Contact contact, Channel channel, Integer conversationId, String searchString, String messageSearchString, String preFilledMessage) {
        if (context != null && context.getApplicationContext() instanceof KmFragmentGetter) {
            return ((KmFragmentGetter) context.getApplicationContext()).getConversationFragment(contact, channel, conversationId, searchString, messageSearchString);
        }
        return ConversationFragment.newInstance(contact, channel, conversationId, searchString, messageSearchString, preFilledMessage);
    }
}
