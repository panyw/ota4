package com.fotile.c2i.ota.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.fotile.c2i.ota.R;

import java.util.LinkedList;


/**
 * 文件名称：OtaTopSnackBar
 * 创建时间：2017/8/15
 * 文件作者：wanghouyu
 * 功能描述：顶部悬浮窗提示类--同项目中的TopSnackBar
 */

public class OtaTopSnackBar {

    public static final int LENGTH_LONG = 4000;
    public static final int LENGTH_SHORT = 1500;
    private static final int MSG_HIDE = 0;
    private static final long NEXT_WAIT = 300;
    private static OtaTopSnackBar topSnackBarInstance;
    private static Context context;
    private static WindowManager windowManager;
    private static Handler handler;
    private int duration;
    private String snackBarContent;
    /**
     * 提示图片URL
     */
    private String avatarId;
    private View layout;
    private WindowManager.LayoutParams layoutParams;
    private TextView contentTv;
    /**
     * 提示图片
     */
    private ImageView avatarImg;
    private volatile boolean isShowing;
    private LinkedList<ShowTask> taskQueue = new LinkedList();
    /**
     * 提示语是否位于中间,默认位于左端
     */
    private boolean isCenter = true;
    /**
     * 是否显示提示图片
     */
    private boolean isShowImg = false;

    static {
        handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case MSG_HIDE:
                        return true;
                }
                return false;
            }
        });
    }

    /**
     * 应用方法
     *
     * @param context
     * @param snackBarContent
     * @param duration
     * @return
     */
    public static OtaTopSnackBar make(Context context, String snackBarContent, int duration) {
        initWindowManager(context);
        initTopSnackBar(snackBarContent, duration);
        return topSnackBarInstance;
    }

    /**
     * @param context
     * @param snackBarContent
     * @param duration
     * @param isCenter        是否需要居中
     * @return
     */

    public static OtaTopSnackBar make(Context context, String snackBarContent, int duration, boolean isCenter) {
        initWindowManager(context);
        initTopSnackBar(snackBarContent, duration, isCenter);
        return topSnackBarInstance;
    }

    /**
     *
     * @param context
     * @param avatarId 提示图片URL
     * @param snackBarContent 提示语
     * @param duration
     * @param isCenter 提示语是否居中
     * @return
     */
    public static OtaTopSnackBar make(Context context, String avatarId, String snackBarContent, int duration, boolean isCenter) {
        initWindowManager(context);
        initTopSnackBar(avatarId, snackBarContent, duration, isCenter);
        return topSnackBarInstance;
    }

    private static void initWindowManager(Context context) {
        com.fotile.c2i.ota.view.OtaTopSnackBar.context = context;
        windowManager = (WindowManager) com.fotile.c2i.ota.view.OtaTopSnackBar.context.getSystemService(Context.WINDOW_SERVICE);
    }

    private static void initTopSnackBar(String snackBarContent, int duration) {
        if (topSnackBarInstance == null) {
            topSnackBarInstance = new OtaTopSnackBar();
        }
        makeSnackBarContent(snackBarContent, duration);
    }

    private static void initTopSnackBar(String snackBarContent, int duration, boolean isCenter) {
        if (topSnackBarInstance == null) {
            topSnackBarInstance = new OtaTopSnackBar();
        }
        makeSnackBarContent(snackBarContent, duration, isCenter);
    }

    private static void initTopSnackBar(String avatarId, String snackBarContent, int duration, boolean isCenter) {
        if (topSnackBarInstance == null) {
            topSnackBarInstance = new OtaTopSnackBar();
        }
        makeSnackBarContent(avatarId, snackBarContent, duration, isCenter);
    }

    private static void makeSnackBarContent(String snackBarContent, int duration) {
        topSnackBarInstance.setContent(snackBarContent);
        topSnackBarInstance.setDuration(duration);
    }

    private static void makeSnackBarContent(String snackBarContent, int duration, boolean isCenter) {
        topSnackBarInstance.setContent(snackBarContent, isCenter);
        topSnackBarInstance.setDuration(duration);
    }

    private static void makeSnackBarContent(String avatarId, String snackBarContent, int duration, boolean isCenter) {
        topSnackBarInstance.setContent(avatarId, snackBarContent, isCenter);
        topSnackBarInstance.setDuration(duration);
    }
    private void setLayoutParam() {
        layoutParams = new WindowManager.LayoutParams();
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.windowAnimations = R.style.top_snack_bar_anim_style;
        layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;;
        layoutParams.gravity = Gravity.TOP;
        //设置图片格式，效果为背景透明
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.x = 0;
        layoutParams.y = 0;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
    }

    private void setLayout(int ksyun_top_snack_bar_layout) {
        layout = LayoutInflater.from(context).inflate(ksyun_top_snack_bar_layout, null, false);
        contentTv = (TextView) layout.findViewById(R.id.content_tv);
        avatarImg = (ImageView) layout.findViewById(R.id.img_head);
        contentTv.setText(snackBarContent);
        if (isCenter) {
            contentTv.setGravity(Gravity.CENTER);
        }
        if (isShowImg) {
//            Glide.with(context).load(avatarId).bitmapTransform(new RoundedCornersTransformation(context, 6, 0, RoundedCornersTransformation.CornerType.ALL)).into(avatarImg);
//            avatarImg.setVisibility(View.VISIBLE);
        }
    }

    private void setDuration(int duration) {
        this.duration = duration;
    }

    private void setContent(String snackBarContent) {
        this.snackBarContent = snackBarContent;
    }

    private void setContent(String snackBarContent, boolean isCenter) {
        this.snackBarContent = snackBarContent;
        this.isCenter = isCenter;
        isShowImg = false;
    }

    private void setContent(String avatarId, String snackBarContent, boolean isCenter) {
        this.snackBarContent = snackBarContent;
        this.avatarId = avatarId;
        this.isCenter = isCenter;
        isShowImg = true;
    }

    public void show() {
        if (!isShowing()) {
            setShowing(true);
            makeSnackBarLayout();
            windowManager.addView(layout, layoutParams);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    hide();
                }
            }, duration);
        } else {
            enqueueShowTask();
        }

    }

    /**
     * 设置弹出的SnackBar布局
     */
    private void makeSnackBarLayout() {
        topSnackBarInstance.setLayout(R.layout.layout_top_snack_bar_ota);
        topSnackBarInstance.setLayoutParam();
    }

    /**
     * 队列化
     */
    private void enqueueShowTask() {
        ShowTask showTask = new ShowTask();
        showTask.setContent(snackBarContent);
        showTask.setDuration(duration);
        taskQueue.add(showTask);
    }

    private void hide() {
        if (layout != null) {
            windowManager.removeView(layout);
        }
        setShowing(false);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                judgeNeedToShowNext();
            }
        }, NEXT_WAIT);
    }

    private void judgeNeedToShowNext() {
        ShowTask task = taskQueue.poll();
        if (task != null) {
            show();
        }
    }

    public boolean isShowing() {
        return isShowing;
    }

    public void setShowing(boolean showing) {
        isShowing = showing;
    }

    private static class ShowTask {
        private String mContent;
        private int mDuration;

        public void setContent(String content) {
            mContent = content;
        }

        public void setDuration(int duration) {
            mDuration = duration;
        }

        public String getContent() {
            return mContent;
        }

        public int getDuration() {
            return mDuration;
        }
    }
}
