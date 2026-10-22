package com.nimbo.launcher;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
import java.util.HashSet;
import java.util.Set;

/** Optional Home-window routing. No window retrieval, keyboard filtering or text inspection. */
public class HomeAccessibilityService extends AccessibilityService {
    private final Set<String> homeWindows = new HashSet<>();
    private long lastLaunch;
    private boolean watching;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final Runnable updateHomes = this::refreshHomes;
    private final BroadcastReceiver packagesChanged = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            main.removeCallbacks(updateHomes);
            main.postDelayed(updateHomes, 200);
        }
    };

    @Override protected void onServiceConnected() {
        if (!watching) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_PACKAGE_ADDED);
            filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
            filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
            filter.addDataScheme("package");
            if (Build.VERSION.SDK_INT >= 33) registerReceiver(packagesChanged, filter, Context.RECEIVER_EXPORTED);
            else registerReceiver(packagesChanged, filter);
            watching = true;
        }
        refreshHomes();
    }

    private void refreshHomes() {
        homeWindows.clear();
        Set<String> packages = new HashSet<>();
        for (ResolveInfo home : getPackageManager().queryIntentActivities(LauncherSetupActivity.homeIntent(), 0)) {
            if (home.activityInfo == null) continue;
            String pkg = home.activityInfo.packageName;
            // Never redirect Android's setup/fallback screen or the configuration app.
            if (getPackageName().equals(pkg) || "com.android.tv.settings".equals(pkg) || "com.android.settings".equals(pkg)) continue;
            homeWindows.add(pkg + "/" + home.activityInfo.name);
            packages.add(pkg);
        }
        // Keep a non-empty filter even when every other launcher is disabled.
        if (packages.isEmpty()) packages.add(getPackageName());
        AccessibilityServiceInfo info = getServiceInfo();
        info.packageNames = packages.toArray(new String[0]);
        setServiceInfo(info);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                || event.getPackageName() == null || event.getClassName() == null) return;
        if (homeWindows.contains(event.getPackageName() + "/" + event.getClassName())) openHome();
    }

    private void openHome() {
        long now = SystemClock.uptimeMillis();
        if (now - lastLaunch < 750) return;
        lastLaunch = now;
        try {
            startActivity(new Intent(this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        } catch (RuntimeException ignored) { }
    }

    @Override public void onInterrupt() { }

    @Override public void onDestroy() {
        main.removeCallbacksAndMessages(null);
        if (watching) unregisterReceiver(packagesChanged);
        watching = false;
        homeWindows.clear();
        super.onDestroy();
    }
}
