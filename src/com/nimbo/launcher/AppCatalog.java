package com.nimbo.launcher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.LruCache;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Discovers and rasterizes applications off the UI thread; keeps only bounded, prepared images. */
final class AppCatalog implements AutoCloseable {
    interface Callback { void loaded(List<Entry> apps); void failed(); }
    static final class Entry {
        final String pkg, label;
        final int flags;
        final Intent launch;
        final Bitmap banner, icon;
        Entry(String pkg, String label, int flags, Intent launch, Bitmap banner, Bitmap icon) {
            this.pkg = pkg; this.label = label; this.flags = flags;
            this.launch = launch; this.banner = banner; this.icon = icon;
        }
    }

    private static final Set<String> TOOLS = new HashSet<>(Arrays.asList("com.android.tv.settings",
            "com.android.settings", "com.android.tv", "com.google.android.tvlauncher",
            "com.google.android.apps.tv.launcherx", "com.google.android.leanbacklauncher"));
    private final Context context;
    private final int width, height, iconSize;
    private final LatestTaskRunner worker = new LatestTaskRunner();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ConcurrentHashMap<String, Integer> revisions = new ConcurrentHashMap<>();
    private final LruCache<String, Entry> cache = new LruCache<String, Entry>(2 * 1024 * 1024) {
        @Override protected int sizeOf(String key, Entry value) {
            return 1024 + (value.banner == null ? 0 : value.banner.getAllocationByteCount())
                    + (value.icon == null ? 0 : value.icon.getAllocationByteCount());
        }
    };
    private volatile boolean closed;
    private int generation;

    AppCatalog(Context context, int width, int height, int iconSize) {
        this.context = context.getApplicationContext();
        this.width = width; this.height = height; this.iconSize = iconSize;
    }

    void invalidate(String pkg) {
        if (pkg != null) revisions.merge(pkg, 1, Integer::sum);
    }

    void refresh(String legacyPreferred, Callback callback) {
        final int expected = ++generation;
        worker.submit(() -> {
            try {
                PackageManager pm = context.getPackageManager();
                LinkedHashSet<String> preferred = new LinkedHashSet<>();
                // Old private preferences remain valid; incoming activity extras are never read.
                if (legacyPreferred != null) for (String p : legacyPreferred.split("[,\\s]+")) {
                    if (p.matches("[A-Za-z0-9_]+(?:\\.[A-Za-z0-9_]+)+") && preferred.size() < 200) preferred.add(p);
                }
                preferred.addAll(Arrays.asList(Apps.LIST));
                preferred.add("com.android.vending");
                Set<String> homes = new HashSet<>();
                for (ResolveInfo info : pm.queryIntentActivities(LauncherSetupActivity.homeIntent(), 0))
                    if (info.activityInfo != null) homes.add(info.activityInfo.packageName);
                LinkedHashSet<String> candidates = new LinkedHashSet<>(preferred);
                List<String> discovered = new ArrayList<>();
                Intent tv = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
                for (ResolveInfo info : pm.queryIntentActivities(tv, 0)) {
                    if (info.activityInfo == null) continue;
                    String pkg = info.activityInfo.packageName;
                    // A factory TV app is still an app; only known tools and other HOME apps are skipped.
                    if (!homes.contains(pkg) && !discovered.contains(pkg)) discovered.add(pkg);
                }
                candidates.addAll(discovered);
                List<Entry> result = new ArrayList<>();
                for (String pkg : candidates) {
                    if (Thread.currentThread().isInterrupted() || closed) return;
                    if (pkg.equals(context.getPackageName()) || TOOLS.contains(pkg)) continue;
                    Entry entry = load(pm, pkg);
                    if (entry != null) result.add(entry);
                }
                main.post(() -> {
                    if (!closed && expected == generation) callback.loaded(result);
                });
            } catch (RuntimeException | OutOfMemoryError error) {
                Log.w("HomeSimples", "Unable to refresh app catalog", error);
                main.post(() -> { if (!closed && expected == generation) callback.failed(); });
            }
        });
    }

    private Entry load(PackageManager pm, String pkg) {
        String key = pkg + ":" + revisions.getOrDefault(pkg, 0);
        Entry cached = cache.get(key);
        if (cached != null) return cached;
        try {
            Intent launch = pm.getLeanbackLaunchIntentForPackage(pkg);
            if (launch == null) launch = pm.getLaunchIntentForPackage(pkg);
            if (launch == null) return null;
            ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
            String label = pm.getApplicationLabel(info).toString();
            Drawable banner = pm.getActivityBanner(launch);
            if (banner == null) banner = pm.getApplicationBanner(info);
            Bitmap preparedBanner = rasterize(banner, width, height, true);
            Bitmap preparedIcon = rasterize(pm.getApplicationIcon(info), iconSize, iconSize, false);
            Entry entry = new Entry(pkg, label, info.flags, launch, preparedBanner, preparedIcon);
            if (!closed && !Thread.currentThread().isInterrupted()) cache.put(key, entry);
            return entry;
        } catch (PackageManager.NameNotFoundException | RuntimeException error) {
            Log.w("HomeSimples", "Unable to load app " + pkg, error);
            return null;
        }
    }

    private Bitmap rasterize(Drawable drawable, int w, int h, boolean crop) {
        if (drawable == null) return null;
        try {
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            int iw = Math.max(1, drawable.getIntrinsicWidth()), ih = Math.max(1, drawable.getIntrinsicHeight());
            float scale = crop ? Math.max(w / (float) iw, h / (float) ih) : Math.min(w / (float) iw, h / (float) ih);
            int dw = Math.max(1, Math.round(iw * scale)), dh = Math.max(1, Math.round(ih * scale));
            drawable.setBounds((w - dw) / 2, (h - dh) / 2, (w + dw) / 2, (h + dh) / 2);
            drawable.draw(new Canvas(bitmap));
            return bitmap;
        } catch (RuntimeException | OutOfMemoryError error) {
            // Never pass the original potentially oversized Drawable back to the UI.
            Log.w("HomeSimples", "Unable to prepare app artwork", error);
            return null;
        }
    }

    @Override public void close() {
        closed = true;
        ++generation;
        worker.close();
        main.removeCallbacksAndMessages(null);
        cache.evictAll();
    }
}
