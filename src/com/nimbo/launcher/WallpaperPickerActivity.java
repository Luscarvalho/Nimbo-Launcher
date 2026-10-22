package com.nimbo.launcher;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.OperationCanceledException;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/** A TV-friendly local image picker, including TVs without DocumentsUI. */
public class WallpaperPickerActivity extends Activity {
    static final String EXTRA_HAS_CUSTOM = "has_custom_wallpaper";
    static final int RESULT_RESET = Activity.RESULT_FIRST_USER;
    private static final int ACCESS = 7, DOCUMENT = 8;
    private final LatestTaskRunner worker = new LatestTaskRunner();
    private ThumbnailLoader thumbnails;
    private CancellationSignal queryCancellation;
    private LinearLayout more;
    private LinearLayout refresh, browse, reset;
    private float scale;
    private int imageLimit = 200;
    private boolean loadingMore;
    private ListView list;
    private TextView status;
    private LinearLayout access;
    private boolean closed;
    private int requestGeneration;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        thumbnails = new ThumbnailLoader(getContentResolver());
        if (state != null) imageLimit = state.getInt("image_limit", 200);
        scale = Math.min(getResources().getDisplayMetrics().widthPixels / 1920f,
                getResources().getDisplayMetrics().heightPixels / 1080f);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(px(44), px(76), px(44), px(40));
        page.setBackgroundColor(0xFF202124);
        TextView title = text(getString(R.string.wallpaper_picker_title), 44);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        page.addView(title);
        TextView hint = text(getString(R.string.wallpaper_picker_hint), 25);
        hint.setTextColor(0xFFADB1B8);
        LinearLayout.LayoutParams hintPosition = new LinearLayout.LayoutParams(-1, -2);
        hintPosition.topMargin = px(12);
        hintPosition.bottomMargin = px(36);
        page.addView(hint, hintPosition);
        access = action(getString(R.string.allow_images), LineIcon.PICTURE, this::requestImageAccess);
        addAction(page, access);
        refresh = action(getString(R.string.refresh), LineIcon.REFRESH, this::reload);
        addAction(page, refresh);
        Intent document = new Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE).setType("image/*");
        if (document.resolveActivity(getPackageManager()) != null) {
            browse = action(getString(R.string.browse_other_locations), LineIcon.FOLDER,
                    () -> startActivityForResult(document, DOCUMENT));
            addAction(page, browse);
        }
        if (getIntent().getBooleanExtra(EXTRA_HAS_CUSTOM, false)) {
            reset = action(getString(R.string.wallpaper_reset), LineIcon.HOME, () -> {
                setResult(RESULT_RESET);
                finish();
            });
            addAction(page, reset);
        }
        status = text("", 22);
        status.setPadding(0, px(20), 0, px(20));
        status.setTextColor(0xFFADB1B8);
        page.addView(status);
        list = new ListView(this);
        list.setId(View.generateViewId());
        list.setDivider(null);
        list.setDividerHeight(px(8));
        StateListDrawable selector = new StateListDrawable();
        selector.addState(new int[] {android.R.attr.state_focused}, selectionBackground());
        selector.addState(new int[] {android.R.attr.state_pressed}, selectionBackground());
        selector.addState(new int[] {}, new ColorDrawable(0x00000000));
        list.setSelector(selector);
        list.setDrawSelectorOnTop(false);
        list.setItemsCanFocus(false);
        page.addView(list, new LinearLayout.LayoutParams(-1, 0, 1));
        more = action(getString(R.string.load_more_images), LineIcon.PLUS, () -> {
            imageLimit += 200;
            loadingMore = true;
            reload();
        });
        more.setVisibility(View.GONE);
        addAction(page, more);
        setContentView(page);
        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(0xFF202124));
        window.getDecorView().setPadding(0, 0, 0, 0);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.END | Gravity.TOP;
        params.width = px(660);
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.dimAmount = 0.48f;
        window.setAttributes(params);
        access.requestFocus();
    }

    @Override protected void onResume() { super.onResume(); reload(); }

    private boolean canReadImages() {
        if (Build.VERSION.SDK_INT >= 33) {
            return checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
                    || (Build.VERSION.SDK_INT >= 34 && checkSelfPermission(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED);
        }
        return checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestImageAccess() {
        if (Build.VERSION.SDK_INT >= 34)
            requestPermissions(new String[] {Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED}, ACCESS);
        else if (Build.VERSION.SDK_INT >= 33)
            requestPermissions(new String[] {Manifest.permission.READ_MEDIA_IMAGES}, ACCESS);
        else requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, ACCESS);
    }

    @Override public void onRequestPermissionsResult(int request, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(request, permissions, results);
        if (request == ACCESS) {
            reload();
            String permission = Build.VERSION.SDK_INT >= 33 ? Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;
            if (!canReadImages() && !shouldShowRequestPermissionRationale(permission)) {
                SettingsSidebar.setActionTitle(access, getString(R.string.allow_in_settings));
                access.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + getPackageName()))));
            }
        }
    }

    private void reload() {
        if (closed) return;
        final int expected = ++requestGeneration;
        final int limit = imageLimit;
        final int oldSelection = Math.max(0, list.getSelectedItemPosition());
        final int nextSelection = loadingMore && list.getAdapter() != null ? list.getAdapter().getCount() : oldSelection;
        loadingMore = false;
        if (queryCancellation != null) queryCancellation.cancel();
        worker.cancel();
        queryCancellation = new CancellationSignal();
        final CancellationSignal cancellation = queryCancellation;
        thumbnails.reset();
        more.setVisibility(View.GONE);
        access.setOnClickListener(v -> requestImageAccess());
        SettingsSidebar.setActionTitle(access, getString(R.string.allow_images));
        boolean allowed = canReadImages();
        boolean full = Build.VERSION.SDK_INT < 33
                || checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        access.setVisibility(allowed && full ? View.GONE : View.VISIBLE);
        if (allowed && !full) SettingsSidebar.setActionTitle(access, getString(R.string.choose_other_images));
        updateFocusLinks();
        if (!allowed) {
            status.setText(getString(R.string.images_permission_needed));
            list.setAdapter(null);
            return;
        }
        status.setText(getString(R.string.images_loading));
        worker.submit(() -> {
            List<MediaItem> items = new ArrayList<>();
            boolean failed = false, hasMore = false;
            String[] columns = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.WIDTH, MediaStore.Images.Media.HEIGHT};
            try (Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    columns, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC, " + MediaStore.Images.Media._ID + " DESC", cancellation)) {
                if (cursor != null) while (cursor.moveToNext()) {
                    if (cancellation.isCanceled() || Thread.currentThread().isInterrupted()) return;
                    if (items.size() == limit) { hasMore = true; break; }
                    Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getLong(0));
                    items.add(new MediaItem(uri, cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getInt(4)));
                }
            } catch (OperationCanceledException ex) { return; }
            catch (RuntimeException ex) { failed = true; }
            final boolean error = failed, canLoadMore = hasMore;
            runOnUiThread(() -> {
                if (closed || expected != requestGeneration) return;
                if (error) status.setText(getString(R.string.images_query_failed));
                else if (items.isEmpty()) status.setText(getString(R.string.images_empty));
                else status.setText(canLoadMore
                        ? getResources().getQuantityString(R.plurals.images_more_status, items.size(), items.size())
                        : getString(R.string.images_choose_hint));
                more.setVisibility(!error && canLoadMore ? View.VISIBLE : View.GONE);
                updateFocusLinks();
                list.setAdapter(new MediaAdapter(items));
                list.setOnItemClickListener((parent, view, position, id) -> select(items.get(position).uri));
                if (!items.isEmpty()) { list.setSelection(Math.min(nextSelection, items.size() - 1)); list.requestFocus(); }
            });
        });
    }

    private void select(Uri uri) {
        setResult(RESULT_OK, new Intent().setData(uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
        finish();
    }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request == DOCUMENT && result == RESULT_OK && data != null && data.getData() != null) select(data.getData());
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        state.putInt("image_limit", imageLimit);
        super.onSaveInstanceState(state);
    }

    @Override protected void onDestroy() {
        closed = true;
        ++requestGeneration;
        if (queryCancellation != null) queryCancellation.cancel();
        worker.close();
        thumbnails.close();
        super.onDestroy();
    }

    private static final class MediaItem {
        final Uri uri;
        final String name, mime;
        final int width, height;
        MediaItem(Uri uri, String name, String mime, int width, int height) {
            this.uri = uri; this.name = name; this.mime = mime; this.width = width; this.height = height;
        }
    }

    private final class MediaAdapter extends BaseAdapter {
        final List<MediaItem> items;
        MediaAdapter(List<MediaItem> items) { this.items = items; }
        @Override public int getCount() { return items.size(); }
        @Override public MediaItem getItem(int position) { return items.get(position); }
        @Override public long getItemId(int position) { return position; }
        @Override public View getView(int position, View reuse, ViewGroup parent) {
            LinearLayout row;
            if (reuse instanceof LinearLayout) row = (LinearLayout) reuse;
            else {
                row = new LinearLayout(WallpaperPickerActivity.this);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(px(20), px(18), px(20), px(18));
                ImageView preview = new ImageView(WallpaperPickerActivity.this);
                preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                row.addView(preview, new LinearLayout.LayoutParams(px(128), px(72)));
                LinearLayout labels = new LinearLayout(WallpaperPickerActivity.this);
                labels.setOrientation(LinearLayout.VERTICAL);
                labels.setPadding(px(24), 0, 0, 0);
                labels.setDuplicateParentStateEnabled(true);
                TextView name = text("", 27);
                name.setDuplicateParentStateEnabled(true);
                name.setTextColor(selectionColors(0xFFF1F3F4, 0xFF202124));
                name.setSingleLine(true);
                name.setEllipsize(TextUtils.TruncateAt.END);
                labels.addView(name);
                TextView detail = text("", 22);
                detail.setDuplicateParentStateEnabled(true);
                detail.setTextColor(selectionColors(0xFFADB1B8, 0xFF51555D));
                labels.addView(detail);
                row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
            }
            MediaItem item = getItem(position);
            ImageView preview = (ImageView) row.getChildAt(0);
            thumbnails.bind(preview, item.uri);
            LinearLayout labels = (LinearLayout) row.getChildAt(1);
            ((TextView) labels.getChildAt(0)).setText(item.name == null ? getString(R.string.image_label) : item.name);
            String kind = "image/gif".equals(item.mime) ? getString(R.string.gif_label) : getString(R.string.image_label);
            ((TextView) labels.getChildAt(1)).setText(item.width > 0 ? getString(R.string.image_dimensions, kind, item.width, item.height) : kind);
            return row;
        }
    }

    private LinearLayout action(String label, int[] icon, Runnable callback) {
        return SettingsSidebar.actionRow(this, scale, new SettingsSidebar.Item(label, "", icon, callback));
    }

    private void addAction(LinearLayout page, View action) {
        LinearLayout.LayoutParams position = new LinearLayout.LayoutParams(-1, -2);
        position.bottomMargin = px(8);
        page.addView(action, position);
    }

    private void updateFocusLinks() {
        List<View> actions = new ArrayList<>();
        if (access.getVisibility() == View.VISIBLE) actions.add(access);
        actions.add(refresh);
        if (browse != null) actions.add(browse);
        if (reset != null) actions.add(reset);
        for (int index = 0; index < actions.size(); index++) {
            View action = actions.get(index);
            action.setNextFocusUpId(actions.get(Math.max(0, index - 1)).getId());
            action.setNextFocusDownId(index + 1 < actions.size() ? actions.get(index + 1).getId() : list.getId());
            action.setNextFocusRightId(action.getId());
        }
        list.setNextFocusUpId(actions.get(actions.size() - 1).getId());
        list.setNextFocusDownId(more.getVisibility() == View.VISIBLE ? more.getId() : list.getId());
        list.setNextFocusRightId(list.getId());
        more.setNextFocusUpId(list.getId());
        more.setNextFocusDownId(more.getId());
        more.setNextFocusRightId(more.getId());
        if (getCurrentFocus() == null) actions.get(0).requestFocus();
    }

    @Override public boolean onKeyDown(int code, KeyEvent event) {
        if (code == KeyEvent.KEYCODE_DPAD_LEFT) return true;
        return super.onKeyDown(code, event);
    }

    @Override public boolean onKeyUp(int code, KeyEvent event) {
        if (code == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (!event.isCanceled()) finish();
            return true;
        }
        return super.onKeyUp(code, event);
    }

    private GradientDrawable selectionBackground() {
        GradientDrawable background = new GradientDrawable();
        background.setColor(0xFFF1F3F4);
        background.setCornerRadius(px(12));
        return background;
    }

    private ColorStateList selectionColors(int normal, int selected) {
        return new ColorStateList(new int[][] {{android.R.attr.state_selected}, {android.R.attr.state_pressed}, {}},
                new int[] {selected, selected, normal});
    }

    private TextView text(String value, int size) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, px(size));
        view.setTextColor(0xFFF1F3F4);
        return view;
    }

    private int px(int value) { return Math.round(value * scale); }
}
