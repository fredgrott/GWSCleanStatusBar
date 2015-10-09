/*
 * Copyright 2014 Emma Guy
 * Modifications Copyright(C) 2015 Fred Grott(GrottWorkShop)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.shareme.gwscleanstatusbar;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.view.Gravity;
import android.view.WindowManager;

import com.github.shareme.gwscleanstatusbar.prefs.TimePreference;
import com.github.shareme.gwscleanstatusbar.util.StatusBarConfig;
import com.github.shareme.gwscleanstatusbar.widgets.StatusBarView;

public class CleanStatusBarService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private static boolean sIsRunning = false;

    private WindowManager mWindowManager;
    private static StatusBarView mStatusBarView;
    private StatusBarConfig mStatusBarConfig;
    private NotificationManager mNotificationManager;

    public CleanStatusBarService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        sIsRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mStatusBarConfig = new StatusBarConfig(MainActivity.getAPIValue(this, getSharedPrefs()), isKitKatGradientEnabled(), getResources(), getAssets());

        if (mStatusBarView == null) {
            mStatusBarView = new StatusBarView(this);
            mWindowManager.addView(mStatusBarView, getWindowManagerParams());
        }
        mStatusBarView.setStatusBarConfig(mStatusBarConfig, getBackgroundColour(), getClockTime(), showWifiIcon(), show3gIcon(), showGpsIcon());

        showNotification();

        return super.onStartCommand(intent, flags, startId);
    }

    private WindowManager.LayoutParams getWindowManagerParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT); // must be translucent to support KitKat gradient
        params.gravity = Gravity.TOP;
        params.height = mStatusBarConfig.getStatusBarHeight();
        return params;
    }

    @Override
    public void onDestroy() {
        sIsRunning = false;

        if (mStatusBarView != null) {
            mWindowManager.removeView(mStatusBarView);
            mStatusBarView = null;
        }
        removeNotification();

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public String getClockTime() {
        return getSharedPrefs().getString(getString(R.string.key_clock_time), TimePreference.DEFAULT_TIME_VALUE);
    }

    public int getBackgroundColour() {
        return getSharedPrefs().getInt(getString(R.string.key_background_colour), 0);
    }

    private boolean isKitKatGradientEnabled() {
        return getSharedPrefs().getBoolean(getString(R.string.key_kit_kat_gradient), false);
    }

    private boolean showGpsIcon() {
        return getSharedPrefs().getBoolean(getString(R.string.key_gps), false);
    }

    private boolean showWifiIcon() {
        return getSharedPrefs().getBoolean(getString(R.string.key_signal_wifi), false);
    }

    private int show3gIcon() {
        return Integer.parseInt(getSharedPrefs().getString(getString(R.string.key_signal_3g), "-1"));
    }

    private SharedPreferences getSharedPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    private void showNotification() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setOngoing(true)
                .setWhen(0)
                .setContentTitle(getString(R.string.clean_status_bar_is_running))
                .setContentText(getString(R.string.touch_to_configure))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0,
                        intent, PendingIntent.FLAG_CANCEL_CURRENT));
        startForeground(NOTIFICATION_ID, builder.build());
    }

    private void removeNotification() {
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    public static boolean isRunning() {
    	return sIsRunning;
    }
}
