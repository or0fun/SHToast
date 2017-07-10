package com.showjoy.android.toast;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;

import com.showjoy.android.toast.util.SHViewUtils;
import com.showjoy.android.toast.view.SHTagView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by lufei on 7/10/17.
 */

public class SHToast {

    public interface ToastListener {
        Activity getCurrentActivity();
    }

    private static ToastListener sToastListener;
    private static Handler handler;

    final static int MSG_NORMAL = 0;
    final static int MSG_REMOVE = 1;

    static SHTagView toastView;

    // 动画时间
    private static final int ANIMATION_DURATION = 600;

    private static AlphaAnimation mFadeOutAnimation;
    private static AlphaAnimation mFadeInAnimation;
    private static boolean isShow = false;

    private static ViewGroup container = null;

    static BlockingQueue<ToastMsg> queue = new LinkedBlockingDeque<>();


    public static final int LENGTH_SHORT = 0;

    public static final int LENGTH_LONG = 1;

    @IntDef({LENGTH_SHORT, LENGTH_LONG})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Duration {
    }

    static class ToastMsg {
        String msg;
        int hideDelay;
        int hideAnimationDelay;
    }

    private ToastMsg toastMsg;

    private SHToast(ToastMsg toastMsg) {
        this.toastMsg = toastMsg;
    }

    public static void init(ToastListener toastListener) {
        sToastListener = toastListener;

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_REMOVE:
                        try {
                            if (null != toastView) {
                                if (toastView.getParent() != null) {
                                    try {
                                        toastView.setVisibility(View.GONE);
                                        container.removeView(toastView);
                                        container = null;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    isShow = false;
                                    if (queue.size() > 0) {
                                        toShow(queue.remove());
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case MSG_NORMAL:
                    default:
                        try {
                            ToastMsg message = (ToastMsg) msg.obj;
                            if (isShow) {
                                queue.add(message);
                            } else {
                                toShow(message);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                }

            }
        };
    }

    public static SHToast makeText(Context context, CharSequence msg, @Duration int duration) {

        ToastMsg toastMsg = new ToastMsg();
        toastMsg.msg = String.valueOf(msg);

        if (duration == LENGTH_LONG) {
            toastMsg.hideDelay = 3500;
        }else {
            toastMsg.hideDelay = 2000;
        }
        toastMsg.hideAnimationDelay = toastMsg.hideDelay - ANIMATION_DURATION;

        return new SHToast(toastMsg);

    }

    public static SHToast makeText(Context context,  @StringRes int resId, @Duration int duration) {

        ToastMsg toastMsg = new ToastMsg();
        toastMsg.msg = context.getString(resId);

        if (duration == LENGTH_LONG) {
            toastMsg.hideDelay = 3500;
        }else {
            toastMsg.hideDelay = 2000;
        }
        toastMsg.hideAnimationDelay = toastMsg.hideDelay - ANIMATION_DURATION;

        return new SHToast(toastMsg);

    }

    public void show() {
        handler.sendMessage(handler.obtainMessage(MSG_NORMAL, toastMsg));
    }

    private static void toShow(Activity activity, ToastMsg msg) {
        try {
            if (!initToastView(activity, msg)) {
                return;
            }

            if (toastView != null) {
                if (toastView.getParent() != null) {
                    container.removeView(toastView);
                }
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
                container.addView(toastView, layoutParams);

                startAnimation(msg);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void toShow(ToastMsg msg) {

        if (null == sToastListener) {
            return;
        }
        Activity activity = sToastListener.getCurrentActivity();
        toShow(activity, msg);

    }

    private static boolean initToastView(Activity activity, ToastMsg msg) {
        if (null == activity) {
            return false;
        }
        FragmentActivity fragmentActivity;
        android.support.v4.app.Fragment visibleFragment = null;
        android.app.Fragment appFragment = null;
        Bundle bundle = new Bundle();
        bundle.putInt("key", 0);
        try {
            appFragment = activity.getFragmentManager().getFragment(bundle, "key");
        } catch (Exception e) {

        }
        if (null != appFragment) {
            View rootView = appFragment.getView();
            ViewParent viewParent = null;
            while (null != rootView.getParent()) {
                viewParent = rootView.getParent();
                if (null != viewParent && viewParent instanceof ViewGroup) {
                    rootView = (ViewGroup) viewParent;
                } else {
                    break;
                }
            }
            container = (ViewGroup) rootView.findViewById(android.R.id.content);
        }

        if (null == container) {
            if (activity instanceof FragmentActivity) {
                fragmentActivity = (FragmentActivity) activity;
                List<android.support.v4.app.Fragment> fragments = fragmentActivity.getSupportFragmentManager().getFragments();
                if (null != fragments) {
                    for (android.support.v4.app.Fragment fragment : fragments) {
                        if (fragment != null && fragment.getUserVisibleHint()) {
                            visibleFragment = fragment;
                            break;
                        }
                    }
                }
            }
            if (null != visibleFragment) {
                View rootView = visibleFragment.getView();
                container = (ViewGroup) rootView.findViewById(android.R.id.content);
            }
        }

        if (null == container) {
            container = (ViewGroup) activity.findViewById(android.R.id.content);
        }

        if (null == container) {
            return false;
        }

        if (null == toastView) {
            toastView = new SHTagView(activity);
            toastView.setCornerRadius(SHViewUtils.dp2px(activity, 5));
            toastView.setColor(Color.parseColor("#b2000000"));
            toastView.setTextColor(Color.WHITE);
            toastView.setTextSize(14);
            int h = SHViewUtils.dp2px(activity, 25);
            int v = SHViewUtils.dp2px(activity, 15);
            toastView.setPadding(h, v, h, v);
        }
        toastView.setGravity(Gravity.CENTER);

        if (!TextUtils.isEmpty(msg.msg)) {
            toastView.setText(msg.msg.trim());
        }
        return true;
    }

    public static void toast(Activity activity, String msg) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        if (null == activity) {
            return;
        }

        makeText(activity, msg, LENGTH_SHORT).show();
    }

    public static void toast(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        if (null == sToastListener) {
            return;
        }
        Activity activity = sToastListener.getCurrentActivity();
        makeText(activity, msg, LENGTH_SHORT).show();
    }

    public static void toast(Context context, @StringRes int stringId) {
        String msg = context.getString(stringId);
        if (TextUtils.isEmpty(msg)) {
            return;
        }

        handler.sendMessage(handler.obtainMessage(0, msg));
    }

    private static void startAnimation(ToastMsg msg) {
        if (isShow) {
            // 若已经显示，则不再次显示
            return;
        }
        isShow = true;
        // 显示动画
        mFadeInAnimation = new AlphaAnimation(0.0f, 1.0f);
        // 消失动画
        mFadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);
        mFadeOutAnimation.setDuration(ANIMATION_DURATION);

        mFadeInAnimation.setDuration(ANIMATION_DURATION);
        toastView.setVisibility(View.VISIBLE);
        toastView.startAnimation(mFadeInAnimation);

        handler.postDelayed(mHideAnimationRunnable, msg.hideAnimationDelay);
        handler.postDelayed(mHideRunnable, msg.hideDelay);
    }

    private static final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            handler.sendEmptyMessage(MSG_REMOVE);
        }
    };
    private static final Runnable mHideAnimationRunnable = new Runnable() {
        @Override
        public void run() {
            toastView.startAnimation(mFadeOutAnimation);
        }
    };
}
