package com.nimbo.launcher;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/** A modal, remote-friendly settings panel. Back/left returns one page or closes it. */
final class SettingsSidebar extends Dialog {
    static final class Item {
        final String title, description;
        final int[] icon;
        final Drawable appIcon;
        final boolean destructive;
        final Runnable action;
        Item(String title, String description, int[] icon, Runnable action) {
            this(title, description, icon, false, action);
        }
        Item(String title, String description, int[] icon, boolean destructive, Runnable action) {
            this.title = title;
            this.description = description;
            this.icon = icon;
            this.appIcon = null;
            this.destructive = destructive;
            this.action = action;
        }
        Item(String title, String description, Drawable appIcon, Runnable action) {
            this.title = title;
            this.description = description;
            this.icon = null;
            this.appIcon = appIcon;
            this.destructive = false;
            this.action = action;
        }
    }

    private final float scale;
    private Runnable back;

    SettingsSidebar(Context context, float scale) {
        super(context, android.R.style.Theme_Material_NoActionBar);
        this.scale = scale;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(true);
        setOnKeyListener((dialog, code, event) -> {
            if (code != KeyEvent.KEYCODE_BACK && code != KeyEvent.KEYCODE_DPAD_LEFT && code != KeyEvent.KEYCODE_MENU)
                return false;
            if (event.getAction() == KeyEvent.ACTION_UP && !event.isCanceled()) {
                if (code == KeyEvent.KEYCODE_MENU || back == null) dismiss();
                else back.run();
            }
            return true;
        });
    }

    void showPage(String title, String description, List<Item> items, Runnable back) {
        showPage(title, description, items, back, null, 0);
    }

    void showAppPage(String title, String description, Drawable appIcon, List<Item> items) {
        showPage(title, description, items, null, appIcon, 0);
    }

    void showPage(String title, String description, List<Item> items, Runnable back, int focusedIndex) {
        showPage(title, description, items, back, null, focusedIndex);
    }

    private void showPage(String title, String description, List<Item> items, Runnable back, Drawable appIcon, int focusedIndex) {
        this.back = back;
        Context c = getContext();
        LinearLayout panel = new LinearLayout(c);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(px(44), px(76), px(44), px(40));
        panel.setBackgroundColor(0xFF202124);
        LinearLayout header = new LinearLayout(c);
        header.setGravity(Gravity.CENTER_VERTICAL);
        if (appIcon != null) {
            ImageView identity = new ImageView(c);
            identity.setImageDrawable(appIcon);
            identity.setScaleType(ImageView.ScaleType.FIT_CENTER);
            identity.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            LinearLayout.LayoutParams identityPosition = new LinearLayout.LayoutParams(px(80), px(80));
            identityPosition.rightMargin = px(28);
            header.addView(identity, identityPosition);
        }
        LinearLayout headingGroup = new LinearLayout(c);
        headingGroup.setOrientation(LinearLayout.VERTICAL);
        TextView heading = text(title, appIcon == null ? 44 : 40, 0xFFF1F3F4);
        heading.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        headingGroup.addView(heading);
        TextView detail = text(description, 25, 0xFFADB1B8);
        LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(-1, -2);
        dp.topMargin = px(12);
        headingGroup.addView(detail, dp);
        header.addView(headingGroup, new LinearLayout.LayoutParams(0, -2, 1));
        LinearLayout.LayoutParams headerPosition = new LinearLayout.LayoutParams(-1, -2);
        headerPosition.bottomMargin = px(44);
        panel.addView(header, headerPosition);
        if (appIcon != null) {
            View divider = new View(c);
            divider.setBackgroundColor(0x1AFFFFFF);
            LinearLayout.LayoutParams dividerPosition = new LinearLayout.LayoutParams(-1, Math.max(1, px(1)));
            dividerPosition.bottomMargin = px(24);
            panel.addView(divider, dividerPosition);
        }

        ScrollView scroll = new ScrollView(c);
        scroll.setFillViewport(false);
        // The card edges align with the heading, using the panel's shared margins.
        scroll.setPadding(0, 0, 0, 0);
        scroll.setClipToPadding(true);
        scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout rows = new LinearLayout(c);
        rows.setOrientation(LinearLayout.VERTICAL);
        List<Item> visible = new ArrayList<>(items);
        if (visible.isEmpty()) visible.add(new Item(back == null ? c.getString(R.string.back_home) : c.getString(R.string.back_settings),
                "", LineIcon.HOME, back == null ? this::dismiss : back));
        List<View> targets = new ArrayList<>();
        for (Item item : visible) {
            LinearLayout row = actionRow(c, scale, item);
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, -2);
            rp.bottomMargin = px(8);
            rows.addView(row, rp);
            targets.add(row);
        }
        for (int n = 0; n < targets.size(); n++) {
            View row = targets.get(n);
            row.setNextFocusUpId(targets.get(Math.max(0, n - 1)).getId());
            row.setNextFocusDownId(targets.get(Math.min(targets.size() - 1, n + 1)).getId());
            row.setNextFocusRightId(row.getId());
        }
        scroll.addView(rows, new ScrollView.LayoutParams(-1, -2));
        panel.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(panel);
        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(0xFF202124));
        window.getDecorView().setPadding(0, 0, 0, 0);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND | WindowManager.LayoutParams.FLAG_FULLSCREEN);
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.RIGHT | Gravity.TOP;
        params.width = px(660);
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.dimAmount = 0.48f;
        window.setAttributes(params);
        window.setWindowAnimations(R.style.SidebarAnimation);
        if (!isShowing()) show();
        targets.get(Math.max(0, Math.min(focusedIndex, targets.size() - 1))).requestFocus();
    }

    static LinearLayout actionRow(Context c, float scale, Item item) {
        LinearLayout row = new LinearLayout(c);
        row.setId(View.generateViewId());
        row.setFocusable(true);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(scaled(scale, 20), scaled(scale, 22), scaled(scale, 20), scaled(scale, 22));
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(scaled(scale, 12));
        bg.setColor(0x00000000);
        row.setBackground(bg);
        ImageView icon = new ImageView(c);
        LineIcon line = item.icon == null ? null : new LineIcon(item.icon);
        final int normalColor = item.destructive ? 0xFFF2B8B5 : 0xFFF1F3F4;
        final int focusColor = item.destructive ? 0xFF601410 : 0xFF202124;
        if (line != null) line.setColorFilter(normalColor, PorterDuff.Mode.SRC_IN);
        icon.setImageDrawable(line != null ? line : item.appIcon);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(scaled(scale, 36), scaled(scale, 36));
        ip.rightMargin = scaled(scale, 24);
        row.addView(icon, ip);
        LinearLayout labels = new LinearLayout(c);
        labels.setOrientation(LinearLayout.VERTICAL);
        TextView name = actionText(c, scale, item.title, 30, normalColor);
        labels.addView(name);
        TextView note = actionText(c, scale, item.description, 22, 0xFFADB1B8);
        if (!item.description.isEmpty()) {
            LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(-1, -2);
            np.topMargin = scaled(scale, 7);
            labels.addView(note, np);
        }
        row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        row.setOnFocusChangeListener((v, has) -> {
            bg.setColor(has ? (item.destructive ? 0xFFF9DEDC : 0xFFF1F3F4) : 0x00000000);
            name.setTextColor(has ? focusColor : normalColor);
            note.setTextColor(has ? 0xFF51555D : 0xFFADB1B8);
            if (line != null) line.setColorFilter(has ? focusColor : normalColor, PorterDuff.Mode.SRC_IN);
        });
        row.setOnClickListener(v -> { if (item.action != null) item.action.run(); });
        // Keep the label reference with its owning row for permission-state updates.
        row.setTag(name);
        return row;
    }

    static void setActionTitle(LinearLayout row, String title) {
        ((TextView) row.getTag()).setText(title);
    }

    private static int scaled(float scale, int value) { return Math.round(scale * value); }


    private static TextView actionText(Context context, float scale, String value, int size, int color) {
        TextView text = new TextView(context);
        text.setText(value);
        text.setTextSize(TypedValue.COMPLEX_UNIT_PX, Math.round(size * scale));
        text.setTextColor(color);
        return text;
    }

    private int px(int value) { return Math.round(value * scale); }

    private TextView text(String value, int size, int color) {
        TextView t = new TextView(getContext());
        t.setText(value);
        t.setTextSize(TypedValue.COMPLEX_UNIT_PX, px(size));
        t.setTextColor(color);
        return t;
    }
}
