package io.kommunicate.ui.conversation.adapter;

import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import io.kommunicate.devkit.KommunicateSettings;
import io.kommunicate.devkit.api.MobiComKitConstants;
import io.kommunicate.devkit.api.account.user.MobiComUserPreference;
import io.kommunicate.devkit.api.attachment.AttachmentManager;
import io.kommunicate.devkit.api.attachment.AttachmentView;
import io.kommunicate.devkit.api.attachment.FileClientService;
import io.kommunicate.devkit.api.attachment.FileMeta;
import io.kommunicate.devkit.api.conversation.Message;
import io.kommunicate.devkit.api.conversation.MobiComConversationService;
import io.kommunicate.devkit.api.conversation.database.MessageDatabaseService;
import io.kommunicate.devkit.api.conversation.stat.SourceUrl;
import io.kommunicate.devkit.api.notification.VideoCallNotificationHelper;
import io.kommunicate.devkit.channel.service.ChannelService;
import io.kommunicate.devkit.contact.AppContactService;
import io.kommunicate.devkit.contact.BaseContactService;
import io.kommunicate.devkit.contact.MobiComVCFParser;
import io.kommunicate.devkit.contact.VCFContactData;
import io.kommunicate.ui.CustomizationSettings;
import io.kommunicate.ui.KmFontManager;
import io.kommunicate.ui.KommunicateSetting;
import io.kommunicate.ui.R;
import io.kommunicate.ui.attachmentview.KmDocumentView;
import io.kommunicate.ui.conversation.ConversationUIService;
import io.kommunicate.ui.conversation.activity.ConversationActivity;
import io.kommunicate.ui.conversation.activity.FullScreenImageActivity;
import io.kommunicate.ui.conversation.activity.MobiComKitActivityInterface;
import io.kommunicate.ui.conversation.activity.OnClickReplyInterface;
import io.kommunicate.ui.conversation.fragment.FeedbackInputFragment;
import io.kommunicate.ui.conversation.richmessaging.KmRichMessageFactory;
import io.kommunicate.ui.conversation.richmessaging.callbacks.KmRichMessageListener;
import io.kommunicate.ui.kommunicate.utils.DimensionsUtils;
import io.kommunicate.ui.kommunicate.utils.KmThemeHelper;
import io.kommunicate.ui.kommunicate.views.KmToast;
import io.kommunicate.ui.uilistener.ContextMenuClickListener;
import io.kommunicate.ui.uilistener.KmStoragePermission;
import io.kommunicate.ui.uilistener.KmStoragePermissionListener;
import io.kommunicate.ui.utils.KmViewHelper;
import io.kommunicate.commons.commons.core.utils.DateUtils;
import io.kommunicate.commons.commons.core.utils.LocationUtils;
import io.kommunicate.commons.commons.core.utils.Utils;
import io.kommunicate.commons.commons.image.ImageCache;
import io.kommunicate.commons.commons.image.ImageLoader;
import io.kommunicate.commons.commons.image.ImageUtils;
import io.kommunicate.commons.emoticon.EmojiconHandler;
import io.kommunicate.commons.emoticon.EmoticonUtils;
import io.kommunicate.commons.file.FileUtils;
import io.kommunicate.commons.json.GsonUtils;
import io.kommunicate.commons.people.channel.Channel;
import io.kommunicate.commons.people.contact.Contact;
import com.bumptech.glide.Glide;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.vectordrawable.graphics.drawable.AnimatorInflaterCompat;

import de.hdodenhof.circleimageview.CircleImageView;
import io.kommunicate.utils.KmAppSettingPreferences;
import io.kommunicate.utils.KmConstants;
import io.kommunicate.utils.KmUtils;
import io.sentry.Hint;
import io.sentry.Sentry;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static io.kommunicate.devkit.api.conversation.stat.SourceUrl.SOURCE_URL;
import static io.kommunicate.utils.KmConstants.KM_SUMMARY;

/**
 * Created by adarsh on 4/7/15.
 */
public class DetailedConversationAdapter extends RecyclerView.Adapter implements Filterable {

    private static final String TAG = "DetailedConversation";

    private static final int FILE_THRESOLD_SIZE = 400;

    public ImageLoader contactImageLoader, loadImage;
    public String searchString;
    private CustomizationSettings customizationSettings;
    private Context context;
    private Contact contact;
    private Channel channel;
    private boolean individual;
    private Drawable sentIcon;
    private Drawable deliveredIcon;
    private Drawable pendingIcon;
    private Drawable readIcon;
    private ImageLoader imageThumbnailLoader;
    private EmojiconHandler emojiconHandler;
    private FileClientService fileClientService;
    private MessageDatabaseService messageDatabaseService;
    private BaseContactService contactService;
    private Class<?> messageIntentClass;
    private List<Message> messageList;
    private List<Message> originalList;
    private MobiComConversationService conversationService;
    private ImageCache imageCache;
    private TextAppearanceSpan highlightTextSpan;
    private View view;
    private ContextMenuClickListener contextMenuClickListener;
    private KmRichMessageListener listener;
    private KmStoragePermissionListener storagePermissionListener;
    private String geoApiKey;
    private float[] sentMessageCornerRadii = {0, 0, 0, 0, 0, 0, 0, 0};
    private float[] receivedMessageCornerRadii = {0, 0, 0, 0, 0, 0, 0, 0};
    private KmFontManager fontManager;
    private KmThemeHelper themeHelper;
    private Message lastSentMessage;
    private List<WebView> webViews = new ArrayList<>();
    private boolean useInnerTimeStampDesign;
    private boolean isDarkModeEnabled = false;
    private static final String IMAGE = "image";
    private static final String VIDEO = "video";
    private static final String AUDIO = "audio";
    private static final String TEXT_XCARD = "text/x-vcard";
    private static final String GMAP_LINK = "http://maps.google.com/maps?q=";
    private static final String SELF_DESTRUCT_IN = "Self destruct in ";
    private static final String PARSE_COLOR = "#808080";
    private static final String COMMENTS = "comments";
    private static final String RATING = "rating";
    private static final String FEEDBACK = "feedback";
    private static final String DAY_FORMAT = "EEEE";
    private static final String DATE_FORMAT = "MMM dd, yyyy";

    public void setAlCustomizationSettings(CustomizationSettings customizationSettings) {
        this.customizationSettings = customizationSettings;
        themeHelper = KmThemeHelper.getInstance(context, customizationSettings);
        initRadius();
        useInnerTimeStampDesign = customizationSettings.getInnerTimestampDesign();
    }

    public void setupDarkMode(boolean isDarkModeEnabled) {
        this.isDarkModeEnabled = isDarkModeEnabled;
    }

    public void setFontManager(KmFontManager fontManager) {
        this.fontManager = fontManager;
    }

    public void initRadius() {
        if (customizationSettings.getSentMessageCornerRadii() != null) {
            for (int i = 0; i < customizationSettings.getSentMessageCornerRadii().length; i++) {
                if (i < customizationSettings.getSentMessageCornerRadii().length && i < 4) {
                    sentMessageCornerRadii[i * 2] = sentMessageCornerRadii[(i * 2) + 1] = DimensionsUtils.convertDpToPixel(customizationSettings.getSentMessageCornerRadii()[i]);
                }
            }
        }

        if (customizationSettings.getReceivedMessageCornerRadii() != null) {
            for (int i = 0; i < customizationSettings.getReceivedMessageCornerRadii().length; i++) {
                if (i < customizationSettings.getReceivedMessageCornerRadii().length && i < 4) {
                    receivedMessageCornerRadii[i * 2] = receivedMessageCornerRadii[(i * 2) + 1] = DimensionsUtils.convertDpToPixel(customizationSettings.getReceivedMessageCornerRadii()[i]);
                }
            }
        }
    }

    public void setContextMenuClickListener(ContextMenuClickListener contextMenuClickListener) {
        this.contextMenuClickListener = contextMenuClickListener;
    }

    public void setRichMessageCallbackListener(KmRichMessageListener listener) {
        this.listener = listener;
    }

    public void setStoragePermissionListener(KmStoragePermissionListener storagePermissionListener) {
        this.storagePermissionListener = storagePermissionListener;
    }

    public DetailedConversationAdapter(final Context context, int textViewResourceId, List<Message> messageList, Channel channel, Class messageIntentClass, EmojiconHandler emojiconHandler) {
        this(context, textViewResourceId, messageList, null, channel, messageIntentClass, emojiconHandler, null);
    }

    public DetailedConversationAdapter(final Context context, int textViewResourceId, List<Message> messageList, Contact contact, Class messageIntentClass, EmojiconHandler emojiconHandler) {
        this(context, textViewResourceId, messageList, contact, null, messageIntentClass, emojiconHandler, null);
    }

    public void setLastSentMessage(Message message) {
        this.lastSentMessage = message;
    }

    public DetailedConversationAdapter(final Context context, int textViewResourceId, List<Message> messageList, final Contact contact, Channel channel, Class messageIntentClass, EmojiconHandler emojiconHandler, CustomizationSettings customizationSettings) {
        //super(context, textViewResourceId, messageList);
        this.messageIntentClass = messageIntentClass;
        this.context = context;
        this.contact = contact;
        this.channel = channel;
        this.emojiconHandler = emojiconHandler;
        this.individual = (contact != null || channel != null);
        this.fileClientService = new FileClientService(context);
        this.messageDatabaseService = new MessageDatabaseService(context);
        this.conversationService = new MobiComConversationService(context);
        this.contactService = new AppContactService(context);
        this.imageCache = ImageCache.getInstance(((FragmentActivity) context).getSupportFragmentManager(), 0.1f);
        this.messageList = messageList;
        this.customizationSettings = customizationSettings;
        geoApiKey = KommunicateSettings.getInstance(context).getGeoApiKey();
        contactImageLoader = new ImageLoader(context, ImageUtils.getLargestScreenDimension((Activity) context)) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return contactService.downloadContactImage(context, (Contact) data);
            }
        };
        contactImageLoader.setLoadingImage(R.drawable.km_ic_contact_picture_180_holo_light);
        contactImageLoader.addImageCache(((FragmentActivity) context).getSupportFragmentManager(), 0.1f);
        contactImageLoader.setImageFadeIn(false);

        loadImage = new ImageLoader(context, ImageUtils.getLargestScreenDimension((Activity) context)) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return fileClientService.loadMessageImage(context, (String) data);
            }
        };
        loadImage.setImageFadeIn(false);
        loadImage.addImageCache(((FragmentActivity) context).getSupportFragmentManager(), 0.1f);
        imageThumbnailLoader = new ImageLoader(context, ImageUtils.getLargestScreenDimension((Activity) context)) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return fileClientService.loadThumbnailImage(context, (Message) data, getImageLayoutParam(false).width, getImageLayoutParam(false).height);
            }
        };
        imageThumbnailLoader.setImageFadeIn(false);
        imageThumbnailLoader.addImageCache(((FragmentActivity) context).getSupportFragmentManager(), 0.1f);

        if (useInnerTimeStampDesign || (customizationSettings != null && !TextUtils.isEmpty(isDarkModeEnabled ? customizationSettings.getMessageStatusIconColor().get(1) : customizationSettings.getMessageStatusIconColor().get(0)))) {
            sentIcon = context.getResources().getDrawable(R.drawable.km_sent_icon_c);
            deliveredIcon = context.getResources().getDrawable(R.drawable.km_delivered_icon_c);
            readIcon = context.getResources().getDrawable(R.drawable.km_read_icon_c);
            pendingIcon = context.getResources().getDrawable(R.drawable.km_pending_icon_c);
        } else {
            sentIcon = AppCompatResources.getDrawable(context, R.drawable.km_sent_icon);
            deliveredIcon = AppCompatResources.getDrawable(context, R.drawable.km_delivered_icon);
            readIcon = AppCompatResources.getDrawable(context, R.drawable.km_read_icon);
            pendingIcon = AppCompatResources.getDrawable(context, R.drawable.km_pending_message_icon);
        }
        final String alphabet = context.getString(R.string.alphabet);
        highlightTextSpan = new TextAppearanceSpan(context, R.style.searchTextHiglight);
    }

    /**
     * Creates different view holder according to message type
     * View mainThreadView : Main thread messages ( both sent and received)
     * View dateViewHolder : Date message viewholder
     * View customViewHolder : Custom message viewholder
     * View customMessageViewHolder : Channel custom message viewholder
     * View callViewHolder : Call message viewholder
     * View feedbackViewHolder : Feedback message viewholder
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (layoutInflater == null) {
            return null;
        }

        if (viewType == 2) {
            View dateViewHolder = layoutInflater.inflate(R.layout.date_layout, parent, false);
            return new MyViewHolder2(dateViewHolder);
        } else if (viewType == 3) {
            View customViewHolder = layoutInflater.inflate(R.layout.km_custom_message_layout, parent, false);
            return new MyViewHolder3(customViewHolder);
        } else if (viewType == 4) {
            if (customizationSettings.isAgentApp()) {
                View customMessageViewHolder = layoutInflater.inflate(R.layout.km_channel_custom_message_layout, parent, false);
                return new MyViewHolder4(customMessageViewHolder, customizationSettings.isAgentApp());
            } else {
                View customMessageViewHolder = layoutInflater.inflate(R.layout.km_new_channel_custom_message, parent, false);
                return new MyViewHolder4(customMessageViewHolder, customizationSettings.isAgentApp());
            }
        } else if (viewType == 5) {
            View callViewHolder = layoutInflater.inflate(R.layout.km_call_layout, parent, false);
            return new MyViewHolder5(callViewHolder);
        } else if (viewType == 6) {
            View feedbackViewHolder = layoutInflater.inflate(R.layout.km_feedback_agent_layout, parent, false);
            return new MyViewHolder6(feedbackViewHolder);
        } else if (viewType == 0) {
            View mainThreadView;
            if (useInnerTimeStampDesign) {
                mainThreadView = layoutInflater.inflate(R.layout.received_message_list_view, parent, false);
            } else {
                mainThreadView = layoutInflater.inflate(R.layout.km_received_message_list_view, parent, false);
            }
            return new MyViewHolder(mainThreadView);
        } else if (viewType == 7) {
            View feedbackViewHolder = layoutInflater.inflate(R.layout.km_static_top_message, parent, false);
            return new StaticMessageHolder(feedbackViewHolder);
        } else if (viewType == 8) {
            View typingViewHolder = layoutInflater.inflate(R.layout.km_typing_indicator_layout, parent, false);
            return new TypingMessageHolder(typingViewHolder, isDarkModeEnabled);
        }
        if (useInnerTimeStampDesign) {
            view = layoutInflater.inflate(R.layout.sent_message_list_view, parent, false);
        } else {
            view = layoutInflater.inflate(R.layout.km_sent_message_list_view, parent, false);
        }
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        final Message message = getItem(position);

        try {
            if (type == 7) {
                StaticMessageHolder messageHolder = (StaticMessageHolder) holder;
                messageHolder.topMessageTextView.setText(customizationSettings.getStaticTopMessage());
                if (!TextUtils.isEmpty(customizationSettings.getStaticTopMessage())) {
                    messageHolder.topMessageImageView.setImageResource(context.getResources().getIdentifier(customizationSettings.getStaticTopIcon(), "drawable", context.getPackageName()));
                }

            } else if (type == 2) {
                MyViewHolder2 myViewHolder2 = (MyViewHolder2) holder;
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
                SimpleDateFormat simpleDateFormatDay = new SimpleDateFormat(DAY_FORMAT);
                Date date = new Date(message.getCreatedAtTime());

                myViewHolder2.dateView.setTextColor(Color.parseColor(isDarkModeEnabled ? customizationSettings.getConversationDateTextColor().get(1).trim() : customizationSettings.getConversationDateTextColor().get(0).trim()));
                myViewHolder2.dayTextView.setTextColor(Color.parseColor(isDarkModeEnabled ? customizationSettings.getConversationDayTextColor().get(1).trim() : customizationSettings.getConversationDayTextColor().get(0).trim()));

                if (DateUtils.isSameDay(message.getCreatedAtTime())) {
                    myViewHolder2.dayTextView.setVisibility(View.VISIBLE);
                    myViewHolder2.dateView.setVisibility(GONE);
                    myViewHolder2.dayTextView.setText(R.string.today);
                } else {
                    myViewHolder2.dayTextView.setVisibility(View.VISIBLE);
                    myViewHolder2.dateView.setVisibility(View.VISIBLE);
                    myViewHolder2.dayTextView.setText(simpleDateFormatDay.format(date));
                    myViewHolder2.dateView.setText(simpleDateFormat.format(date));
                }
                return;
            } else if (type == 3) {
                MyViewHolder3 myViewHolder3 = (MyViewHolder3) holder;
                myViewHolder3.customContentTextView.setText(message.getMessage());
                myViewHolder3.customContentTextView.setVisibility(VISIBLE);
                return;
            } else if (type == 4) {
                MyViewHolder4 myViewHolder4 = (MyViewHolder4) holder;

                if (message.getMetadata().containsKey(KM_SUMMARY)
                        && Objects.equals(message.getMetadata().get(KM_SUMMARY), "true")
                ) {
                    myViewHolder4.normalTextLayout.setVisibility(GONE);

                    if (customizationSettings.isAgentApp()) {
                        // Show summary UI
                        myViewHolder4.summaryCardView.setVisibility(VISIBLE);
                        myViewHolder4.summaryMessage.setText(message.getMessage());
                        myViewHolder4.summaryReadMore.setOnClickListener(view -> {
                            // Open Dialog...
                            createCustomDialog(message.getMessage());
                        });
                    }
                } else if (customizationSettings.isAgentApp()) {
                    myViewHolder4.summaryCardView.setVisibility(GONE);
                    myViewHolder4.normalTextLayout.setVisibility(VISIBLE);

                    GradientDrawable bgGradientDrawable = (GradientDrawable) myViewHolder4.channelMessageTextView.getBackground();
                    bgGradientDrawable.setColor(Color.parseColor(isDarkModeEnabled ? customizationSettings.getChannelCustomMessageBgColor().get(1) :
                            customizationSettings.getChannelCustomMessageBgColor().get(0)));
                    bgGradientDrawable.setStroke(3, Color.parseColor(isDarkModeEnabled ? customizationSettings.getChannelCustomMessageBorderColor().get(1) : customizationSettings.getChannelCustomMessageBorderColor().get(0)));
                    myViewHolder4.channelMessageTextView.setTextColor(Color.parseColor(isDarkModeEnabled ? customizationSettings.getChannelCustomMessageTextColor().get(1) : customizationSettings.getChannelCustomMessageTextColor().get(0)));
                    myViewHolder4.channelMessageTextView.setText(message.getMessage());
                } else {
                    myViewHolder4.channelMessageTextView.setText(message.getLocalizationValue());
                    myViewHolder4.channelMessageTextView.setTextColor(Color.parseColor(isDarkModeEnabled ? customizationSettings.getChannelCustomMessageTextColor().get(1) : customizationSettings.getChannelCustomMessageTextColor().get(0)));
                    myViewHolder4.channelMessageStaticText.setTextColor(Color.parseColor(isDarkModeEnabled ? customizationSettings.getChannelCustomMessageTextColor().get(1) : customizationSettings.getChannelCustomMessageTextColor().get(0)));
                    myViewHolder4.channelMessageLeftBg.setBackgroundColor(Color.parseColor(isDarkModeEnabled ? customizationSettings.getChannelCustomMessageBgColor().get(1) : customizationSettings.getChannelCustomMessageBgColor().get(0)));
                    myViewHolder4.channelMessageRightBg.setBackgroundColor(Color.parseColor(isDarkModeEnabled ? customizationSettings.getChannelCustomMessageBgColor().get(1) : customizationSettings.getChannelCustomMessageBgColor().get(0)));
                }
                return;
            } else if (type == 5) {
                MyViewHolder5 myViewHolder5 = (MyViewHolder5) holder;

                if (message != null) {
                    myViewHolder5.timeTextView.setText(DateUtils.getFormattedDate(message.getCreatedAtTime()));
                    if (message.getMetadata() != null) {
                        myViewHolder5.statusTextView.setText(VideoCallNotificationHelper.getStatus(message.getMetadata()));
                    }

                    if (VideoCallNotificationHelper.isMissedCall(message)) {
                        myViewHolder5.imageView.setImageResource(R.drawable.ic_communication_call_missed);
                    }

                    if (VideoCallNotificationHelper.isAudioCall(message)) {
                        myViewHolder5.imageView.setImageResource(R.drawable.km_ic_action_call);
                    } else {
                        myViewHolder5.imageView.setImageResource(R.drawable.ic_videocam_white_24px);
                    }
                    if (message.getMetadata() != null) {
                        if (message.getMetadata().get(VideoCallNotificationHelper.MSG_TYPE).equals(VideoCallNotificationHelper.CALL_END)) {
                            String duration = message.getMetadata().get(VideoCallNotificationHelper.CALL_DURATION);

                            if (!TextUtils.isEmpty(duration)) {
                                myViewHolder5.durationTextView.setVisibility(View.VISIBLE);
                                duration = Utils.getTimeDurationInFormat(Long.parseLong(duration));
                                myViewHolder5.durationTextView.setText(duration);
                            }
                        } else {
                            myViewHolder5.durationTextView.setVisibility(View.GONE);
                        }
                    }
                }
            } else if (type == 6)  {
                MyViewHolder6 myViewholder6 = (MyViewHolder6) holder;
                if (message.getMetadata() != null) {
                    String json = message.getMetadata().get(FEEDBACK);
                    if (json == null) {
                        Sentry.captureException(new RuntimeException("Unable to find feedback in message of type feedback message " + message));
                        return;
                    }

                    JSONObject jsonObject = new JSONObject(json);
                    int ratingValue = (int) jsonObject.get(RATING);

                    if (KmAppSettingPreferences.getRatingBase() != 3) {
                        switch (ratingValue) {
                            case 1:
                                myViewholder6.imageViewFeedbackRating.setImageDrawable(ContextCompat.getDrawable(context,  R.drawable.star));
                                break;
                            case 2:
                                myViewholder6.imageViewFeedbackRating.setImageDrawable(ContextCompat.getDrawable(context,  R.drawable.ic_two_star_filled));
                                break;
                            case 4:
                                myViewholder6.imageViewFeedbackRating.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_four_star_filled));
                                break;
                            case 5:
                                myViewholder6.imageViewFeedbackRating.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_five_star_filled));
                                break;
                            default:
                                myViewholder6.imageViewFeedbackRating.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_three_star_filled));
                        }
                        if (customizationSettings.isAgentApp()) {
                            myViewholder6.textViewFeedbackText.setText(context.getString(R.string.user_rating_text));
                            myViewholder6.textViewFeedbackText.setVisibility(View.VISIBLE);

                        } else {
                            myViewholder6.textViewFeedbackText.setText("");
                            myViewholder6.textViewFeedbackText.setVisibility(GONE);
                        }
                        if (!jsonObject.has(COMMENTS)) {
                            myViewholder6.scrollViewFeedbackCommentWrap.setVisibility(GONE);
                            return;
                        }
                        String comment = String.valueOf(jsonObject.get(COMMENTS));
                        myViewholder6.scrollViewFeedbackCommentWrap.setVisibility(View.VISIBLE);
                        myViewholder6.textViewFeedbackComment.setText(comment);
                    } else {
                        switch (ratingValue) {
                            case FeedbackInputFragment.RATING_POOR:
                                myViewholder6.imageViewFeedbackRating.setImageDrawable(ContextCompat.getDrawable(context, io.kommunicate.ui.R.drawable.ic_sad_1));
                                break;
                            case FeedbackInputFragment.RATING_GOOD:
                                myViewholder6.imageViewFeedbackRating.setImageDrawable(ContextCompat.getDrawable(context, io.kommunicate.ui.R.drawable.ic_happy));
                                break;
                            default:
                                myViewholder6.imageViewFeedbackRating.setImageDrawable(ContextCompat.getDrawable(context, io.kommunicate.ui.R.drawable.ic_confused));

                        }
                        myViewholder6.textViewFeedbackText.setVisibility(View.VISIBLE);

                        if (!jsonObject.has(COMMENTS)) {
                            myViewholder6.scrollViewFeedbackCommentWrap.setVisibility(GONE);
                            return;
                        }
                        String comment = String.valueOf(jsonObject.get(COMMENTS));
                        myViewholder6.scrollViewFeedbackCommentWrap.setVisibility(View.VISIBLE);
                        myViewholder6.textViewFeedbackComment.setText(comment);

                        if (customizationSettings.isAgentApp()) {
                            myViewholder6.textViewFeedbackText.setText(context.getString(R.string.user_rating_text));
                        } else {
                            myViewholder6.textViewFeedbackText.setText(context.getString(R.string.rating_text));
                        }
                    }
                }
            } else if (type == 8) {
                TypingMessageHolder typingMessageHolder = (TypingMessageHolder) holder;
                typingMessageHolder.setupDarkMode(isDarkModeEnabled);
                if (messageList.size() - 1 != position) {
                    ((TypingMessageHolder) holder).parentLayout.setVisibility(GONE);
                } else {
                    ((TypingMessageHolder) holder).parentLayout.setVisibility(View.VISIBLE);
                }
            } else {
                bindMessageView(holder, message, position);
            }
        } catch (Exception e) {
            Hint hint = new Hint();
            hint.set("MESSAGE", message);
            Sentry.captureException(e, hint);
        }
    }

    private void createCustomDialog(String message) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_dialog_layout);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView dialogMessage = dialog.findViewById(R.id.dialog_message);
        dialogMessage.setText(message);

        ImageButton btnClose = dialog.findViewById(R.id.dialog_close);
        btnClose.setOnClickListener( view -> {
                dialog.dismiss();
        });

        dialog.show();
    }

    protected void bindMessageView(RecyclerView.ViewHolder holder, final Message message, final int position) {
        final MyViewHolder myHolder = (MyViewHolder) holder;
        if (message != null) {
            Contact receiverContact = null;
            Contact contactDisplayName = null;

            int index = messageList.indexOf(message);
            boolean hideRecursiveImages = false;
            boolean showTimestamp;
            if (useInnerTimeStampDesign) {
                showTimestamp = message.isTypeOutbox() || index == messageList.size() - 1 || !messageList.get(index + 1).isRichMessage();
            } else if (message.isTypeOutbox()) {
                showTimestamp = index == messageList.size() - 1
                        || messageList.get(index + 1).getCreatedAtTime() - message.getCreatedAtTime() > KmConstants.MESSAGE_CLUBBING_TIME_FRAME
                        || !messageList.get(index + 1).isTypeOutbox();
            } else {
                showTimestamp = index == messageList.size() - 1
                        || messageList.get(index + 1).getCreatedAtTime() - message.getCreatedAtTime() > KmConstants.MESSAGE_CLUBBING_TIME_FRAME
                        || messageList.get(index + 1).isTypeOutbox()
                        || !message.getContactIds().equals(messageList.get(index + 1).getContactIds())
                        || messageList.get(index + 1).isActionMessage();
            }

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) myHolder.messageRootLayout.getLayoutParams();
            if (!message.isTypeOutbox()) {
                if (index != 0 && !messageList.get(index - 1).isTypeOutbox()
                        && messageList.get(index - 1).getContentType() != 10
                        && messageList.get(index - 1).getContentType() != 103
                        && messageList.get(index - 1).getTo() != null
                        && message.getTo() != null
                        && messageList.get(index - 1).getTo().equals(message.getTo())) {

                    hideRecursiveImages = true;
                    params.setMargins(0, DimensionsUtils.convertDpToPx(3), 0, 0);
                } else {
                    params.setMargins(0, DimensionsUtils.convertDpToPx(8), 0, 0);
                }
            } else {
                if (index != 0 && !messageList.get(index - 1).isTypeOutbox()) {
                    params.setMargins(0, DimensionsUtils.convertDpToPx(8), 0, 0);
                } else {
                    params.setMargins(0, DimensionsUtils.convertDpToPx(3), 0, 0);
                }
            }

            if (message.getGroupId() == null) {
                List<String> items = Arrays.asList(message.getContactIds().split("\\s*,\\s*"));
                List<String> userIds = null;
                if (!TextUtils.isEmpty(message.getContactIds())) {
                    userIds = Arrays.asList(message.getContactIds().split("\\s*,\\s*"));
                }
                if (individual) {
                    receiverContact = contact;
                    contact.setContactNumber(items.get(0));
                    if (userIds != null) {
                        contact.setUserId(userIds.get(0));
                    }
                } else {
                    receiverContact = contactService.getContactReceiver(items, userIds);
                }
            } else {
                if (!TextUtils.isEmpty(message.getContactIds())) {
                    contactDisplayName = contactService.getContactById(message.getContactIds());
                }
            }
            Configuration config = context.getResources().getConfiguration();
            if (message.getMetadata() != null && !message.getMetadata().isEmpty() && message.getMetadata().containsKey(Message.MetaDataType.AL_REPLY.getValue())) {
                final Message msg = messageDatabaseService.getMessage(message.getMetaDataValueForKey(Message.MetaDataType.AL_REPLY.getValue()));
                if (msg != null) {
                    String displayName;

                    myHolder.replyRelativeLayout.setBackgroundColor(message.isTypeOutbox() ?
                            Color.parseColor(isDarkModeEnabled ? customizationSettings.getReplyMessageLayoutSentMessageBackground().get(1) : customizationSettings.getReplyMessageLayoutSentMessageBackground().get(0)) : Color.parseColor(isDarkModeEnabled ? customizationSettings.getReplyMessageLayoutReceivedMessageBackground().get(1) : customizationSettings.getReplyMessageLayoutReceivedMessageBackground().get(0)));

                    myHolder.replyNameTextView.setTextColor(message.isTypeOutbox() ?
                            Color.parseColor(isDarkModeEnabled ? customizationSettings.getSentMessageTextColor().get(1) : customizationSettings.getSentMessageTextColor().get(0)) : Color.parseColor(isDarkModeEnabled ? customizationSettings.getReceivedMessageTextColor().get(1) : customizationSettings.getReceivedMessageTextColor().get(0)));

                    myHolder.replyMessageTextView.setTextColor(message.isTypeOutbox() ?
                            Color.parseColor(isDarkModeEnabled ? customizationSettings.getSentMessageTextColor().get(1) : customizationSettings.getSentMessageTextColor().get(0)) : Color.parseColor(isDarkModeEnabled ? customizationSettings.getReceivedMessageTextColor().get(1) : customizationSettings.getReceivedMessageTextColor().get(0)));

                    if (msg.getGroupId() != null) {
                        if (MobiComUserPreference.getInstance(context).getUserId().equals(msg.getContactIds()) || TextUtils.isEmpty(msg.getContactIds())) {
                            displayName = context.getString(R.string.you_string);
                        } else {
                            displayName = contactService.getContactById(msg.getContactIds()).getDisplayName();
                        }
                    } else {
                        if (msg.isTypeOutbox()) {
                            displayName = context.getString(R.string.you_string);
                        } else {
                            displayName = contactService.getContactById(msg.getContactIds()).getDisplayName();
                        }
                    }

                    myHolder.replyNameTextView.setText(displayName);
                    if (msg.hasAttachment()) {
                        FileMeta fileMeta = msg.getFileMetas();
                        myHolder.imageViewForAttachmentType.setVisibility(View.VISIBLE);
                        if (fileMeta.getContentType().contains(IMAGE)) {
                            myHolder.imageViewForAttachmentType.setImageResource(R.drawable.km_ic_image_camera_alt);
                            if (TextUtils.isEmpty(msg.getMessage())) {
                                myHolder.replyMessageTextView.setText(context.getString(R.string.photo_string));
                            } else {
                                myHolder.replyMessageTextView.setText(msg.getMessage());
                            }
                            myHolder.imageViewPhoto.setVisibility(View.VISIBLE);
                            myHolder.imageViewRLayout.setVisibility(View.VISIBLE);
                            imageThumbnailLoader.loadImage(msg, myHolder.imageViewPhoto);
                        } else if (fileMeta.getContentType().contains(VIDEO)) {
                            myHolder.imageViewForAttachmentType.setImageResource(R.drawable.km_ic_action_video);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                if (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                                    myHolder.imageViewForAttachmentType.setScaleX(-1);
                                }
                            }
                            if (TextUtils.isEmpty(msg.getMessage())) {
                                myHolder.replyMessageTextView.setText(context.getString(R.string.video_string));
                            } else {
                                myHolder.replyMessageTextView.setText(msg.getMessage());
                            }
                            myHolder.imageViewPhoto.setVisibility(View.VISIBLE);
                            myHolder.imageViewRLayout.setVisibility(View.VISIBLE);
                            if (msg.getFilePaths() != null && msg.getFilePaths().size() > 0) {
                                if (imageCache.getBitmapFromMemCache(msg.getKeyString()) != null) {
                                    myHolder.imageViewPhoto.setImageBitmap(imageCache.getBitmapFromMemCache(msg.getKeyString()));
                                } else {
                                    imageCache.addBitmapToCache(message.getKeyString(), fileClientService.createAndSaveVideoThumbnail(msg.getFilePaths().get(0)));
                                    myHolder.imageViewPhoto.setImageBitmap(fileClientService.createAndSaveVideoThumbnail(msg.getFilePaths().get(0)));
                                }
                            }
                        } else if (fileMeta.getContentType().contains(AUDIO)) {
                            myHolder.imageViewForAttachmentType.setImageResource(R.drawable.km_ic_music_note);
                            if (TextUtils.isEmpty(msg.getMessage())) {
                                myHolder.replyMessageTextView.setText(context.getString(R.string.audio_string));
                            } else {
                                myHolder.replyMessageTextView.setText(msg.getMessage());
                            }
                            myHolder.imageViewPhoto.setVisibility(View.GONE);
                            myHolder.imageViewRLayout.setVisibility(View.GONE);
                        } else if (msg.isContactMessage()) {
                            MobiComVCFParser parser = new MobiComVCFParser();
                            try {
                                VCFContactData data = parser.parseCVFContactData(msg.getFilePaths().get(0));
                                if (data != null) {
                                    myHolder.imageViewForAttachmentType.setImageResource(R.drawable.km_ic_person_white);
                                    myHolder.replyMessageTextView.setText(context.getString(R.string.contact_string));
                                    myHolder.replyMessageTextView.append(" " + data.getName());
                                }
                            } catch (Exception e) {
                                myHolder.imageViewForAttachmentType.setImageResource(R.drawable.km_ic_person_white);
                                myHolder.replyMessageTextView.setText(context.getString(R.string.contact_string));
                            }
                            myHolder.imageViewPhoto.setVisibility(View.GONE);
                            myHolder.imageViewRLayout.setVisibility(View.GONE);
                        } else {
                            myHolder.imageViewForAttachmentType.setImageResource(R.drawable.km_ic_action_attachment);
                            if (TextUtils.isEmpty(msg.getMessage())) {
                                myHolder.replyMessageTextView.setText(context.getString(R.string.attachment_string));
                            } else {
                                myHolder.replyMessageTextView.setText(KmUtils.getAttachmentName(msg));
                            }
                            myHolder.imageViewPhoto.setVisibility(View.GONE);
                            myHolder.imageViewRLayout.setVisibility(View.GONE);
                        }
                        myHolder.imageViewForAttachmentType.setColorFilter(Color.parseColor(message.isTypeOutbox() ? (isDarkModeEnabled ? customizationSettings.getSentMessageTextColor().get(1) : customizationSettings.getSentMessageTextColor().get(0)) : (isDarkModeEnabled ? customizationSettings.getReceivedMessageTextColor().get(1) : customizationSettings.getReceivedMessageTextColor().get(0))));
                    } else if (msg.getContentType() == Message.ContentType.LOCATION.getValue()) {
                        myHolder.imageViewForAttachmentType.setVisibility(View.VISIBLE);
                        myHolder.imageViewPhoto.setVisibility(View.VISIBLE);
                        myHolder.imageViewRLayout.setVisibility(View.VISIBLE);
                        myHolder.replyMessageTextView.setText(context.getString(R.string.al_location_string));
                        myHolder.imageViewForAttachmentType.setColorFilter(Color.parseColor(message.isTypeOutbox() ? (isDarkModeEnabled ? customizationSettings.getSentMessageTextColor().get(1) : customizationSettings.getSentMessageTextColor().get(0)) : (isDarkModeEnabled ? customizationSettings.getReceivedMessageTextColor().get(1) : customizationSettings.getReceivedMessageTextColor().get(0))));
                        myHolder.imageViewForAttachmentType.setImageResource(R.drawable.km_ic_location_on_white_24dp);
                        loadImage.setLoadingImage(R.drawable.km_map_offline_thumbnail);
                        loadImage.loadImage(LocationUtils.loadStaticMap(msg.getMessage(), geoApiKey), myHolder.imageViewPhoto);
                    } else {
                        myHolder.imageViewForAttachmentType.setVisibility(View.GONE);
                        myHolder.imageViewRLayout.setVisibility(View.GONE);
                        myHolder.imageViewPhoto.setVisibility(View.GONE);
                        myHolder.replyMessageTextView.setText(KmUtils.getAttachmentName(msg));
                    }
                    myHolder.replyRelativeLayout.setVisibility(View.VISIBLE);
                    myHolder.replyRelativeLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((OnClickReplyInterface) context).onClickOnMessageReply(msg);
                        }
                    });
                }
            } else {
                myHolder.replyRelativeLayout.setVisibility(GONE);
            }

            if (TextUtils.isEmpty(message.getMessage())) {
                myHolder.messageTextView.setVisibility(View.GONE);
            } else {
                myHolder.messageTextView.setVisibility((message.getContentType() == Message.ContentType.LOCATION.getValue() || isHtmlTypeMessage(message)) ? View.GONE : View.VISIBLE);
            }

            myHolder.mapImageView.setVisibility(GONE);

            if (channel != null) {
                if (!message.hasAttachment() && TextUtils.isEmpty(message.getMessage()) && message.getMetadata() == null) {
                    myHolder.messageTextView.setText("");
                }
            }

            if (myHolder.chatLocation != null) {
                myHolder.chatLocation.setVisibility(View.GONE);
            }

            if (myHolder.attachedFile != null) {
                //myHolder.attachedFile.setText("");
                myHolder.attachedFile.setVisibility(View.GONE);
            }

            if (myHolder.attachmentIcon != null) {
                myHolder.attachmentIcon.setVisibility(View.GONE);
            }

            if (channel != null && myHolder.nameTextView != null && contactDisplayName != null) {
                myHolder.nameTextView.setVisibility(Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType()) || hideRecursiveImages ? View.GONE : View.VISIBLE);
                if (customizationSettings.isLaunchChatFromProfilePicOrName()) {
                    myHolder.nameTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(context, ConversationActivity.class);
                            intent.putExtra(ConversationUIService.USER_ID, message.getContactIds());
                            if (message.getConversationId() != null) {
                                intent.putExtra(ConversationUIService.CONVERSATION_ID, message.getConversationId());
                            }
                            context.startActivity(intent);
                        }
                    });
                }
                String customBotName = KmUtils.getCustomBotName(message, context);
                if (!TextUtils.isEmpty(customBotName)) {
                    myHolder.nameTextView.setText(customBotName);
                } else {
                    myHolder.nameTextView.setText(contactDisplayName.getDisplayName());
                }
                myHolder.nameTextView.setTextColor(Color.parseColor(isDarkModeEnabled ? customizationSettings.getReceiverNameTextColor().get(1) : customizationSettings.getReceiverNameTextColor().get(0)));
            }

            if (message.isTypeOutbox()) {
                myHolder.createdAtTime.setTextColor(
                        themeHelper.parseColorWithDefault(customizationSettings.getSentMessageCreatedAtTimeColor().get(isDarkModeEnabled ? 1 : 0),
                                Color.parseColor(PARSE_COLOR)));
            } else {
                myHolder.createdAtTime.setTextColor(
                        themeHelper.parseColorWithDefault(customizationSettings.getReceivedMessageCreatedAtTimeColor().get(isDarkModeEnabled ? 1 : 0),
                                Color.parseColor(PARSE_COLOR)));
            }

            if (isHtmlTypeMessage(message)) {
                if (myHolder.viaEmailView != null) {
                    myHolder.viaEmailView.setVisibility(isEmailTypeMessage(message) ? View.VISIBLE : GONE);

                    if (message.isTypeOutbox()) {
                        myHolder.viaEmailView.setTextColor(context.getResources().getColor(R.color.white));
                    } else {
                        myHolder.viaEmailView.setTextColor(context.getResources().getColor(R.color.km_via_email_text_color));
                    }
                }
                if (myHolder.emailLayout != null) {
                    myHolder.emailLayout.setVisibility(View.VISIBLE);
                    loadHtml(myHolder.emailLayout, message);
                }

            } else {
                if (myHolder.viaEmailView != null) {
                    myHolder.viaEmailView.setVisibility(GONE);
                }
                if (myHolder.emailLayout != null) {
                    myHolder.emailLayout.setVisibility(View.GONE);
                }
            }

            myHolder.attachmentDownloadLayout.setVisibility(View.GONE);
            myHolder.attachmentView.setVisibility(View.GONE);

            if (message.isTypeOutbox() && !message.isCanceled()) {
                myHolder.mediaUploadProgressBar.setVisibility(View.GONE);
                myHolder.mediaUploadProgressBar.setVisibility(message.isAttachmentUploadInProgress() ? View.VISIBLE : View.GONE);
            } else {
                myHolder.mediaUploadProgressBar.setVisibility(View.GONE);
            }

            if (myHolder.attachedFile != null) {
                myHolder.attachedFile.setVisibility(message.hasAttachment() ? View.VISIBLE : View.GONE);
            }

            if (individual && message.getTimeToLive() != null) {
                myHolder.selfDestruct
                        .setText(SELF_DESTRUCT_IN + message.getTimeToLive() + " mins");
                myHolder.selfDestruct.setVisibility(View.VISIBLE);
            } else {
                myHolder.selfDestruct.setText("");
                myHolder.selfDestruct.setVisibility(View.GONE);
            }

            if (myHolder.sentOrReceived != null) {
                if ((!message.isCall()) || message.isDummyEmptyMessage()) {
                    myHolder.sentOrReceived.setVisibility(View.GONE);
                } else if (message.isCall()) {
                    myHolder.sentOrReceived.setImageResource(R.drawable.km_ic_action_call_holo_light);
                } else if (getItemViewType(position) == 0) {
                    myHolder.sentOrReceived.setImageResource(R.drawable.social_forward);
                } else {
                    myHolder.sentOrReceived.setImageResource(R.drawable.social_reply);
                }

                if (message.isCall()) {
                    myHolder.messageTextView.setTextColor(context.getResources().getColor(message.isIncomingCall() ? R.color.incoming_call : R.color.outgoing_call));
                }
            }

            if (myHolder.nameTextLayout != null && contact != null && hideRecursiveImages) {
                myHolder.nameTextLayout.setVisibility(View.GONE);
            }

            if (message.isCall() || message.isDummyEmptyMessage()) {
                if (useInnerTimeStampDesign) {
                    myHolder.statusTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                } else {
                    myHolder.statusImageView.setImageDrawable(null);
                }
            } else if (!message.isSentToServer() && message.isTypeOutbox()) {
                if (useInnerTimeStampDesign) {
                    myHolder.statusTextView.setCompoundDrawablesWithIntrinsicBounds(pendingIcon, null, null, null);
                } else {
                    myHolder.statusImageView.setImageDrawable(pendingIcon);
                }
            } else if (message.getKeyString() != null && message.isTypeOutbox() && message.isSentToServer()) {
                Drawable statusIcon;
                if (message.isDeliveredAndRead()) {
                    statusIcon = readIcon;
                } else {
                    statusIcon = (message.getDelivered() ? deliveredIcon : sentIcon);
                }
                if (useInnerTimeStampDesign) {
                    myHolder.statusTextView.setCompoundDrawablesWithIntrinsicBounds(statusIcon, null, null, null);
                } else {
                    myHolder.statusImageView.setImageDrawable(statusIcon);
                }

                //
            } else {
                //donothing
            }

            if (message.isCall()) {
                myHolder.deliveryStatus.setText("");
            }

            if (contactDisplayName != null && myHolder.contactImage != null && customizationSettings.isLaunchChatFromProfilePicOrName()) {
                myHolder.contactImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, ConversationActivity.class);
                        intent.putExtra(ConversationUIService.USER_ID, message.getContactIds());
                        if (message.getConversationId() != null) {
                            intent.putExtra(ConversationUIService.CONVERSATION_ID, message.getConversationId());
                        }
                        context.startActivity(intent);
                    }
                });

                myHolder.alphabeticTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, ConversationActivity.class);
                        intent.putExtra(ConversationUIService.USER_ID, message.getContactIds());
                        if (message.getConversationId() != null) {
                            intent.putExtra(ConversationUIService.CONVERSATION_ID, message.getConversationId());
                        }
                        context.startActivity(intent);
                    }
                });
            }

            if (!message.isTypeOutbox()) {
                loadProfileImage(receiverContact != null ? receiverContact : contactDisplayName, myHolder.contactImage, myHolder.alphabeticTextView, hideRecursiveImages);
            }
            KmDocumentView audioView = new KmDocumentView(this.context, storagePermissionListener);
            audioView.inflateViewWithMessage(myHolder.view, message);
            audioView.hideView(true);

            if (message.hasAttachment() && myHolder.attachedFile != null & !(message.getContentType() == Message.ContentType.TEXT_URL.getValue())) {
                myHolder.mainAttachmentLayout.setLayoutParams(getImageLayoutParam(false));
                myHolder.mainAttachmentLayout.setVisibility(View.VISIBLE);
                if (message.getFileMetas() != null && (message.getFileMetas().getContentType().contains(IMAGE) || message.getFileMetas().getContentType().contains(VIDEO))) {
                    myHolder.attachedFile.setVisibility(View.GONE);
                }
                if (message.isAttachmentDownloaded()) {
                    myHolder.mapImageView.setVisibility(View.GONE);
                    //myHolder.preview.setVisibility(View.GONE);
                    String[] filePaths = new String[message.getFilePaths().size()];
                    int i = 0;
                    for (final String filePath : message.getFilePaths()) {
                        filePaths[i++] = filePath;
                        final String mimeType = FileUtils.getMimeType(filePath);
                        if (mimeType != null && mimeType.startsWith(IMAGE)) {
                            myHolder.attachmentView.setVisibility(View.GONE);
                            myHolder.videoIcon.setVisibility(View.GONE);
                            myHolder.preview.setVisibility(View.VISIBLE);
                            myHolder.preview.setImageBitmap(null);
                            myHolder.attachmentDownloadLayout.setVisibility(View.GONE);
                            myHolder.attachmentDownloadProgressLayout.setVisibility(View.GONE);
                            Glide.with(context).load(new File(filePath)).into(myHolder.preview);
                            myHolder.attachmentView.setMessage(message);
                            myHolder.mediaDownloadProgressBar.setVisibility(View.GONE);
                            myHolder.attachedFile.setVisibility(View.GONE);
                            myHolder.attachmentView.setProressBar(myHolder.mediaDownloadProgressBar);
                            myHolder.attachmentView.setDownloadProgressLayout(myHolder.attachmentDownloadProgressLayout);
                        } else if (mimeType != null && mimeType.startsWith(VIDEO)) {
                            myHolder.preview.setVisibility(View.VISIBLE);
                            myHolder.videoIcon.setVisibility(View.VISIBLE);
                            myHolder.mediaDownloadProgressBar.setVisibility(View.GONE);
                            myHolder.attachmentDownloadLayout.setVisibility(View.GONE);
                            myHolder.attachmentDownloadProgressLayout.setVisibility(View.GONE);
                            myHolder.attachedFile.setVisibility(View.GONE);
                            if (imageCache.getBitmapFromMemCache(message.getKeyString()) != null) {
                                myHolder.preview.setImageBitmap(imageCache.getBitmapFromMemCache(message.getKeyString()));
                            } else {
                                imageCache.addBitmapToCache(message.getKeyString(), fileClientService.createAndSaveVideoThumbnail(filePath));
                                myHolder.preview.setImageBitmap(fileClientService.createAndSaveVideoThumbnail(filePath));
                            }
                        } else {
                            myHolder.preview.setVisibility(View.GONE);
                            myHolder.mediaDownloadProgressBar.setVisibility(View.GONE);
                            myHolder.attachmentDownloadLayout.setVisibility(View.GONE);
                            myHolder.attachmentDownloadProgressLayout.setVisibility(View.GONE);
                            showAttachmentIconAndText(myHolder.attachedFile, message, mimeType);
                        }
                    }
                } else if (message.isAttachmentUploadInProgress()) {
                    //showPreview(smListItem, preview, attachmentDownloadLayout);
                    myHolder.preview.setImageDrawable(null);
                    myHolder.preview.setImageBitmap(null);
                    myHolder.attachmentDownloadProgressLayout.setVisibility(View.VISIBLE);
                    myHolder.mediaDownloadProgressBar.setVisibility(View.VISIBLE);
                    myHolder.videoIcon.setVisibility(GONE);
                    myHolder.attachmentView.setProressBar(myHolder.mediaDownloadProgressBar);
                    myHolder.attachmentView.setDownloadProgressLayout(myHolder.attachmentDownloadProgressLayout);
                    myHolder.attachmentView.setMessage(message);
                    myHolder.attachmentView.setVisibility(View.VISIBLE);
                    myHolder.attachedFile.setVisibility(GONE);
                    myHolder.attachmentIcon.setVisibility(GONE);
                } else if (AttachmentManager.isAttachmentInProgress(message.getKeyString())) {
                    //ondraw is called and thread is assigned to the attachment view...
                    myHolder.preview.setImageDrawable(null);
                    myHolder.preview.setImageBitmap(null);
                    myHolder.attachmentView.setMessage(message);
                    myHolder.attachmentView.setVisibility(View.VISIBLE);
                    myHolder.mediaDownloadProgressBar.setVisibility(View.VISIBLE);
                    myHolder.attachmentView.setProressBar(myHolder.mediaDownloadProgressBar);
                    myHolder.attachmentView.setDownloadProgressLayout(myHolder.attachmentDownloadProgressLayout);
                    myHolder.preview.setVisibility(View.VISIBLE);
                    showPreview(message, myHolder.preview, myHolder.attachmentDownloadLayout);
                    FileMeta fileMeta = message.getFileMetas();
                    final String mimeType = FileUtils.getMimeType(fileMeta.getName());
                    if (!fileMeta.getContentType().contains(IMAGE) && !fileMeta.getContentType().contains(VIDEO)) {
                        showAttachmentIconAndText(myHolder.attachedFile, message, mimeType);
                    }
                    myHolder.downloadSizeTextView.setText(fileMeta.getSizeInReadableFormat());
                    myHolder.attachmentView.setDownloadProgressLayout(myHolder.attachmentDownloadProgressLayout);
                    myHolder.attachmentDownloadProgressLayout.setVisibility(View.VISIBLE);
                } else {
                    String fileKeys = message.getFileMetaKeyStrings();
                    int i = 0;
                    myHolder.preview.setVisibility(View.GONE);
                    //showPreview(null, myHolder.preview, myHolder.attachmentDownloadLayout);
                    showPreview(message, myHolder.preview, myHolder.attachmentDownloadLayout);
                    myHolder.preview.setVisibility(View.VISIBLE);
                    myHolder.videoIcon.setVisibility(View.GONE);
                    //TODO: while doing multiple image support in single sms ...we might improve this
                    // for (String fileKey : message.getFileMetaKeyStrings()) {
                    if (message.getFileMetas() != null) {
                        FileMeta fileMeta = message.getFileMetas();
                        myHolder.attachmentDownloadLayout.setVisibility(View.VISIBLE);
                        myHolder.attachmentDownloadProgressLayout.setVisibility(View.GONE);
                        myHolder.downloadSizeTextView.setText(fileMeta.getSizeInReadableFormat());
                        final String mimeType = FileUtils.getMimeType(fileMeta.getName());
                        if (!fileMeta.getContentType().contains(IMAGE) && !fileMeta.getContentType().contains(VIDEO)) {
                            showAttachmentIconAndText(myHolder.attachedFile, message, mimeType);
                        }
                    }
                }
                if (isNormalAttachment(message)) {
                    myHolder.videoIcon.setVisibility(View.GONE);
                    myHolder.attachedFile.setVisibility(View.GONE);
                    myHolder.mainAttachmentLayout.setVisibility(View.GONE);
                    myHolder.mainContactShareLayout.setVisibility(View.GONE);
                    myHolder.chatLocation.setVisibility(View.GONE);
                    myHolder.preview.setVisibility(View.GONE);
                    audioView.hideView(false);
                    myHolder.createdAtTime.setText(DateUtils.getFormattedDate(message.getCreatedAtTime()));
                }
            }
            if (message.isCanceled()) {
                myHolder.attachmentRetry.setVisibility(View.VISIBLE);
            } else {
                myHolder.attachmentRetry.setVisibility(View.GONE);
            }
            myHolder.attachmentRetry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Utils.isInternetAvailable(context)) {
                        File file = null;
                        if (message != null && message.getFilePaths() != null) {
                            file = new File(message.getFilePaths().get(0));
                        }
                        if (file != null && !file.exists()) {
                            KmToast.error(context, context.getString(R.string.file_does_not_exist), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        KmToast.success(context, context.getString(R.string.km_resending_attachment), Toast.LENGTH_LONG).show();
                        myHolder.mediaUploadProgressBar.setVisibility(View.VISIBLE);
                        myHolder.attachmentRetry.setVisibility(View.GONE);
                        //updating Cancel Flag to smListItem....
                        message.setCanceled(false);
                        messageDatabaseService.updateCanceledFlag(message.getMessageId(), 0);
                        conversationService.sendMessage(message, messageIntentClass);
                    } else {
                        KmToast.error(context, context.getString(R.string.internet_connection_not_available), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            myHolder.attachmentDownloadProgressLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    myHolder.attachmentView.setVisibility(View.GONE);
                    myHolder.attachmentView.cancelDownload();
                    myHolder.attachmentDownloadProgressLayout.setVisibility(View.GONE);
                    message.setAttDownloadInProgress(false);
                }
            });
            //final ProgressBar mediaDownloadProgressBar = mediaDownloadProgressBar;
            myHolder.preview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: 1. get the image Size and decide if we can download directly
                    //2. if already downloaded to ds card show it directly ....
                    //3. if deleted from sd crad only ...ask user to download it again or skip ...
                    if (message.getContentType() == Message.ContentType.TEXT_URL.getValue()) {
                        return;
                    }
                    if (message.isAttachmentDownloaded()) {
                        if (storagePermissionListener.isPermissionGranted()) {
                            showFullView(message);
                        } else {
                            storagePermissionListener.checkPermission(new KmStoragePermission() {
                                @Override
                                public void onAction(boolean didGrant) {
                                    if (didGrant) {
                                        showFullView(message);
                                    }
                                }
                            });
                        }
                    } else {
                        if (storagePermissionListener.isPermissionGranted()) {
                            myHolder.attachmentDownloadLayout.setVisibility(View.GONE);
                            myHolder.mediaDownloadProgressBar.setVisibility(View.VISIBLE);
                            myHolder.attachmentView.setProressBar(myHolder.mediaDownloadProgressBar);
                            myHolder.attachmentView.setDownloadProgressLayout(myHolder.attachmentDownloadProgressLayout);
                            myHolder.attachmentView.setMessage(message);
                            myHolder.attachmentView.setVisibility(View.VISIBLE);
                            myHolder.attachmentDownloadProgressLayout.setVisibility(View.VISIBLE);
                        } else {
                            storagePermissionListener.checkPermission(new KmStoragePermission() {
                                @Override
                                public void onAction(boolean didGrant) {
                                    if (didGrant) {
                                        myHolder.attachmentDownloadLayout.setVisibility(View.GONE);
                                        myHolder.mediaDownloadProgressBar.setVisibility(View.VISIBLE);
                                        myHolder.attachmentView.setProressBar(myHolder.mediaDownloadProgressBar);
                                        myHolder.attachmentView.setDownloadProgressLayout(myHolder.attachmentDownloadProgressLayout);
                                        myHolder.attachmentView.setMessage(message);
                                        myHolder.attachmentView.setVisibility(View.VISIBLE);
                                        myHolder.attachmentDownloadProgressLayout.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }
                    }
                }
            });

            myHolder.attachmentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (storagePermissionListener.isPermissionGranted()) {
                        showFullView(message);
                    } else {
                        storagePermissionListener.checkPermission(new KmStoragePermission() {
                            @Override
                            public void onAction(boolean didGrant) {
                                if (didGrant) {
                                    showFullView(message);
                                }
                            }
                        });
                    }
                }
            });
            if (useInnerTimeStampDesign) {
                myHolder.createdAtTime.setVisibility(showTimestamp && !message.isRichMessage() ? View.VISIBLE : GONE);
            } else {
                myHolder.timestampLayout.setVisibility(showTimestamp && !message.isRichMessage() ? View.VISIBLE : GONE);
                if (index != messageList.size() - 1 && !message.isRichMessage() && !messageList.get(index + 1).isRichMessage()) {
                    myHolder.messageTextLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            myHolder.timestampLayout.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    myHolder.messageTextLayout.setOnClickListener(null);
                }

            }
            if (message.getScheduledAt() != null) {
                myHolder.createdAtTime.setText(DateUtils.getFormattedDate(message.getScheduledAt()));
            } else if (myHolder.createdAtTime != null && message.isDummyEmptyMessage()) {
                myHolder.createdAtTime.setText("");
            } else if (myHolder.createdAtTime != null) {
                myHolder.createdAtTime.setText(DateUtils.getFormattedDate(message.getCreatedAtTime()));
            }

            if (myHolder.messageTextView != null) {
                myHolder.messageTextView.setTextColor(message.isTypeOutbox() ?
                        Color.parseColor(isDarkModeEnabled ? customizationSettings.getSentMessageTextColor().get(1) : customizationSettings.getSentMessageTextColor().get(0)) : Color.parseColor(isDarkModeEnabled ? customizationSettings.getReceivedMessageTextColor().get(1) : customizationSettings.getReceivedMessageTextColor().get(0)));
                myHolder.messageTextView.setLinkTextColor(message.isTypeOutbox() ?
                        Color.parseColor(isDarkModeEnabled ? customizationSettings.getSentMessageLinkTextColor().get(1) : customizationSettings.getSentMessageLinkTextColor().get(0)) : Color.parseColor(isDarkModeEnabled ? customizationSettings.getReceivedMessageLinkTextColor().get(1) : customizationSettings.getReceivedMessageLinkTextColor().get(0)));

                if (message.getContentType() == Message.ContentType.TEXT_URL.getValue()) {
                    try {
                        myHolder.mapImageView.setVisibility(View.GONE);
                        myHolder.attachedFile.setVisibility(View.GONE);
                        myHolder.preview.setVisibility(View.VISIBLE);
                        setMessageText(myHolder.messageTextView, message);
                        loadImage.setImageFadeIn(false);
                        loadImage.loadImage(message.getFileMetas().getBlobKeyString(), myHolder.preview);
                        myHolder.attachmentDownloadLayout.setVisibility(View.GONE);
                    } catch (Exception e) {
                    }
                } else if (message.getContentType() == Message.ContentType.LOCATION.getValue()) {
                    myHolder.chatLocation.setLayoutParams(getImageLayoutParam(false));
                    myHolder.chatLocation.setVisibility(View.VISIBLE);
                    loadImage.setImageFadeIn(false);
                    myHolder.mapImageView.setVisibility(View.VISIBLE);
                    loadImage.setLoadingImage(R.drawable.km_map_offline_thumbnail);
                    loadImage.loadImage(LocationUtils.loadStaticMap(message.getMessage(), geoApiKey), myHolder.mapImageView);
                    myHolder.messageTextView.setVisibility(View.GONE);
                    myHolder.preview.setVisibility(View.GONE);

                    myHolder.mapImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String uri = String.format(Locale.ENGLISH, GMAP_LINK + LocationUtils.getLocationFromMessage(message.getMessage()));
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                            context.startActivity(intent);
                        }
                    });
                } else if (message.getContentType() == Message.ContentType.PRICE.getValue()) {
                    myHolder.mapImageView.setVisibility(View.GONE);
                    myHolder.messageTextView.setText(ConversationUIService.FINAL_PRICE_TEXT + message.getMessage());
                } else if ((message.getContentType() == Message.ContentType.VIDEO_MSG.getValue()) && !message.isAttachmentDownloaded()) {
                    myHolder.preview.setVisibility(View.VISIBLE);
                    myHolder.mapImageView.setVisibility(View.GONE);
                    myHolder.preview.setImageResource(R.drawable.km_video_default_thumbnail);
                } else if (message.getContentType() == Message.ContentType.TEXT_HTML.getValue()) {
                    myHolder.mapImageView.setVisibility(View.GONE);
                    setMessageText(myHolder.messageTextView, message);
                } else {
                    myHolder.mapImageView.setVisibility(View.GONE);
                    myHolder.chatLocation.setVisibility(View.GONE);
                    myHolder.messageTextView.setText(EmoticonUtils.getSmiledText(context, message.getMessage(), emojiconHandler));
                }

                if (myHolder.messageTextLayout != null) {
                    GradientDrawable bgShape;
                    if (message.isTypeOutbox()) {
                        bgShape = (GradientDrawable) myHolder.messageTextInsideLayout.getBackground();
                    } else {
                        bgShape = (GradientDrawable) myHolder.messageTextLayout.getBackground();
                    }

                    if (bgShape != null) {
                        Pair<Integer, Integer> receivedMessageColorPair = getReceivedMessageBgColors(contactDisplayName, message);
                        bgShape.setColor(message.isTypeOutbox() ? themeHelper.getSentMessageBackgroundColor() : receivedMessageColorPair.first);
                        bgShape.setStroke(3, message.isTypeOutbox() ? themeHelper.getSentMessageBorderColor() : receivedMessageColorPair.second);
                        if (customizationSettings.getSentMessageCornerRadii() != null && message.isTypeOutbox()) {
                            bgShape.setCornerRadii(sentMessageCornerRadii);
                        } else if (customizationSettings.getReceivedMessageCornerRadii() != null) {
                            bgShape.setCornerRadii(receivedMessageCornerRadii);
                        }
                    }
                }
            }

            if (!message.hasAttachment()) {
                myHolder.preview.setVisibility(View.GONE);
                myHolder.attachedFile.setVisibility(View.GONE);
                myHolder.mainAttachmentLayout.setVisibility(View.GONE);
                myHolder.mediaDownloadProgressBar.setVisibility(View.VISIBLE);
                myHolder.attachmentView.setVisibility(View.GONE);
                myHolder.videoIcon.setVisibility(View.GONE);
                myHolder.attachedFile.setVisibility(View.GONE);
                myHolder.mainContactShareLayout.setVisibility(View.GONE);
            }

            myHolder.messageTextLayout.setVisibility(View.VISIBLE);

            if (message.isRichMessage()) {
                if (!TextUtils.isEmpty(message.getMessage())) {
                    myHolder.messageTextLayout.setVisibility(View.VISIBLE);
                } else {
                    myHolder.messageTextLayout.setVisibility(GONE);
                }

                myHolder.richMessageLayout.setVisibility(View.VISIBLE);
                try {
                    KmRichMessageFactory.getInstance().getRichMessage(context, myHolder.richMessageLayout, message, listener, customizationSettings, showTimestamp, isDarkModeEnabled).createRichMessage(isMessageProcessed(message));
                } catch (Exception e) {
                    e.printStackTrace();
                    myHolder.richMessageLayout.setVisibility(View.GONE);
                }
            } else {
                myHolder.richMessageLayout.setVisibility(View.GONE);
                myHolder.messageTextLayout.setVisibility(View.VISIBLE);
            }

            //Handling contact share
            if (message.isContactMessage()) {
                myHolder.attachedFile.setVisibility(View.GONE);
                myHolder.mainAttachmentLayout.setVisibility(View.GONE);
                setupContactShareView(message, myHolder);
            } else {
                myHolder.mainContactShareLayout.setVisibility(View.GONE);
            }

            int startIndex = indexOfSearchQuery(message.getMessage());
            if (startIndex != -1) {
                final SpannableString highlightedName = new SpannableString(message.getMessage());

                // Sets the span to start at the starting point of the match and end at "length"
                // characters beyond the starting point
                highlightedName.setSpan(highlightTextSpan, startIndex,
                        startIndex + searchString.toString().length(), 0);

                myHolder.messageTextView.setText(highlightedName);
            }
            myHolder.messageTextLayout.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                    int positionInSmsList = position;

                    if (positionInSmsList < 0 || messageList.isEmpty() || positionInSmsList >= messageList.size()) {
                        return;
                    }

                    Message message = messageList.get(positionInSmsList);

                    if (message.isTempDateType() || message.isCustom() || message.isChannelCustomMessage()) {
                        return;
                    }

                    String[] menuItems = context.getResources().getStringArray(R.array.menu);

                    //TODO: Show Info and Delete menu with sync and layout fix
                    for (int i = 0; i < menuItems.length; i++) {

                        if (menuItems[i].equals(context.getString(R.string.info))) {
                            continue;
                        }
                        if (!(message.isGroupMessage() && message.isTypeOutbox() && message.isSentToServer()) && menuItems[i].equals(context.getResources().getString(R.string.info))) {
                            continue;
                        }
                        if ((message.hasAttachment() || message.getContentType() == Message.ContentType.LOCATION.getValue() || message.isVideoOrAudioCallMessage()) &&
                                menuItems[i].equals(context.getResources().getString(R.string.copy))) {
                            continue;
                        }
                        if (menuItems[i].equals(context.getResources().getString(R.string.delete_for_all))) {
                            continue;
                        }
                        if (((channel != null && Channel.GroupType.OPEN.getValue().equals(channel.getType())) || message.isCall() || (message.hasAttachment() && !message.isAttachmentDownloaded()) || message.isVideoOrAudioCallMessage()) && (menuItems[i].equals(context.getResources().getString(R.string.forward)) ||
                                menuItems[i].equals(context.getResources().getString(R.string.resend)))) {
                            continue;
                        }
                        if (menuItems[i].equals(context.getResources().getString(R.string.resend)) && (!message.isSentViaApp() || message.isSentToServer() || message.isVideoOrAudioCallMessage())) {
                            continue;
                        }
                        if (menuItems[i].equals(context.getResources().getString(R.string.reply)) && (!customizationSettings.isReplyOption() || message.isAttachmentUploadInProgress() || TextUtils.isEmpty(message.getKeyString()) || !message.isSentToServer() || (channel != null && Channel.GroupType.OPEN.getValue().equals(channel.getType())) || (message.hasAttachment() && !message.isAttachmentDownloaded()) || channel != null && !ChannelService.getInstance(context).processIsUserPresentInChannel(channel.getKey()) || message.isVideoOrAudioCallMessage() || contact != null && contact.isDeleted())) {
                            continue;
                        }
                        if (menuItems[i].equals(context.getResources().getString(R.string.delete)) && (TextUtils.isEmpty(message.getKeyString()) || !customizationSettings.isAgentApp() || (channel != null && Channel.GroupType.OPEN.getValue().equals(channel.getType())))) {
                            continue;
                        }
                        if (menuItems[i].equals(context.getResources().getString(R.string.info)) && (TextUtils.isEmpty(message.getKeyString()) || (channel != null && Channel.GroupType.OPEN.getValue().equals(channel.getType())) || message.isVideoOrAudioCallMessage())) {
                            continue;
                        }
                        if (menuItems[i].equals(context.getResources().getString(R.string.share)) && (message.isAttachmentUploadInProgress() || message.getFilePaths() == null || !(new File(message.getFilePaths().get(0)).exists()))) {
                            continue;
                        }
                        final MenuItem item = contextMenu.add(Menu.NONE, i, i, menuItems[i]);
                        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                return contextMenuClickListener == null || contextMenuClickListener.onItemClick(position, item);
                            }
                        });
                    }
                    view.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0f, 0f, 0));
                }
            });

            if (showSourceURls(message, position)) {
                String json = message.getMetadata().get(SOURCE_URL);
                if(json == null) {
                    return;
                }
                myHolder.sourceText.setVisibility(View.VISIBLE);
                myHolder.urlsLayout.setVisibility(View.VISIBLE);
                myHolder.sourceUrlDivider.setVisibility(View.VISIBLE);

                Type listType = new TypeToken<List<SourceUrl>>() {}.getType();
                List<SourceUrl> links = GsonUtils.getObjectFromJson(json, listType);

                myHolder.urlsLayout.removeAllViews();
                for(SourceUrl url : links) {
                    SpannableString content = new SpannableString(url.getTitle());
                    content.setSpan(new UnderlineSpan(), 0, content.length(), 0);

                    View view = LayoutInflater
                            .from(myHolder.urlsLayout.getContext())
                            .inflate(R.layout.km_link_view, null, false);

                    TextView tv =  view.findViewById(R.id.src_urls_textview);
                    tv.setText(content);

                    view.setOnClickListener((data) -> {
                        openURL(view.getContext(), url.getUrl());
                    });
                    myHolder.urlsLayout.addView(view);
                }
            }
        }
    }

    private void openURL(Context context, String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(intent);
    }

    private boolean showSourceURls(Message message, int position) {
        return message.getMetadata() != null
                && message.getContentType() == 0
                && getItemViewType(position) == 0
                && !message.getMessage().isEmpty()
                && message.getType() == 4
                && message.getMetadata().containsKey(SOURCE_URL);
    }

    //insert new item and only update item which requires updated and not the whole layout
    public void onItemInserted(int position) {
        notifyItemInserted(position);
        while (position != 0 &&
                (messageList.get(position).getCreatedAtTime() - messageList.get(position - 1).getCreatedAtTime() < KmConstants.MESSAGE_CLUBBING_TIME_FRAME)
                && (messageList.get(position).getType().equals(messageList.get(position - 1).getType()))) {
            notifyItemChanged(position - 1);
            position--;
        }
    }

    protected Pair<Integer, Integer> getReceivedMessageBgColors(Contact contact, Message message) {
        return new Pair<>(isHtmlTypeMessage(message) ? (isDarkModeEnabled ? context.getResources().getColor(R.color.dark_mode_default) : Color.WHITE) : Color.parseColor(isDarkModeEnabled ? customizationSettings.getReceivedMessageBackgroundColor().get(1) : customizationSettings.getReceivedMessageBackgroundColor().get(0)), Color.parseColor(isDarkModeEnabled ? customizationSettings.getReceivedMessageBorderColor().get(1) : customizationSettings.getReceivedMessageBorderColor().get(0)));
    }

    private boolean isMessageProcessed(Message message) {
        if (themeHelper.isHidePostCTA() || themeHelper.isDisableFormPostSubmit()) {
            return lastSentMessage != null && lastSentMessage.getCreatedAtTime() > message.getCreatedAtTime();
        }
        return false;
    }

    public void updateLastSentMessage(Message message) {
        if ((themeHelper.isHidePostCTA() || themeHelper.isDisableFormPostSubmit()) && message.isTypeOutbox()) {
            lastSentMessage = message;
        }
    }

    private void loadHtml(FrameLayout emailLayout, Message message) {
        WebView webView = emailLayout.findViewById(R.id.emailWebView);
        webViews.add(webView);
        webView.getSettings().setJavaScriptEnabled(customizationSettings.isJavaScriptEnabled());

        String styledHtml = message.getMessage();

        if (isDarkModeEnabled) {
            webView.setBackgroundColor(context.getResources().getColor(R.color.dark_mode_default));
            styledHtml = "<html><body style='color: "
                    + String.format("#%06X", (0xFFFFFF & context.getResources().getColor(R.color.white)))
                    + ";'>"
                    + message.getMessage()
                    + "</body></html>";
        }

        webView.getSettings().setDomStorageEnabled(true);
        webView.loadDataWithBaseURL(null, message.getMessage(), "text/html", "charset=UTF-8", null);
    }

    public static boolean isEmailTypeMessage(Message message) {
        return message.getSource() == 7;
    }

    public static boolean isHtmlTypeMessage(Message message) {
        return Message.ContentType.TEXT_HTML.getValue().equals(message.getContentType()) || isEmailTypeMessage(message);
    }

    private void setupContactShareView(final Message message, MyViewHolder myViewHolder) {
        myViewHolder.mainContactShareLayout.setVisibility(View.VISIBLE);
        myViewHolder.mainContactShareLayout.setLayoutParams(getImageLayoutParam(false));
        MobiComVCFParser parser = new MobiComVCFParser();
        try {

            VCFContactData data = parser.parseCVFContactData(message.getFilePaths().get(0));
            myViewHolder.shareContactName.setText(data.getName());

            int resId = message.isTypeOutbox() ? Color.parseColor(isDarkModeEnabled ? customizationSettings.getSentMessageTextColor().get(1) : customizationSettings.getSentMessageTextColor().get(0)) : Color.parseColor(isDarkModeEnabled ? customizationSettings.getReceivedMessageTextColor().get(1) : customizationSettings.getReceivedMessageTextColor().get(0));
            myViewHolder.shareContactName.setTextColor(resId);
            myViewHolder.shareContactNo.setTextColor(resId);
            myViewHolder.shareEmailContact.setTextColor(resId);
            myViewHolder.addContactButton.setTextColor(resId);

            if (data.getProfilePic() != null) {
                if (imageCache.getBitmapFromMemCache(message.getKeyString()) == null) {
                    imageCache.addBitmapToCache(message.getKeyString(), data.getProfilePic());
                }
                myViewHolder.shareContactImage.setImageBitmap(imageCache.getBitmapFromMemCache(message.getKeyString()));
            }
            if (!TextUtils.isEmpty(data.getTelephoneNumber())) {
                myViewHolder.shareContactNo.setText(data.getTelephoneNumber());
            } else {
                myViewHolder.shareContactNo.setVisibility(View.GONE);
            }
            if (data.getEmail() != null) {
                myViewHolder.shareEmailContact.setText(data.getEmail());
            } else {
                myViewHolder.shareEmailContact.setVisibility(View.GONE);
            }


            myViewHolder.addContactButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (storagePermissionListener.isPermissionGranted()) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        Uri outputUri = null;
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        if (Utils.hasNougat()) {
                            outputUri = FileProvider.getUriForFile(context, Utils.getMetaDataValue(context, MobiComKitConstants.PACKAGE_NAME) + ".provider", new File(message.getFilePaths().get(0)));
                        } else {
                            outputUri = Uri.fromFile(new File(message.getFilePaths().get(0)));
                        }
                        try {
                            intent.setDataAndType(outputUri, TEXT_XCARD);
                            context.startActivity(intent);
                        } catch (ActivityNotFoundException activityNotFoundException) {
                            KmToast.error(context, R.string.info_app_not_found_to_open_file, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        storagePermissionListener.checkPermission(new KmStoragePermission() {
                            @Override
                            public void onAction(boolean didGrant) {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                Uri outputUri = null;
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                if (Utils.hasNougat()) {
                                    outputUri = FileProvider.getUriForFile(context, Utils.getMetaDataValue(context, MobiComKitConstants.PACKAGE_NAME) + ".provider", new File(message.getFilePaths().get(0)));
                                } else {
                                    outputUri = Uri.fromFile(new File(message.getFilePaths().get(0)));
                                }
                                try {
                                    intent.setDataAndType(outputUri, TEXT_XCARD);
                                    context.startActivity(intent);
                                } catch (ActivityNotFoundException activityNotFoundException) {
                                    KmToast.error(context, R.string.info_app_not_found_to_open_file, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
            });

        } catch (Exception e) {
            Utils.printLog(context, "DetailedConvAdapter", "Exception in parsing");
        }

    }

    public void loadProfileImage(Contact contact, CircleImageView imageView, TextView textView, boolean hideRecursiveImages) {
        if (hideRecursiveImages) {
            imageView.setVisibility(GONE);
            textView.setVisibility(GONE);
        } else {
            if (contact != null) {
                KmViewHelper.loadContactImage(context, imageView, textView, contact, R.drawable.km_ic_contact_picture_holo_light);
            }
        }
    }

    private void showAttachmentIconAndText(TextView attachedFile, final Message message, final String mimeType) {

        attachedFile.setTextColor(message.isTypeOutbox() ?
                Color.parseColor(isDarkModeEnabled ? customizationSettings.getSentMessageTextColor().get(1) : customizationSettings.getSentMessageTextColor().get(0)) : Color.parseColor(isDarkModeEnabled ? customizationSettings.getReceivedMessageTextColor().get(1) : customizationSettings.getReceivedMessageTextColor().get(0)));
        attachedFile.setText(KmUtils.getAttachmentName(message));
        attachedFile.setVisibility(View.VISIBLE);
        attachedFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (message.isAttachmentDownloaded()) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        Uri outputUri;
                        if (Utils.hasNougat()) {
                            outputUri = FileProvider.getUriForFile(context, Utils.getMetaDataValue(context, MobiComKitConstants.PACKAGE_NAME) + ".provider", new File(message.getFilePaths().get(0)));
                        } else {
                            outputUri = Uri.fromFile(new File(message.getFilePaths().get(0)));
                        }
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        try {
                            intent.setDataAndType(outputUri, mimeType);
                            context.startActivity(intent);
                        } catch (ActivityNotFoundException activityNotFoundException) {
                            KmToast.error(context, R.string.info_app_not_found_to_open_file, Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (Exception e) {
                    Utils.printLog(context, TAG, "No application found to open this file");
                }
            }

        });
    }

    public void setMessageText(TextView messageTextView, Message message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            messageTextView.setText(Html.fromHtml(message.getMessage(), Html.FROM_HTML_MODE_COMPACT));
        } else {
            messageTextView.setText(Html.fromHtml(message.getMessage()));
        }
    }

    private void showPreview(Message message, ImageView preview, LinearLayout attachmentDownloadLayout) {
        imageThumbnailLoader.setImageFadeIn(false);
        imageThumbnailLoader.setLoadingImage(R.id.media_upload_progress_bar);
        imageThumbnailLoader.loadImage(message, preview);
        attachmentDownloadLayout.setVisibility(View.GONE);
    }

    private void showFullView(Message smListItem) {
        try {
            final String mimeType = FileUtils.getMimeType(smListItem.getFilePaths().get(0));
            if (mimeType != null) {
                if (smListItem.getFileMetas() == null) {
                    KmToast.error(context, "File not uploaded, please retry", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mimeType.startsWith(IMAGE)) {
                    Intent intent = new Intent(context, FullScreenImageActivity.class);
                    intent.putExtra(MobiComKitConstants.MESSAGE_JSON_INTENT, GsonUtils.getJsonFromObject(smListItem, Message.class));
                    ((MobiComKitActivityInterface) context).startActivityForResult(intent, MobiComKitActivityInterface.REQUEST_CODE_FULL_SCREEN_ACTION);
                }
                if (mimeType.startsWith(VIDEO)) {
                    if (smListItem.isAttachmentDownloaded()) {
                        Intent intentVideo = new Intent();
                        intentVideo.setAction(Intent.ACTION_VIEW);
                        Uri outputUri;
                        if (Utils.hasNougat()) {
                            outputUri = FileProvider.getUriForFile(context, Utils.getMetaDataValue(context, MobiComKitConstants.PACKAGE_NAME) + ".provider", new File(smListItem.getFilePaths().get(0)));
                        } else {
                            outputUri = Uri.fromFile(new File(smListItem.getFilePaths().get(0)));
                        }
                        intentVideo.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        if (intentVideo.resolveActivity(context.getPackageManager()) != null) {
                            intentVideo.setDataAndType(outputUri, "video/*");
                            context.startActivity(intentVideo);
                        } else {
                            KmToast.error(context, R.string.info_app_not_found_to_open_file, Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        } catch (Exception e) {
            Utils.printLog(context, TAG, "No application found to open this file");
        }

    }

    @Override
    public int getItemViewType(int position) {
        Message message = getItem(position);
        if (message == null) {
            return 0;
        }
        if (message.isInitialFirstMessage()) {
            return 7;
        }
        if (message.isTempDateType()) {
            return 2;
        }
        //feedback is also a custom message, so check it before
        if (message.isFeedbackMessage()) {
            return 6;
        }
        if (message.isCustom()) {
            return 3;
        }
        if (message.isChannelCustomMessage()) {
            return 4;
        }
        if (message.isVideoCallMessage()) {
            return 5;
        }
        if (message.isTypingMessage()) {
            return 8;
        }


        return message.isTypeOutbox() ? 1 : 0;
    }

    private Message getItem(int position) {
        return messageList.get(position);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public ViewGroup.LayoutParams getImageLayoutParam(boolean outBoxType) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        float wt_px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, context.getResources().getDisplayMetrics());
        ViewGroup.MarginLayoutParams params;
        if (outBoxType) {
            params = new RelativeLayout.LayoutParams(metrics.widthPixels + (int) wt_px * 2, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins((int) wt_px, 0, (int) wt_px, 0);
        } else {
            params = new LinearLayout.LayoutParams(metrics.widthPixels - (int) wt_px * 2, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 0);

        }
        return params;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                final FilterResults oReturn = new FilterResults();
                final List<Message> results = new ArrayList<>();
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

    private int indexOfSearchQuery(String message) {
        if (!TextUtils.isEmpty(searchString)) {
            return message.toLowerCase(Locale.getDefault()).indexOf(
                    searchString.toLowerCase(Locale.getDefault()));
        }
        return -1;
    }

    public void refreshContactData() {
        if (contact != null) {
            contact = contactService.getContactById(contact.getContactIds());
        }
    }

    public void refreshWebView() {
        if (webViews != null) {
            for (WebView webView : webViews) {
                webView.reload();
            }
        }
    }

    private boolean isNormalAttachment(Message message) {
        if (message.getFileMetas() != null) {
            return !(message.getFileMetas().getContentType().contains(IMAGE) || message.getFileMetas().getContentType().contains(VIDEO) || message.isContactMessage());
        } else if (message.getFilePaths() != null) {
            String filePath = message.getFilePaths().get(0);
            final String mimeType = FileUtils.getMimeType(filePath);
            if (mimeType != null) {
                return !(mimeType.contains(IMAGE) || mimeType.contains(VIDEO) || message.isContactMessage());
            }
        }
        return false;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView mapImageView;
        RelativeLayout chatLocation;
        TextView downloadSizeTextView;
        AttachmentView attachmentView;
        LinearLayout attachmentDownloadLayout;
        ImageView preview;
        LinearLayout attachmentRetry;
        RelativeLayout attachmentDownloadProgressLayout;
        RelativeLayout mainAttachmentLayout;
        LinearLayout mainContactShareLayout;
        ImageView videoIcon;
        ProgressBar mediaDownloadProgressBar;
        ProgressBar mediaUploadProgressBar;
        ImageView attachmentIcon, shareContactImage;
        TextView alphabeticTextView;
        CircleImageView contactImage;
        View messageTextLayout;
        TextView nameTextView;
        TextView attachedFile;
        ImageView sentOrReceived;
        TextView messageTextView;
        TextView createdAtTime;
        TextView onlineTextView;
        TextView selfDestruct;
        TextView deliveryStatus, shareContactName, shareContactNo, shareEmailContact;
        LinearLayout nameTextLayout;
        View view;
        RelativeLayout replyRelativeLayout;
        RelativeLayout imageViewRLayout;
        TextView replyMessageTextView;
        ImageView imageViewPhoto;
        TextView replyNameTextView;
        ImageView imageViewForAttachmentType;
        Button addContactButton;
        int position;
        TextView statusTextView;
        LinearLayout messageTextInsideLayout;
        LinearLayout richMessageLayout;
        RelativeLayout messageRootLayout;
        FrameLayout emailLayout;
        TextView viaEmailView;
        View statusIconBackground;
        LinearLayout timestampLayout;
        ImageView statusImageView;
        View sourceUrlDivider;
        TextView sourceText;
        LinearLayout urlsLayout;

        public MyViewHolder(final View customView) {
            super(customView);

            position = getLayoutPosition();               //   getAdapterPosition();
            this.view = customView;
            mapImageView = (ImageView) customView.findViewById(R.id.static_mapview);
            chatLocation = (RelativeLayout) customView.findViewById(R.id.chat_location);
            preview = (ImageView) customView.findViewById(R.id.preview);
            attachmentView = (AttachmentView) customView.findViewById(R.id.main_attachment_view);
            attachmentIcon = (ImageView) customView.findViewById(R.id.attachmentIcon);
            downloadSizeTextView = (TextView) customView.findViewById(R.id.attachment_size_text);
            attachmentDownloadLayout = (LinearLayout) customView.findViewById(R.id.attachment_download_layout);
            attachmentRetry = (LinearLayout) customView.findViewById(R.id.attachment_retry_layout);
            attachmentDownloadProgressLayout = (RelativeLayout) customView.findViewById(R.id.attachment_download_progress_layout);
            mainAttachmentLayout = (RelativeLayout) customView.findViewById(R.id.attachment_preview_layout);
            mainContactShareLayout = (LinearLayout) customView.findViewById(R.id.contact_share_layout);
            videoIcon = (ImageView) customView.findViewById(R.id.video_icon);
            mediaDownloadProgressBar = (ProgressBar) customView.findViewById(R.id.media_download_progress_bar);
            mediaUploadProgressBar = (ProgressBar) customView.findViewById(R.id.media_upload_progress_bar);
            messageTextLayout = customView.findViewById(R.id.messageTextLayout);
            createdAtTime = (TextView) customView.findViewById(R.id.createdAtTime);
            messageTextView = (TextView) customView.findViewById(R.id.message);
            contactImage = (CircleImageView) customView.findViewById(R.id.contactImage);
            alphabeticTextView = (TextView) customView.findViewById(R.id.alphabeticImage);
            deliveryStatus = (TextView) customView.findViewById(R.id.status);
            selfDestruct = (TextView) customView.findViewById(R.id.selfDestruct);
            nameTextView = (TextView) customView.findViewById(R.id.name_textView);
            attachedFile = (TextView) customView.findViewById(R.id.attached_file);
            onlineTextView = (TextView) customView.findViewById(R.id.onlineTextView);
            nameTextLayout = (LinearLayout) customView.findViewById(R.id.nameTextLayout);
            replyRelativeLayout = (RelativeLayout) customView.findViewById(R.id.reply_message_layout);
            imageViewRLayout = (RelativeLayout) customView.findViewById(R.id.imageViewRLayout);
            replyMessageTextView = (TextView) customView.findViewById(R.id.messageTextView);
            imageViewPhoto = (ImageView) customView.findViewById(R.id.imageViewForPhoto);
            replyNameTextView = (TextView) customView.findViewById(R.id.replyNameTextView);
            imageViewForAttachmentType = (ImageView) customView.findViewById(R.id.imageViewForAttachmentType);
            statusTextView = (TextView) customView.findViewById(R.id.statusImage);
            messageTextInsideLayout = customView.findViewById(R.id.messageTextInsideLayout);
            richMessageLayout = (LinearLayout) customView.findViewById(R.id.alRichMessageView);
            messageRootLayout = (RelativeLayout) customView.findViewById(R.id.messageLayout);
            emailLayout = customView.findViewById(R.id.emailLayout);
            viaEmailView = customView.findViewById(R.id.via_email_text_view);
            statusIconBackground = customView.findViewById(R.id.statusIconBackground);
            sourceUrlDivider = customView.findViewById(R.id.src_divider);
            sourceText = customView.findViewById(R.id.sources);
            urlsLayout = customView.findViewById(R.id.linksView);

            if (useInnerTimeStampDesign) {
                if (statusIconBackground != null) {
                    KmUtils.setGradientSolidColor(statusIconBackground, themeHelper.getMessageStatusIconColor());
                }
            } else {
                if (statusIconBackground != null) {
                    if (!TextUtils.isEmpty(isDarkModeEnabled ? customizationSettings.getMessageStatusIconColor().get(1) : customizationSettings.getMessageStatusIconColor().get(0))) {
                        KmUtils.setGradientSolidColor(statusIconBackground, themeHelper.getMessageStatusIconColor());
                    } else {
                        statusIconBackground.setVisibility(GONE);
                    }
                }
                timestampLayout = customView.findViewById(R.id.timestampLayout);
                statusImageView = customView.findViewById(R.id.statusImageView);
            }


            shareContactImage = (ImageView) mainContactShareLayout.findViewById(R.id.contact_share_image);
            shareContactName = (TextView) mainContactShareLayout.findViewById(R.id.contact_share_tv_name);
            shareContactNo = (TextView) mainContactShareLayout.findViewById(R.id.contact_share_tv_no);
            shareEmailContact = (TextView) mainContactShareLayout.findViewById(R.id.contact_share_emailId);
            addContactButton = (Button) mainContactShareLayout.findViewById(R.id.contact_share_add_btn);

            mapImageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return false;
                }
            });
            preview.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return false;
                }
            });

            attachmentView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return false;
                }
            });

            if (attachedFile != null) {
                attachedFile.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        return false;
                    }
                });
            }

            if (fontManager != null) {
                if (fontManager.getMessageTextFont() != null && messageTextView != null) {
                    messageTextView.setTypeface(fontManager.getMessageTextFont());
                }
                if (fontManager.getMessageDisplayNameFont() != null && nameTextView != null) {
                    nameTextView.setTypeface(fontManager.getMessageDisplayNameFont());
                }
                if (fontManager.getCreatedAtTimeFont() != null && createdAtTime != null) {
                    createdAtTime.setTypeface(fontManager.getCreatedAtTimeFont());
                }
            }
        }
    }

    static class MyViewHolder2 extends RecyclerView.ViewHolder {
        TextView dateView;
        TextView dayTextView;

        public MyViewHolder2(View itemView) {
            super(itemView);
            dateView = (TextView) itemView.findViewById(R.id.chat_screen_date);
            dayTextView = (TextView) itemView.findViewById(R.id.chat_screen_day);
        }
    }

    static class MyViewHolder3 extends RecyclerView.ViewHolder {
        TextView customContentTextView;

        public MyViewHolder3(View itemView) {
            super(itemView);
            customContentTextView = (TextView) itemView.findViewById(R.id.custom_message_layout_content);
        }
    }

    static class MyViewHolder4 extends RecyclerView.ViewHolder {
        TextView channelMessageTextView;
        TextView channelMessageStaticText;
        View channelMessageLeftBg;
        View channelMessageRightBg;
        CardView summaryCardView;
        TextView summaryMessage;
        Button summaryReadMore;
        LinearLayout normalTextLayout;

        public MyViewHolder4(View itemView, boolean isAgentApp) {
            super(itemView);
            if (isAgentApp) {
                channelMessageTextView = (TextView) itemView.findViewById(R.id.channel_message);
                summaryCardView = (CardView) itemView.findViewById(R.id.conversation_summary);
                summaryMessage = (TextView) itemView.findViewById(R.id.summary_message);
                summaryReadMore = (Button) itemView.findViewById(R.id.summary_read_more);
            } else {
                channelMessageTextView = (TextView) itemView.findViewById(R.id.km_transferred_to);
                channelMessageStaticText = (TextView) itemView.findViewById(R.id.km_transferred_text);
                channelMessageLeftBg = (View) itemView.findViewById(R.id.km_transferred_to_left_bg);
                channelMessageRightBg = (View) itemView.findViewById(R.id.km_transferred_to_right_bg);
            }
            normalTextLayout = (LinearLayout) itemView.findViewById(R.id.normal_text);
        }
    }

    static class MyViewHolder5 extends RecyclerView.ViewHolder {
        TextView statusTextView;
        TextView timeTextView;
        TextView durationTextView;
        ImageView imageView;

        public MyViewHolder5(View itemView) {
            super(itemView);
            statusTextView = (TextView) itemView.findViewById(R.id.call_status);
            timeTextView = (TextView) itemView.findViewById(R.id.call_timing);
            durationTextView = (TextView) itemView.findViewById(R.id.call_duration);
            imageView = (ImageView) itemView.findViewById(R.id.call_image_type);
        }
    }

    static class MyViewHolder6 extends RecyclerView.ViewHolder {
        TextView textViewFeedbackComment;
        ImageView imageViewFeedbackRating;
        ScrollView scrollViewFeedbackCommentWrap;
        TextView textViewFeedbackText;


        public MyViewHolder6(View itemView) {
            super(itemView);
            textViewFeedbackComment = itemView.findViewById(R.id.idFeedbackComment);
            imageViewFeedbackRating = itemView.findViewById(R.id.idRatingImage);
            scrollViewFeedbackCommentWrap = itemView.findViewById(R.id.idCommentScrollView);
            textViewFeedbackText = itemView.findViewById(R.id.idFeedbackRateText);
        }
    }

    static class StaticMessageHolder extends RecyclerView.ViewHolder {
        TextView topMessageTextView;
        ImageView topMessageImageView;

        public StaticMessageHolder(@NonNull View itemView) {
            super(itemView);
            topMessageImageView = itemView.findViewById(R.id.top_message_image_view);
            topMessageTextView = itemView.findViewById(R.id.top_message_text_view);

        }
    }

    static class TypingMessageHolder extends RecyclerView.ViewHolder {
        LinearLayout parentLayout;
        TextView firstDot;
        TextView secondDot;
        TextView thirdDot;
        boolean isDarkModeEnabled;

        public TypingMessageHolder(@NonNull View itemView, boolean isDarkModeEnabled) {
            super(itemView);
            this.isDarkModeEnabled = isDarkModeEnabled;
            parentLayout = itemView.findViewById(R.id.typing_linear_layout);
            firstDot = itemView.findViewById(R.id.typing_first_dot);
            secondDot = itemView.findViewById(R.id.typing_second_dot);
            thirdDot = itemView.findViewById(R.id.typing_third_dot);
            setupBackground();
            startTypingAnimation();
        }

        private void setupBackground() {
            GradientDrawable bgShape;
            bgShape = (GradientDrawable) parentLayout.getBackground();

            if (bgShape != null) {
                String bgColor = isDarkModeEnabled ? new CustomizationSettings().getReceivedMessageBackgroundColor().get(1) : new CustomizationSettings().getReceivedMessageBackgroundColor().get(0);
                bgShape.setColor(Color.parseColor(bgColor));
                bgShape.setStroke(3, Color.parseColor(bgColor));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                firstDot.getBackground().setTint(isDarkModeEnabled ? Color.WHITE : Color.BLACK);
                secondDot.getBackground().setTint(isDarkModeEnabled ? Color.WHITE : Color.BLACK);
                thirdDot.getBackground().setTint(isDarkModeEnabled ? Color.WHITE : Color.BLACK);
            }
        }

        @SuppressLint("RestrictedApi")
        private void startTypingAnimation() {
            AnimatorSet firstAnimatorSet = (AnimatorSet) AnimatorInflaterCompat.loadAnimator(itemView.getContext(), R.animator.km_blinking);
            firstAnimatorSet.setTarget(firstDot);
            firstAnimatorSet.start();

            AnimatorSet secondAnimatorSet = (AnimatorSet) AnimatorInflaterCompat.loadAnimator(itemView.getContext(), R.animator.km_blinking);
            secondAnimatorSet.setStartDelay(200);
            secondAnimatorSet.setTarget(secondDot);
            secondAnimatorSet.start();

            AnimatorSet thirdAnimatorSet = (AnimatorSet) AnimatorInflaterCompat.loadAnimator(itemView.getContext(), R.animator.km_blinking);
            thirdAnimatorSet.setStartDelay(400);
            thirdAnimatorSet.setTarget(thirdDot);
            thirdAnimatorSet.start();
        }

        void setupDarkMode(boolean isDarkModeEnabled) {
            this.isDarkModeEnabled = isDarkModeEnabled;
            setupBackground();
        }
    }
}

