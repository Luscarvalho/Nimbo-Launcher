package com.nimbo.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

/** Observes network state without sending network traffic; the launcher supplies the settings shortcut. */
final class NetworkStatusIndicator extends View {
    private final ConnectivityManager connectivity;
    private final WifiManager wifi;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Network active;
    private NetworkCapabilities capabilities;
    private boolean wifiEnabled, watching;

    private final ConnectivityManager.NetworkCallback callback = new ConnectivityManager.NetworkCallback() {
        @Override public void onCapabilitiesChanged(Network network, NetworkCapabilities caps) {
            active = network;
            capabilities = caps;
            updateState();
        }
        @Override public void onLost(Network network) {
            if (network.equals(active)) {
                active = null;
                capabilities = null;
                updateState();
            }
        }
    };

    private final BroadcastReceiver wifiChanges = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            wifiEnabled = wifi != null && wifi.isWifiEnabled();
            updateState();
        }
    };

    NetworkStatusIndicator(Context context) {
        super(context);
        connectivity = context.getSystemService(ConnectivityManager.class);
        wifi = context.getApplicationContext().getSystemService(WifiManager.class);
        setFocusable(false);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    void start() {
        if (watching) return;
        wifiEnabled = wifi != null && wifi.isWifiEnabled();
        if (connectivity != null) {
            active = connectivity.getActiveNetwork();
            capabilities = active == null ? null : connectivity.getNetworkCapabilities(active);
            connectivity.registerDefaultNetworkCallback(callback, new Handler(Looper.getMainLooper()));
        }
        IntentFilter filter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        if (Build.VERSION.SDK_INT >= 33) getContext().registerReceiver(wifiChanges, filter, Context.RECEIVER_NOT_EXPORTED);
        else getContext().registerReceiver(wifiChanges, filter);
        watching = true;
        updateState();
    }

    void stop() {
        if (!watching) return;
        watching = false;
        if (connectivity != null) connectivity.unregisterNetworkCallback(callback);
        getContext().unregisterReceiver(wifiChanges);
    }

    private boolean connected() { return capabilities != null; }
    private boolean wired() {
        return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
    }
    private boolean validated() {
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    private void updateState() {
        String label;
        if (connected()) {
            String type = getContext().getString(wired() ? R.string.network_type_wired
                    : capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    ? R.string.network_type_wifi : R.string.network_type_other);
            label = getContext().getString(validated() ? R.string.network_connected : R.string.network_unvalidated, type);
        } else label = getContext().getString(wifiEnabled ? R.string.network_wifi_disconnected : R.string.network_wifi_off);
        setContentDescription(getContext().getString(R.string.network_settings_description, label));
        setTooltipText(label);
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float size = Math.min(getWidth(), getHeight()) * (38f / 72f);
        canvas.save();
        canvas.translate((getWidth() - size) / 2, (getHeight() - size) / 2);
        canvas.scale(size / 24f, size / 24f);
        paint.setColor(hasFocus() ? 0xFF202124 : !connected() ? 0xFF9AA0A6 : validated() ? 0xFFEBEBEB : 0xFFFFCA80);
        paint.setStrokeWidth(1.7f);
        paint.setStyle(Paint.Style.STROKE);
        if (wired()) {
            canvas.drawRoundRect(3, 3, 21, 16, 1.5f, 1.5f, paint);
            canvas.drawLine(12, 16, 12, 21, paint);
            canvas.drawLine(7, 21, 17, 21, paint);
            canvas.drawLine(7, 7, 7, 11, paint);
            canvas.drawLine(12, 7, 12, 11, paint);
            canvas.drawLine(17, 7, 17, 11, paint);
        } else {
            // Three rounded Wi-Fi arcs and a dot; no bitmap allocation on updates.
            canvas.drawArc(-1, 3, 25, 27, 222, 96, false, paint);
            canvas.drawArc(3, 8, 21, 26, 222, 96, false, paint);
            canvas.drawArc(7, 13, 17, 23, 222, 96, false, paint);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(12, 20, 1.1f, paint);
        }
        if (!connected()) {
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawLine(3, 3, 21, 21, paint);
        } else if (!validated()) {
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawLine(22, 15, 22, 18, paint);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(22, 21, 0.9f, paint);
        }
        canvas.restore();
    }
}
