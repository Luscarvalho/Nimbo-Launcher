package com.nimbo.launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.LinkedHashMap;

public class MainActivity extends Activity {

    // apps.txt and existing private preferences define initial priorities; activity extras cannot change them.
    private static final String PREFS = "config";
    private static final String SETTINGS = "settings";
    private static final String NETWORK = "network";
    private static final String LAUNCHER_SETUP = "launcher_setup";
    private static final String HIDDEN_APPS = "hidden_apps";
    private static final String ADD_APPS = "add_apps";
    private static final String APP_ORDER = "app_order";
    private static final int PICK_WALLPAPER = 41;
    private static final int UNINSTALL_APP = 42;
    private static final int MAX_INPUTS = 4;

    // Layout is designed at 1920x1080 and scaled to the TV's real UI size by px().
    private static final int MARGIN = 96, GAP = 24, RADIUS = 22, CARD_WIDTH = 288;
    // Covers both the 10% zoom and the elevation shadow at the ends of the shelf.
    private static final int FOCUS_INSET = 64;
    private static final int ROW1_TOP = 810;
    private float scale = 1f;

    private static Typeface regular, semibold;

    private FrameLayout root;
    private FrameLayout appRow;
    private TextView appsLabel;
    private WallpaperController wallpaper;
    private final List<View> tiles = new ArrayList<>();
    private boolean dirty = true;
    private String focusedKey;
    private View settingsButton;
    private SettingsSidebar sidebar;
    private LauncherSetupPage launcherSetup;
    private NetworkStatusIndicator networkStatus;
    private AppCatalog catalog;
    private final Map<String, AppCatalog.Entry> catalogApps = new LinkedHashMap<>();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final Runnable refreshApps = this::refreshCatalog;
    private boolean resumed, destroyed, catalogReady;
    private String initialFocusKey;

    private final BroadcastReceiver packagesChanged = new BroadcastReceiver() {
        @Override public void onReceive(Context c, Intent intent) {
            if (intent.getData() != null) catalog.invalidate(intent.getData().getSchemeSpecificPart());
            dirty = true;
            if (resumed) {
                main.removeCallbacks(refreshApps);
                main.postDelayed(refreshApps, 250);
            }
        }
    };

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        initialFocusKey = state == null ? null : state.getString("focused_app");
        loadFonts();
        scale = Math.min(getResources().getDisplayMetrics().widthPixels / 1920f,
                getResources().getDisplayMetrics().heightPixels / 1080f);
        catalog = new AppCatalog(this, px(CARD_WIDTH), px(CARD_WIDTH) * 9 / 16, px(80));
        getWindow().setBackgroundDrawable(background());

        root = new FrameLayout(this);
        root.setClipChildren(false);
        ImageView wallpaperView = new ImageView(this);
        wallpaperView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        root.addView(wallpaperView, new FrameLayout.LayoutParams(-1, -1));
        View shade = new View(this);
        shade.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {0x33000000, 0x22000000, 0xAA000000}));
        shade.setVisibility(View.GONE);
        root.addView(shade, new FrameLayout.LayoutParams(-1, -1));
        wallpaper = new WallpaperController(this, wallpaperView, shade);
        wallpaper.load();

        LinearLayout clockGroup = new LinearLayout(this);
        clockGroup.setOrientation(LinearLayout.VERTICAL);
        TextClock date = clock("EEEE, d MMMM", "EEEE, d MMMM", px(30), 0xA8FFFFFF, regular);
        TextClock time = clock("h:mm", "H:mm", px(176), Color.WHITE, semibold);
        time.setLetterSpacing(-0.028f);
        time.setFontFeatureSettings("tnum");
        time.setIncludeFontPadding(false);
        clockGroup.addView(date, new LinearLayout.LayoutParams(-2, -2));
        LinearLayout.LayoutParams timePosition = new LinearLayout.LayoutParams(-2, -2);
        timePosition.topMargin = px(-20);
        clockGroup.addView(time, timePosition);
        root.addView(clockGroup, at(px(MARGIN), px(92), -2, -2));
        addSettingsButton();
        networkStatus = new NetworkStatusIndicator(this);
        networkStatus.setId(View.generateViewId());
        networkStatus.setFocusable(true);
        GradientDrawable networkBackground = new GradientDrawable();
        networkBackground.setShape(GradientDrawable.OVAL);
        networkBackground.setColor(0x28000000);
        networkStatus.setBackground(networkBackground);
        networkStatus.setOnFocusChangeListener((v, has) -> {
            if (has) focusedKey = NETWORK;
            networkBackground.setColor(has ? 0xFFF1F1F1 : 0x28000000);
            v.invalidate();
            v.animate().scaleX(has ? 1.08f : 1f).scaleY(has ? 1.08f : 1f).setDuration(120).start();
        });
        networkStatus.setOnClickListener(v -> openNetworkSettings());
        networkStatus.setNextFocusUpId(networkStatus.getId());
        networkStatus.setNextFocusLeftId(networkStatus.getId());
        networkStatus.setNextFocusRightId(settingsButton.getId());
        settingsButton.setNextFocusLeftId(networkStatus.getId());
        FrameLayout.LayoutParams networkPosition = new FrameLayout.LayoutParams(px(72), px(72), Gravity.TOP | Gravity.RIGHT);
        networkPosition.topMargin = px(64);
        networkPosition.rightMargin = px(MARGIN + 88);
        root.addView(networkStatus, networkPosition);

        appsLabel = text(px(27), 0xCDFFFFFF, regular);
        appsLabel.setText(getString(R.string.applications));
        root.addView(appsLabel, at(px(MARGIN), px(ROW1_TOP - 62), -2, -2));

        HorizontalScrollView appScroll = new HorizontalScrollView(this);
        appScroll.setHorizontalScrollBarEnabled(false);
        appScroll.setFillViewport(false);
        appScroll.setFocusable(false);
        // The zoom space belongs inside appRow: the scroll view can clip at its child's bounds.
        appScroll.setPadding(0, 0, 0, 0);
        appScroll.setClipToPadding(false);
        appRow = new FrameLayout(this);
        appRow.setClipChildren(false);
        appScroll.addView(appRow, new FrameLayout.LayoutParams(0, -2));
        int rowHeight = px(CARD_WIDTH) * 9 / 16 + 2 * px(FOCUS_INSET) + px(70);
        root.addView(appScroll, at(px(MARGIN - FOCUS_INSET), px(ROW1_TOP - FOCUS_INSET),
                getResources().getDisplayMetrics().widthPixels - px(2 * MARGIN - 2 * FOCUS_INSET), rowHeight));

        setContentView(root);

        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_PACKAGE_ADDED);
        f.addAction(Intent.ACTION_PACKAGE_REMOVED);
        f.addAction(Intent.ACTION_PACKAGE_CHANGED);
        f.addAction(Intent.ACTION_PACKAGE_REPLACED);
        f.addDataScheme("package");
        if (android.os.Build.VERSION.SDK_INT >= 33) registerReceiver(packagesChanged, f, Context.RECEIVER_EXPORTED);
        else registerReceiver(packagesChanged, f);
    }

    @Override
    protected void onStart() {
        super.onStart();
        networkStatus.start();
    }

    @Override
    protected void onStop() {
        networkStatus.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        main.removeCallbacksAndMessages(null);
        catalog.close();
        if (sidebar != null) sidebar.dismiss();
        wallpaper.close();
        unregisterReceiver(packagesChanged);
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent.hasCategory(Intent.CATEGORY_HOME) && sidebar != null) sidebar.dismiss();
        if (dirty && resumed) refreshCatalog();
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        state.putString("focused_app", initialFocusKey != null ? initialFocusKey : focusedKey);
        super.onSaveInstanceState(state);
    }

    private void refreshCatalog() {
        main.removeCallbacks(refreshApps);
        if (destroyed || !resumed) return;
        dirty = false;
        catalog.refresh(getSharedPreferences(PREFS, 0).getString("apps", null), new AppCatalog.Callback() {
            @Override public void loaded(List<AppCatalog.Entry> apps) {
                if (destroyed) return;
                catalogApps.clear();
                for (AppCatalog.Entry entry : apps) catalogApps.put(entry.pkg, entry);
                if (!catalogReady) {
                    if (initialFocusKey != null) focusedKey = initialFocusKey;
                    else if (ADD_APPS.equals(focusedKey)) focusedKey = null;
                    initialFocusKey = null;
                }
                catalogReady = true;
                buildTiles();
            }
            @Override public void failed() {
                dirty = true;
                Toast.makeText(MainActivity.this, getString(R.string.apps_refresh_failed), Toast.LENGTH_SHORT).show();
                if (tiles.isEmpty()) buildTiles();
            }
        });
    }

    private List<String> appList() {
        return AppOrder.visible(catalogApps.keySet(), hiddenApps(),
                getSharedPreferences(PREFS, 0).getString(APP_ORDER, ""));
    }

    private Drawable appIcon(String pkg) {
        AppCatalog.Entry entry = catalogApps.get(pkg);
        return entry == null || entry.icon == null ? new LineIcon(LineIcon.HOME)
                : new BitmapDrawable(getResources(), entry.icon);
    }

    // SharedPreferences returns a shared set: always copy before modifying it.
    private Set<String> hiddenApps() {
        return new HashSet<>(getSharedPreferences(PREFS, 0).getStringSet(HIDDEN_APPS, Collections.emptySet()));
    }

    private String appName(String pkg) {
        AppCatalog.Entry entry = catalogApps.get(pkg);
        return entry == null ? pkg : entry.label;
    }

    private void appMenu(String pkg) {
        final String[] focusAfterClose = {pkg};
        sidebar = new SettingsSidebar(this, scale);
        sidebar.setOnDismissListener(d -> appRow.post(() -> focusAppTile(focusAfterClose[0])));
        List<SettingsSidebar.Item> items = new ArrayList<>();
        items.add(new SettingsSidebar.Item(getString(R.string.open_app), getString(R.string.open_app_description), LineIcon.PLAY, () -> {
            sidebar.dismiss();
            open(pkg);
        }));
        items.add(new SettingsSidebar.Item(getString(R.string.move_app), getString(R.string.move_app_description), LineIcon.MOVE, () -> {
            showMoveApp(pkg);
        }));
        items.add(new SettingsSidebar.Item(getString(R.string.hide_app), getString(R.string.hide_app_description), LineIcon.HIDDEN, () -> {
                    int oldIndex = 0;
                    for (int i = 0; i < tiles.size(); i++) if (pkg.equals(tiles.get(i).getTag())) { oldIndex = i; break; }
                    Set<String> hidden = hiddenApps();
                    hidden.add(pkg);
                    getSharedPreferences(PREFS, 0).edit().putStringSet(HIDDEN_APPS, hidden).apply();
                    buildTiles();
                    if (!tiles.isEmpty()) focusAfterClose[0] = (String) tiles.get(Math.min(oldIndex, tiles.size() - 1)).getTag();
                    sidebar.dismiss();
                    Toast.makeText(this, getString(R.string.app_hidden), Toast.LENGTH_SHORT).show();
                }));
        items.add(new SettingsSidebar.Item(getString(R.string.app_info), getString(R.string.app_info_description), LineIcon.INFO, () -> {
            sidebar.dismiss();
            openAppDetails(pkg);
        }));
        AppCatalog.Entry info = catalogApps.get(pkg);
        if (info != null) {
            boolean system = (info.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            boolean updated = (info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
            if (!system || updated) {
                items.add(new SettingsSidebar.Item(updated ? getString(R.string.uninstall_updates) : getString(R.string.uninstall_app),
                        updated ? getString(R.string.uninstall_updates_description) : getString(R.string.uninstall_app_description),
                        LineIcon.TRASH, true, () -> {
                            sidebar.dismiss();
                            requestUninstall(pkg);
                        }));
            }
        }
        sidebar.showAppPage(appName(pkg), getString(R.string.app_options), appIcon(pkg), items);
    }

    private void focusAppTile(String key) {
        for (View tile : tiles) if (key.equals(tile.getTag())) { tile.requestFocus(); return; }
        if (!tiles.isEmpty()) tiles.get(0).requestFocus();
    }

    private void openAppDetails(String pkg) {
        try { startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + pkg))); }
        catch (android.content.ActivityNotFoundException | SecurityException ex) {
            Toast.makeText(this, getString(R.string.app_info_failed), Toast.LENGTH_SHORT).show();
        }
    }

    private void requestUninstall(String pkg) {
        Intent uninstall = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, Uri.parse("package:" + pkg));
        uninstall.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        try { startActivityForResult(uninstall, UNINSTALL_APP); }
        catch (android.content.ActivityNotFoundException | SecurityException ex) { openAppDetails(pkg); }
    }

    private void showMoveApp(String pkg) {
        List<String> current = appList();
        List<SettingsSidebar.Item> positions = new ArrayList<>();
        for (int i = 0; i < current.size(); i++) {
            final int position = i;
            String description = pkg.equals(current.get(i)) ? getString(R.string.current_position) : appName(current.get(i));
            positions.add(new SettingsSidebar.Item(getString(R.string.app_position, i + 1), description, LineIcon.MOVE, () -> {
                List<String> order = appList();
                if (order.contains(pkg)) {
                    order = AppOrder.move(order, pkg, position);
                    saveAppOrder(order);
                    focusedKey = pkg;
                    buildTiles();
                }
                sidebar.dismiss();
            }));
        }
        sidebar.showAppPage(appName(pkg), getString(R.string.choose_position), appIcon(pkg), positions);
    }

    private void saveAppOrder(List<String> apps) {
        String previous = getSharedPreferences(PREFS, 0).getString(APP_ORDER, "");
        getSharedPreferences(PREFS, 0).edit().putString(APP_ORDER, AppOrder.save(apps, previous)).apply();
    }

    private void showAddApps() {
        final String[] focusAfterClose = {ADD_APPS};
        sidebar = new SettingsSidebar(this, scale);
        // The dialog/window may restore the old + focus while closing. Apply the chosen target afterwards.
        sidebar.setOnDismissListener(d -> appRow.post(() -> {
            for (View tile : tiles) {
                if (tile.getTag().equals(focusAfterClose[0])) {
                    tile.requestFocus();
                    return;
                }
            }
            for (View tile : tiles) if (ADD_APPS.equals(tile.getTag())) tile.requestFocus();
        }));
        List<String> hidden = new ArrayList<>(hiddenApps());
        hidden.removeIf(pkg -> !catalogApps.containsKey(pkg));
        Collections.sort(hidden, (a, b) -> appName(a).compareToIgnoreCase(appName(b)));
        List<SettingsSidebar.Item> items = new ArrayList<>();
        for (String pkg : hidden) {
            items.add(new SettingsSidebar.Item(appName(pkg), getString(R.string.add_to_home), appIcon(pkg), () -> {
                    Set<String> remaining = hiddenApps();
                    remaining.remove(pkg);
                    getSharedPreferences(PREFS, 0).edit().putStringSet(HIDDEN_APPS, remaining).apply();
                    focusAfterClose[0] = pkg;
                    focusedKey = pkg;
                    buildTiles();
                    sidebar.dismiss();
                }));
        }
        sidebar.showPage(getString(R.string.add_apps), hidden.isEmpty()
                ? getString(R.string.no_hidden_apps)
                : getString(R.string.choose_hidden_app), items, null);
    }

    private void addSettingsButton() {
        FrameLayout button = new FrameLayout(this);
        button.setId(View.generateViewId());
        button.setFocusable(true);
        button.setContentDescription(getString(R.string.settings));
        final GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(0x28000000);
        button.setBackground(bg);
        ImageView icon = new ImageView(this);
        LineIcon gear = new LineIcon(LineIcon.GEAR);
        icon.setImageDrawable(gear);
        button.addView(icon, new FrameLayout.LayoutParams(px(38), px(38), Gravity.CENTER));
        button.setOnFocusChangeListener((v, has) -> {
            if (has) focusedKey = SETTINGS;
            bg.setColor(has ? 0xFFF1F1F1 : 0x28000000);
            gear.setColorFilter(has ? 0xFF202124 : 0xEBFFFFFF, android.graphics.PorterDuff.Mode.SRC_IN);
            v.animate().scaleX(has ? 1.08f : 1f).scaleY(has ? 1.08f : 1f).setDuration(120).start();
        });
        button.setOnClickListener(v -> showSettings());
        button.setNextFocusUpId(button.getId());
        button.setNextFocusLeftId(button.getId());
        button.setNextFocusRightId(button.getId());
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(px(72), px(72), Gravity.TOP | Gravity.RIGHT);
        p.topMargin = px(64);
        p.rightMargin = px(MARGIN);
        root.addView(button, p);
        settingsButton = button;
    }

    private void showSettings() {
        if (sidebar != null && sidebar.isShowing()) return;
        sidebar = new SettingsSidebar(this, scale);
        sidebar.setOnDismissListener(d -> { launcherSetup = null; settingsButton.requestFocus(); });
        settingsPage();
    }

    private void openNetworkSettings() {
        // Try the direct Wi-Fi screen first; some TV firmware exposes only general network settings.
        for (String action : new String[] {Settings.ACTION_WIFI_SETTINGS, Settings.ACTION_WIRELESS_SETTINGS, Settings.ACTION_SETTINGS}) {
            try {
                startActivity(new Intent(action));
                return;
            } catch (android.content.ActivityNotFoundException | SecurityException ignored) { }
        }
        Toast.makeText(this, getString(R.string.network_settings_failed), Toast.LENGTH_SHORT).show();
    }

    private void settingsPage() {
        launcherSetup = null;
        List<SettingsSidebar.Item> items = new ArrayList<>();
        items.add(new SettingsSidebar.Item(getString(R.string.tv_settings), getString(R.string.tv_settings_description), LineIcon.GEAR, () -> {
            sidebar.dismiss();
            open(SETTINGS);
        }));
        items.add(new SettingsSidebar.Item(getString(R.string.wallpaper), getString(R.string.wallpaper_choose), LineIcon.PICTURE, this::chooseWallpaper));
        items.add(new SettingsSidebar.Item(getString(R.string.default_home), getString(R.string.default_home_description),
                LineIcon.HOME, this::launcherSetupPage));
        for (TvInputInfo in : hdmiInputs()) {
            items.add(new SettingsSidebar.Item(inputLabel(in), getString(R.string.open_tv_input), LineIcon.HDMI, () -> {
                sidebar.dismiss();
                open(in.getId());
            }));
        }
        sidebar.showPage(getString(R.string.settings), getString(R.string.app_name), items, null);
    }

    private void launcherSetupPage() {
        launcherSetup = new LauncherSetupPage(this, sidebar, this::settingsPage);
        launcherSetup.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumed = true;
        if (launcherSetup != null && sidebar != null && sidebar.isShowing()) launcherSetup.show();
        wallpaper.setVisible(true);
        if (tiles.isEmpty()) buildTiles();
        if (dirty) refreshCatalog();
        View f = getCurrentFocus();
        if (f == null && !tiles.isEmpty()) tiles.get(0).requestFocus();
    }

    @Override
    protected void onPause() {
        resumed = false;
        main.removeCallbacks(refreshApps);
        wallpaper.setVisible(false);
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int code, KeyEvent event) {
        if (code == KeyEvent.KEYCODE_MENU) return true;
        return super.onKeyDown(code, event);
    }

    @Override
    public boolean onKeyUp(int code, KeyEvent event) {
        if (code == KeyEvent.KEYCODE_MENU) {
            if (!event.isCanceled()) showSettings();
            return true;
        }
        return super.onKeyUp(code, event);
    }

    private void chooseWallpaper() {
        sidebar.dismiss();
        Intent picker = new Intent(this, WallpaperPickerActivity.class)
                .putExtra(WallpaperPickerActivity.EXTRA_HAS_CUSTOM, wallpaper.hasCustomWallpaper());
        startActivityForResult(picker, PICK_WALLPAPER);
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request == UNINSTALL_APP) dirty = true;
        if (request == PICK_WALLPAPER) {
            if (result == WallpaperPickerActivity.RESULT_RESET) wallpaper.clear(() -> {});
            else if (result == RESULT_OK && data != null && data.getData() != null)
                wallpaper.importImage(data.getData());
        }
    }

    @Override
    public void onBackPressed() { /* home screen: nothing to go back to */ }

    // Static warm gradient, drawn once by the window: no extra view, no animation.
    private Drawable background() {
        GradientDrawable base = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {0xFF15100F, 0xFF0A0809, 0xFF050405});
        GradientDrawable warm = radial(0x36FF8048, 0.08f, -0.12f, px(1250));
        GradientDrawable plum = radial(0x2EAA3C8C, 1.0f, 1.1f, px(1000));
        for (GradientDrawable g : new GradientDrawable[] {base, warm, plum}) g.setDither(true);
        return new LayerDrawable(new Drawable[] {base, warm, plum});
    }

    private static GradientDrawable radial(int color, float cx, float cy, float r) {
        GradientDrawable g = new GradientDrawable();
        g.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        g.setColors(new int[] {color, color & 0x00FFFFFF});
        g.setGradientCenter(cx, cy);
        g.setGradientRadius(r);
        return g;
    }

    private void buildTiles() {
        String restoreKey = focusedKey;
        appRow.removeAllViews();
        tiles.clear();

        int g = px(GAP), w = px(CARD_WIDTH);
        int h = w * 9 / 16;

        int col = 0;
        List<String> apps = appList();
        int inset = px(FOCUS_INSET);
        int contentWidth = (apps.size() + 1) * w + apps.size() * g + 2 * inset;
        int contentHeight = h + 2 * inset + px(70);
        // HorizontalScrollView measures its child with an unconstrained width. LayoutParams alone
        // do not reserve the trailing space: include real padding and a minimum measured size.
        appRow.setPadding(0, 0, inset, px(70));
        appRow.setClipToPadding(false);
        appRow.setMinimumWidth(contentWidth);
        appRow.setMinimumHeight(contentHeight);
        appRow.setLayoutParams(new FrameLayout.LayoutParams(contentWidth, contentHeight));
        for (String pkg : apps) {
            AppCatalog.Entry entry = catalogApps.get(pkg);
            if (entry == null) continue;
            Drawable banner = entry.banner == null ? null : new BitmapDrawable(getResources(), entry.banner);
            addTile(pkg, entry.label, banner, null, appIcon(pkg), inset + col * (w + g), inset, w, h);
            col++;
        }
        addTile(ADD_APPS, getString(R.string.add_apps), null, LineIcon.PLUS, null, inset + col * (w + g), inset, w, h);

        // Explicit links make the upper-right button reachable from every app.
        for (int n = 0; n < tiles.size(); n++) {
            View tile = tiles.get(n);
            tile.setNextFocusUpId(settingsButton.getId());
            tile.setNextFocusDownId(tile.getId());
            tile.setNextFocusLeftId(tiles.get(Math.max(0, n - 1)).getId());
            tile.setNextFocusRightId(tiles.get(Math.min(tiles.size() - 1, n + 1)).getId());
        }
        if (!tiles.isEmpty()) {
            settingsButton.setNextFocusDownId(tiles.get(0).getId());
            networkStatus.setNextFocusDownId(tiles.get(0).getId());
        } else {
            settingsButton.setNextFocusDownId(settingsButton.getId());
            networkStatus.setNextFocusDownId(networkStatus.getId());
        }

        View restore = null;
        for (View t : tiles) if (t.getTag().equals(restoreKey)) restore = t;
        if (NETWORK.equals(restoreKey)) networkStatus.requestFocus();
        else if (SETTINGS.equals(restoreKey)) settingsButton.requestFocus();
        else if (restore != null) restore.requestFocus();
        else if (!tiles.isEmpty()) tiles.get(0).requestFocus();
        else settingsButton.requestFocus();
    }

    private void addTile(final String key, String label, Drawable banner, int[] lineIcon, Drawable appIcon,
                         int x, int y, int w, int h) {
        FrameLayout card = new FrameLayout(this);
        final GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(px(RADIUS));
        if (banner != null) {
            bg.setColor(0xFF1E1A1C);
            ImageView iv = new ImageView(this);
            iv.setImageDrawable(banner);
            card.addView(iv, new FrameLayout.LayoutParams(-1, -1));
        } else {
            // Quiet "glass" tile: icon above label.
            bg.setColor(0x13FFFFFF);
            bg.setStroke(Math.max(1, px(2)), 0x17FFFFFF);
            LinearLayout inner = new LinearLayout(this);
            inner.setOrientation(LinearLayout.VERTICAL);
            inner.setGravity(Gravity.CENTER);
            ImageView iv = new ImageView(this);
            int size = lineIcon != null ? px(44) : px(64);
            if (lineIcon != null) iv.setImageDrawable(new LineIcon(lineIcon));
            else if (appIcon != null) iv.setImageDrawable(appIcon);
            inner.addView(iv, new LinearLayout.LayoutParams(size, size));
            if (!ADD_APPS.equals(key)) {
                TextView t = text(px(24), 0xE0FFFFFF, regular);
                t.setText(label);
                t.setSingleLine(true);
                t.setEllipsize(android.text.TextUtils.TruncateAt.END);
                t.setGravity(Gravity.CENTER);
                t.setPadding(px(16), 0, px(16), 0);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
                lp.topMargin = px(12);
                inner.addView(t, lp);
            }
            card.addView(inner, new FrameLayout.LayoutParams(-1, -1));
        }
        card.setBackground(bg);
        card.setClipToOutline(true);
        card.setFocusable(true);
        card.setId(View.generateViewId());
        card.setContentDescription(label);
        card.setTag(key);

        // Banner tiles show their name underneath when focused.
        final TextView caption = banner == null ? null : text(px(24), Color.WHITE, regular);
        if (caption != null) {
            caption.setText(label);
            caption.setGravity(Gravity.CENTER_HORIZONTAL);
            caption.setVisibility(View.INVISIBLE);
        }

        final boolean glass = banner == null;
        card.setOnFocusChangeListener((v, has) -> {
            if (has) {
                focusedKey = key;
                settingsButton.setNextFocusDownId(v.getId());
                networkStatus.setNextFocusDownId(v.getId());
                v.post(() -> {
                    if (v.hasFocus()) {
                        int space = px(FOCUS_INSET);
                        v.requestRectangleOnScreen(new Rect(-space, -space, w + space, h + space), false);
                    }
                });
            }
            if (glass) bg.setColor(has ? 0x38FFFFFF : 0x13FFFFFF);
            v.animate().scaleX(has ? 1.1f : 1f).scaleY(has ? 1.1f : 1f).translationZ(has ? px(26) : 0)
                    .setDuration(140).withLayer().start();
            if (caption != null) caption.setVisibility(has ? View.VISIBLE : View.INVISIBLE);
        });
        card.setOnClickListener(v -> open(key));
        // Handle horizontal focus here so HorizontalScrollView cannot wrap/reset focus at an edge.
        card.setOnKeyListener((v, code, event) -> {
            if (code != KeyEvent.KEYCODE_DPAD_LEFT && code != KeyEvent.KEYCODE_DPAD_RIGHT) return false;
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                int current = tiles.indexOf(v);
                if (current >= 0) {
                    int next = Math.max(0, Math.min(tiles.size() - 1,
                            current + (code == KeyEvent.KEYCODE_DPAD_LEFT ? -1 : 1)));
                    if (next != current) tiles.get(next).requestFocus();
                }
            }
            return true;
        });
        if (key.indexOf('.') > 0 && !isInput(key)) {
            // Native View dispatch prevents a click on key-up after a handled long press.
            card.setOnLongClickListener(v -> {
                appMenu(key);
                return true;
            });
        }

        FrameLayout wrap = new FrameLayout(this);
        wrap.setClipChildren(false);
        wrap.addView(card, new FrameLayout.LayoutParams(w, h));
        if (caption != null) {
            FrameLayout.LayoutParams cp = new FrameLayout.LayoutParams(w, -2);
            cp.topMargin = h + px(22);
            wrap.addView(caption, cp);
        }
        appRow.addView(wrap, at(x, y, w, -2));
        tiles.add(card);
    }

    private void open(String key) {
        Intent i;
        if (ADD_APPS.equals(key)) {
            showAddApps();
            return;
        } else if (LAUNCHER_SETUP.equals(key)) {
            startActivity(new Intent(this, LauncherSetupActivity.class));
            return;
        } else if (SETTINGS.equals(key)) {
            i = new Intent(Settings.ACTION_SETTINGS);
        } else if (isInput(key)) {
            i = new Intent(Intent.ACTION_VIEW, TvContract.buildChannelUriForPassthroughInput(key));
            // Prefer the stock Live TV app when it's there; otherwise let the TV pick.
            Intent live = new Intent(i).setClassName("com.android.tv", "com.android.tv.MainActivity");
            if (live.resolveActivity(getPackageManager()) != null) i = live;
        } else {
            AppCatalog.Entry entry = catalogApps.get(key);
            if (entry == null) return;
            i = new Intent(entry.launch);
        }
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try { startActivity(i); } catch (Exception ignored) {}
    }

    private static boolean isInput(String key) {
        return key.indexOf('/') > 0;
    }

    // The TV's own HDMI inputs (not devices behind them), in order, skipping hidden ones.
    private List<TvInputInfo> hdmiInputs() {
        List<TvInputInfo> out = new ArrayList<>();
        TvInputManager tim = (TvInputManager) getSystemService(Context.TV_INPUT_SERVICE);
        if (tim == null) return out;
        for (TvInputInfo in : tim.getTvInputList()) {
            if (in.getType() == TvInputInfo.TYPE_HDMI && in.getParentId() == null && !in.isHidden(this)) out.add(in);
        }
        Collections.sort(out, (a, b) -> a.getId().compareTo(b.getId()));
        return out.size() > MAX_INPUTS ? out.subList(0, MAX_INPUTS) : out;
    }

    // The name set in Settings > Inputs (like "Bedroom"), else the TV's own name (like "HDMI 1").
    private String inputLabel(TvInputInfo in) {
        CharSequence custom = in.loadCustomLabel(this);
        if (custom != null && custom.length() > 0) return custom.toString();
        CharSequence label = in.loadLabel(this);
        return label != null && label.length() > 0 ? label.toString() : getString(R.string.hdmi);
    }

    // Xiaomi TVs ship MiLan Pro but don't register it by name; every other TV gets Roboto.
    private static void loadFonts() {
        if (regular != null) return;
        regular = fromFile("/system/fonts/MILanPro_Normal.ttf", Typeface.create("sans-serif", Typeface.NORMAL));
        semibold = fromFile("/system/fonts/MILanPro_SemiBold.ttf", Typeface.create("sans-serif-medium", Typeface.NORMAL));
    }

    private static Typeface fromFile(String path, Typeface fallback) {
        try { return new File(path).exists() ? Typeface.createFromFile(path) : fallback; }
        catch (Exception e) { return fallback; }
    }

    private int px(int v) {
        return Math.round(v * scale);
    }

    private TextClock clock(String f12, String f24, int px, int color, Typeface tf) {
        TextClock c = new TextClock(this);
        c.setFormat12Hour(f12);
        c.setFormat24Hour(f24);
        c.setTextSize(TypedValue.COMPLEX_UNIT_PX, px);
        c.setTextColor(color);
        c.setTypeface(tf);
        return c;
    }

    private TextView text(int px, int color, Typeface tf) {
        TextView t = new TextView(this);
        t.setTextSize(TypedValue.COMPLEX_UNIT_PX, px);
        t.setTextColor(color);
        if (tf != null) t.setTypeface(tf);
        return t;
    }

    private static FrameLayout.LayoutParams at(int x, int y, int w, int h) {
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(w, h);
        lp.leftMargin = x;
        lp.topMargin = y;
        return lp;
    }
}
