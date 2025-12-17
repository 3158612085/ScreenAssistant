package com.screenassistant;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class FloatingWindowService extends Service {
    private WindowManager windowManager;
    private View floatingView;
    private View resultView;
    private WindowManager.LayoutParams floatingParams;
    private WindowManager.LayoutParams resultParams;
    private ScreenCaptureHelper captureHelper;
    private OCRHelper ocrHelper;
    private AIHelper aiHelper;
    private boolean isResultShowing = false;

    private static final String CHANNEL_ID = "ScreenAssistant";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        captureHelper = new ScreenCaptureHelper(this);
        ocrHelper = new OCRHelper(this);
        aiHelper = new AIHelper();
        
        createFloatingButton();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "屏幕答题助手",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("屏幕答题助手")
                .setContentText("服务运行中")
                .setSmallIcon(android.R.drawable.ic_menu_camera)
                .build();
        
        startForeground(NOTIFICATION_ID, notification);
        return START_STICKY;
    }

    private void createFloatingButton() {
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_button, null);
        
        int layoutType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutType = WindowManager.LayoutParams.TYPE_PHONE;
        }
        
        floatingParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        
        floatingParams.gravity = Gravity.TOP | Gravity.START;
        floatingParams.x = 100;
        floatingParams.y = 100;
        
        windowManager.addView(floatingView, floatingParams);
        
        ImageView floatingButton = floatingView.findViewById(R.id.floatingButton);
        floatingButton.setOnClickListener(v -> captureAndAnalyze());
        
        setupDraggable(floatingView, floatingParams);
    }

    private void setupDraggable(View view, WindowManager.LayoutParams params) {
        view.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                        
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(view, params);
                        return true;
                        
                    case MotionEvent.ACTION_UP:
                        if (Math.abs(event.getRawX() - initialTouchX) < 10 &&
                            Math.abs(event.getRawY() - initialTouchY) < 10) {
                            v.performClick();
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void captureAndAnalyze() {
        Toast.makeText(this, "正在截取屏幕...", Toast.LENGTH_SHORT).show();
        
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Bitmap screenshot = captureHelper.captureScreen();
            if (screenshot != null) {
                analyzeScreenshot(screenshot);
            } else {
                Toast.makeText(this, "截图失败", Toast.LENGTH_SHORT).show();
            }
        }, 500);
    }

    private void analyzeScreenshot(Bitmap bitmap) {
        Toast.makeText(this, "正在识别文字...", Toast.LENGTH_SHORT).show();
        
        ocrHelper.recognizeText(bitmap, new OCRHelper.OCRCallback() {
            @Override
            public void onSuccess(String text) {
                if (text != null && !text.isEmpty()) {
                    getAnswer(text);
                } else {
                    showToast("未识别到文字");
                }
            }

            @Override
            public void onFailure(String error) {
                showToast("识别失败: " + error);
            }
        });
    }

    private void getAnswer(String question) {
        Toast.makeText(this, "正在获取答案...", Toast.LENGTH_SHORT).show();
        
        aiHelper.getAnswer(question, new AIHelper.AICallback() {
            @Override
            public void onSuccess(String answer) {
                showResult(question, answer);
            }

            @Override
            public void onFailure(String error) {
                showResult(question, "获取答案失败: " + error);
            }
        });
    }

    private void showResult(String question, String answer) {
        if (isResultShowing && resultView != null) {
            windowManager.removeView(resultView);
        }
        
        resultView = LayoutInflater.from(this).inflate(R.layout.result_window, null);
        
        int layoutType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutType = WindowManager.LayoutParams.TYPE_PHONE;
        }
        
        resultParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        );
        
        resultParams.gravity = Gravity.CENTER;
        
        android.widget.TextView questionContent = resultView.findViewById(R.id.questionContent);
        android.widget.TextView answerContent = resultView.findViewById(R.id.answerContent);
        android.widget.Button closeButton = resultView.findViewById(R.id.closeButton);
        
        questionContent.setText(question);
        answerContent.setText(answer);
        
        closeButton.setOnClickListener(v -> {
            if (resultView != null) {
                windowManager.removeView(resultView);
                resultView = null;
                isResultShowing = false;
            }
        });
        
        windowManager.addView(resultView, resultParams);
        isResultShowing = true;
    }

    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) {
            windowManager.removeView(floatingView);
        }
        if (resultView != null) {
            windowManager.removeView(resultView);
        }
        if (captureHelper != null) {
            captureHelper.release();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
