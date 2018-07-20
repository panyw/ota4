package com.fotile.c2i.ota.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dl7.downloaderlib.entity.FileInfo;
import com.dl7.downloaderlib.model.DownloadStatus;
import com.fotile.c2i.ota.R;
import com.fotile.c2i.ota.bean.DownloadEvent;
import com.fotile.c2i.ota.bean.OtaFileInfo;
import com.fotile.c2i.ota.bean.UpgradeInfo;
import com.fotile.c2i.ota.service.DownLoadService;
import com.fotile.c2i.ota.service.DownloadAction;
import com.fotile.c2i.ota.util.OtaConstant;
import com.fotile.c2i.ota.util.OtaListener;
import com.fotile.c2i.ota.util.OtaLog;
import com.fotile.c2i.ota.util.OtaTool;
import com.fotile.c2i.ota.util.OtaUpgradeUtil;
import com.fotile.c2i.ota.view.HorizontalProgressBarWithNumber;
import com.fotile.c2i.ota.view.OtaLoadingView;
import com.fotile.c2i.ota.view.OtaTopSnackBar;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 文件名称：SystemUpgradeFragment
 * 创建时间：2017/915
 * 文件作者：fuzya
 * 功能描述：系统升级
 */

public class SystemUpgradeFragment extends Fragment {
    /**
     * 无更新版本布局  2
     */
    RelativeLayout layout_no_upgrade;
    /**
     * 下载中布局
     */
    RelativeLayout layout_upgrading;
    /**
     * 检测中布局 8
     */
    RelativeLayout lay_laoding;
    /**
     * 有 新版本    重试    下载完成 布局
     */
    RelativeLayout layout_main_completed;
    /**
     * wifi 为打开界面
     */
    RelativeLayout layout_error;
    /**
     * 查询失败界面
     */
    RelativeLayout layout_error_connect;
    /***********************************************   无新版本界面 ******************************************/
    TextView txt_version_info;
    /***********************************************   下载中界面 ******************************************/
    /** 进度条 **/
    HorizontalProgressBarWithNumber pbar_download;
    /** 下载提示 **/
    TextView tv_tips;
    /***********************************************   检测中界面 ******************************************/
    /** 加载中动画 **/
    OtaLoadingView img_loading;
    /***********************************************   下载完成界面 ******************************************/
    /** 立即更新**/
    Button btn_upgrade_now;
    /** 稍后更新**/
    Button btn_upgrade_later;
    /** 系统更新**/
    Button update_bottom;
    /** 系统更新-- 重试**/
    Button update_retry_bottom;
    /** 系统更新-- 版本信息**/
    TextView txt_update_version;
    /** 系统更新-- 更新内容**/
    TextView tv_update_comment;
    /***********************************************   错误界面 ******************************************/
    /** 下载错误界面 去设置wifi**/
    Button set_wifi;
    /** 链接错误按钮 重试**/
    Button retry;
    /** 下载完成提示 */
    TextView txt_update_download_finish;
    public static final int NO_INVALID_PACKAGE = 0;//无新的升级包
    public static final int NEW_INVALID_PACKAGE = 1;//有新的升级包
    public static final int ERROR_INVALID_PACKAGE = 2;//获取数据异常
    /**
     * 下载工具类
     */
    private OtaUpgradeUtil otaUpgradeUtil;

    public UpgradeInfo mInfo;
    /**
     * 校验包名
     */
    private String check_package_name;
    /**
     * 校验本地版本号
     */
    private String check_version_code;
    /**
     * 校验本地mac
     */
    private String check_mac_address;

    protected View view;

    private DownloadAction.ActionListener action;

    private OtaListener otaListener;

    /**
     * view状态-获取信息中
     */
    private static int VIEW_STATE_LOADING = -1;
    /**
     * view状态-获取信息失败，或者无网络
     */
    private static int VIEW_STATE_NO_DATA = 1;
    /**
     * view状态-没有可更新
     */
    private static int VIEW_STATE_NO_PACKAGE = 2;
    /**
     * view状态-有可更新
     */
    private static int VIEW_STATE_NEW_PACKAGE = 3;
    /**
     * view状态-wifi未打开
     */
    private static int VIEW_WIFI_NO_OPEN = 7;

    /**
     * 下载中
     */
    private static int VIEW_DOWN_DOWNING = 4;
    /**
     * 下载错误
     */
    private static int VIEW_DOWN_ERROR = 5;
    /**
     * 下载完成
     */
    private static int VIEW_DOWN_COMPLETE = 6;
    /**
     * 获取超时
     */
    private static  int GET_INFO_TIMEOUT = 9;
    /**
     * 显示titleBar
     *
     */
    private static int SHOW_TOPBAR = 99;
    /**
     * 是否正在获取固件信息
     */
    private boolean is_loading_version_data;
    private int last_view_munber = -1;
    private Timer timer_net;
    private  int retry_time = 0;

    public SystemUpgradeFragment(String packageName, Typeface typeface, OtaListener otaListener) {
        check_package_name = packageName;
        this.otaListener = otaListener;
        OtaTool.setTypeFace(typeface);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_system_upgradeback, container, false);
        initView();
        createAction();

        return view;
    }


    public void createAction() {
        action = new DownloadAction.ActionListener() {
            @Override
            public void onAction(OtaFileInfo otaFileInfo) {
                OtaLog.LOGOta("系统升级界面","createAction  当前文件状态"+otaFileInfo.fileInfo.getStatus());
                FileInfo fileInfo = otaFileInfo.fileInfo;
                if(otaFileInfo.errorMsg!=null && (otaFileInfo.errorMsg.equals(OtaConstant.MD5_CHECK_ERROR)||otaFileInfo.errorMsg.equals(OtaConstant.MCU_MD5_CHECK_ERROR))){
                    showView(VIEW_STATE_LOADING);
                    startInitLogicThread();
                    return;
                }else {

                }
                switch (fileInfo.getStatus()) {
                    //开始下载
                    case DownloadStatus.START:
                        updateDownloadValue(VIEW_DOWN_DOWNING);
                        pbar_download.setMax(fileInfo.getTotalBytes());
                        pbar_download.setProgress(fileInfo.getLoadBytes());
                        break;
                    //正在下载
                    case DownloadStatus.DOWNLOADING:
                        //等待固件信息获取完毕，才去刷新
                        if (!is_loading_version_data) {
                            updateDownloadValue(VIEW_DOWN_DOWNING);
                            pbar_download.setMax(fileInfo.getTotalBytes());
                            pbar_download.setProgress(fileInfo.getLoadBytes());
                        }
                        break;
                    //下载失败
                    case DownloadStatus.ERROR:
                        //等待固件信息获取完毕，才去刷新
                        OtaLog.LOGOta("下载监听 ","下载出错");
                        if (!is_loading_version_data) {
                            updateDownloadValue(VIEW_DOWN_ERROR);
                        }
                        break;
                    //下载完成
                    case DownloadStatus.COMPLETE:
                        //等待固件信息获取完毕，才去刷新
                        if (!is_loading_version_data) {
                            updateDownloadValue(VIEW_DOWN_COMPLETE);
                        }
                        break;
                }
            }
        };
        DownloadAction.getInstance().addAction(action);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != action) {
            DownloadAction.getInstance().removeAction();
        }
        cancelNetTimer();
        checkhandler.removeCallbacksAndMessages(null);
        showViewhandler.removeCallbacksAndMessages(null);
    }



    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        OtaLog.LOGOta("升级界面状态","当前系统升级fregement 状态 ="+hidden);
        if (!hidden) {
            //针对清空缓存逻辑
            OtaLog.LOGOta("升级界面状态","当前下载状态"+ DownLoadService.getDownLoadState());
            updateUI();
            DownLoadService.setShow_downing_tip(false);
            if (!is_loading_version_data) {
                int state = DownLoadService.getDownLoadState();
                //如果文件不存在，并且服务状态是完成状态，表示下载完了被清空了
                if (!checkDownloadFileExists() && state == DownloadStatus.COMPLETE) {
                    showView(VIEW_STATE_LOADING);
                    startInitLogicThread();
                }
            }

        }
    }

    /**
     * 1--显示升级界面
     * 2--显示完成界面
     *
     * @param type
     */
    private void showView(int type) {
        last_view_munber =type;
        layout_no_upgrade.setVisibility(View.GONE);
        layout_upgrading.setVisibility(View.GONE);
        lay_laoding.setVisibility(View.GONE);
        layout_error.setVisibility(View.GONE);
        layout_error_connect.setVisibility(View.GONE);
        layout_main_completed.setVisibility(View.GONE);
        img_loading.stopRotationAnimation();
        if(type == VIEW_STATE_LOADING){
            lay_laoding.setVisibility(View.VISIBLE);
            img_loading.startRotationAnimation();
        }else if (type == VIEW_STATE_NO_DATA){
            layout_error_connect.setVisibility(View.VISIBLE);
        }else if (type == VIEW_STATE_NO_PACKAGE){
            layout_no_upgrade .setVisibility(View.VISIBLE);
        }else if (type == VIEW_STATE_NEW_PACKAGE || type == VIEW_DOWN_COMPLETE || type == VIEW_DOWN_ERROR){
            layout_main_completed.setVisibility(View.VISIBLE);
            showCompletedView(type);
        }else if (type == VIEW_WIFI_NO_OPEN){
            layout_error.setVisibility(View.VISIBLE);
        }else if (type == VIEW_DOWN_DOWNING ){
            layout_upgrading.setVisibility(View.VISIBLE);
        }else{
            layout_no_upgrade.setVisibility(View.VISIBLE);
        }
        updateVersionValue(type);
    }
    private void showCompletedView(int type){
        btn_upgrade_now.setVisibility(View.GONE);
        btn_upgrade_later.setVisibility(View.GONE);
        update_bottom.setVisibility(View.GONE);
        update_retry_bottom.setVisibility(View.GONE);

        if(type == VIEW_STATE_NEW_PACKAGE){
            txt_update_download_finish.setVisibility(View.INVISIBLE);
            update_bottom.setVisibility(View.VISIBLE);
        }else if(type == VIEW_DOWN_COMPLETE){
            txt_update_download_finish.setVisibility(View.VISIBLE);
            btn_upgrade_now.setVisibility(View.VISIBLE);
            btn_upgrade_later.setVisibility(View.VISIBLE);
        }else if(type == VIEW_DOWN_ERROR){
            txt_update_download_finish.setVisibility(View.INVISIBLE);
            update_retry_bottom.setVisibility(View.VISIBLE);
        }else {
            txt_update_download_finish.setVisibility(View.INVISIBLE);
        }
    }

    private void initView() {
        layout_no_upgrade = (RelativeLayout) view.findViewById(R.id.layout_no_upgrade);
        layout_upgrading= (RelativeLayout) view.findViewById(R.id.layout_upgrading);
        lay_laoding= (RelativeLayout) view.findViewById(R.id.lay_laoding);
        layout_error= (RelativeLayout) view.findViewById(R.id.layout_error);
        layout_error_connect = (RelativeLayout) view.findViewById(R.id.layout_error_connect);
        layout_main_completed = (RelativeLayout) view.findViewById(R.id.layout_main_completed);
        txt_version_info = (TextView) view.findViewById(R.id.txt_version_info);
        pbar_download = (HorizontalProgressBarWithNumber) view.findViewById(R.id.pbar_download);
        img_loading = (OtaLoadingView) view.findViewById(R.id.img_loading);
        update_bottom = (Button)view.findViewById(R.id.update_bottom);
        update_retry_bottom = (Button) view.findViewById(R.id.update_retry_bottom);
        txt_update_version = (TextView) view.findViewById(R.id.txt_update_version);
        tv_update_comment = (TextView) view.findViewById(R.id.tv_update_comment);
        btn_upgrade_now = (Button) view.findViewById(R.id.btn_upgrade_now);
        btn_upgrade_later = (Button) view.findViewById(R.id.btn_upgrade_later);
        set_wifi = (Button) view.findViewById(R.id.set_wifi);
        retry = (Button) view.findViewById(R.id.retry);
        tv_tips = (TextView)view.findViewById(R.id.tv_tips);
        txt_update_download_finish = (TextView)view.findViewById(R.id.download_finish_tips);
        /*************************  去设置wifi  ****************/
        set_wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != otaListener ) {
                    otaListener.gotoWifiActivity();

                }
            }
        });

        //----------------------------------------------------------------------//
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showView(VIEW_STATE_LOADING);
                startInitLogicThread();
            }
        });

        //-----------------------------------listener-----------------------------------//
        //点击下载或者重新下载
        update_bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (OtaTool.fastclick()) {
                    if (OtaTool.isNetworkAvailable(getActivity()) && null != mInfo) {
                        OtaLog.LOGOta("升级按钮","需要下载的文件大小："+mInfo.size);
                        if(mInfo.size!=null){
                            if(OtaTool.canUpdate(Long.valueOf(mInfo.size))){
                                startDownLoadService();
                                showView(VIEW_DOWN_DOWNING);
                            }else {
                                OtaTopSnackBar.make(getActivity(), "本地剩余空间不足，请先清理缓存或删除收藏的不常用菜谱", OtaTopSnackBar.LENGTH_SHORT).show();
                            }
                        }else {
                            //如果不返回文件大小，默认文件满足下载条件
                            startDownLoadService();
                            showView(VIEW_DOWN_DOWNING);
                        }


                    } else {
                        OtaTopSnackBar.make(getActivity(), "请检查网络连接！", OtaTopSnackBar.LENGTH_SHORT).show();
                    }
                }
            }
        });
        //立即安装
        btn_upgrade_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OtaTool.setNowVersion(getActivity(), OtaTool.getProperty("ro.cvte.customer.version", "unknow"));

                //如果设备没有工作中，才执行ota
                if (null != otaListener && null != mInfo) {
                    if (OtaTool.fastclick()) {

                        boolean contain_mcu = !TextUtils.isEmpty(mInfo.ex_url) && OtaTool.checkMCUFiles(getActivity());
                        otaListener.onInstallNow(contain_mcu);
                    }
                }
            }
        });
        //稍后安装
        btn_upgrade_later.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != otaListener && null != mInfo) {
                    if (OtaTool.fastclick()) {
                        boolean contain_mcu = !TextUtils.isEmpty(mInfo.ex_url) && OtaTool.checkMCUFiles(getActivity());
                        otaListener.onInstallLater(contain_mcu);
                    }
                }
            }
        });
        update_retry_bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (OtaTool.fastclick()) {
                    if (OtaTool.isNetworkAvailable(getActivity()) && null != mInfo) {
                        startDownLoadService();
                        showView(VIEW_DOWN_DOWNING);
                    } else {
                        OtaTopSnackBar.make(getActivity(), "请检查网络连接！", OtaTopSnackBar.LENGTH_SHORT).show();
                    }
                }
            }
        });

        //-----------------------------------listener-----------------------------------//

        //默认显示升级界面
//        OtaTool.showTips(getActivity());
        OtaLog.LOGOta("升级界面","当前下载状态"+ DownLoadService.getDownLoadState());

        showView(VIEW_STATE_LOADING);
        otaUpgradeUtil = new OtaUpgradeUtil();
        startInitLogicThread();
        DownLoadService.setShow_downing_tip(false);

    }
    private void startInitLogicThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                startInitLogic();
            }
        }).start();

    }

    private void startInitLogic() {
        //显示加载动画--如果网络畅通，获取升级包信息
        Context mcontext = getActivity();
        if(mcontext == null )return;
        OtaLog.LOGOta("升级界面","获取网络状态");
        try {
            OtaLog.LOGOta("升级界面"," 获取网络状态"+ OtaTool.getWifiState(mcontext));
        }catch (Exception e){

        }
        File file = new File(OtaConstant.FILE_NAME_OTA);
        //网络断开且有本地文件
        if (file.exists() && !OtaTool.isNetworkAvailable(mcontext)){
            OtaLog.LOGOta("升级界面"," 开始校验md5 ===  有本地文件");

            String lastMd5 = OtaTool.getLastUpdateVersionMD5(mcontext);
            String lastname =OtaTool.getLastUpdateVersionName(mcontext);
            String lastversionCommnet = OtaTool.getLastUpdateVersionComment(mcontext);
            if( OtaUpgradeUtil.md5sum(file.getPath()).equals(lastMd5)){
                OtaLog.LOGOta("升级界面"," 校验md5 完成 成功");
                if(mInfo == null){
                    mInfo = new UpgradeInfo();
                }
                mInfo.name = lastname;
                mInfo.comment = lastversionCommnet;
                File file1 = new File(OtaConstant.FILE_NAME_MCU);
                if (file1.exists()){
                    mInfo.ex_url = "存在url";
                }else {
                    mInfo.ex_url="";
                }
                showViewhandler.sendEmptyMessage(VIEW_DOWN_COMPLETE);
                return;
            }
            OtaLog.LOGOta("升级界面"," 校验md5 完成 失败");
        }
        //网络断开
        if(OtaTool.getWifiState( mcontext) == WifiManager.WIFI_STATE_DISABLED || TextUtils.isEmpty(OtaTool.getConnectWifiSsid(mcontext)) ){
            showViewhandler.sendEmptyMessage(VIEW_WIFI_NO_OPEN);
            return;
        }


        //网络可用
        if (OtaTool.isNetworkAvailable(mcontext)) {
            is_loading_version_data = true;
            showViewhandler.sendEmptyMessage(VIEW_STATE_LOADING);
            getParams();
        } else {
            showViewhandler.sendEmptyMessage(VIEW_STATE_NO_DATA);
            showViewhandler.sendEmptyMessage(SHOW_TOPBAR);
        }
    }



    /**
     * 根据获取到的版本信息，来显示版本相关view
     *
     * @param type -1-固件包数据获取中，loading现实中
     *             0-网络异常，或者没有获取到数据
     *             1-没有可更新的固件包
     *             2-有可更新的固件包
     */
    private void updateVersionValue(int type) {
        //tv_old_version_tip.setText("方太智慧厨房 " + OtaTool.getProperty("ro.cvte.customer.version", "unknow"));
        //信息获取中
        if (type == VIEW_STATE_LOADING) {
        }
        //获取数据失败，没有网络
        else if (type == VIEW_STATE_NO_DATA) {
            //当前系统版本
            String tip = "方太智慧厨房 " + OtaTool.getProperty("ro.cvte.customer.version", "unknow");
            txt_version_info.setText(tip);
            txt_version_info.setVisibility(View.VISIBLE);

        }
        //没有可更新的固件包
        if (type == VIEW_STATE_NO_PACKAGE) {
            String tip = "方太智慧厨房 " + OtaTool.getProperty("ro.cvte.customer.version", "unknow") + "\n当前为最新版本。";
            txt_version_info.setText(tip);
            txt_version_info.setVisibility(View.VISIBLE);

        }
        //有可更新的固件包
        if (type == VIEW_STATE_NEW_PACKAGE || type == VIEW_DOWN_COMPLETE) {
            String tip = "检测到有新系统版本" + mInfo.name;
            txt_update_version.setText(tip);
            txt_update_version.setVisibility(View.VISIBLE);
            tv_update_comment.setText(mInfo.comment);
            tv_update_comment.setVisibility(View.VISIBLE);

        }
    }


    private synchronized void cancelNetTimer() {
        if (null != timer_net) {
            timer_net.cancel();
            timer_net.purge();
            timer_net = null;
            retry_time = 0;
        }
    }

    /**
     * 根据下载状态来显示相应的view
     *
     * @param type
     */
    private void updateDownloadValue(int type) {
        //下载完成
        if (type == VIEW_DOWN_COMPLETE) {
            //tv_tips.setText("升级包下载完成");
            //下载完成界面跳转
            if (null != otaListener && null != mInfo) {
                otaListener.onDownloadCompleted(mInfo.name);
                OtaTool.setLastUpdateVersion(getActivity(),mInfo,OtaConstant.UPDATEFINISH);
            }



            showView(VIEW_DOWN_COMPLETE);
        }
        //下载失败
        if (type == VIEW_DOWN_ERROR) {
            //网络造成的下载失败
            if (!OtaTool.isNetworkAvailable(getActivity())) {

                tv_tips.setText("暂停下载升级包");

                if (null == timer_net) {
                    timer_net = new Timer();
                    retry_time = 0;
                }
                timer_net.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        //网络连接时，自动下载
                        if (OtaTool.isNetworkAvailable(getActivity())) {
                            startDownLoadService();
                            cancelNetTimer();
                        }else {
                            retry_time++;
                        }
                        if(retry_time>=120){
                            showView(VIEW_DOWN_ERROR);
                            cancelNetTimer();
                        }
                    }
                }, 1000, 1000);
            } else {
                startDownLoadService();
                cancelNetTimer();
            }
        }
        //下载中
        if (type == VIEW_DOWN_DOWNING) {
            tv_tips.setText("正在下载升级包");
            showView(VIEW_DOWN_DOWNING);
        }

    }

    /**
     * 开启下载服务
     */
    public void startDownLoadService() {
        if (null != mInfo) {
            OtaLog.LOGOta("升级界面","开始下载服务 ");
            Intent intent = new Intent(getActivity(), DownLoadService.class);
            intent.putExtra("url", mInfo.url);
            intent.putExtra("md5", mInfo.md5);
            intent.putExtra("ex_url", mInfo.ex_url);
            intent.putExtra("ex_md5", mInfo.ex_md5);
            getActivity().startService(intent);
        }
    }


    /**
     * 固件包升级信息回调
     * 进入该回调，mInfo有值了
     */
    private Handler checkhandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(android.os.Message msg) {
            OtaLog.LOGOta("升级界面","当前信息回调"+msg.what);
            //如果这里是false 说明已经有返回了  不需要继续处理
            if(!is_loading_version_data){
                return;
            }
            is_loading_version_data = false;
            if (msg.what == ERROR_INVALID_PACKAGE) {
                showView(VIEW_STATE_NO_DATA);

                //没有可更新的固件包
            } else if (msg.what == NO_INVALID_PACKAGE) {
                showView(VIEW_STATE_NO_PACKAGE);

                //有可更新的固件包
            } else if (msg.what == NEW_INVALID_PACKAGE) {
                OtaLog.LOGOta("===升级界面","当前的工作状态："+"进入有可更新固件 且没有本地文件");

                showView(VIEW_STATE_NEW_PACKAGE);
                if(getActivity()!=null){
                    OtaTool.setLastUpdateVersion(getActivity(), mInfo, OtaConstant.UPDATEFINISH);
                }
                int state = DownLoadService.getDownLoadState();
                OtaLog.LOGOta("===当前获取到固件信息后","当前的工作状态："+state);
                //如果当前后台正在下载
                if (state == DownloadStatus.DOWNLOADING) {
                    showView(VIEW_DOWN_DOWNING);

                }else if (state == DownloadStatus.ERROR) {//如果当前后台下载错误
                    showView(VIEW_DOWN_ERROR);
                }

            } else if (msg.what == GET_INFO_TIMEOUT) {
                OtaLog.LOGOta("升级界面","获取超时");
                showView(VIEW_STATE_NO_DATA);

            } else if(msg.what == VIEW_DOWN_COMPLETE) { //下载完成
                OtaLog.LOGOta("===升级界面","当前的工作状态："+"进入有可更新固件 且本地文件已经校验通过");
                showView(VIEW_DOWN_COMPLETE);
                if(null != otaListener){
                    otaListener.onDownloadCompleted(mInfo.name);
                }
            }


        }
    };
    /**
     * 固件包升级信息回调
     * 进入该回调，mInfo有值了
     */
    private Handler showViewhandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == VIEW_DOWN_COMPLETE) {
                showView(VIEW_DOWN_COMPLETE);
            } else if (msg.what == VIEW_WIFI_NO_OPEN) {
                showView(VIEW_WIFI_NO_OPEN);
            } else if (msg.what == VIEW_STATE_LOADING) {
                showView(VIEW_STATE_LOADING);
            } else if (msg.what == VIEW_STATE_NO_DATA) {
                showView(VIEW_STATE_NO_DATA);
            }else if(msg.what == SHOW_TOPBAR){
                OtaTopSnackBar.make(getActivity(), "请检查网络连接！", OtaTopSnackBar.LENGTH_SHORT).show();
            }
        }


    };

    /**
     * 获取升级包信息
     */
    private void getParams() {
        //用于匹配OTA服务器的包名
        //check_package_name = OtaConstant.PACKAGE_NAME;
        //校验匹配版本号
        check_version_code = OtaTool.getProperty("ro.cvte.customer.version", "100");
        //校验mac地址
        check_mac_address = OtaTool.getLocalMacAddress();
        getUpgradeInfo();
    }


    /**
     * 判断ota文件是否存在--包含mcu文件，两个文件只要有一个存在就存在
     *
     * @return
     */
    public boolean checkDownloadFileExists() {
        File file_ota = new File(OtaConstant.FILE_NAME_OTA);
        File file_mcu = new File(OtaConstant.FILE_NAME_MCU);
        return file_ota.exists() || file_mcu.exists();
    }


    /**
     * 获取OTA服务器上的固件的信息
     */

    private void getUpgradeInfo() {
        String reqUrl = otaUpgradeUtil.buildUrl(check_package_name, check_version_code, check_mac_address != null ?
                check_mac_address.replace(":", "") : "",getActivity());
        if (OtaConstant.TEST_URL_FLAG) {
            reqUrl = OtaConstant.TEST_URL.replace("{version}",OtaTool.getProperty("ro.cvte.customer.version", "100"));
        }
        OtaLog.LOGOta("请求Ota包信息url","url地址"+ reqUrl);
        String content = "";
        String miwen = "";
        String mingwen = "";
        try {
            checkhandler.sendEmptyMessageDelayed(GET_INFO_TIMEOUT,20*1000);
            content = otaUpgradeUtil.httpGet(reqUrl);
            checkhandler.removeMessages(GET_INFO_TIMEOUT);
            if (content == null || content.equals("{}")) {
                checkhandler.sendEmptyMessage(NO_INVALID_PACKAGE);
                OtaTool.RedTips = 0;
                OtaTool.delectFile();
                EventBus.getDefault().post(new DownloadEvent(OtaConstant.DOWNLOAD_COMPLETE_ERROR,"没有新版本"));
                return;
            }
            JSONObject jo = new JSONObject(content);
            miwen = jo.getString("message");
            mingwen = otaUpgradeUtil.Decrypt(miwen, OtaConstant.PASSWORD);
            OtaLog.LOGOta("请求Ota包信息返回数据",""+ mingwen);
        } catch (IOException e) {
            e.printStackTrace();
            checkhandler.sendEmptyMessage(ERROR_INVALID_PACKAGE);
            return;
        } catch (JSONException e) {
            e.printStackTrace();
            checkhandler.sendEmptyMessage(ERROR_INVALID_PACKAGE);
            return;
        } catch (Exception e) {
            e.printStackTrace();
            checkhandler.sendEmptyMessage(ERROR_INVALID_PACKAGE);
            return;
        }
        Gson parser = new Gson();
        mInfo = parser.fromJson(mingwen, UpgradeInfo.class);
        //这个是获取网络文件大小，应该让后台改，实在不行在这里改一下
        //mInfo.size = String.valueOf( OtaTool.getFileLength(mInfo.url));
        File file = new File(OtaConstant.FILE_NAME_OTA);
        //有本地文件
        int state = DownLoadService.getDownLoadState();
        if(state == DownloadStatus.COMPLETE) //下载完成了
        {
            if (file.exists() && OtaTool.checkDownloadFileMd5(mInfo)){
                checkhandler.sendEmptyMessage(VIEW_DOWN_COMPLETE);
                OtaTool.RedTips = 1;
                EventBus.getDefault().post(new DownloadEvent(OtaConstant.DOWNLOAD_COMPLETE,"下载完成"));
                return;
            }else {

                file.delete();
                OtaTool.RedTips = 2;
                EventBus.getDefault().post(new DownloadEvent(OtaConstant.DOWNLOAD_COMPLETE_ERROR,"下载完成,但是失败了"));
            }
        }

        checkhandler.sendEmptyMessage(NEW_INVALID_PACKAGE);
    }

    public void updateUI(){
        OtaLog.LOGOta("升级界面","上一个状态"+last_view_munber);
        if((last_view_munber != -1) && (last_view_munber == VIEW_WIFI_NO_OPEN || last_view_munber == VIEW_STATE_NO_DATA || last_view_munber == VIEW_DOWN_COMPLETE)){
            showView(VIEW_STATE_LOADING);

            startInitLogicThread();
        }
    }


}
