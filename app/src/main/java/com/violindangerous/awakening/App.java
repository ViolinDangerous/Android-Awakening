package com.violindangerous.awakening;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

/**
 * @author yyl
 * @date 2024/6/1 13:44
 */
public class App extends Application {
    public static App app;
    static String TAG = "app";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        app = this;
    }

    public static boolean isRunning() {
        if (app == null) return false;
        return ServiceUtils.isServiceRunning(app, AwakeService.class);
    }

    public static void startService() {
        Log.e(TAG, "startService");
        Intent intent = new Intent(app, AwakeService.class);
        app.startForegroundService(intent);
    }

    public static void stopService() {
        Log.e(TAG, "stopService");
        app.stopService(new Intent(app, AwakeService.class));
    }

    public static void toggleService() {
        if (App.isRunning()) {
            stopService();
        } else {
            startService();
        }
    }
}
