package com.example.meizu_tool;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.os.BatteryManager;
import android.os.IBinder;
import android.view.Gravity;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;

public class CircleService extends Service {
    private WindowManager windowManager;
    private BatteryView circleView;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // 创建自定义视图实例
        circleView = new BatteryView(this);

        // 设置窗口参数
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                100,
                100,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.CENTER;
        params.y = 10;

        windowManager.addView(circleView, params);
        registerBatteryReceiver();
    }

    private void registerBatteryReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                float batteryPct = (level / (float) scale) * 100;
                int sweepAngle = (int) (360 * (batteryPct / 100f));
                int color = 0;
                if (batteryPct > 50) {
                    color = Color.GREEN;
                } else if (batteryPct < 50 && batteryPct > 20) {
                    color = Color.YELLOW;
                } else if (batteryPct < 20) {
                    color = Color.RED;
                }
                circleView.setSweepAngle(sweepAngle, color);
            }
        }, filter);
    }
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (circleView == null) return;

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            circleView.setVisibility(View.VISIBLE);
        } else {
            circleView.setVisibility(View.GONE);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (circleView != null && windowManager != null) {
            windowManager.removeView(circleView);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    // 自定义视图内部类
    private static class BatteryView extends View {
        private int sweepAngle;
        private int color;

        public BatteryView(Context context) {
            super(context);
        }

        public void setSweepAngle(int sweepAngle,int color) {
            this.sweepAngle = sweepAngle;
            this.color = color;
            invalidate(); // 触发重绘
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);
            @SuppressLint("DrawAllocation") Paint paint = new Paint();
            paint.setColor(color);
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);
            @SuppressLint("DrawAllocation") RectF rect = new RectF(0, 0, getWidth(), getHeight());
            canvas.drawArc(rect, -90, sweepAngle, true, paint);
        }
    }
}
