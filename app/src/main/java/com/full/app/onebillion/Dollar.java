package com.full.app.onebillion;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public class Dollar {
    private Bitmap bitmap;
    private int x;
    private int y;
    private boolean enable;
    private int time;

    public Dollar(Context context, int screenX, int screenY, int rotate) {
        Bitmap src = BitmapFactory.decodeResource(context.getResources(), R.drawable.bablo);
        bitmap = Bitmap.createScaledBitmap(src, screenX, (screenX) * src.getHeight() / src.getWidth(), false);
//        bitmap = RotateBitmap(bitmap, 90 + 180 * rotate);
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public void update() {
        //
    }

    void setTime(int time) {
        this.time = time;
    }

    int getTime() { return time; }

    Bitmap getBitmap() {
        return bitmap;
    }

    boolean enabled() {
        return enable;
    }

    void setEnable(boolean enable) { this.enable = enable; }

    int getX() {
        return x;
    }

    int getY() {
        return y;
    }
}

