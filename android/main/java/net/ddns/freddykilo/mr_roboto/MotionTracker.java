package net.ddns.freddykilo.mr_roboto;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.IOException;

public class MotionTracker extends Service implements SensorEventListener {

    private static final String TAG = "test";

    private boolean enabled;
    private PowerManager.WakeLock mWakeLock;
    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor rotationSensor;

    private BluetoothSerial bluetoothSerial;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!enabled && intent != null) {
            start();
            return START_STICKY;
        } else {
            stop();
            return START_NOT_STICKY;
        }
    }

    public void start() {
        Toast.makeText(this, "STARTING", Toast.LENGTH_SHORT).show();

        bluetoothSerial = new BluetoothSerial();

        // Keep the screen on for as long as this service is running
        PowerManager mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
        mWakeLock.acquire();

        // Set up the sensor listeners
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);

        enabled = true;
    }

    public void stop() {
        Toast.makeText(this, "STOPPING", Toast.LENGTH_SHORT).show();

        mSensorManager.unregisterListener(this);
        mWakeLock.release();
        try {
            bluetoothSerial.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        enabled = false;
    }

    /**
     * Y AXIS for up and down movement
     * X AXIS for left and right
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Convert the accelerometer input into angles. 90 degrees is looking straight ahead
            int yRotation = (Math.round(event.values[2] * -9) + 90);
            try {
                bluetoothSerial.sendData("y:" + String.valueOf(yRotation));
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "AXIS Y: " + yRotation);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void setButtonImage(int drawable) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName componentName = new ComponentName(this, WidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
        for (int widgetId : allWidgetIds) {
            int baseLayout = R.layout.widget_layout;
            appWidgetManager.updateAppWidget(widgetId, getRemoteView(drawable, baseLayout));
        }
    }

    /**
     * Return a RemoteViews object based on the layout that is passed
     *
     * @param drawable R.drawable.foo_image
     * @param layout   R.layout.foo_layout
     * @return RemoteViews object
     */
    private RemoteViews getRemoteView(int drawable, int layout) {
        RemoteViews remoteViews = new RemoteViews(this.getPackageName(), layout);
        remoteViews.setImageViewResource(R.id.button_image, drawable);
        return remoteViews;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
