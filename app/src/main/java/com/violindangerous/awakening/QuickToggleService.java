package com.violindangerous.awakening;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;
import android.widget.Toast;

/**
 * @author yyl
 * @date 2024/6/1 13:36
 */
public class QuickToggleService extends TileService {
    private BroadcastReceiver serviceStatusReceiver;

    @Override
    public void onClick() {
        Tile tile = getQsTile();
        if (tile == null) return;

        if (tile.getState() == Tile.STATE_ACTIVE) {
            App.stopService();
        } else if (tile.getState() == Tile.STATE_INACTIVE) {
            App.startService();
        }
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        registerServiceStatusReceiver();
        updateTile();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
        unregisterReceiver(serviceStatusReceiver);
    }

    public void updateTile() {
        Tile tile = getQsTile();
        if (tile == null)
            return;

        Log.d("QuickToggleService", "updateTile: " + App.isRunning());
        tile.setState(ServiceUtils.isServiceRunning(this,AwakeService.class) ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.updateTile();
    }

    private void registerServiceStatusReceiver() {
        serviceStatusReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d("QuickToggleService", "onReceive: " + action);
                if (action != null) {
                    switch (action) {
                        case AwakeService.ACTION_SERVICE_CREATED:
                        case AwakeService.ACTION_SERVICE_DESTROYED:
                            updateTile();
                            break;
                    }
                }
            }
        };

        // 创建意图过滤器
        IntentFilter filter = new IntentFilter();
        filter.addAction(AwakeService.ACTION_SERVICE_CREATED);
        filter.addAction(AwakeService.ACTION_SERVICE_DESTROYED);

        // 注册广播接收器
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(serviceStatusReceiver, filter, RECEIVER_EXPORTED);
        } else {
            registerReceiver(serviceStatusReceiver, filter);
        }
    }
}
