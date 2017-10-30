package com.full.app.onebillion;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import static java.lang.Thread.sleep;

public class GameView extends SurfaceView implements Runnable {

    // ~~~~~~~~~~ Create game ~~~~~~~~~~

    int ScreenX;
    int ScreenY;

    volatile boolean playing = true;
    private Thread gameThread = new Thread(this);
    private Splash splash;

//    private Bitmap bitmap;
//    private Bitmap src;

    Dollar oneDollar;
    Dollar twoDollar;
    Dollar threeDollar;
    Dollar fourDollar;

    Hand hand;

    Background background1;

    int background1X;
    float background1Y;

    int canvasWidth;
    int canvasHeight;

    private SharedPreferences mSettings;
    public static final String APP_PREFERENCES = "mysettings";
    public static final String APP_PREFERENCES_COUNTER0 = "counter0";
    public static final String APP_PREFERENCES_COUNTER1 = "counter1";
    public static final String APP_PREFERENCES_COUNTER2 = "counter2";
    public static final String APP_PREFERENCES_COUNTER3 = "counter3";
    public static final String APP_PREFERENCES_COUNTER4 = "counter4";
    public static final String APP_PREFERENCES_COUNTER5 = "counter5";
    public static final String APP_PREFERENCES_COUNTER6 = "counter6";

    public GameView(Context context, int screenX, int screenY) {

        super(context);

        mSettings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        if (mSettings.contains(APP_PREFERENCES_COUNTER0)) {
            money[0] = mSettings.getInt(APP_PREFERENCES_COUNTER0, 0);
        }
        if (mSettings.contains(APP_PREFERENCES_COUNTER1)) {
            money[1] = mSettings.getInt(APP_PREFERENCES_COUNTER1, 0);
        }
        if (mSettings.contains(APP_PREFERENCES_COUNTER2)) {
            money[2] = mSettings.getInt(APP_PREFERENCES_COUNTER2, 0);
        }
        if (mSettings.contains(APP_PREFERENCES_COUNTER3)) {
            money[3] = mSettings.getInt(APP_PREFERENCES_COUNTER3, 0);
        }
        if (mSettings.contains(APP_PREFERENCES_COUNTER4)) {
            money[4] = mSettings.getInt(APP_PREFERENCES_COUNTER4, 0);
        }
        if (mSettings.contains(APP_PREFERENCES_COUNTER5)) {
            money[5] = mSettings.getInt(APP_PREFERENCES_COUNTER5, 0);
        }
        if (mSettings.contains(APP_PREFERENCES_COUNTER6)) {
            money[6] = mSettings.getInt(APP_PREFERENCES_COUNTER6, 0);
        }

        canvasWidth = screenX;
        canvasHeight = screenY;

        fpsRect = new Rect(screenX - 300, 0, screenX, 150);

        fpsPaint = new Paint();
        fpsPaint.setColor(Color.RED);
        fpsPaint.setTextSize(40);

        STEP = 1000 / getResources().getInteger(R.integer.frame_rate);

        FADE_TIME = getResources().getInteger(R.integer.fade_time);

        ALPHA_STEP = 256 / (FADE_TIME / STEP);

        SPLASH_TIME = getResources().getInteger(R.integer.splash_time);

        ScreenX = screenX;
        ScreenY = screenY;

        splash = new Splash(getResources().getBoolean(R.bool.portrait), context, Math.max(screenX, screenY), Math.min(screenX, screenY));

//        player = new Player(context, screenX, screenY);

        oneDollar = new Dollar(context, screenX, screenY, 1);
        twoDollar = new Dollar(context, screenX, screenY, 0);
        threeDollar = new Dollar(context, screenX, screenY, 0);
        fourDollar = new Dollar(context, screenX, screenY, 0);

        hand = new Hand(context, screenX, screenY, 1);

        background1 = new Background(context, screenX, screenY);

        background1X = 0;

        surfaceHolder = getHolder();
        paint = new Paint();

//        int starNums = 500;
//        for (int i = 0; i < starNums; i++) {
//            Star s = new Star(screenX, screenY);
//            stars.add(s);
//        }

//        src = BitmapFactory.decodeResource(this.getResources(), R.drawable.brick_tile);
//        bitmap = Bitmap.createScaledBitmap(src, 300, 300, false);

        fadein();
    }

    // ~~~~~~~~~~ Game activity ~~~~~~~~~~

    @Override
    public void run() {
        while (playing) {
            long startTime = System.currentTimeMillis();
            update();
            draw();
            long endTime = System.currentTimeMillis();
            int remainTime = (int) STEP - (int) (endTime - startTime);
            if (remainTime > 0) {
                try {
                    sleep(remainTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // ~~~~~~~~~~ Update game data ~~~~~~~~~~

    float ALPHA_STEP;
    long STEP;
    long FADE_TIME;
    long SPLASH_TIME;

    boolean sw = false;
    boolean sw2 = false;
    float swipe = 1;

    private void update() {
        if (splash.enabled()) { // All about splash of the application
            if (splash.getTime() >= FADE_TIME) {
                if (splash.getTime() >= SPLASH_TIME - FADE_TIME) {
                    fadein();
                }
                splash.setTime(splash.getTime() - (int) STEP);
            } else {
                fadeout();
                if (currentAlpha <= ALPHA_STEP) {
                    splash.setEnable(false);
                }
            }
        } else { // All about game

            if (sw) {
                if (swipe < 250) {
                    swipe *= 2;
                } else {
                    swipe += 250;
                }
            }
            if (swipe > oneDollar.getBitmap().getHeight()) {
                sw = false;
//                swipe = 1;
            }

            background1X--;
            background1Y -= 0.2;
            if (background1X < -canvasWidth) { background1X = 0; background1Y = 0; }

//            player.update();
//
//            for (Star s : stars) {
//                s.update(player.getBoosting(), player.getSpeed());
//            }
        }
    }

    // ~~~~~~~~~~ Fade in and fade out animations ~~~~~~~~~~

    private int currentAlpha = 0;

    private void fadeout() {
        if (currentAlpha >= ALPHA_STEP) {
            currentAlpha -= ALPHA_STEP;
        }
    }

    private void fadein() {
        if (currentAlpha <= 255 - ALPHA_STEP) {
            currentAlpha += ALPHA_STEP;
        }
    }

    // ~~~~~~~~~~ Draw game ~~~~~~~~~~

    private Paint paint;
    private SurfaceHolder surfaceHolder;

    int last = 0;
    int curr = 0;
    int next = 0;

//    public int money = 0;
    public int[] money = {0,0,0,0,0,0,0};

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            Canvas canvas = surfaceHolder.lockCanvas();
            if (splash.enabled()) {
                canvas.drawColor(Color.WHITE);
                paint.setARGB(currentAlpha, 255, 255, 255);

                canvas.drawBitmap(splash.getBitmap(), splash.getX(), splash.getY(), paint);
            } else {

                paint.setARGB(100, 255, 255, 255);

                canvas.drawColor(Color.WHITE);

                canvas.drawBitmap(background1.getBitmap(), background1X, canvas.getHeight() - canvas.getHeight()*20/100 + background1Y, paint);

                paint.setARGB(255, 255, 255, 255);

                canvas.drawBitmap(threeDollar.getBitmap(),
                        0,
                        canvas.getHeight() - (canvas.getHeight()*20/100 + threeDollar.getBitmap().getHeight()),
                        paint);

                switch (next) {
                    case 0:
                        canvas.drawBitmap(twoDollar.getBitmap(),
                                0,
                                canvas.getHeight() - (canvas.getHeight()*22/100 + twoDollar.getBitmap().getHeight()),
                                paint);
                        break;
                    case 1:
                        canvas.drawBitmap(oneDollar.getBitmap(),
                                0,
                                canvas.getHeight() - (canvas.getHeight()*22/100 + oneDollar.getBitmap().getHeight()),
                                paint);
                        break;
                    default:
                        canvas.drawBitmap(oneDollar.getBitmap(),
                                0,
                                canvas.getHeight() - (canvas.getHeight()*22/100 + oneDollar.getBitmap().getHeight()),
                                paint);
                        break;
                }

                switch (curr) {
                    case 0:
                        canvas.drawBitmap(twoDollar.getBitmap(),
                                0,
                                canvas.getHeight() - (canvas.getHeight()*22/100 + swipe + twoDollar.getBitmap().getHeight()),
                                paint);
                        break;
                    case 1:
                        canvas.drawBitmap(oneDollar.getBitmap(),
                                0,
                                canvas.getHeight() - (canvas.getHeight()*22/100 + swipe + oneDollar.getBitmap().getHeight()),
                                paint);
                        break;
                    default:
                        canvas.drawBitmap(oneDollar.getBitmap(),
                                0,
                                canvas.getHeight() - (canvas.getHeight()*22/100 + swipe + oneDollar.getBitmap().getHeight()),
                                paint);
                        break;
                }

                paint.setColor(Color.DKGRAY);

                for (int i = 0; i < 8; i++) {
                    canvas.drawRect(
                            52 + ((canvas.getWidth() - 100) / 8) * i,
                            canvas.getHeight() - canvas.getHeight()/6,
                            48 + ((canvas.getWidth() - 100) / 8) * (i + 1),
                            canvas.getHeight() - canvas.getHeight()/24,
                            paint);
                }

                paint.setARGB(255, 255, 255, 255);

                if (!sw2) {
                    canvas.drawBitmap(hand.getBitmap(), hand.getBitmap().getWidth() / 3, hand.getBitmap().getHeight() / 3, paint);
                }

                paint.setColor(Color.WHITE);
                paint.setTextSize(canvas.getWidth()*17/100);

                canvas.drawText(
                        "K",
                        canvas.getWidth()/19 + ((canvas.getWidth() - 100) / 8) * 7,
                        canvas.getHeight() - canvas.getHeight()/15,
                        paint);

                canvas.drawText(
                        Integer.toString(money[0]),
                        canvas.getWidth()/19 + ((canvas.getWidth() - 100) / 8) * 6,
                        canvas.getHeight() - canvas.getHeight()/15,
                        paint);

                canvas.drawText(
                        Integer.toString(money[1]),
                        canvas.getWidth()/19 + ((canvas.getWidth() - 100) / 8) * 5,
                        canvas.getHeight() - canvas.getHeight()/15,
                        paint);

                canvas.drawText(
                        Integer.toString(money[2]),
                        canvas.getWidth()/19 + ((canvas.getWidth() - 100) / 8) * 4,
                        canvas.getHeight() - canvas.getHeight()/15,
                        paint);

                canvas.drawText(
                        Integer.toString(money[3]),
                        canvas.getWidth()/19 + ((canvas.getWidth() - 100) / 8) * 3,
                        canvas.getHeight() - canvas.getHeight()/15,
                        paint);

                canvas.drawText(
                        Integer.toString(money[4]),
                        canvas.getWidth()/19 + ((canvas.getWidth() - 100) / 8) * 2,
                        canvas.getHeight() - canvas.getHeight()/15,
                        paint);

                canvas.drawText(
                        Integer.toString(money[5]),
                        canvas.getWidth()/19 + ((canvas.getWidth() - 100) / 8),
                        canvas.getHeight() - canvas.getHeight()/15,
                        paint);

                canvas.drawText(
                        Integer.toString(money[6]),
                        canvas.getWidth()/19,
                        canvas.getHeight() - canvas.getHeight()/15,
                        paint);

//                canvas.drawText(money + "$", 100, canvas.getHeight() - 80, paint);

            }
            if (getResources().getBoolean(R.bool.showFPS)) {
                drawFps(canvas);
            }
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    // ~~~~~~~~~~ Pause and resume game ~~~~~~~~~~

    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException ignored) {
        }
    }

    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    // ~~~~~~~~~~ Motion events ~~~~~~~~~~

    private int lastX = 0;
    private int lastY = 0;
    private int newX = 0;
    private int newY = 0;
    private long lastTime;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = (int) event.getX();
                lastY = (int) event.getY();
                lastTime = System.currentTimeMillis();
                return true;
            case MotionEvent.ACTION_MOVE:
//                int x = (int) event.getX();
//                int y = (int) event.getY();
//                int time = (int) (System.currentTimeMillis() - lastTime);
                return true;
            case MotionEvent.ACTION_UP:
                int x = (int) event.getX();
                int y = (int) event.getY();
                int time = (int) (System.currentTimeMillis() - lastTime);
                float vx = (x - lastX) / (float) time;
                float vy = (y - lastY) / (float) time;
                handleTouchEvent(vx, vy, time);
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void handleTouchEvent(float vx, float vy, int time) {
        float absVx = Math.abs(vx);
        float absVy = Math.abs(vy);
        if (absVx < 0.2 && absVy < 0.2) {
            if (time < 300) {
                // Tap
            } else {
                // Long press
            }
        } else if (absVx > absVy) {
            if (vx > 0) {
                // Right Swipe
//                player.setBoosting();
            } else {
                // Left swipe
//                player.stopBoosting();
            }
        } else if (vy > 0) {
            //Down swipe
        } else {
            // Up swipe
            sw2 = true;
            curr = next;
            next = (int) Math.round(Math.random());
            swipe = 1;
            sw = true;
//            money++;
            if (money[0] == 9 &&
                    money[1] == 9 &&
                    money[2] == 9 &&
                    money[3] == 9 &&
                    money[4] == 9 &&
                    money[5] == 9 &&
                    money[6] == 9) {
            } else {
                money[0]++;
            }
            for (int i = 0; i < 7; i++) {
                if (money[i] > 9) {
                    money[i] = 0;
                    money[i+1]++;
                }
            }
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putInt(APP_PREFERENCES_COUNTER0, money[0]);
            editor.putInt(APP_PREFERENCES_COUNTER1, money[1]);
            editor.putInt(APP_PREFERENCES_COUNTER2, money[2]);
            editor.putInt(APP_PREFERENCES_COUNTER3, money[3]);
            editor.putInt(APP_PREFERENCES_COUNTER4, money[4]);
            editor.putInt(APP_PREFERENCES_COUNTER5, money[5]);
            editor.putInt(APP_PREFERENCES_COUNTER6, money[6]);
            editor.apply();
        }
    }

    // ~~~~~~~~~~ FPS counter ~~~~~~~~~~

    private Rect fpsRect;
    private Paint fpsPaint;
    private long fpsLastTime;

    private void drawFps(Canvas canvas) {
        long currTime = System.currentTimeMillis();
        if (fpsLastTime != 0) {
            int fps = (int) (1000 / (currTime - fpsLastTime));
            canvas.drawText("fps: " + Integer.toString(fps), fpsRect.centerX(), fpsRect.centerY(), fpsPaint);
        }
        fpsLastTime = currTime;
    }

}
