/*
 * Copyright 2014 Red Dye No. 2
 *
 * This file is part of Hash My pass.
 *
 * Hash my pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Hash my pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Hash my pass.  If not, see <http://www.gnu.org/licenses/>.
 */

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
