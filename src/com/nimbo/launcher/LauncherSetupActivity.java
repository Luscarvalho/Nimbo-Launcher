package com.nimbo.launcher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

/** Standalone entry point; normal Home navigation opens this page inside its settings panel. */
public class LauncherSetupActivity extends Activity {
    private SettingsSidebar sidebar;
    private LauncherSetupPage setup;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        FrameLayout background = new FrameLayout(this);
        background.setBackgroundColor(0xFF202124);
        setContentView(background);
        float scale = Math.min(getResources().getDisplayMetrics().widthPixels / 1920f,
                getResources().getDisplayMetrics().heightPixels / 1080f);
        sidebar = new SettingsSidebar(this, scale);
        sidebar.setOnDismissListener(dialog -> finish());
        setup = new LauncherSetupPage(this, sidebar, null);
        setup.show();
    }

    @Override protected void onResume() {
        super.onResume();
        if (setup != null && sidebar.isShowing()) setup.show();
    }

    @Override protected void onDestroy() {
        if (sidebar != null) {
            sidebar.setOnDismissListener(null);
            sidebar.dismiss();
        }
        super.onDestroy();
    }

    static Intent homeIntent() { return new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME); }
}
