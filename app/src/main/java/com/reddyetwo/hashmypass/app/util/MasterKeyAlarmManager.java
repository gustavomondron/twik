package com.reddyetwo.hashmypass.app.util;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

import com.reddyetwo.hashmypass.app.HashMyPassApplication;

public class MasterKeyAlarmManager extends BroadcastReceiver {

    public static final int REQUEST_REMOVE_MASTER_KEY = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        /* Remove cached master key */
        HashMyPassApplication.setCachedMasterKey("");
        HashMyPassApplication.setCachedTag("");
        HashMyPassApplication.setCachedHashedPassword("");
    }

    @TargetApi(15)
    public static void setAlarm(Context context, int minutes) {
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent intent = PendingIntent
                .getBroadcast(context, REQUEST_REMOVE_MASTER_KEY,
                        new Intent(context, MasterKeyAlarmManager.class), 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + minutes * 60000, intent);
        } else {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + minutes * 60000, intent);
        }

    }

    public static void cancelAlarm(Context context) {
        PendingIntent intent = PendingIntent
                .getBroadcast(context, REQUEST_REMOVE_MASTER_KEY,
                        new Intent(context, MasterKeyAlarmManager.class), 0);
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(intent);
    }
}
