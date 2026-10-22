package com.nimbo.launcher;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

// Tiny white outline icons on a 24x24 grid, so the app needs no image resources.
// Each icon is a list of ops: 0=moveTo x y, 1=lineTo x y, 2=close, 3=circle cx cy r.
final class LineIcon extends Drawable {
    static final int[] TRASH = {0, 45, 65, 1, 195, 65, 0, 85, 65, 1, 85, 35, 1, 155, 35, 1, 155, 65,
            0, 65, 65, 1, 75, 210, 1, 165, 210, 1, 175, 65,
            0, 100, 100, 1, 105, 175, 0, 140, 100, 1, 135, 175};
    static final int[] MOVE = {0, 30, 120, 1, 210, 120, 0, 65, 85, 1, 30, 120, 1, 65, 155,
            0, 175, 85, 1, 210, 120, 1, 175, 155, 0, 120, 30, 1, 120, 210,
            0, 85, 65, 1, 120, 30, 1, 155, 65, 0, 85, 175, 1, 120, 210, 1, 155, 175};
    static final int[] ACCESSIBILITY = {3, 120, 40, 18, 0, 45, 85, 1, 195, 85,
            0, 120, 85, 1, 120, 140, 0, 120, 140, 1, 75, 210, 0, 120, 140, 1, 165, 210};
    static final int[] REFRESH = {0, 195, 95, 1, 195, 40, 0, 195, 95, 1, 140, 95,
            0, 195, 95, 1, 170, 55, 1, 120, 35, 1, 65, 55, 1, 35, 105,
            1, 45, 160, 1, 90, 200, 1, 150, 200, 1, 195, 165};
    static final int[] FOLDER = {0, 25, 65, 1, 100, 65, 1, 125, 90, 1, 215, 90,
            1, 215, 195, 1, 25, 195, 2};
    static final int[] PLUS = {0, 120, 45, 1, 120, 195, 0, 45, 120, 1, 195, 120};
    static final int[] GEAR = {0, 100, 25, 1, 140, 25, 1, 148, 53, 1, 167, 64,
            1, 195, 57, 1, 215, 91, 1, 195, 112, 1, 195, 130, 1, 215, 150,
            1, 195, 184, 1, 167, 177, 1, 148, 188, 1, 140, 215, 1, 100, 215,
            1, 92, 188, 1, 73, 177, 1, 45, 184, 1, 25, 150, 1, 45, 130,
            1, 45, 112, 1, 25, 91, 1, 45, 57, 1, 73, 64, 1, 92, 53, 2,
            3, 120, 120, 34};
    static final int[] INFO = {3, 120, 120, 90, 3, 120, 75, 3,
            0, 120, 110, 1, 120, 170};
    static final int[] HIDDEN = {0, 20, 120, 1, 60, 75, 1, 120, 55, 1, 180, 75, 1, 220, 120,
            1, 180, 165, 1, 120, 185, 1, 60, 165, 2, 3, 120, 120, 32,
            0, 30, 30, 1, 210, 210};
    static final int[] HOME = {0, 25, 110, 1, 120, 35, 1, 215, 110,
            0, 50, 100, 1, 50, 205, 1, 100, 205, 1, 100, 140, 1, 140, 140,
            1, 140, 205, 1, 190, 205, 1, 190, 100};
    static final int[] PICTURE = {0, 30, 40, 1, 210, 40, 1, 210, 200, 1, 30, 200, 2,
            0, 30, 170, 1, 90, 110, 1, 130, 150, 1, 160, 120, 1, 210, 170, 3, 165, 80, 16};
    static final int[] PLAY = {0, 60, 45, 1, 60, 195, 1, 190, 120, 2};
    static final int[] HDMI = {0, 30, 90, 1, 210, 90, 1, 210, 130, 1, 185, 160, 1, 55, 160, 1, 30, 130, 2,
            0, 80, 120, 1, 160, 120};
    static final int[] SETTINGS = {0, 40, 70, 1, 140, 70, 0, 180, 70, 1, 200, 70, 0, 40, 170, 1, 60, 170,
            0, 100, 170, 1, 200, 170, 3, 160, 70, 20, 3, 80, 170, 20};

    private final int[] ops;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();

    LineIcon(int[] ops) {
        this.ops = ops;
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setColor(0xEBFFFFFF);
    }

    @Override
    public void draw(Canvas c) {
        if (ops == null) return;
        float s = getBounds().width() / 240f; // ops are in tenths of the 24-unit grid
        paint.setStrokeWidth(16 * s);
        path.reset();
        for (int i = 0; i < ops.length; ) {
            switch (ops[i]) {
                case 0: path.moveTo(ops[i + 1] * s, ops[i + 2] * s); i += 3; break;
                case 1: path.lineTo(ops[i + 1] * s, ops[i + 2] * s); i += 3; break;
                case 2: path.close(); i += 1; break;
                default: path.addCircle(ops[i + 1] * s, ops[i + 2] * s, ops[i + 3] * s, Path.Direction.CW); i += 4;
            }
        }
        c.save();
        c.translate(getBounds().left, getBounds().top);
        c.drawPath(path, paint);
        c.restore();
    }

    // Paint changes must invalidate the Drawable so ImageView redraws its display list on focus changes.
    @Override public void setAlpha(int a) {
        paint.setAlpha(a);
        invalidateSelf();
    }
    @Override public void setColorFilter(ColorFilter cf) {
        paint.setColorFilter(cf);
        invalidateSelf();
    }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    @Override public int getIntrinsicWidth() { return 44; }
    @Override public int getIntrinsicHeight() { return 44; }
}
