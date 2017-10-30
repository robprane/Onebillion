package com.full.app.onebillion;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public class Background {

    private Bitmap bitmap;
    private int x;
    private int y;
    private boolean enable;
    private int time;

    public Background(Context context, int screenX, int screenY) {
        Bitmap src = BitmapFactory.decodeResource(context.getResources(), R.drawable.money_bag_background);
        bitmap = Bitmap.createScaledBitmap(src, screenX * 2, screenX * 2, false);
//        bitmap = RotateBitmap(bitmap, 270);
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
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
