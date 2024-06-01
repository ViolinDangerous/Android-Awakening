package com.violindangerous.awakening;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.core.app.NotificationCompat;

/**
 * @author yyl
 * @date 2024/6/1 13:38
 */
public class AwakeService  extends Service {
    private static final String TAG = "myapp:KeepScreenOnService";
    private static final String CHANNEL_ID = "KeepScreenOnChannel";
    public static final String ACTION_SERVICE_CREATED = "com.example.KeepScreenOnService.SERVICE_CREATED";
    public static final String ACTION_SERVICE_DESTROYED = "com.example.KeepScreenOnService.SERVICE_DESTROYED";
    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate");
        createNotificationChannel();

        startForegroundServiceWithNotification();

        setAwake();

        sendBroadcast(new Intent(ACTION_SERVICE_CREATED));
    }
    private View mView;

    private void setAwake() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
        }

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.empty_view, null);
        mView.setKeepScreenOn(true);
        mView.setClickable(false);
        mView.setFocusable(false);
        mView.setLongClickable(false);

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.width  =1;
        params.height=1;
        params.gravity = Gravity.TOP | Gravity.START;

        wm.addView(mView, params);
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Keep Screen On Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void startForegroundServiceWithNotification() {

        Notification notification = getNotification("Keep Screen On Service",
                "This service keeps the screen on.") ;

        startForeground(1, notification);
    }

    public Notification getNotification(String newTitle, String newText) {
        int pendingIntentFlag = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 31) {
            pendingIntentFlag |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0,
                        new Intent(this, MainActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                                        | Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        , pendingIntentFlag);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .setContentTitle(newTitle)
                .setContentText(newText)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        if (mView != null) {
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            wm.removeView(mView);
        }
        mView = null;
        sendBroadcast(new Intent(ACTION_SERVICE_DESTROYED));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
