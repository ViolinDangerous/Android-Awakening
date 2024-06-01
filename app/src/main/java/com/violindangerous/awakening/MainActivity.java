package com.violindangerous.awakening;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.slider.Slider;

public class MainActivity extends AppCompatActivity {
    private BroadcastReceiver serviceStatusReceiver;
    private Button button;
    private static final int REQUEST_CODE_SYSTEM_ALERT_WINDOW = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        button = findViewById(R.id.button);
        updateButtonText("onCreateActivity isRunning:");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 检查并请求 SYSTEM_ALERT_WINDOW 权限
                checkAndRequestSystemAlertWindowPermission();
            }
        });
    }

    private void updateButtonText(String x) {
        boolean running = App.isRunning();
        System.out.println(x + running);
        button.setText(running ? "Stop" : "Start");
    }

    private void checkAndRequestSystemAlertWindowPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_SYSTEM_ALERT_WINDOW);
        } else {
            // 权限已经授予，继续执行
            App.toggleService();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SYSTEM_ALERT_WINDOW) {
            if (Settings.canDrawOverlays(this)) {
                // 权限已授予，继续执行
                App.toggleService();
            } else {
                // 权限未授予，提示用户
                Toast.makeText(this, "请授予显示在其他应用上层的权限", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerServiceStatusReceiver();
        updateButtonText("onStart:");
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(serviceStatusReceiver);
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
                            updateButtonText("");
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