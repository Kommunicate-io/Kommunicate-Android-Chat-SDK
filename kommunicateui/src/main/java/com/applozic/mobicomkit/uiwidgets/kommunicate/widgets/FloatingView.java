package com.applozic.mobicomkit.uiwidgets.kommunicate.widgets;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.broadcast.AlEventManager;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.commons.image.ImageLoader;
import com.applozic.mobicommons.commons.image.ImageUtils;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import io.kommunicate.async.KmAppSettingTask;
import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.models.KmAppSettingModel;


public class FloatingView {
    private Context mContext;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParamsWindowManager;
    private ViewGroup.MarginLayoutParams mParamsViewGroup;
    private View rootView;
    private View.OnClickListener onClickListener;
    private boolean isShowing = false;
    private TYPE type = TYPE.OVERLAY_SYSTEM;
    private KmAppSettingModel.KmChatWidget kmChatWidget;
    private FloatingViewConfig config;
    private int width, height;
    private Boolean movable;
    private ImageView KmFloatingView;
    private FrameLayout frameLayout;
    private enum TYPE{
        OVERLAY_SYSTEM, OVERLAY_ACTIVITY, OVERLAY_VIEWGROUP
    }
    public static final String LEFT = "left";
    public static final String RIGHT = "right";
    private static final String TAG = "KmFloatingIcon";

    //
    public FloatingView(final Context context) {
        this(context, new FloatingViewConfig.Builder().build());
        mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater mInflater = LayoutInflater.from(context);
        KmFloatingWidgetHelper kmFloatingWidgetHelper = new KmFloatingWidgetHelper(this);
        AlEventManager.getInstance().registerUIListener("123", kmFloatingWidgetHelper);
        rootView = mInflater.inflate(R.layout.view_floating, null, false);
        frameLayout = rootView.findViewById(R.id.circular_frame_layout);
        KmFloatingView = (ImageView) rootView.findViewById(R.id.km_floating_widget);
        new KmAppSettingTask(context, Applozic.getInstance(context).getApplicationKey(), new KmCallback() {
            @Override
            public void onSuccess(Object message) {
                final KmAppSettingModel appSettingModel = (KmAppSettingModel) message;
                if (appSettingModel != null && appSettingModel.getResponse() != null && appSettingModel.getChatWidget() != null) {
                        kmChatWidget = appSettingModel.getResponse().getChatWidget();
                        config.gravity =  kmChatWidget.getPosition().equals(LEFT) ?  FloatingViewConfig.GRAVITY.LEFT_BOTTOM : FloatingViewConfig.GRAVITY.RIGHT_BOTTOM;
                        DrawableCompat.setTint(DrawableCompat.wrap(frameLayout.getBackground()), Color.parseColor(kmChatWidget.getPrimaryColor()));
                        if(kmChatWidget.getIconIndex().equals("image")) {
                            ImageLoader loadImage = new ImageLoader(context, ImageUtils.getLargestScreenDimension((Activity) context)) {
                                @Override
                                protected Bitmap processBitmap(Object data) {
                                    return new FileClientService(context).loadMessageImage(context, kmChatWidget.getWidgetImageLink());
                                }
                            };
                            loadImage.loadImage(kmChatWidget.getWidgetImageLink(), KmFloatingView);
                        }
                        else {
                            switch (kmChatWidget.getIconIndex()) {
                                case ("1"):
                                    KmFloatingView.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.km_icon_1));
                                    break;
                                case ("2"):
                                    KmFloatingView.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.km_icon_2));
                                    break;
                                case ("3"):
                                    KmFloatingView.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.km_icon_3));
                                    break;
                                case ("4"):
                                    KmFloatingView.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.km_icon_4));
                                    break;
                            }
                        }
                        measure();
                        showOverlayActivity();
                } else {
                    Utils.printLog(context, TAG, "Failed to fetch App setting");
                    //loginUserWithKmCallBack(context, kmUser, callback);
                }
            }

            @Override
            public void onFailure(Object error) {
                Utils.printLog(context, TAG, "Failed to fetch AppSetting");
            }
        }).execute();

    }

    public void showUnreadCount() {
        Log.e("floatingview", "happens");
    }

    public FloatingView(Context context, int resource, FloatingViewConfig config) {
        this(context, config);
        LayoutInflater mInflater = LayoutInflater.from(context);
        rootView = mInflater.inflate(resource, null, false);
        measure();
    }

    public FloatingView(Context context, View view, FloatingViewConfig config) {
        this(context, config);
        rootView = view;
        measure();
    }

    private FloatingView(Context context, FloatingViewConfig config) {
        this.mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

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

    private void measure() {
        rootView.measure(0, 0);
        width = rootView.getMeasuredWidth();
        height = rootView.getMeasuredHeight();
    }

    public void showOverlaySystem() {
        if (isShowing) {
            return;
        }
        type = TYPE.OVERLAY_SYSTEM;
        initParams();
        initPosition();
        initWindowView();
        isShowing = true;
        mWindowManager.addView(rootView, mParamsWindowManager);
//        AndPermission.with(mContext)
//                .overlay()
//                .onGranted(new Action<Void>() {
//                    @Override
//                    public void onAction(Void data) {
//                        isShowing = true;
//                        mWindowManager.addView(rootView, mParamsWindowManager);
//                    }
//                })
//                .onDenied(new Action<Void>() {
//                    @Override
//                    public void onAction(Void data) {
//                        Toast.makeText(mContext, "需要悬浮窗权限", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .start();
    }


    public void showOverlayActivity() {
        if (isShowing) {
            return;
        }
        type = TYPE.OVERLAY_ACTIVITY;
        initParams();
        initPosition();
        initWindowView();
        isShowing = true;
        mWindowManager.addView(rootView, mParamsWindowManager);
    }

    public void showOverlayViewGroup(FrameLayout parent) {
        if (isShowing) {
            return;
        }
        type = TYPE.OVERLAY_VIEWGROUP;
        initParams();
        initPosition();
        initWindowView();
        isShowing = true;
        parent.addView(rootView, mParamsViewGroup);
    }

    public void hide() {
        if (!isShowing) {
            return;
        }
        isShowing = false;
        if (type == TYPE.OVERLAY_VIEWGROUP) {
            if (rootView.getParent() != null) {
                ((ViewGroup)rootView.getParent()).removeView(rootView);
            }
        } else if (type == TYPE.OVERLAY_SYSTEM || type == TYPE.OVERLAY_ACTIVITY){
            mWindowManager.removeView(rootView);
        }
    }

    public boolean isShowing() {
        return isShowing;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    private void initParams(){
        if (type == TYPE.OVERLAY_VIEWGROUP) {
            if (mParamsViewGroup == null) {
                mParamsViewGroup = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        } else if (type == TYPE.OVERLAY_SYSTEM){
            if (mParamsWindowManager == null) {
                mParamsWindowManager = new WindowManager.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mParamsWindowManager.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                }
                mParamsWindowManager.gravity = Gravity.LEFT | Gravity.TOP;
                mParamsWindowManager.width = WindowManager.LayoutParams.WRAP_CONTENT;
                mParamsWindowManager.height = WindowManager.LayoutParams.WRAP_CONTENT;
            }
        } else if (type == TYPE.OVERLAY_ACTIVITY) {
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
        }
    }

    private void initPosition() {
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
        if (type == TYPE.OVERLAY_SYSTEM || type == TYPE.OVERLAY_ACTIVITY) {
            mParamsWindowManager.x = x;
            mParamsWindowManager.y = y;
        } else if (type == TYPE.OVERLAY_VIEWGROUP){
            int marginLeft = rootView.getLeft() + x;
            marginLeft = Math.max(marginLeft, 0);
            marginLeft = Math.min(marginLeft, config.displayWidth - width);

            int marginTop = rootView.getTop() + y;
            marginTop = Math.max(marginTop, 0);
            marginTop = Math.min(marginTop, config.displayHeight - height);

            mParamsViewGroup.setMargins(marginLeft, marginTop, 0, 0);
        }
    }

    private void initWindowView(){
        final GestureDetector gestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener(){

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (onClickListener != null) {
                    onClickListener.onClick(rootView);
                }
                return true;
            }
        });
        if(!config.movable)
            return;
        rootView.setOnTouchListener(new View.OnTouchListener() {
            float[] temp = new float[]{0, 0};
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (onClickListener != null && gestureDetector.onTouchEvent(motionEvent)){
                    return true;
                }
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        if (type == TYPE.OVERLAY_VIEWGROUP) {
                            temp[0] = motionEvent.getRawX();
                            temp[1] = motionEvent.getRawY();
                        } else if (type == TYPE.OVERLAY_SYSTEM || type == TYPE.OVERLAY_ACTIVITY){
                            temp[0] = motionEvent.getX();
                            temp[1] = motionEvent.getY();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (type == TYPE.OVERLAY_VIEWGROUP) {
                            int offsetX = (int)(motionEvent.getRawX() - temp[0]);
                            int offsetY = (int)(motionEvent.getRawY() - temp[1]);
                            moveWindow(offsetX, offsetY);
                            temp[0] = motionEvent.getRawX();
                            temp[1] = motionEvent.getRawY();
                        } else if (type == TYPE.OVERLAY_SYSTEM || type == TYPE.OVERLAY_ACTIVITY){
                            int x = (int)(motionEvent.getRawX() - temp[0]);
                            int y = (int)(motionEvent.getRawY() - temp[1]);
                            moveWindow(x, y);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return true;
            }
        });
    }

    private void moveWindow(int x, int y){

        if (type == TYPE.OVERLAY_VIEWGROUP) {
            int marginLeft = rootView.getLeft() + x;
            marginLeft = Math.max(marginLeft, 0);
            marginLeft = Math.min(marginLeft, config.displayWidth - width);

            int marginTop = rootView.getTop() + y;
            marginTop = Math.max(marginTop, 0);
            marginTop = Math.min(marginTop, config.displayHeight - height);

            mParamsViewGroup.setMargins(marginLeft, marginTop, 0, 0);
            rootView.requestLayout();
        } else if (type == TYPE.OVERLAY_SYSTEM || type == TYPE.OVERLAY_ACTIVITY){
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            int statusBarHeight = r.top;

            mParamsWindowManager.x = x;
            mParamsWindowManager.y = y - statusBarHeight;
            updateWindowSize();
        }
    }

    private void updateWindowSize(){
        mWindowManager.updateViewLayout(rootView, mParamsWindowManager);
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, mContext.getResources().getDisplayMetrics());
    }
}
