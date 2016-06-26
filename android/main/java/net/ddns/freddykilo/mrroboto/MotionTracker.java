package net.ddns.freddykilo.mrroboto;

import android.app.Service;
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
import android.widget.Toast;

public class MotionTracker extends Service implements SensorEventListener {

    private static final String TEST = "test";

    private PowerManager.WakeLock mWakeLock;
    private SensorManager mSensorManager;
    private Servo servoX;
    private Servo servoY;
    protected static BluetoothSerial bluetoothSerial;
    private SensorFusion sensorFusion;

    private boolean enabled;

    @Override
    public void onCreate() {
        super.onCreate();
        sensorFusion = new SensorFusion();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!enabled && intent != null && startOK()) {
            Toast.makeText(this, "STARTING", Toast.LENGTH_SHORT).show();
            return START_STICKY;
        } else {
            Toast.makeText(this, "STOPPING", Toast.LENGTH_SHORT).show();
            stop();
            return START_NOT_STICKY;
        }
    }

    private boolean startOK() {
        setUpWakeLock();
        setUpSensors();
        if (bluetoothConnectOK()) {
            setUpServos();
            enabled = true;
        }
        return enabled;
    }

    /**
     * Set up the wake lock to keep the screen on for as long as the service is running
     */
    private void setUpWakeLock() {
        PowerManager mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TEST);
        mWakeLock.acquire();
    }

    /**
     * Set up and register sensor listeners
     */
    private void setUpSensors() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);

    }

    private void setUpServos() {
        servoX = new Servo(3);
        servoY = new Servo(1);
    }

    private boolean bluetoothConnectOK() {
        bluetoothSerial = new BluetoothSerial();
        if (!bluetoothSerial.connectOK()) {
            Toast.makeText(this, "Could not connect\nto Mr Roboto", Toast.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }
    }

    /**
     * Unregister sensor listeners, disable wake lock, disconnect from bluetooth, and stop service
     */
    private void stop() {
        if (mSensorManager != null) mSensorManager.unregisterListener(this);
        if (mWakeLock != null) mWakeLock.release();
        if (bluetoothSerial != null) bluetoothSerial.disconnect();
        enabled = false;
        stopSelf();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                sensorFusion.setAccel(event.values);
                sensorFusion.calculateAccMagOrientation();
                break;

            case Sensor.TYPE_GYROSCOPE:
                sensorFusion.gyroFunction(event);
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                sensorFusion.setMagnet(event.values);
                break;
        }
        setTargetServoX(sensorFusion.getAzimuth());
        setTargetServoY(sensorFusion.getRoll());
    }

    private void setTargetServoX(double value) {
        int target = (int) ((-value * 19) + 4000); // Convert the value to quarter-milliseconds
        if (target >= 3900 && target <= 7500) {
            servoX.setTarget(target);
//            Log.d(TEST, "X axis value: " + target);
        }
    }

    private void setTargetServoY(double value) {
        int target = (int) ((-value * 50)) - 700; // Convert value to quarter-microseconds
        if (target >= 3000 && target <= 12000) {
            servoY.setTarget(target);
//            Log.d(TEST, "Y axis value: " + target);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TEST, "Sensor : " + sensor.getName());
        Log.d(TEST, "Accuracy: " + accuracy);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
