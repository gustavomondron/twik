/*
 * Copyright 2014 Red Dye No. 2
 *
 * This file is part of Twik.
 *
 * Twik is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Twik is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Twik.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.reddyetwo.hashmypass.app.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

import com.reddyetwo.hashmypass.app.TwikApplication;

/**
 * Manager for master key cache expired alarms
 */
public class MasterKeyAlarmManager extends BroadcastReceiver {

    private static final int REQUEST_REMOVE_MASTER_KEY = 1;
    private static final int MILLIS_IN_A_MINUTE = 60000;

    /**
     * Set the alarm
     *
     * @param context the {@link android.content.Context} instance
     * @param minutes the minutes before the alarm is triggered
     */
    public static void setAlarm(Context context, int minutes) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent intent = PendingIntent.getBroadcast(context, REQUEST_REMOVE_MASTER_KEY,
                new Intent(context, MasterKeyAlarmManager.class), 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + minutes * MILLIS_IN_A_MINUTE, intent);
        } else {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + minutes * MILLIS_IN_A_MINUTE, intent);
        }
    }

    /**
     * Cancel the alarm
     *
     * @param context the {@link android.content.Context} instance
     */
    public static void cancelAlarm(Context context) {
        PendingIntent intent = PendingIntent.getBroadcast(context, REQUEST_REMOVE_MASTER_KEY,
                new Intent(context, MasterKeyAlarmManager.class), 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Remove cached master key
        TwikApplication.getInstance().wipeCachedMasterKey();
    }
}
