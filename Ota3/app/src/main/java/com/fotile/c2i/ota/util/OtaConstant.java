package com.fotile.c2i.ota.util;

import android.os.Environment;

import java.io.File;

/**
 * Created by yaohx on 2017/12/14.
 */

public class OtaConstant {
    /**
     * 打包改为false
     */
    public static boolean TEST = false;
    public static boolean TEST_URL_FLAG = false;
    public static boolean TEST_PRINT_FILE = true;
//    public static String TEST_URL = OtaUpgradeUtil.ServerURL + "package=com.fotile.c2i" +
//            ".sterilizer&version=C2SL-SA111&mac=00259219e046";
    //老司机要求不影响其他设备
    public static String TEST_URL = "http://develop.fotile.com:8080/fotileAdminSystem/upgrade.do?package=com.OTAceshi&version={version}&mac=";
    /**
     * OTA升级包文件名称
     */
    public final static String OTANAME = "update.zip";
    /**
     * MCU升级包文件名称
     */
    public final static String OTANAME_MCU = "mcu.bin";
    public final static String OTANAME_MCU_BACK = "mcu.back";
    public final static String FILE_FOLDER_TEST = Environment.getExternalStorageDirectory().getPath() + "/ota/";
    /**
     * 固件包的下载目录
     */
    public final static String FILE_FOLDER = TEST ? FILE_FOLDER_TEST : Environment.getDataDirectory() + File
            .separator + "media" + File.separator;

    /**
     * 固件包的完整名称
     */
    public final static String FILE_NAME_OTA = FILE_FOLDER + OTANAME;
    /**
     * mcu包的完整名称
     */
    public final static String FILE_NAME_MCU = FILE_FOLDER + OTANAME_MCU;
    /**
     * mcu备份包的完整名称
     */
    public final static String FILE_NAME_MCU_BACK = FILE_FOLDER + OTANAME_MCU_BACK;
    /**
     * OTA解密密码
     */
    public static final String PASSWORD =
            "9588028820109132570743325311898426347857298773549468758875018579537757772163084478873699447306034466200616411960574122434059469100235892702736860872901247123456";

    public static final String MD5_CHECK_ERROR = "md5_check_error";
    public static final String MCU_MD5_CHECK_ERROR = "mcu_md5_check_error";
    public static final String DOWNLOAD_COMPLETE = "complete";
    public static final String DOWNLOAD_COMPLETE_ERROR = "complete_error";
    public static final String NO_DOWNLOAD = "no_download";
    public static final String TEST_PACKAGE_NAME = "com.OTAceshi";
    public static final String SYS_OF_PACKAGE ="com.fotile.c2i.ota.package";
    public static final String UPDATEING = "YES";
    public static final String UPDATEFINISH = "NO";
    //    @Subscribe(threadMode = ThreadMode.MAIN)
    //    public void DownloadEvent(DownloadEvent downloadEvent) {
    //        if(downloadEvent!=null && downloadEvent.getDownload_state().equals(OtaConstant.DOWNLOAD_COMPLETE)){
    //            LogUtil.LOGOta("========","========= 下载事件完成，去判断");
    //            //这边是现在完成并且ota，校验通过
    //        }else
    //        if(downloadEvent!=null && downloadEvent.getDownload_state().equals(OtaConstant.DOWNLOAD_COMPLETE_ERROR)){
    //            LogUtil.LOGOta("========","========= 下载事件完成，去判断,但是失败了");
    //            //这边是现在完成并且ota，校验不通过，，，这里会直接删除
    //        }
    //    }
}
