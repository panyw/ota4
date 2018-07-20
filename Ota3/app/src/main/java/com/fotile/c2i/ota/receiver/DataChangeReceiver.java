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
                OtaLog.LOGOta("时间改变监听","开始判断计时器状态 == 如果没有计时器就创建 有就不处理");
                OtaTool.startDownloadBackGround(OtaTool.getMetaValue(context, OtaConstant.SYS_OF_PACKAGE),context);
            }
        }).start();
    }


}
