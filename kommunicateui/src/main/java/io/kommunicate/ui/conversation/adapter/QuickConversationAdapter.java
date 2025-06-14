package io.kommunicate.ui.conversation.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.kommunicate.devkit.api.account.user.MobiComUserPreference;
import io.kommunicate.devkit.api.conversation.Message;
import io.kommunicate.devkit.api.conversation.database.MessageDatabaseService;
import io.kommunicate.devkit.api.notification.VideoCallNotificationHelper;
import io.kommunicate.devkit.channel.database.ChannelDatabaseService;
import io.kommunicate.devkit.channel.service.ChannelService;
import io.kommunicate.devkit.contact.AppContactService;
import io.kommunicate.devkit.contact.BaseContactService;
import io.kommunicate.ui.CustomizationSettings;
import io.kommunicate.ui.R;
import io.kommunicate.ui.alphanumbericcolor.AlphaNumberColorUtil;
import io.kommunicate.ui.conversation.ConversationUIService;
import io.kommunicate.ui.conversation.activity.MobiComKitActivityInterface;
import io.kommunicate.ui.instruction.InstructionUtil;
import io.kommunicate.ui.utils.KmViewHelper;
import io.kommunicate.commons.commons.core.utils.DateUtils;
import io.kommunicate.commons.commons.image.ImageLoader;
import io.kommunicate.commons.commons.image.ImageUtils;
import io.kommunicate.commons.emoticon.EmojiconHandler;
import io.kommunicate.commons.emoticon.EmoticonUtils;
import io.kommunicate.commons.people.channel.Channel;
import io.kommunicate.commons.people.channel.ChannelUtils;
import io.kommunicate.commons.people.contact.Contact;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.kommunicate.utils.KmUtils;

/**
 * Created by adarsh on 4/7/15.
 */
public class QuickConversationAdapter extends RecyclerView.Adapter implements Filterable {

    private static final String DEFAULT_MSG_BODY = "Message";
    private static Map<Short, Integer> messageTypeColorMap = new HashMap<Short, Integer>();
    private static final String CONVERSATION_SOURCE = "source";
    private static final String SOURCE_FACEBOOK = "FACEBOOK";

    static {
        messageTypeColorMap.put(Message.MessageType.INBOX.getValue(), R.color.message_type_inbox);
        messageTypeColorMap.put(Message.MessageType.OUTBOX.getValue(), R.color.message_type_outbox);
        messageTypeColorMap.put(Message.MessageType.OUTBOX_SENT_FROM_DEVICE.getValue(), R.color.message_type_outbox_sent_from_device);
        messageTypeColorMap.put(Message.MessageType.MT_INBOX.getValue(), R.color.message_type_mt_inbox);
        messageTypeColorMap.put(Message.MessageType.MT_OUTBOX.getValue(), R.color.message_type_mt_outbox);
        messageTypeColorMap.put(Message.MessageType.CALL_INCOMING.getValue(), R.color.message_type_incoming_call);
        messageTypeColorMap.put(Message.MessageType.CALL_OUTGOING.getValue(), R.color.message_type_outgoing_call);
    }

    public ImageLoader contactImageLoader, channelImageLoader;
    public String searchString = null;
    private Context context;
    private MessageDatabaseService messageDatabaseService;
    private List<Message> messageList;
    private BaseContactService contactService;
    private EmojiconHandler emojiconHandler;
    private List<Message> originalList;
    private TextAppearanceSpan highlightTextSpan;
    private CustomizationSettings customizationSettings;
    private View view;
    private ConversationUIService conversationUIService;
    private int loggedInUserRoleType;
    private String loggedInUserId;
    private boolean isDarkMode;

    public void setAlCustomizationSettings(CustomizationSettings customizationSettings) {
        this.customizationSettings = customizationSettings;
    }

    public void setDarkMode(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;
    }

    public QuickConversationAdapter(final Context context, List<Message> messageList, EmojiconHandler emojiconHandler) {
        this.context = context;
        this.emojiconHandler = emojiconHandler;
        this.contactService = new AppContactService(context);
        this.messageDatabaseService = new MessageDatabaseService(context);
        this.messageList = messageList;
        conversationUIService = new ConversationUIService((FragmentActivity) context);
        loggedInUserRoleType = MobiComUserPreference.getInstance(context).getUserRoleType();
        loggedInUserId = MobiComUserPreference.getInstance(context).getUserId();
        contactImageLoader = new ImageLoader(context, ImageUtils.getLargestScreenDimension((Activity) context)) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return contactService.downloadContactImage((Activity) context, (Contact) data);
            }
        };
        contactImageLoader.addImageCache(((FragmentActivity) context).getSupportFragmentManager(), 0.1f);
        contactImageLoader.setImageFadeIn(false);
        channelImageLoader = new ImageLoader(context, ImageUtils.getLargestScreenDimension((Activity) context)) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return contactService.downloadGroupImage((Activity) context, (Channel) data);
            }
        };
        channelImageLoader.addImageCache(((FragmentActivity) context).getSupportFragmentManager(), 0.1f);
        channelImageLoader.setImageFadeIn(false);
        highlightTextSpan = new TextAppearanceSpan(context, R.style.searchTextHiglight);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (viewType == 2) {
            View v2 = inflater.inflate(R.layout.message_list_header_footer, parent, false);
            return new FooterViewHolder(v2);
        } else {
            view = inflater.inflate(R.layout.message_row_view, parent, false);
            return new Myholder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == 2) {
            FooterViewHolder myHolder = (FooterViewHolder) holder;
            myHolder.infoBroadCast.setVisibility(View.GONE);
        } else {
            Myholder myholder = (Myholder) holder;
            final Message message = getItem(position);
            myholder.smTime.setVisibility(View.GONE);
            if (message != null) {
                List<String> items = null;
                List<String> userIds = null;

                final Channel channel = ChannelDatabaseService.getInstance(context).getChannelByChannelKey(message.getGroupId());

                if (channel == null && message.getGroupId() == null) {
                    items = Arrays.asList(message.getTo().split("\\s*,\\s*"));
                    if (!TextUtils.isEmpty(message.getContactIds())) {
                        userIds = Arrays.asList(message.getContactIds().split("\\s*,\\s*"));
                    }
                }

                final Contact contactReceiver = contactService.getContactReceiver(items, userIds);

                myholder.contactImage.setVisibility(View.GONE);
                myholder.alphabeticTextView.setVisibility(View.GONE);
                myholder.onlineTextView.setVisibility(View.GONE);

                if (customizationSettings.isOnlineStatusMasterList() && message.getGroupId() == null) {
                    myholder.onlineTextView.setVisibility(contactReceiver != null && contactReceiver.isOnline() ? View.VISIBLE : View.GONE);
                    myholder.offlineTextView.setVisibility(contactReceiver != null && contactReceiver.isOnline() ? View.GONE : View.VISIBLE);
                }

                if (myholder.attachedFile != null) {
                    myholder.attachedFile.setText("");
                    myholder.attachedFile.setVisibility(View.GONE);
                }

                if (contactReceiver != null) {
                    String contactInfo = contactReceiver.getDisplayName();
                    if (items != null && items.size() > 1) {
                        Contact contact2 = contactService.getContactById(items.get(1));
                        contactInfo = TextUtils.isEmpty(contactReceiver.getFirstName()) ? contactReceiver.getContactNumber() : contactReceiver.getFirstName() + ", "
                                + (TextUtils.isEmpty(contact2.getFirstName()) ? contact2.getContactNumber() : contact2.getFirstName()) + (items.size() > 2 ? " & others" : "");
                    }
                    myholder.smReceivers.setText(contactInfo);
                    contactImageLoader.setLoadingImage(R.drawable.km_ic_contact_picture_holo_light);
                    processContactImage(contactReceiver, myholder.onlineTextView, myholder.offlineTextView, myholder.alphabeticTextView, myholder.contactImage);
                } else if (message.getGroupId() != null && channel != null) {
                    if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                        contactImageLoader.setLoadingImage(R.drawable.km_ic_contact_picture_holo_light);
                        Contact withUserContact = contactService.getContactById(ChannelService.getInstance(context).getGroupOfTwoReceiverUserId(channel.getKey()));
                        if (withUserContact != null) {
                            myholder.smReceivers.setText(withUserContact.getDisplayName());
                            processContactImage(withUserContact, myholder.onlineTextView, myholder.offlineTextView, myholder.alphabeticTextView, myholder.contactImage);
                        }
                    } if (channel.getKmStatus() == Channel.IN_QUEUE_CONVERSATION) {
                        myholder.smReceivers.setText(R.string.in_queue);
                        channelImageLoader.setLoadingImage(R.drawable.km_message_in_queue);
                        myholder.contactImage.setImageResource(R.drawable.km_message_in_queue);
                        myholder.contactImage.setVisibility(View.VISIBLE);
                    }else if (Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType())) {
                        Contact withUserContact = contactService.getContactById(channel.getConversationAssignee());
                        myholder.smReceivers.setText(channel.getName() != null ? channel.getName() : "");
                        channelImageLoader.setLoadingImage(R.drawable.km_ic_contact_picture_holo_light);
                        myholder.contactImage.setImageResource(R.drawable.km_ic_contact_picture_holo_light);
                        KmViewHelper.loadContactImage(context, myholder.contactImage, myholder.alphabeticTextView, withUserContact, R.drawable.km_ic_contact_picture_holo_light);
                    } else {
                        myholder.smReceivers.setText(channel.getName() != null ? channel.getName() : "");
                        channelImageLoader.setLoadingImage(R.drawable.km_group_icon);
                        myholder.contactImage.setImageResource(R.drawable.km_group_icon);

                        myholder.alphabeticTextView.setVisibility(View.GONE);
                        myholder.contactImage.setVisibility(View.VISIBLE);
                        myholder.smReceivers.setText(ChannelUtils.getChannelTitleName(channel, loggedInUserId));

                        if (channel != null && !TextUtils.isEmpty(channel.getImageUrl())) {
                            channelImageLoader.loadImage(channel, myholder.contactImage);
                            myholder.alphabeticTextView.setVisibility(View.GONE);
                            myholder.contactImage.setVisibility(View.VISIBLE);
                        } else if (channel != null && channel.isBroadcastMessage()) {
                            myholder.contactImage.setImageResource(R.drawable.km_ic_applozic_broadcast);
                            myholder.alphabeticTextView.setVisibility(View.GONE);
                            myholder.contactImage.setVisibility(View.VISIBLE);
                        } else {
                            channelImageLoader.setLoadingImage(R.drawable.km_group_icon);
                            myholder.alphabeticTextView.setVisibility(View.GONE);
                            myholder.contactImage.setVisibility(View.VISIBLE);
                        }
                    }
                }
                if (isDarkMode) {
                    myholder.smReceivers.setTextColor(context.getResources().getColor(R.color.messageReceiver_background_night));
                    myholder.createdAtTime.setTextColor(context.getResources().getColor(R.color.createdAtTime_background_night));
                    myholder.messageTextView.setTextColor(context.getResources().getColor(R.color.createdAtTime_background_night));
                    myholder.attachmentIcon.setColorFilter(context.getResources().getColor(R.color.white));
                } else {
                    myholder.smReceivers.setTextColor(context.getResources().getColor(R.color.km_conversation_list_item_title_text_color));
                    myholder.createdAtTime.setTextColor(context.getResources().getColor(R.color.km_conversation_list_item_created_at_time_text_color));
                    myholder.messageTextView.setTextColor(context.getResources().getColor(R.color.km_conversation_list_message_text_color));
                    myholder.attachmentIcon.clearColorFilter();
                }

                if (channel != null && channel.getKmStatus() == Channel.IN_QUEUE_CONVERSATION) {
                    myholder.messageTextView.setVisibility(View.GONE);
                } else if (message.isVideoCallMessage()) {
                    createVideoCallView(message, myholder.attachmentIcon, myholder.messageTextView);
                } else if (message.hasAttachment() && myholder.attachmentIcon != null && !(message.getContentType() == Message.ContentType.TEXT_URL.getValue())) {
                    //Todo: handle it for fileKeyStrings when filePaths is empty
                    String filePath = message.getFileMetas() == null && message.getFilePaths() != null ? message.getFilePaths().get(0).substring(message.getFilePaths().get(0).lastIndexOf("/") + 1) :
                            message.getFileMetas() != null ? message.getFileMetas().getName() : "";
                    myholder.attachmentIcon.setVisibility(View.VISIBLE);
                    myholder.attachmentIcon.setImageResource(R.drawable.km_ic_action_attachment);
                    myholder.messageTextView.setText(KmUtils.getAttachmentName(message));
                } else if (myholder.attachmentIcon != null && message.getContentType() == Message.ContentType.LOCATION.getValue()) {
                    myholder.attachmentIcon.setVisibility(View.VISIBLE);
                    myholder.attachmentIcon.setImageResource(R.drawable.notification_location_icon);
                    myholder.messageTextView.setText(context.getString(R.string.Location));
                } else if (message.getContentType() == Message.ContentType.PRICE.getValue()) {
                    myholder.messageTextView.setText(EmoticonUtils.getSmiledText(context, ConversationUIService.FINAL_PRICE_TEXT + message.getMessage(), emojiconHandler));
                } else if (message.getContentType() == Message.ContentType.TEXT_HTML.getValue()) {
                    KmUtils.setIconInsideTextView(myholder.messageTextView, R.drawable.ic_messageicon, Color.TRANSPARENT, KmUtils.LEFT_POSITION, 20, isDarkMode);                    if (DetailedConversationAdapter.isEmailTypeMessage(message)) {
                    myholder.messageTextView.setText(DEFAULT_MSG_BODY);
                        myholder.attachmentIcon.setVisibility(View.VISIBLE);
                        myholder.attachmentIcon.setImageResource(R.drawable.email);
                    }
                }
                else if(TextUtils.isEmpty(message.getMessage()) && message.isRichMessage()) {
                    myholder.attachmentIcon.setVisibility(View.GONE);
                    KmUtils.setIconInsideTextView(myholder.messageTextView, R.drawable.ic_messageicon, Color.TRANSPARENT, KmUtils.LEFT_POSITION, 20, isDarkMode);
                    myholder.messageTextView.setText(DEFAULT_MSG_BODY);
                }
                else {
                    String messageSubString = (!TextUtils.isEmpty(message.getMessage()) ? message.getMessage().substring(0, Math.min(message.getMessage().length(), 50)) : "");
                    myholder.messageTextView.setText(EmoticonUtils.getSmiledText(context, messageSubString, emojiconHandler));
                    showConversationSourceIcon(channel, myholder.attachmentIcon);
                    KmUtils.setIconInsideTextView(myholder.messageTextView);
                }

                if (myholder.sentOrReceived != null) {
                    if (message.isCall()) {
                        myholder.sentOrReceived.setImageResource(R.drawable.km_ic_action_call_holo_light);
                        myholder.messageTextView.setTextColor(context.getResources().getColor(message.isIncomingCall() ? R.color.incoming_call : R.color.outgoing_call));
                    } else if (getItemViewType(position) == 0) {
                        myholder.sentOrReceived.setImageResource(R.drawable.social_forward);
                    } else {
                        myholder.sentOrReceived.setImageResource(R.drawable.social_reply);
                    }
                }
                if (myholder.createdAtTime != null) {
                    myholder.createdAtTime.setText(DateUtils.getFormattedDateAndTime(context, message.getCreatedAtTime(), R.string.JUST_NOW, R.plurals.MINUTES, R.plurals.HOURS));
                }
                int messageUnReadCount = 0;
                if (message.getGroupId() == null && contactReceiver != null && !TextUtils.isEmpty(contactReceiver.getContactIds())) {
                    messageUnReadCount = messageDatabaseService.getUnreadMessageCountForContact(contactReceiver.getContactIds());
                } else if (channel != null && channel.getKey() != null && channel.getKey() != 0) {
                    messageUnReadCount = messageDatabaseService.getUnreadMessageCountForChannel(channel.getKey());
                }
                if (channel != null && channel.getKmStatus() == Channel.IN_QUEUE_CONVERSATION) {
                    myholder.unReadCountTextView.setVisibility(View.GONE);
                } else if (messageUnReadCount > 0) {
                    myholder.unReadCountTextView.setVisibility(View.VISIBLE);
                    myholder.unReadCountTextView.setText(String.valueOf(messageUnReadCount));
                } else {
                    myholder.unReadCountTextView.setVisibility(View.GONE);
                }

                int startIndex = indexOfSearchQuery(message.getMessage());
                if (startIndex != -1) {

                    final SpannableString highlightedName = new SpannableString(message.getMessage());

                    // Sets the span to start at the starting point of the match and end at "length"
                    // characters beyond the starting point
                    highlightedName.setSpan(highlightTextSpan, startIndex,
                            startIndex + searchString.toString().length(), 0);

                    myholder.messageTextView.setText(highlightedName);
                }
            }
        }
    }

    public Message getItem(int position) {
        return messageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public int getItemViewType(int position) {
        return getItem(position) != null ? getItem(position).isTypeOutbox() ? 1 : 0 : 2;
    }

    private int indexOfSearchQuery(String message) {
        if (!TextUtils.isEmpty(searchString)) {
            return message.toLowerCase(Locale.getDefault()).indexOf(
                    searchString.toString().toLowerCase(Locale.getDefault()));
        }
        return -1;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                final FilterResults oReturn = new FilterResults();
                final List<Message> results = new ArrayList<Message>();
                if (originalList == null)
                    originalList = messageList;
                if (constraint != null) {
                    searchString = constraint.toString();
                    if (originalList != null && originalList.size() > 0) {
                        for (final Message message : originalList) {
                            if (message.getMessage().toLowerCase()
                                    .contains(constraint.toString())) {
                                results.add(message);
                            }
                        }
                    }
                    oReturn.values = results;
                } else {
                    oReturn.values = originalList;
                }
                return oReturn;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,
                                          FilterResults results) {
                messageList = (ArrayList<Message>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public void createVideoCallView(Message message, ImageView attachmentIcon, TextView
            messageTextView) {
        if (message.getMetadata() == null || message.getMetadata().isEmpty()) {
            if (attachmentIcon != null) {
                attachmentIcon.setImageResource(R.drawable.ic_videocam_white_24px);
                attachmentIcon.setColorFilter(R.color.core_green_color);
                return;
            }
        }

        if (messageTextView != null) {
            messageTextView.setText(VideoCallNotificationHelper.getStatus(message.getMetadata()));
        }

        if (attachmentIcon != null) {
            attachmentIcon.setVisibility(View.VISIBLE);
            if (VideoCallNotificationHelper.isMissedCall(message)) {
                attachmentIcon.setImageResource(R.drawable.ic_communication_call_missed);
            } else if (VideoCallNotificationHelper.isAudioCall(message)) {
                attachmentIcon.setImageResource(R.drawable.km_ic_action_call_holo_light);
            } else {
                attachmentIcon.setImageResource(R.drawable.ic_videocam_white_24px);
                attachmentIcon.setColorFilter(R.color.core_green_color);
            }
        }
    }

    private void processContactImage(Contact contact, TextView textView, TextView
            offlineTv, TextView alphabeticTextView, CircleImageView contactImage) {
        try {
            String contactNumber = "";
            char firstLetter = 0;
            contactNumber = contact.getDisplayName().toUpperCase();
            firstLetter = contact.getDisplayName().toUpperCase().charAt(0);

            if (contact != null) {
                if (firstLetter != '+') {
                    alphabeticTextView.setText(String.valueOf(firstLetter));
                } else if (contactNumber.length() >= 2) {
                    alphabeticTextView.setText(String.valueOf(contactNumber.charAt(1)));
                }
                Character colorKey = AlphaNumberColorUtil.alphabetBackgroundColorMap.containsKey(firstLetter) ? firstLetter : null;
                GradientDrawable bgShape = (GradientDrawable) alphabeticTextView.getBackground();
                bgShape.setColor(context.getResources().getColor(AlphaNumberColorUtil.alphabetBackgroundColorMap.get(colorKey)));
            }
            alphabeticTextView.setVisibility(View.GONE);
            contactImage.setVisibility(View.VISIBLE);
            if (contact != null) {
                if (contact.isDrawableResources()) {
                    int drawableResourceId = context.getResources().getIdentifier(contact.getrDrawableName(), "drawable", context.getPackageName());
                    contactImage.setImageResource(drawableResourceId);
                } else {
                    contactImageLoader.loadImage(contact, contactImage, alphabeticTextView);
                }
            }
            textView.setVisibility(contact != null && contact.isOnline() ? View.VISIBLE : View.GONE);
            offlineTv.setVisibility(contact != null && contact.isOnline() ? View.GONE : View.VISIBLE);
        } catch (Exception e) {

        }
    }

    public class Myholder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {

        TextView smReceivers;
        TextView createdAtTime;
        TextView messageTextView;
        CircleImageView contactImage;
        TextView alphabeticTextView;
        TextView onlineTextView, offlineTextView;
        ImageView sentOrReceived;
        TextView attachedFile;
        final ImageView attachmentIcon;
        TextView unReadCountTextView;
        TextView smTime;
        RelativeLayout rootView, profileImageRelativeLayout;

        public Myholder(View itemView) {
            super(itemView);

            smReceivers = (TextView) itemView.findViewById(R.id.smReceivers);
            createdAtTime = (TextView) itemView.findViewById(R.id.createdAtTime);
            messageTextView = (TextView) itemView.findViewById(R.id.message);
            //ImageView contactImage = (ImageView) customView.findViewById(R.id.contactImage);
            contactImage = (CircleImageView) itemView.findViewById(R.id.contactImage);
            alphabeticTextView = (TextView) itemView.findViewById(R.id.alphabeticImage);
            onlineTextView = (TextView) itemView.findViewById(R.id.onlineTextView);
            //sentOrReceived = (ImageView) itemView.findViewById(R.id.sentOrReceivedIcon);
            attachedFile = (TextView) itemView.findViewById(R.id.attached_file);
            attachmentIcon = (ImageView) itemView.findViewById(R.id.attachmentIcon);
            unReadCountTextView = (TextView) itemView.findViewById(R.id.unreadSmsCount);
            smTime = (TextView) itemView.findViewById(R.id.smTime);
            profileImageRelativeLayout = itemView.findViewById(R.id.profile_image_relative_layout);
            rootView = itemView.findViewById(R.id.rootView);
            offlineTextView = itemView.findViewById(R.id.offlineTextView);

            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View v) {

            int itemPosition = this.getLayoutPosition();
            if (itemPosition != -1 && !messageList.isEmpty()) {
                Message message = getItem(itemPosition);
                if (message != null) {
                    InstructionUtil.hideInstruction(context, R.string.instruction_open_conversation_thread);
                    ((MobiComKitActivityInterface) context).onQuickConversationFragmentItemClick(message, message.getConversationId(), searchString);
                }
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            int position = this.getLayoutPosition();

            if (messageList.size() <= position) {
                return;
            }
            Message message = messageList.get(position);
            String[] menuItems = context.getResources().getStringArray(R.array.conversation_options_menu);

            boolean isUserPresentInGroup = false;
            boolean isChannelDeleted = false;
            boolean isSupportGroup = false;
            Channel channel = null;
            if (message.getGroupId() != null) {
                channel = ChannelService.getInstance(context).getChannelByChannelKey(message.getGroupId());
                if (channel != null) {
                    isChannelDeleted = channel.isDeleted();
                    isSupportGroup = Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType());
                }
                isUserPresentInGroup = ChannelService.getInstance(context).processIsUserPresentInChannel(message.getGroupId());
            }

            for (int i = 0; i < menuItems.length; i++) {

                if ((message.getGroupId() == null || (channel != null && Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType()))) && (menuItems[i].equals(context.getResources().getString(R.string.delete_group)) ||
                        menuItems[i].equals(context.getResources().getString(R.string.exit_group)))) {
                    continue;
                }

                if (menuItems[i].equals(context.getResources().getString(R.string.exit_group)) && (isChannelDeleted || !isUserPresentInGroup || isSupportGroup)) {
                    continue;
                }

                if (menuItems[i].equals(context.getResources().getString(R.string.delete_group)) && (isUserPresentInGroup || !isChannelDeleted || isSupportGroup)) {
                    continue;
                }
                if (menuItems[i].equals(context.getResources().getString(R.string.delete_conversation_context)) && !customizationSettings.isDeleteOption()) {
                    continue;
                }

                MenuItem item = menu.add(Menu.NONE, i, i, menuItems[i]);
                item.setOnMenuItemClickListener(onEditMenu);
            }
        }

        private final MenuItem.OnMenuItemClickListener onEditMenu = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int position = getLayoutPosition();

                if (messageList.size() <= position || position == -1) {
                    return true;
                }

                Message message = messageList.get(position);

                Channel channel = null;
                if (message.getGroupId() != null) {
                    channel = ChannelDatabaseService.getInstance(context).getChannelByChannelKey(message.getGroupId());
                }

                switch (item.getItemId()) {
                    case 0:
                        if (channel != null) {
                            conversationUIService.deleteChannel(context, channel);
                        }
                        break;
                    case 1:
                        conversationUIService.deleteGroupConversation(channel);
                        break;
                    case 2:
                        conversationUIService.channelLeaveProcess(channel);
                        break;
                    default:
                        //return onMenuItemClick(item);
                }
                return true;
            }
        };
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {
        TextView infoBroadCast;
        ProgressBar loadMoreProgressBar;

        public FooterViewHolder(View itemView) {
            super(itemView);
            infoBroadCast = (TextView) itemView.findViewById(R.id.info_broadcast);
            loadMoreProgressBar = (ProgressBar) itemView.findViewById(R.id.load_more_progressbar);
        }
    }

    private void showConversationSourceIcon(Channel channel, ImageView attachmentIcon) {
        if (channel != null
                && Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType())
                && channel.getMetadata() != null
                && channel.getMetadata().containsKey(CONVERSATION_SOURCE)) {
            attachmentIcon.setVisibility(View.VISIBLE);
            if (SOURCE_FACEBOOK.equals(channel.getMetadata().get(CONVERSATION_SOURCE))) {
                attachmentIcon.setImageResource(R.drawable.ic_facebook_icon);
            } else {
                attachmentIcon.setVisibility(View.GONE);
            }
        } else {
            attachmentIcon.setVisibility(View.GONE);
        }
    }
}
