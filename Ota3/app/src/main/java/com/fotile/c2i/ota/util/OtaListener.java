package com.fotile.c2i.ota.util;

/**
 * Created by yaohx on 2017/12/25.
 */

public interface OtaListener {


    abstract void onDownloadCompleted(String newVersion);

    abstract void onInstallNow(boolean containMcu);

    abstract void onInstallLater(boolean containMcu);
    abstract void gotoWifiActivity();
}
