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
    private static final float ALPHA_X = .6f; // lower alpha should equal smoother movement
    private static final float ALPHA_Y = .5f; // lower alpha should equal smoother movement

    private PowerManager.WakeLock mWakeLock;
    private SensorManager mSensorManager;
    private Servo servoX;
    private Servo servoY;
    protected static BluetoothSerial bluetoothSerial;

    private boolean enabled;
    private float[] accelerometerValues;
    private float[] magnetometerValues;
    private float[] orientation;
    private float[] R;
    private float[] I;

    @Override
    public void onCreate() {
        super.onCreate();
        R = new float[9];
        I = new float[9];
        orientation = new float[3];
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
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void setUpServos() {
        servoX = new Servo(3);
        servoY = new Servo(1);
        servoX.setSmoothness(32);
        servoY.setSmoothness(5);
        servoX.setThreshold(200);
//        servoY.setThreshold(200);
//        servoX.setSpeed(0);
//        servoY.setSpeed(0);
//        servoX.setAcceleration(0);
//        servoY.setAcceleration(30);
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
        mSensorManager.unregisterListener(this);
        mWakeLock.release();
        bluetoothSerial.disconnect();
        enabled = false;
        stopSelf();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerValues = applyLowPassFilter(event.values.clone(), accelerometerValues, ALPHA_Y);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magnetometerValues = applyLowPassFilter(event.values.clone(), magnetometerValues, ALPHA_X);
        }
        float[] values = getOrientation();
        if (values != null) {
            setTargetServoX(values[0]);
            setTargetServoY(values[2]);
        }
    }

    private float[] applyLowPassFilter(float[] input, float[] output, float alpha) {
        if (output == null) return input;
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + alpha * (input[i] - output[i]);
        }
        return output;
    }

    private float[] getOrientation() {
        if (accelerometerValues != null && magnetometerValues != null) {
            if (SensorManager.getRotationMatrix(R, I, accelerometerValues, magnetometerValues)) {
                return SensorManager.getOrientation(R, orientation);
            }
        }
        return null;
    }

    private void setTargetServoX(float value) {
        int target = (int) ((value * 2000) + 3800); // Convert the value to quarter-milliseconds
        if (target >= 1000 && target <= 10000) {
            if (servoX.setTarget(target)) {
                Log.d(TEST, "X axis value: " + servoX.getTarget());
            }
        }
    }

    private void setTargetServoY(float value) {
        int target = (int) ((-value * 2400)) + 1200; // Convert value to quarter-microseconds
        if (target >= 3000 && target <= 12000) {
            servoY.setTarget(target);
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
