package com.example.blinder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private MainThread thread;
    private int x = 0, y = 0;
    private int winWidth, winHeight;

    private SensorManager sm;

    private Sensor blind;
    private Sensor move;

    private boolean isBlinded;

    SensorEventListener blindListener;
    SensorEventListener moveListener;

    private boolean goingRight;
    private boolean goingLeft;

    private int speed = 10;
    private final int normalSpeed = 10;

    private int points = 0;

    private int leftLine;
    private int rightLine;

    public GameView(final Context context, Point size, SensorManager sman) {
        super(context);
        this.sm = sman;

        blind = sm.getDefaultSensor(Sensor.TYPE_LIGHT);
        move = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        winHeight = size.y;
        winWidth = size.x;

        x = winWidth / 2;
        y = winHeight / 2;

        leftLine = (int) (winWidth / 2 - 0.1 * winWidth);
        rightLine = (int) (winWidth / 2 + 0.1 * winWidth);

        blindListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                //android.widget.Toast.makeText(context, Float.toString(event.values[0]), Toast.LENGTH_SHORT).show();
                if (event.values[0] < 10) {
                    isBlinded = true;
                } else {
                    isBlinded = false;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        moveListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                final float alpha = 0.8f;

                float gravity = 0;
                float linAccl = 0;

                gravity = alpha * gravity + (1 - alpha) * event.values[1];
                linAccl = event.values[1] - gravity;

                //Log.println(10,"XD", Float.toString(linAccl));

                if (linAccl > 1) {
                    goingLeft = false;
                    goingRight = true;
                } else if (linAccl > -1 && linAccl < 1) {
                    goingRight = false;
                    goingLeft = false;
                } else {
                    goingLeft = true;
                    goingRight = false;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        sm.registerListener(blindListener, blind, SensorManager.SENSOR_DELAY_FASTEST);
        sm.registerListener(moveListener, move, SensorManager.SENSOR_DELAY_NORMAL);

        getHolder().addCallback(this);
        thread = new MainThread(getHolder(), this);
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        sm.unregisterListener(blindListener);
        sm.unregisterListener(moveListener);

        boolean retry = true;
        while (retry) {
            try {
                thread.setRunning(false);
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retry = false;
        }
    }

    public void update() {
        if (goingLeft) {
            speed = -normalSpeed;
        } else if (goingRight) {
            speed = normalSpeed;
        } else {
            speed = 0;
        }

        int mod = winWidth / 2 - (x + 50);

        if (mod >= 0) {
            mod = 1;
        } else {
            mod = -1;
        }

        int counterSpeed = 4 * mod;

        if (!(x < 0 || x + 100 > winWidth)) {
            x += speed - counterSpeed;
        } else {
            x = winWidth / 2;
        }

        if (x + 100 < rightLine && x > leftLine && isBlinded) {
            points += 1;
        } else if (isBlinded) {
            points -= 1;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas != null) {
            canvas.drawColor(Color.WHITE);
            Paint paint = new Paint();
            paint.setColor(Color.rgb(153, 0, 153));
            canvas.drawRect(x, y, x + 100, y + 100, paint);

            Paint paint2 = new Paint();
            paint.setColor(Color.rgb(250, 0, 0));

            //canvas.drawRect((int)(winHeight/2-0.1*winHeight),0,(int)((winHeight/2-0.1*winHeight)+10),winWidth,paint2);

            canvas.drawRect(rightLine, 0, (rightLine + 10), winHeight, paint2);
            canvas.drawRect(leftLine, 0, (leftLine - 10), winHeight, paint2);

            Paint paint3 = new Paint();
            paint3.setColor(Color.BLACK);
            paint3.setTextSize(200);

            canvas.drawText(Integer.toString(points), 15, 200, paint3);
        }
    }

}
