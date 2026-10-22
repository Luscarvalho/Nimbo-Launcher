package com.nimbo.launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.role.RoleManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

/** HOME setup uses the same sidebar and native system actions as the other settings. */
final class LauncherSetupPage {
    private static final int HOME_ROLE = 21;
    private final Activity host;
    private final SettingsSidebar sidebar;
    private final Runnable back;
    private int selected;

    LauncherSetupPage(Activity host, SettingsSidebar sidebar, Runnable back) {
        this.host = host;
        this.sidebar = sidebar;
        this.back = back;
    }

    void show() {
        boolean home = isDefaultHome(), enabled = accessibilityEnabled();
        List<SettingsSidebar.Item> items = new ArrayList<>();
        items.add(new SettingsSidebar.Item(host.getString(R.string.choose_default_launcher),
                host.getString(R.string.setup_action_status,
                        host.getString(home ? R.string.setup_default_ready : R.string.setup_default_description),
                        host.getString(home ? R.string.setup_state_default : R.string.setup_state_pending)),
                LineIcon.HOME, () -> { selected = 0; requestDefault(); }));
        items.add(new SettingsSidebar.Item(host.getString(R.string.configure_accessibility),
                host.getString(R.string.setup_action_status,
                        host.getString(enabled ? R.string.setup_accessibility_ready : R.string.setup_accessibility_description),
                        host.getString(enabled ? R.string.setup_state_active : R.string.setup_state_optional)),
                LineIcon.ACCESSIBILITY, () -> { selected = 1; openAccessibility(); }));
        items.add(new SettingsSidebar.Item(host.getString(R.string.test_home_button),
                host.getString(R.string.setup_test_description), LineIcon.PLAY,
                () -> { selected = 2; testHome(); }));
        sidebar.showPage(host.getString(R.string.default_home), host.getString(R.string.app_name), items, back, selected);
    }

    private static Intent homeIntent() { return LauncherSetupActivity.homeIntent(); }

    private boolean isDefaultHome() {
        ResolveInfo resolved = host.getPackageManager().resolveActivity(homeIntent(), PackageManager.MATCH_DEFAULT_ONLY);
        return resolved != null && resolved.activityInfo != null && host.getPackageName().equals(resolved.activityInfo.packageName);
    }

    private boolean accessibilityEnabled() {
        String services = Settings.Secure.getString(host.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (services == null) return false;
        ComponentName ours = new ComponentName(host, HomeAccessibilityService.class);
        for (String item : services.split(":"))
            if (ours.equals(ComponentName.unflattenFromString(item))) return true;
        return false;
    }

    private void requestDefault() {
        if (isDefaultHome()) {
            Toast.makeText(host, host.getString(R.string.already_default_toast), Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT >= 29) {
            RoleManager manager = host.getSystemService(RoleManager.class);
            if (manager != null && manager.isRoleAvailable(RoleManager.ROLE_HOME) && !manager.isRoleHeld(RoleManager.ROLE_HOME)) {
                try { host.startActivityForResult(manager.createRequestRoleIntent(RoleManager.ROLE_HOME), HOME_ROLE); return; }
                catch (RuntimeException ignored) { }
            }
        }
        Intent settings = new Intent(Settings.ACTION_HOME_SETTINGS);
        if (settings.resolveActivity(host.getPackageManager()) != null) {
            try { host.startActivity(settings); return; } catch (RuntimeException ignored) { }
        }
        showFallback();
    }

    private void showFallback() {
        new AlertDialog.Builder(host).setTitle(host.getString(R.string.launcher_change))
                .setMessage(host.getString(R.string.launcher_change_incomplete))
                .setPositiveButton(host.getString(R.string.open_accessibility), (dialog, which) -> openAccessibility())
                .setNegativeButton(host.getString(R.string.back), null).show();
    }

    private void openAccessibility() {
        try { host.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)); }
        catch (RuntimeException ex) {
            Toast.makeText(host, host.getString(R.string.accessibility_settings_hint), Toast.LENGTH_LONG).show();
        }
    }

    private void testHome() {
        try { host.startActivity(homeIntent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); }
        catch (RuntimeException ex) { showFallback(); }
    }

}
