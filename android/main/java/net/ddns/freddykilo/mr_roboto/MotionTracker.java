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
import android.widget.Toast;

import java.io.IOException;

public class MotionTracker extends Service implements SensorEventListener {

    private static final String TEST = "test";
    private static final float ALPHA_X = 0.4f; // lower alpha should equal smoother movement
    private static final float ALPHA_Y = 0.6f;  // lower alpha should equal smoother movement
    private static final int X_AXIS_SERVO_NUMBER = 3;
    private static final int Y_AXIS_SERVO_NUMBER = 1;

    private static final int X_AXIS = 0;        // The index of the float[] returned from accelerometer sensor reading
    private static final int Y_AXIS = 2;        // The index of the float[] returned from magnetometer sensor reading

    private boolean enabled;
    private PowerManager.WakeLock mWakeLock;
    private SensorManager mSensorManager;
    private BluetoothSerial bluetoothSerial;

    private byte[] targetBytes;
    private byte[] accelBytes;
    private float[] accelerometerValues;
    private float[] magnetometerValues;
    private float[] R;
    private float[] I;
    private float[] orientation;

    @Override
    public void onCreate() {
        super.onCreate();
        targetBytes = new byte[4];
        accelBytes = new byte[4];
        targetBytes[0] = (byte) 0x84; // Command byte to set the target of a servo
        accelBytes[0] = (byte) 0x89;  // Command byte to set the acceleration value of a servo
        R = new float[9];
        I = new float[9];
        orientation = new float[3];
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

    public boolean startOK() {
        setUpWakeLock();
        setUpSensors();
        if (bluetoothConnectOK()) {
            setServoAcceleration(0, X_AXIS_SERVO_NUMBER);
            setServoAcceleration(0, Y_AXIS_SERVO_NUMBER);
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

    private boolean bluetoothConnectOK() {
        bluetoothSerial = new BluetoothSerial();
        if (!bluetoothSerial.connectOK()) {
            Toast.makeText(this, "Could not connect\nto Mr Roboto", Toast.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }
    }

    private void setServoAcceleration(int accelerationValue, int servoNumber) {
        accelBytes[1] = (byte) servoNumber; // Servo channel number
        accelBytes[2] = (byte) (accelerationValue & 0x7F);
        accelBytes[3] = (byte) ((accelerationValue >> 7) & 0x7F);
        try {
            bluetoothSerial.sendData(accelBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Unregister sensor listeners, disable wake lock and disconnect from bluetooth
     */
    public void stop() {
        mSensorManager.unregisterListener(this);
        mWakeLock.release();
        bluetoothSerial.disconnect();
        enabled = false;
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
            setTargetAxisX(values[X_AXIS]);
            setTargetAxisY(values[Y_AXIS]);
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

    /**
     * Using the Pololu Micro Maestro servo controller, the rx pin accepts a byte array
     * where byte 0 is the command, byte 1 is the servo channel, and bytes 2 and 3 are the
     * target of the servo position. This value is in quarter microseconds (range 4000 - 8000)
     */
    private void setTargetAxisX(float value) {
        targetBytes[1] = 3; // Servo at channel 3
        float conversion = (value * 2000) + 3000; // Convert the value to a range of 4000 - 8000
        if (conversion >= 4000 && conversion <= 8000) {
            int target = Math.round(conversion);
            targetBytes[2] = (byte) (target & 0x7F);
            targetBytes[3] = (byte) ((target >> 7) & 0x7F);
            try {
                bluetoothSerial.sendData(targetBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param value Orientation input range from 0 (looking down) to -3.14 (looking up)
     */
    private void setTargetAxisY(float value) {
        targetBytes[1] = 1; // Servo at channel 1
        float conversion = (value * 2100) + 10200; // Convert the value to a range of 4000 - 8000
        if (conversion >= 4000 && conversion <= 8000) {
            int target = Math.round(conversion);
            targetBytes[2] = (byte) (target & 0x7F);
            targetBytes[3] = (byte) ((target >> 7) & 0x7F);
            try {
                bluetoothSerial.sendData(targetBytes);
            } catch (IOException e) {
                e.printStackTrace();
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
