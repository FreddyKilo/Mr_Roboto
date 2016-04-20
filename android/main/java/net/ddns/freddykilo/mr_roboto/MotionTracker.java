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

    private boolean enabled;
    private PowerManager.WakeLock mWakeLock;
    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor orientationSensor;

    private int yRotation;
    private byte[] serialBytes;

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
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        orientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_NORMAL);
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
     * Using the Pololu Micro Maestro servo controller, the rx line accepts a byte array
     * where byte 0 is the command, byte 1 is the servo channel, and bytes 2 and 3 are the
     * target of the servo position. This value is in quarter microseconds (range 4000 - 8000)
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            serialBytes[1] = 1; // Servo at channel 1
            // The value received is from -10 to 10, let's convert this to degrees
            float value2 = (event.values[2] * 11) + 90; // Had to multiply by 11 instead of 9 to calibrate servo movement

            // Let's only send data if the reading changes more than a degree.
            // This will reduce some jitter caused by noisy accelerometer data
            if (value2 > yRotation + 1.5 || value2 < yRotation - 1.5) {
                yRotation = Math.round(value2);
                int target = (yRotation * 22) + 4800;
                try {
                    Log.d(TAG, "sendData(): " + target);
                    serialBytes[2] = (byte) (target & 0x7F);
                    serialBytes[3] = (byte) ((target >> 7) & 0x7F);
                    bluetoothSerial.sendData(serialBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
//            Log.d(TAG, "Orientation value 0: " + event.values[0]);
//            Log.d(TAG, "Orientation value 1: " + event.values[1]);
//            Log.d(TAG, "Orientation value 2: " + event.values[2]);
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
