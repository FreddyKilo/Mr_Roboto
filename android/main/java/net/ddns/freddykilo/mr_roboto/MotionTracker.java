package net.ddns.freddykilo.mr_roboto;

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

import java.io.IOException;

public class MotionTracker extends Service implements SensorEventListener {

    private static final String TAG = "test";
    private static final int SERVO_THRESHOLD = 20;
    private static final int X_AXIS = 0;
    private static final int Y_AXIS = 2;

    private boolean enabled;
    private PowerManager.WakeLock mWakeLock;
    private SensorManager mSensorManager;

    private int xRotation;
    private int yRotation;
    private byte[] serialBytes;
    private float[] accelerometerValues;
    private float[] magnetometerValues;

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
        serialBytes = new byte[4];
        serialBytes[0] = (byte) 0x84; // Set target command
        setUpWakeLock();
        setUpSensors();
        enabled = true;
    }

    /**
     * Set up the wake lock to keep the screen on for as long as the service is running
     */
    private void setUpWakeLock() {
        PowerManager mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
        mWakeLock.acquire();
    }

    /**
     * Set up and register sensor listeners
     */
    private void setUpSensors() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    /**
     * Unregister sensor listeners, disable wake lock and close the bluetooth connection
     */
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
     * Using the Pololu Micro Maestro servo controller, the rx pin accepts a byte array
     * where byte 0 is the command, byte 1 is the servo channel, and bytes 2 and 3 are the
     * target of the servo position. This value is in quarter microseconds (range 4000 - 8000)
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerValues = event.values;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magnetometerValues = event.values;
        }
        float[] values = getOrientation();
        if(values != null) {
            setTargetAxisX(values[X_AXIS]);
            setTargetAxisY(values[Y_AXIS]);
        }
    }

    private float[] getOrientation() {
        if (accelerometerValues != null && magnetometerValues != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            if (SensorManager.getRotationMatrix(R, I, accelerometerValues, magnetometerValues)) {
                float orientation[] = new float[3];
                return SensorManager.getOrientation(R, orientation);
            }
        }
        return null;
    }

    private void setTargetAxisX(float value) {

        serialBytes[1] = 3; // Servo at channel 3

//        Log.d(TAG, "Orientation X: " + value);
        float conversion = (value * 1500) + 4000; // Convert the value to a range of 4000 - 8000
        if (conversion > 4000 && conversion < 8000) {
            int target = Math.round(conversion);
            // Let's only send data if the reading changes more than a given amount.
            // This will reduce some jitter caused by noisy accelerometer data
            if (target > xRotation + 50 || target < xRotation - 50) {
                xRotation = target;
                Log.d(TAG, String.format("sendData(%d)", target));
                serialBytes[2] = (byte) (target & 0x7F);
                serialBytes[3] = (byte) ((target >> 7) & 0x7F);
                try {
                    bluetoothSerial.sendData(serialBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * @param value Orientation input range from 0 (looking down) to -3.14 (looking up)
     */
    private void setTargetAxisY(float value) {

        serialBytes[1] = 1; // Servo at channel 1

        float conversion = (value * 2100) + 10500; // Convert the value to a range of 4000 - 8000
        if (conversion > 4000 && conversion < 8000) {
            int target = Math.round(conversion);
            // Let's only send data if the reading changes more than a given amount.
            // This will reduce some jitter caused by noisy accelerometer data
            if (target > yRotation + SERVO_THRESHOLD || target < yRotation - SERVO_THRESHOLD) {
                yRotation = target;
//                Log.d(TAG, "sendData(): " + target);
                serialBytes[2] = (byte) (target & 0x7F);
                serialBytes[3] = (byte) ((target >> 7) & 0x7F);
                try {
                    bluetoothSerial.sendData(serialBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
