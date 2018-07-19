package com.fotile.c2i.ota.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.fotile.c2i.ota.util.OtaConstant;
import com.fotile.c2i.ota.util.OtaLog;
import com.fotile.c2i.ota.util.OtaTool;
import com.fotile.c2i.ota.view.OtaTopSnackBar;

/** 系统升级回调
 * Created by pyw on 2018/04/25.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        String oldVersion=  OtaTool.getNowVersion(context);
        String nowVersion = OtaTool.getProperty("ro.cvte.customer.version", "unknow");
        String updateState = OtaTool.getLastUpdateState(context);
        if(oldVersion!=null && oldVersion.equals(nowVersion)&& !oldVersion.equals("unknow") && updateState.equals(OtaConstant.UPDATEING)){//升级版本失败
            OtaLog.LOGOta("=== 系统升级失败","系统升级失败----------------------------------"+oldVersion+"当前版本"+nowVersion+" 升级状态"+updateState);
            OtaTopSnackBar.make(context, "系统升级失败", OtaTopSnackBar.LENGTH_LONG).show();
        }else if(oldVersion!=null && oldVersion.equals(nowVersion)&& !oldVersion.equals("unknow")&& updateState.equals(OtaConstant.UPDATEFINISH )) {
            OtaLog.LOGOta("=== 正常重启","正常启动------------------------------------");

        }else if(oldVersion!=null && !oldVersion.equals(nowVersion)&& !oldVersion.equals("unknow")&& updateState.equals(OtaConstant.UPDATEING )){
            OtaTool.delectFile();
            OtaLog.LOGOta("=== 系统升级成功","系统升级成功----------------------------------");
        }
        OtaTool.setLastUpdateVersion(context,OtaConstant.UPDATEFINISH);
        OtaTool.checkDownloadTips(context);

    }
}
