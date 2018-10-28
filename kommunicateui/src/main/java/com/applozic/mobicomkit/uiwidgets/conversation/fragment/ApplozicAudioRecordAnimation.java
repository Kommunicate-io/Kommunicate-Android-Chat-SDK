package com.applozic.mobicomkit.uiwidgets.conversation.fragment;

import android.view.View;
import android.view.animation.Animation;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

public class ApplozicAudioRecordAnimation extends Animation {

    private static final WeakHashMap<View, ApplozicAudioRecordAnimation> PROXIES = new WeakHashMap<View, ApplozicAudioRecordAnimation>();
    private final WeakReference<View> mView;
    private float mAlpha = 1;

    private ApplozicAudioRecordAnimation(View view) {
        setDuration(0);
        setFillAfter(true);
        view.setAnimation(this);
        mView = new WeakReference<View>(view);
    }

    public static ApplozicAudioRecordAnimation wrap(View view) {
        ApplozicAudioRecordAnimation proxy = PROXIES.get(view);
        Animation animation = view.getAnimation();
        if (proxy == null || proxy != animation && animation != null) {
            proxy = new ApplozicAudioRecordAnimation(view);
            PROXIES.put(view, proxy);
        } else if (animation == null) {
            view.setAnimation(proxy);
        }
        return proxy;
    }

    public static void setAlpha(View view, float alpha) {
        view.setAlpha(alpha);
    }

    public static float getX(View view) {
        return view.getX();
    }

    public float getAlpha() {
        return mAlpha;
    }

    public void setAlpha(float alpha) {
        if (mAlpha != alpha) {
            mAlpha = alpha;
            View view = mView.get();
            if (view != null) {
                view.invalidate();
            }
        }
    }

    public float getX() {
        View view = mView.get();
        if (view == null) {
            return 0;
        }
        return view.getLeft();
    }
}
