package com.full.app.onebillion;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Splash {
    private Bitmap bitmap;
    private int x;
    private int y;
    private boolean enable;
    private int time;

    public Splash(boolean portrait, Context context, int screenX, int screenY) {
        if (portrait) {
            y = (screenX / 2) - (screenY / 2);
            x = 0;
        } else {
            x = (screenX / 2) - (screenY / 2);
            y = 0;
        }
        Bitmap src = BitmapFactory.decodeResource(context.getResources(), R.drawable.splash);
        bitmap = Bitmap.createScaledBitmap(src, screenY, screenY, false);
        time = context.getResources().getInteger(R.integer.splash_time);
        enable = context.getResources().getBoolean(R.bool.splash_enabled);
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
