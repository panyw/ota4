package com.fotile.c2i.ota.util;

import android.util.Log;

import com.dl7.downloaderlib.model.DownloadStatus;

/**
 * 文件名称：OtaLog
 * 创建时间：2017/8/7 15:14
 * 文件作者：yaohx
 * 功能描述：项目全局打印日志
 */
public class OtaLog {
    private static boolean isDebug = HttpUtil.isDeBag();

    public static void LOGOta(String tag, Object obj) {
        if (isDebug) {
            if (null != obj) {
                Log.e("Ota升级==" + tag, obj.toString());
            } else {
                Log.e("Ota升级==" + tag, "null");
            }
        }
    }

    public static void LogState(int state) {
        String tag = state + "";
        switch (state) {
            case DownloadStatus.NORMAL:
                tag = "初始状态";
                break;
            case DownloadStatus.ERROR:
                tag = "下载错误";
                break;
            case DownloadStatus.DOWNLOADING:
                tag = "下载中";
                break;
            case DownloadStatus.COMPLETE:
                tag = "下载完成";
                break;
        }
        OtaLog.LOGOta("当前下载状态", tag);
    }


}
