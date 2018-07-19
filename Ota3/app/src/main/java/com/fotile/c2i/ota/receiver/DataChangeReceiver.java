package com.fotile.c2i.ota.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.fotile.c2i.ota.util.OtaConstant;
import com.fotile.c2i.ota.util.OtaLog;
import com.fotile.c2i.ota.util.OtaTool;

/**
 * Created by panyw on 2018/1/23.
 */

public class DataChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                OtaLog.LOGOta("时间改变监听","开始执行代码 巴拉巴拉小魔仙");
                OtaTool.startDownloadBackGround(OtaTool.getMetaValue(context, OtaConstant.SYS_OF_PACKAGE),context);
            }
        }).start();
    }


}
