package com.fotile.c2i.ota.util;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.StatFs;
import android.text.TextUtils;

import com.dl7.downloaderlib.FileDownloader;
import com.fotile.c2i.ota.bean.DiskStat;
import com.fotile.c2i.ota.bean.DownloadEvent;
import com.fotile.c2i.ota.bean.UpgradeInfo;
import com.fotile.c2i.ota.service.DownLoadService;
import com.fotile.c2i.ota.service.DownloadAction;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by yaohx on 2017/12/25.
 */

public class OtaTool {

    private static Timer timer_download;
    /**
     * 文鼎细黑
     */
    private static Typeface thinBoldFace;

    private static long last_time = 0;
    private static long ONEDAYMISS = 1000*60*60*24;//一天的毫秒数
    /**最多的分散时间*/
    private static int MAX_TIME_RANDOM = 7200*1000;

    private static String LAST_UPDATE_VERSION ="last_update_version";
    private static String VERSION = "version";
    private static String VERSION_NAME = "name";
    private static String ISDOWM = "isdown";
    private static String COMMENT = "comment";
    private static String MCU_MD5 = "mcu_md5";
    private static String OTA_MD5 = "ota_md5";
    private static String NOW_VERSION = "now_version";
    public static boolean ischeckmd5 = false;
    public static int RedTips = 0;
    /**
     * 限制按钮的快速点击情况
     *
     * @return true 执行click事件
     */
    public static boolean fastclick() {
        long time = System.currentTimeMillis();
        if (time - last_time > 800) {
            return true;
        }
        return false;
    }

    public static void setTypeFace(Typeface t) {
        if (null == thinBoldFace && t != null) {
            thinBoldFace = t;

        }
    }

    public static Typeface getTypeface() {
        return thinBoldFace;
    }

    /**
     * 根据md5校验文件是否下载完成或者已经下载过了--包含mcu文件，只有两个文件md5校验都通过了才返回true
     *
     * @return
     */
    public static boolean checkDownloadFileMd5(UpgradeInfo mInfo) {
        if (null == mInfo) {
            return false;
        }


        //判断ota文件是否完整下载过了
        boolean file_exist_ota = false;
        File file_ota = new File(OtaConstant.FILE_NAME_OTA);
        if (file_ota.exists()) {
            //本地文件md5
            String str_md5_ota = OtaUpgradeUtil.md5sum(file_ota.getPath());
            if (!TextUtils.isEmpty(str_md5_ota) && str_md5_ota.equals(mInfo.md5)) {
                file_exist_ota = true;
            }
        }
        //判断mcu文件是否完整下载过了
        boolean file_exist_mcu = false;
        File file_mcu = new File(OtaConstant.FILE_NAME_MCU);
        if (file_mcu.exists()) {
            //本地md5
            String str_md5_mcu = OtaUpgradeUtil.md5sum(file_mcu.getPath());
            if (!TextUtils.isEmpty(str_md5_mcu) && str_md5_mcu.equals(mInfo.ex_md5)) {
                file_exist_mcu = true;
            }
        }

        //如果包括mcu
        if (!TextUtils.isEmpty(mInfo.ex_md5)) {
            return file_exist_mcu && file_exist_ota;
        } else {
            return file_exist_ota;
        }
    }


    //获取amc地址
    public static String getLocalMacAddress() {
        String macSerial = null;
        String str = "";
        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
            if (!TextUtils.isEmpty(macSerial)) {
                macSerial = macSerial.replace(":", "");
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        return macSerial;
    }

    /**
     * 判断网络是否可用
     *
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context
                .CONNECTIVITY_SERVICE);
        if(mConnectivityManager == null){
            return false;
        }
        NetworkInfo netInfo = mConnectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isAvailable()) {
            return true;
        }
        return false;
    }

    /**
     * 获得当前activity的名字
     *
     * @param context
     * @return
     */
    public static String getCurrentActivityName(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = activityManager.getRunningTasks(1);
        ComponentName componentInfo = taskInfo.get(0).topActivity;
        String name = componentInfo.getClassName();
//        if (name.contains(".")) {
//            name = name.substring(name.lastIndexOf(".") + 1);
//        }
        return name;
    }

    /**
     * 反射机制获取android.os.SystemProperties
     *
     * @param key          属性的路径
     * @param defaultValue
     * @return
     */
    public static String getProperty(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(c, key, defaultValue));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return value;
        }
    }

    /**
     * 开启后台夜间下载
     *
     * @param check_package_name 设备校验包名
     * @param context
     */
    public static void startDownloadBackGround(final String check_package_name, final Context context) {
        /***************************处理随机事件逻辑**********************/
        //获取当天日期对应的时间戳
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        long current_time = System.currentTimeMillis();
        Date date = new Date(current_time);
        //获取yyyy-MM-dd HH:mm:ss 对应的时间戳-在这之间发起请求指令
        if(!OtaConstant.TEST_URL_FLAG){
            String time_str_start = df.format(date) + " 01:50:00";
            String time_str_end = df.format(date) + " 02:00:00";
            try {
                df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                //起点和结束点对应的时间戳
                long time_start = df.parse(time_str_start).getTime();
                long time_end = df.parse(time_str_end).getTime();
                long time_bettwen = time_end - time_start;
                OtaLog.LOGOta("后台下载时间校验起点", time_str_start + "--" + time_start);
                OtaLog.LOGOta("后台下载时间校验终点", time_str_end + "--" + time_end);
                OtaLog.LOGOta("后台下载时间当前时间点", current_time);
                OtaLog.LOGOta("计时器状态", "计时器状态"+(timer_download != null));
                //如果当前时间点在这两点之间

                    //如果不存在定时器
                    if (null == timer_download) {
                        OtaLog.LOGOta("开启后台下载线程", "开启后台下载线程");
                        //获取一个随机数，随机数之后开启线程
                        long dely_download_time = 0;
                        long random_count = (long) OtaTool.randomByMac(7200*1000);
                        if(random_count == 0){
                            random_count = (long)(Math.random() * 3600 * 1000 * 2);
                        }
                        if (current_time > time_end+random_count) { //超过凌晨2点+ 一个随机 说明下次启动是明天凌晨两点
                            long next_download_time = time_end+random_count+ONEDAYMISS;
                            dely_download_time = next_download_time - current_time;

                        }else {// 凌晨的时候
                            dely_download_time = time_end+random_count;
                        }
                        OtaLog.LOGOta("新建计时器", "延迟时间："+dely_download_time+"; 共："+dely_download_time/3600000+"小时"+dely_download_time%3600000/60000+"分钟"+dely_download_time%60000/1000+"秒");
                        timer_download = new Timer("auto_download",true);//启动定时器， 设置定时器线程名字，，，， 线程守护线程。
                        timer_download.scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {

                                if(OtaConstant.TEST_PRINT_FILE){
                                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    long downloadtime = System.currentTimeMillis();
                                    final String nowtime = df.format(downloadtime) ;
                                    startDownload(check_package_name, context,nowtime);
                                }else {
                                    startDownload(check_package_name, context);
                                }
                            }
                        }, dely_download_time,ONEDAYMISS);
                     }else {//有定时器在运行
                        //

                        OtaLog.LOGOta("计时器状态", "定时器正在运行中..........................");
                    }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }else { // 测试代码
            String time_str_start = df.format(date) + " 00:00:00";
            try {
                df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                //起点和结束点对应的时间戳
                long time_start = df.parse(time_str_start).getTime();
                long today_time = current_time - time_start;
                long hour_time = today_time%3600000;
                if( (hour_time % 600000 - 5*60*1000 ) <= 0){
                    OtaLog.LOGOta("===","当前时间在10分钟的前5分钟");
                    OtaLog.LOGOta("计时器状态", "计时器状态"+(timer_download != null));
                    if (null == timer_download) {
                        OtaLog.LOGOta("开启后台下载线程", "开启后台下载线程");

                        timer_download = new Timer();
                        timer_download.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                //十分钟后开始下载
                                if(OtaConstant.TEST_PRINT_FILE){
                                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    long downloadtime = System.currentTimeMillis();
                                    final String nowtime = df.format(downloadtime) ;
                                    startDownload(check_package_name, context,nowtime);
                                }else {
                                    startDownload(check_package_name, context);
                                }

                                timer_download = null;
                            }
                        }, 2*60*1000+5*60*1000);
                    }
                }else {
                    OtaLog.LOGOta("===","当前时间在10分钟的后5分钟  不进行判断");
                    OtaLog.LOGOta("计时器状态", "计时器状态"+(timer_download != null));
                }
            }catch  (ParseException e) {
                e.printStackTrace();
            }
        }

        /***************************处理随机事件逻辑**********************/

    }

    private static void startDownload(final String check_package_name, final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                /*******************************获取下载包信息*******************************/
                OtaUpgradeUtil otaUpgradeUtil = new OtaUpgradeUtil();
                //校验版本号
                String check_version_code = getProperty("ro.cvte.customer.version", "100");
                //校验mac地址
                String check_mac_address = getLocalMacAddress();
                UpgradeInfo mInfo = null;

                String reqUrl = otaUpgradeUtil.buildUrl(check_package_name, check_version_code, check_mac_address !=
                        null ? check_mac_address.replace(":", "") : "",context);
//                //后台下载不使用测试
                if (OtaConstant.TEST_URL_FLAG) {
                    reqUrl = OtaConstant.TEST_URL.replace("{version}",OtaTool.getProperty("ro.cvte.customer.version", "100"));
                }
                OtaLog.LOGOta("开始获取数据","链接："+reqUrl);
                try {
                    String content = otaUpgradeUtil.httpGet(reqUrl);
                    if (!TextUtils.isEmpty(content)) {
                        JSONObject jo = new JSONObject(content);
                        String mingwen = otaUpgradeUtil.Decrypt(jo.getString("message"), OtaConstant.PASSWORD);
                        OtaLog.LOGOta("请求Ota包信息返回数据", mingwen);
                        Gson parser = new Gson();
                        mInfo = parser.fromJson(mingwen, UpgradeInfo.class);
                    }
                } catch (Exception e) {
                    //异常不处理
                    e.printStackTrace();
                }
                /*******************************获取下载包信息*******************************/

                if (null != mInfo) {
                    //判断本地文件md5和网络接口获取的是否一样，不一样才执行下载
                    boolean md5_like = false;
                    //判断下载完成，文件已经保存了一份
                    File file = new File(OtaConstant.FILE_NAME_OTA);
                    if (file.exists()) {
                        OtaLog.LOGOta("本地文件已经存在","本地存在ota文件");
                        String filemd5 = ""+OtaUpgradeUtil.md5sum(file.getPath());
                        if (filemd5.equals(mInfo.md5)) {
                            md5_like = true;
                            return;
                        }
                    }

                    if (!md5_like) {
                        //不通知界面
                        DownloadAction.getInstance().removeAction();
                        OtaTool.setLastUpdateVersion(context,mInfo,OtaConstant.UPDATEFINISH);
                        OtaTool.delectFile();
                        //开启下载服务
                        Intent intent = new Intent(context, DownLoadService.class);
                        intent.putExtra("url", mInfo.url);
                        intent.putExtra("md5", mInfo.md5);
                        intent.putExtra("ex_url", mInfo.ex_url);
                        intent.putExtra("ex_md5", mInfo.ex_md5);
                        context.startService(intent);
                        OtaLog.LOGOta("后台下载md5校验", "本地文件md5不同，执行下载");
                    }
                }
            }
        }).start();
    }

    private static void startDownload(final String check_package_name, final Context context,final String nowtime) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                /*******************************获取下载包信息*******************************/
                OtaUpgradeUtil otaUpgradeUtil = new OtaUpgradeUtil();
                //校验版本号
                String check_version_code = getProperty("ro.cvte.customer.version", "100");
                //校验mac地址
                String check_mac_address = getLocalMacAddress();
                UpgradeInfo mInfo = null;

                String reqUrl = otaUpgradeUtil.buildUrl(check_package_name, check_version_code, check_mac_address !=
                        null ? check_mac_address.replace(":", "") : "",context);
//                //后台下载不使用测试
                if (OtaConstant.TEST_URL_FLAG) {
                    reqUrl = OtaConstant.TEST_URL.replace("{version}",OtaTool.getProperty("ro.cvte.customer.version", "100"));
                }
                OtaLog.LOGOta("开始获取数据","链接："+reqUrl);
                String content ="";
                try {
                    content = otaUpgradeUtil.httpGet(reqUrl);
                    if (!TextUtils.isEmpty(content)) {
                        JSONObject jo = new JSONObject(content);
                        String mingwen = otaUpgradeUtil.Decrypt(jo.getString("message"), OtaConstant.PASSWORD);
                        OtaLog.LOGOta("请求Ota包信息返回数据", mingwen);
                        Gson parser = new Gson();
                        mInfo = parser.fromJson(mingwen, UpgradeInfo.class);
                    }
                    String outputinfo = "";
                    outputinfo += "当前版本："+check_version_code+"    \n";
                    outputinfo += "MAC地址："+check_mac_address+"    \n";
                    outputinfo += "请求的URL："+reqUrl+"    \n";
                    outputinfo += "网络请求content："+content+"    \n";
                    String filepath = OtaConstant.FILE_FOLDER+nowtime+".txt";
                    File file = new File(filepath);
                    if(mInfo!=null){
                        outputinfo += "mInfo数据："+mInfo.toString();
                    }
                    File file222 = new File(OtaConstant.FILE_NAME_OTA);
                    if (file222.exists()) {
                        outputinfo += "本地数据文件存在：    \n";
                        OtaLog.LOGOta("本地文件已经存在","本地存在ota文件");
                        String filemd5 = ""+ OtaUpgradeUtil.md5sum(file.getPath());
                        if (mInfo!=null && filemd5.equals(mInfo.md5)) {
                            outputinfo += "本地数据文件匹配:(不会下载    \n";
                        }else {
                            outputinfo += "本地数据文件不匹配:（会下载    \n";
                        }
                    }else {
                        outputinfo += "本地数据文件不存在：（会下载    \n";
                    }
                    writeDownloadInfo(nowtime,outputinfo);
                } catch (Exception e) {
                    //异常不处理
                    e.printStackTrace();
                }
                /*******************************获取下载包信息*******************************/

                if (null != mInfo) {
                    //判断本地文件md5和网络接口获取的是否一样，不一样才执行下载
                    boolean md5_like = false;
                    //判断下载完成，文件已经保存了一份
                    File file = new File(OtaConstant.FILE_NAME_OTA);
                    if (file.exists()) {
                        OtaLog.LOGOta("本地文件已经存在"," 本地存在ota文件");
                        String filemd5 =""+ OtaUpgradeUtil.md5sum(file.getPath());
                        if (filemd5.equals(mInfo.md5)) {
                            md5_like = true;
                            return;
                        }
                    }

                    if (!md5_like) {
                        //不通知界面
                        DownloadAction.getInstance().removeAction();
                        OtaTool.setLastUpdateVersion(context,mInfo,OtaConstant.UPDATEFINISH);
                        OtaTool.delectFile();
                        //开启下载服务
                        Intent intent = new Intent(context, DownLoadService.class);
                        intent.putExtra("url", mInfo.url);
                        intent.putExtra("md5", mInfo.md5);
                        intent.putExtra("ex_url", mInfo.ex_url);
                        intent.putExtra("ex_md5", mInfo.ex_md5);
                        context.startService(intent);
                        OtaLog.LOGOta("后台下载md5校验", "本地文件md5不同，执行下载");
                    }
                }
            }
        }).start();
    }

    public static int randomByMac(int max_time){
        if(max_time<=0 || max_time>MAX_TIME_RANDOM){
            max_time=MAX_TIME_RANDOM;
        }
        String mac = OtaTool.getLocalMacAddress();
        if(TextUtils.isEmpty(mac)){
            return 0;
        }else {
            mac = mac.substring(6,12);
        }
        OtaLog.LOGOta("mac地址","mac = "+mac+"time"+Integer.parseInt(mac ,16)%max_time);
        return Integer.parseInt(mac ,16)%max_time;

    }
    /**  获取wifi状态*/
    @SuppressLint("WrongConstant")
    public static int getWifiState(final Context context){
        int wifiState = -1;
        WifiManager wifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if(wifiManager != null){

            wifiState = wifiManager.getWifiState();

        }
        return wifiState;
    }
    public static String getConnectWifiSsid(final Context context){
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(wifiManager == null){
            return "";
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        OtaLog.LOGOta("wifiInfo", wifiInfo.toString());
        OtaLog.LOGOta("SSID","设置wifi ssid:"+wifiInfo.getSSID());
        if(wifiInfo.getSSID()==null || wifiInfo.getSSID().equals("0x")){
            return "";
        }
        return wifiInfo.getSSID();
    }
    /**设置最新版本**/
    public static void setLastUpdateVersion(final Context context, UpgradeInfo upgradeInfo, String isdown){
        SharedPreferences sharedPreferences = context.getSharedPreferences(LAST_UPDATE_VERSION,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(VERSION_NAME,upgradeInfo.name);
        editor.putString(VERSION,upgradeInfo.version);
        editor.putString(ISDOWM,isdown);
        editor.putString(COMMENT,upgradeInfo.comment);
        if(upgradeInfo.md5 != null){
            editor.putString(OTA_MD5,upgradeInfo.md5);
        }else {
            editor.putString(OTA_MD5,"");
        }
        if(upgradeInfo.ex_md5 != null){
            editor.putString(MCU_MD5,upgradeInfo.ex_md5);
        }else {
            editor.putString(MCU_MD5,"");
        }


        editor.apply();
        editor.commit();
    }
    /**设置最新版本**/
    public static void setLastUpdateVersion(final Context context,String isdown){
        Context context1 = context.getApplicationContext();
        SharedPreferences sharedPreferences = context1.getSharedPreferences(LAST_UPDATE_VERSION,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ISDOWM,isdown);
        editor.apply();
        editor.commit();
    }
    /**设置最新版本**/
    public static void setNowVersion(final Context context,String now_version){
        if (context == null)return;
        Context context1 = context.getApplicationContext();
        SharedPreferences sharedPreferences = context1.getSharedPreferences(LAST_UPDATE_VERSION,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(NOW_VERSION,now_version);
        editor.apply();
        editor.commit();
    }
    /**获取最新版本**/
    public static String getLastUpdateVersion(final Context context){
        if (context == null)return "";
        Context context1 = context.getApplicationContext();
        SharedPreferences sharedPreferences = context1.getSharedPreferences(LAST_UPDATE_VERSION,MODE_PRIVATE);
        if(sharedPreferences == null){
            return "";
        }
        return sharedPreferences.getString(VERSION,"");
    }
    /**获取最新版本名称**/
    public static String getLastUpdateVersionName(final Context context){
        if (context == null)return "";
        Context context1 = context.getApplicationContext();
        SharedPreferences sharedPreferences = context1.getSharedPreferences(LAST_UPDATE_VERSION,MODE_PRIVATE);
        if(sharedPreferences == null){
            return "";
        }
        return sharedPreferences.getString(VERSION_NAME,"");
    }
    /**获取是否下载**/
    public static String getLastUpdateState(final Context context){
        if (context == null)return OtaConstant.UPDATEFINISH;
        Context context1 = context.getApplicationContext();
        SharedPreferences sharedPreferences = context1.getSharedPreferences(LAST_UPDATE_VERSION,MODE_PRIVATE);
        if(sharedPreferences == null){
            return OtaConstant.UPDATEFINISH;
        }
        return sharedPreferences.getString(ISDOWM,OtaConstant.UPDATEFINISH);
    }
    /**获取最新版本的MD5**/
    public static String getLastUpdateVersionMD5(final Context context){
        if (context == null)return "";
        Context context1 = context.getApplicationContext();
        SharedPreferences sharedPreferences = context1.getSharedPreferences(LAST_UPDATE_VERSION,MODE_PRIVATE);
        if(sharedPreferences == null){
            return "";
        }
        return sharedPreferences.getString(OTA_MD5,"");
    }
    /**获取最新版本的更新内容**/
    public static String getLastUpdateVersionComment(final Context context){
        if (context == null)return "";
        Context context1 = context.getApplicationContext();
        SharedPreferences sharedPreferences = context1.getSharedPreferences(LAST_UPDATE_VERSION,MODE_PRIVATE);
        if(sharedPreferences == null){
            return "";
        }
        return sharedPreferences.getString(COMMENT,"");
    }
    /**获取更新前的版本**/
    public static String getNowVersion(final Context context){
        if (context == null)return "";
        Context context1 = context.getApplicationContext();
        SharedPreferences sharedPreferences = context1.getSharedPreferences(LAST_UPDATE_VERSION,MODE_PRIVATE);
        if(sharedPreferences == null){
            return "";
        }
        return sharedPreferences.getString(NOW_VERSION,"");
    }
    /** 开始升级的标志*/
    public static void updateIng(final Context context){
        if (context == null)return ;
        OtaTool.setLastUpdateVersion(context,OtaConstant.UPDATEING);
    }


    public static void delectFile(){
        File file = new File(OtaConstant.FILE_NAME_OTA);
        if(file.exists()){
            file.delete();
        }
        File file1 = new File(OtaConstant.FILE_NAME_MCU);
        if(file1.exists()){
            file1.delete();
        }
    }
    /**
     * 校验文件是否下载完成或者已经下载过了
     *
     * @return
     */
    public static boolean checkFiles(final Context context) {
        File file = new File(OtaConstant.FILE_NAME_OTA);
        if (file.exists()) {
            return true;
        }else {
            OtaLog.LOGOta("文件不存在","系统升级文件不存在： ");
            return false;
        }
    }

    /**
     * 校验文件MCU文件是否下载过
     *
     * @return
     */
    public static boolean checkMCUFiles(final Context context) {
        File file = new File(OtaConstant.FILE_NAME_MCU);
        if (file.exists()) {
            return true;
        }else {
            OtaLog.LOGOta("文件不存在","系统升级文件不存在： ");
            return false;
        }
    }

    /**
     * 备份MCU文件
     */
    public static void backMcu(){
        File file = new File(OtaConstant.FILE_NAME_MCU_BACK);
        if (file.exists()) {
            file.delete();
        }
        File filein = new File(OtaConstant.FILE_NAME_MCU);
        if(!filein.exists()){
            return;
        }
        fileChannelCopy(filein,file);
    }

    /**
     *判断是否要显示小红点，逻辑，不需要进入设置界面。
     * @writer panyw
     * @param context
     * @return
     */
    public static boolean showTips (final Context context){
        if(getLastUpdateVersion(context).equals( OtaTool.getProperty("ro.cvte.customer.version", "100"))){
            File file = new File(OtaConstant.FILE_NAME_OTA);
            if(file.exists()){
                file.delete();
            }
        }
        boolean flag = (!getLastUpdateVersion(context).equals( OtaTool.getProperty("ro.cvte.customer.version", "100")) && checkFiles(context));
        OtaLog.LOGOta("小红点","是否显示小红点"+flag+"; 当前版本："+ OtaTool.getProperty("ro.cvte.customer.version", "100")+"；最新版本"+getLastUpdateVersion(context));
        return flag;
    }
    /**
     * 校验文件是否下载完成或者已经下载过了 新的方法，会校验md5
     *
     * @return
     */
    public static boolean checkFiles(final Context context,boolean flag) {
        File file = new File(OtaConstant.FILE_NAME_OTA);
        if (file.exists()) {
            String filemd5 = OtaUpgradeUtil.md5sum(file.getPath());
            String fileinfoMd5 =getLastUpdateVersionMD5(context);
            OtaLog.LOGOta("文件md5","系统升级文件md5： "+filemd5+"保存的md5"+fileinfoMd5);
            if (filemd5.equals(fileinfoMd5)) {
                return  true;

            }else {
                file.delete();
                return false;
            }
        }else {
            OtaLog.LOGOta("文件不存在","系统升级文件不存在： ");
            return false;
        }
    }
    /**
     *判断是否要显示小红点，逻辑，不需要进入设置界面。 新的方法，会校验md5 ，时间比较久。
     * @writer panyw
     * @param context 上下文
     * @return 是否显示小红点
     */
    public static boolean showTips (final Context context,boolean flagofThead){
        if(getLastUpdateVersion(context).equals( OtaTool.getProperty("ro.cvte.customer.version", "100"))){
            OtaLog.LOGOta("小红点","版本相同 ，升级成功，删除文件");
            File file = new File(OtaConstant.FILE_NAME_OTA);
            if(file.exists()){
                file.delete();
            }
        }
        boolean flag = (!getLastUpdateVersion(context).equals( OtaTool.getProperty("ro.cvte.customer.version", "100")) && checkFiles(context,true));
        OtaLog.LOGOta("小红点","是否显示小红点"+flag+"; 当前版本："+ OtaTool.getProperty("ro.cvte.customer.version", "100")+"；最新版本"+getLastUpdateVersion(context));
        return flag;
    }
    public static void checkDownloadTips(final Context context){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (ischeckmd5){
                    return;
                }
                ischeckmd5 = true;
                if(showTips(context,true)){
                    ischeckmd5 = false;
                    RedTips = 1;
                    EventBus.getDefault().post(new DownloadEvent(OtaConstant.DOWNLOAD_COMPLETE,"下载完成"));
                }else {
                    ischeckmd5 = false;
                    RedTips = 2;
                    EventBus.getDefault().post(new DownloadEvent(OtaConstant.NO_DOWNLOAD,"未下载，或下载损坏"));
                }
            }
        }).start();
    }
    public static void ResetRedTips(){
        RedTips = 0;
    }
    /**
     * 计算目标路径的磁盘使用情况
     *
     * @param path
     * @return
     */
    public static DiskStat getDiskCapacity(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        StatFs stat = new StatFs(path);
        long blockSize = stat.getBlockSize();
        long totalBlockCount = stat.getBlockCount();
        long feeBlockCount = stat.getAvailableBlocks();
        OtaLog.LOGOta("当前可见","当前可用空间 ："+blockSize * feeBlockCount/1024/1024/1024+"GB");
        return new DiskStat(blockSize * feeBlockCount, blockSize
                * totalBlockCount);
    }

    public static boolean canUpdate(long fileSize){
        DiskStat diskStat = getDiskCapacity(OtaConstant.FILE_FOLDER);
        //可用空间是否大于 文件大小的两倍 +50 MB
        return diskStat!= null && (diskStat.getFree() - fileSize*2- 50*1024*1024 >0 ) ;
    }

    /**
     * 获取网络文件大小
     * @param url1
     * @return
     *
     */
    public static int getFileLength(String url1) {
        int length=0;
        URL url;
        try {
            url = new  URL(url1);
            HttpURLConnection urlcon=(HttpURLConnection)url.openConnection();
            //根据响应获取文件大小
            length=urlcon.getContentLength();
            urlcon.disconnect();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        OtaLog.LOGOta("ota文件大小"," 网络文件大小 :----"+length);
        return length;
    }
    /**
     * 关闭下载服务 同时删除所有tmp文件
     * @return
     */
    public static void cancalDowloadServer(Context context){
        Intent intent = new Intent(context,DownLoadService.class);
        context.stopService(intent);
        //下载服务非处于初始化状态
        FileDownloader.stopAll();
        deleteTmpFiles();

    }
    public static void deleteTmpFiles(){
        File file = new File(OtaConstant.FILE_NAME_OTA+".tmp");
        if (file.exists()) {
            file.delete();
        }
        File file1 = new File(OtaConstant.FILE_NAME_MCU+".tmp");
        if (file1.exists()) {
            file1.delete();
        }
        delectFile();
    }
    public static boolean creatDownloadInfo(String time){
        boolean bool =false;
        String filepath = OtaConstant.FILE_FOLDER+time+".txt";
        File file = new File(filepath);
        try {
            if (!file.exists()){
                file.createNewFile();
                OtaLog.LOGOta("=== 创建文件成功","创建文件成功");
                bool =true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bool;
    }
    public static boolean writeDownloadInfo(String time,String info){
        boolean bool =false;
        String filepath = OtaConstant.FILE_FOLDER+time+".txt";
        String filedir = OtaConstant.FILE_FOLDER;
        File file1 = new File(filedir);
        File file = new File(filepath);
        try {
            if(!file1.exists()){
                file1.mkdirs();
            }
            if (!file.exists()){
                file.createNewFile();
                OtaLog.LOGOta("=== 创建文件成功","创建文件成功");
            }
            //文件输出流
            FileOutputStream fos = new FileOutputStream(file);
            //写数据
            fos.write((info).getBytes());

            //关闭文件流
            fos.close();
            bool = true;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bool;
    }

    /**获取meta-data数据*
     * @param metaKey
     */

    public static String getMetaValue(Context context, String metaKey) {
        Bundle metaData = null;
        String metaValue = null;
        if (context == null || metaKey == null) {
            return null;
        }
        try {
            OtaLog.LOGOta("当前包名：","当前包名开始获取");
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            if (null != ai) {
                metaData = ai.metaData;
            }else {
                OtaLog.LOGOta("当前包名：","metadata 为空");
            }
            if (null != metaData) {
                OtaLog.LOGOta("当前包名：","开始根据key获取值");
                metaValue = metaData.getString(metaKey);
            }
        } catch (PackageManager.NameNotFoundException e) {

        }

        if(metaValue != null){
            return metaValue;
        }else {
            return OtaConstant.TEST_PACKAGE_NAME;
        }

    }
    public static void fileChannelCopy(File s, File t) {

        FileInputStream fi = null;

        FileOutputStream fo = null;

        FileChannel in = null;

        FileChannel out = null;

        try {

            fi = new FileInputStream(s);

            fo = new FileOutputStream(t);

            in = fi.getChannel();//得到对应的文件通道

            out = fo.getChannel();//得到对应的文件通道

            in.transferTo(0, in.size(), out);//连接两个通道，并且从in通道读取，然后写入out通道

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            try {
                if(fi!= null)
                fi.close();
                if(in != null)
                in.close();
                if(fo!=null)
                fo.close();
                if(out!=null)
                out.close();

            } catch (IOException e) {

                e.printStackTrace();

            }

        }

    }

    /**
     * @param si whether using SI unit refer to International System of Units.
     */
    public static String humanReadableBytes(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format(Locale.ENGLISH, "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
