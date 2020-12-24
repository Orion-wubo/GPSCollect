package com.fengmap.gpscollect;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.fengmap.gpscollect.gpsLocation.MainActivity;

import static android.content.Context.ALARM_SERVICE;

public class AlarmUtil {

    private static final long INTERVAL = 1000 * 60 * 10;
    private final Context context;

    public AlarmUtil(Context context){
        this.context = context;
    }

    public void registAlarm(){
        AlarmManager alarmService = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, MainActivity.ScreenControlAlarmReceiver.class).setAction("intent_alarm_log");
        PendingIntent broadcast = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);//通过广播接收
        alarmService.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + INTERVAL, broadcast);//INTERVAL毫秒后触发
    }

    public void removeAlarm(){
        AlarmManager alarmService = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, MainActivity.ScreenControlAlarmReceiver.class).setAction("intent_alarm_log");
        PendingIntent broadcast = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        alarmService.cancel(broadcast);
    }
}
