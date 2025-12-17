package com.screenassistant;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private Button startButton;
    private Button stopButton;
    private TextView statusText;
    private static final int REQUEST_CODE_OVERLAY = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        statusText = findViewById(R.id.statusText);

        startButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, REQUEST_CODE_OVERLAY);
                    return;
                }
            }
            startFloatingService();
        });

        stopButton.setOnClickListener(v -> stopFloatingService());
    }

    private void startFloatingService() {
        Intent intent = new Intent(this, FloatingWindowService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        statusText.setText("服务运行中");
        Toast.makeText(this, "服务已启动", Toast.LENGTH_SHORT).show();
    }

    private void stopFloatingService() {
        Intent intent = new Intent(this, FloatingWindowService.class);
        stopService(intent);
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        statusText.setText("屏幕答题助手");
        Toast.makeText(this, "服务已停止", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OVERLAY) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    startFloatingService();
                } else {
                    Toast.makeText(this, "需要悬浮窗权限才能使用", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
