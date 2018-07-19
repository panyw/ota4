package com.fotile.c2i.ota.service;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.fotile.c2i.ota.receiver.DataChangeReceiver;
import com.fotile.c2i.ota.util.OtaLog;

/**
 * Created by panyw on 2018/1/23.
 */
public class TimeService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        OtaLog.LOGOta("时间进程", "后台进程绑定。。。");
        return null;
    }

    @Override
    public void onCreate() {

        super.onCreate();
        OtaLog.LOGOta("时间进程", "后台进程被创建。。。");

//服务启动广播接收器，使得广播接收器可以在程序退出后在后天继续执行，接收系统时间变更广播事件
        DataChangeReceiver receiver=new DataChangeReceiver();
        registerReceiver(receiver,new IntentFilter(Intent.ACTION_TIME_TICK));

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
//        int anHour = 2*60 * 1000; // 这是一分钟的毫秒数
//        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
//        Intent i = new Intent(this, AlarmReceiver.class);
//        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
//        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
//        //OtaLog.LOGOta("=====", "==========后台进程。。。");
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {

        OtaLog.LOGOta("时间进程", "后台进程被销毁了。。。");
        super.onDestroy();
    }

}
