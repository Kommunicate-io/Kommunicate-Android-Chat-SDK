package com.applozic.mobicomkit.uiwidgets.kommunicate.widgets;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.broadcast.AlEventManager;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.commons.image.ImageLoader;
import com.applozic.mobicommons.commons.image.ImageUtils;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import io.kommunicate.Kommunicate;
import io.kommunicate.async.KmAppSettingTask;
import io.kommunicate.callbacks.KMLoginHandler;
import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.models.KmAppSettingModel;
import io.kommunicate.users.KMUser;

/**
 * Chat Launcher icon view
 *
 * @author Aman
 * @date December '21
 */

public class KmChatWidget {
    public static KmChatWidget kmChatWidget;
    private Context mContext;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParamsWindowManager;
    private View rootView;
    private View.OnClickListener onClickListener;
    private KmAppSettingModel.KmChatWidget kmChatWidgetModel;
    private KmChatWidgetConfig config;
    private ImageView KmFloatingView;
    private FrameLayout frameLayout;
    private TextView unreadCountText;
    private KmChatWidgetHelper kmChatWidgetHelper;
    private boolean isShowing = false;
    private boolean fetchingSettings = false;
    private boolean showTriggered = false;
    private int width, height;
    private static final String TAG = "KmChatWidget";
    public static final String LEFT = "left";
    public static final String RIGHT = "right";

    /**
     * Constructor to initialize chat widget
     *
     * @param context the Activity's context
     */
    public KmChatWidget(Context context) {
        this(context, new KmChatWidgetConfig.Builder().build());
        initialize();
        configureWidgetFromDashboard();

    }

    /**
     * Constructor to initialize chat widget
     *
     * @param context the Activity's context
     * @param fetchDashboardSettings set true if you want to fetch settings from dashboard
     * @param config pass the build of KmChatWidgetConfig
     */
    public KmChatWidget(Context context, boolean fetchDashboardSettings, KmChatWidgetConfig config) {
        this(context, config);
        initialize();
        if (fetchDashboardSettings) {
            configureWidgetFromDashboard();
        }

    }

    private void initialize() {
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        rootView = mInflater.inflate(R.layout.km_chat_widget, null, false);
        frameLayout = rootView.findViewById(R.id.circular_frame_layout);
        unreadCountText = rootView.findViewById(R.id.unreadSmsCount);
        KmFloatingView = (ImageView) rootView.findViewById(R.id.km_floating_widget);
        kmChatWidgetHelper = new KmChatWidgetHelper(this, mContext);
        AlEventManager.getInstance().registerUIListener("KmChatWidget", kmChatWidgetHelper);
        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Kommunicate.openConversation(mContext);
            }
        });
        rootView.measure(0, 0);
        width = rootView.getMeasuredWidth();
        height = rootView.getMeasuredHeight();
        if(!KMUser.isLoggedIn(mContext)) {
            Kommunicate.loginAsVisitor(mContext, new KMLoginHandler() {
                @Override
                public void onSuccess(RegistrationResponse registrationResponse, Context context) {
                    Utils.printLog(mContext, TAG, "Registered as Visitor : " + registrationResponse);
                }

                @Override
                public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                    Utils.printLog(mContext, TAG, "Failed to login");
                }
            });
        }
    }

    public static KmChatWidget getInstance(Context context) {
        if (kmChatWidget == null) {
            return null;
        }
        return kmChatWidget;
    }

    public void showUnreadCount(Integer unreadCount) {
        if (unreadCount > 0) {
            unreadCountText.setVisibility(View.VISIBLE);
            unreadCountText.setText(String.valueOf(unreadCount));
        } else {
            unreadCountText.setVisibility(View.GONE);
        }
    }

    private KmChatWidget(Context context, KmChatWidgetConfig config) {
        this.mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        kmChatWidget = this;
        this.config = config;
        if (config.displayWidth == Integer.MAX_VALUE) {
            DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
            config.displayWidth = metrics.widthPixels;
        }
        if (config.displayHeight == Integer.MAX_VALUE) {
            DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
            config.displayHeight = (int) (metrics.heightPixels - 25 * metrics.density);
        }
        config.paddingLeft = dp2px(config.paddingLeft);
        config.paddingTop = dp2px(config.paddingTop);
        config.paddingRight = dp2px(config.paddingRight);
        config.paddingBottom = dp2px(config.paddingBottom);

    }

    private void configureWidgetFromDashboard() {
        fetchingSettings = true;
        new KmAppSettingTask(mContext, Applozic.getInstance(mContext).getApplicationKey(), new KmCallback() {
            @Override
            public void onSuccess(Object message) {
                fetchingSettings = false;
                final KmAppSettingModel appSettingModel = (KmAppSettingModel) message;
                if (appSettingModel != null && appSettingModel.getResponse() != null && appSettingModel.getChatWidget() != null) {
                    kmChatWidgetModel = appSettingModel.getResponse().getChatWidget();
                    config.gravity = kmChatWidgetModel.getPosition().equals(LEFT) ? KmChatWidgetConfig.GRAVITY.LEFT_BOTTOM : KmChatWidgetConfig.GRAVITY.RIGHT_BOTTOM;
                    DrawableCompat.setTint(DrawableCompat.wrap(frameLayout.getBackground()), Color.parseColor(kmChatWidgetModel.getPrimaryColor()));
                    if (kmChatWidgetModel.getIconIndex().equals("image")) {
                        ImageLoader loadImage = new ImageLoader(mContext, ImageUtils.getLargestScreenDimension((Activity) mContext)) {
                            @Override
                            protected Bitmap processBitmap(Object data) {
                                return new FileClientService(mContext).loadMessageImage(mContext, kmChatWidgetModel.getWidgetImageLink());
                            }
                        };
                        loadImage.loadImage(kmChatWidgetModel.getWidgetImageLink(), KmFloatingView);
                    } else {
                        switch (kmChatWidgetModel.getIconIndex()) {
                            case ("1"):
                                KmFloatingView.setImageDrawable(AppCompatResources.getDrawable(mContext, R.drawable.km_icon_1));
                                break;
                            case ("2"):
                                KmFloatingView.setImageDrawable(AppCompatResources.getDrawable(mContext, R.drawable.km_icon_2));
                                break;
                            case ("3"):
                                KmFloatingView.setImageDrawable(AppCompatResources.getDrawable(mContext, R.drawable.km_icon_3));
                                break;
                            case ("4"):
                                KmFloatingView.setImageDrawable(AppCompatResources.getDrawable(mContext, R.drawable.km_icon_4));
                                break;
                        }
                    }
                    checkAndShowLauncher();
                } else {
                    Utils.printLog(mContext, TAG, "Failed to fetch App setting");
                }
            }

            @Override
            public void onFailure(Object error) {
                fetchingSettings = false;
                Utils.printLog(mContext, TAG, "Failed to fetch AppSetting");
            }
        }).execute();
    }

    /**
     * Method to show the chat widget on screen
     */

    public void show() {
        showTriggered = true;
        checkAndShowLauncher();
    }

    private void checkAndShowLauncher() {
        if (showTriggered && !fetchingSettings) {
            isShowing = true;
            setupChatWidget();
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    mWindowManager.addView(rootView, mParamsWindowManager);
                }
            });
        }
    }

    /**
     * Method to hide the chat widget on screen
     * Have to be called in Activity's onDestroy method
     */

    public void hide() {
        if (!isShowing) {
            return;
        }
        isShowing = false;
        Applozic.disconnectPublish(mContext);
        mWindowManager.removeViewImmediate(rootView);
    }

    public boolean isShowing() {
        return isShowing;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    private void setupChatWidget() {
        if (mParamsWindowManager == null) {
            mParamsWindowManager = new WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG,
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM |
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            mParamsWindowManager.gravity = Gravity.LEFT | Gravity.TOP;
            mParamsWindowManager.width = WindowManager.LayoutParams.WRAP_CONTENT;
            mParamsWindowManager.height = WindowManager.LayoutParams.WRAP_CONTENT;
        }

        int x = 0, y = 0;
        switch (config.gravity) {
            case LEFT_CENTER:
                x = config.paddingLeft;
                y = config.displayHeight / 2 - height / 2;
                break;
            case LEFT_TOP:
                x = config.paddingLeft;
                y = config.paddingTop;
                break;
            case TOP_CENTER:
                x = config.displayWidth / 2 - width / 2;
                y = config.paddingTop;
                break;
            case TOP_RIGHT:
                x = config.displayWidth - width - config.paddingRight;
                y = config.paddingTop;
                break;
            case RIGHT_CENTER:
                x = config.displayWidth - width - config.paddingRight;
                y = config.displayHeight / 2 - height / 2;
                break;
            case RIGHT_BOTTOM:
                x = config.displayWidth - width - config.paddingRight;
                y = config.displayHeight - height - config.paddingBottom;
                break;
            case BOTTOM_CENTER:
                x = config.displayWidth / 2 - width / 2;
                y = config.displayHeight - height - config.paddingBottom;
                break;
            case LEFT_BOTTOM:
                x = config.paddingLeft;
                y = config.displayHeight - height - config.paddingBottom;
                break;
            case CENTER:
                x = config.displayWidth / 2 - width / 2;
                y = config.displayHeight / 2 - height / 2;
                break;
        }
        mParamsWindowManager.x = x;
        mParamsWindowManager.y = y;
        if (config.launcherSize != 0) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(config.launcherSize, config.launcherSize);
            frameLayout.setLayoutParams(params);
            KmFloatingView.setLayoutParams(params);
        }

        final GestureDetector gestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (onClickListener != null) {
                    onClickListener.onClick(rootView);
                }
                return true;
            }
        });

        rootView.setOnTouchListener(new View.OnTouchListener() {
            float[] temp = new float[]{0, 0};

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (onClickListener != null && gestureDetector.onTouchEvent(motionEvent)) {
                    return true;
                }
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        temp[0] = motionEvent.getX();
                        temp[1] = motionEvent.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!config.movable)
                            break;
                        int x = (int) (motionEvent.getRawX() - temp[0]);
                        int y = (int) (motionEvent.getRawY() - temp[1]);
                        moveWindow(x, y);
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return true;
            }
        });
    }

    private void moveWindow(int x, int y) {
        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        int statusBarHeight = r.top;

        mParamsWindowManager.x = x;
        mParamsWindowManager.y = y - statusBarHeight;
        updateWindowSize();
    }

    private void updateWindowSize() {
        mWindowManager.updateViewLayout(rootView, mParamsWindowManager);
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, mContext.getResources().getDisplayMetrics());
    }
}
