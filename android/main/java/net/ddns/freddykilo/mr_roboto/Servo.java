package net.ddns.freddykilo.mr_roboto;

/**
 * Using the Pololu Micro Maestro servo controller, the rx pin accepts a byte array
 * where byte 0 is the command, byte 1 is the servo channel, and bytes 2 and 3 are the
 * target of the servo position. This value is in quarter microseconds (i.e. range 4000 - 8000).
 * For detailed documentation visit <a href="https://www.pololu.com/docs/0J40/5.e">pololu.com</a>
 */
public class Servo {

    private int speed;
    private int acceleration;
    private int target;
    private byte[] command;

    /**
     * A new Servo to control with a Pololu Micro Maestro servo controller
     * @param channel The channel number that the servo is connected to
     */
    public Servo(int channel) {
        command = new byte[4];
        command[1] = (byte) channel;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
        command[0] = (byte) 0x87;
        setData(speed);
        MotionTracker.bluetoothSerial.send(command);
    }

    public void setAcceleration(int acceleration) {
        this.acceleration = acceleration;
        command[0] = (byte) 0x89;
        setData(acceleration);
        MotionTracker.bluetoothSerial.send(command);
    }

    public void setTarget(int target) {
        this.target = target;
        command[0] = (byte) 0x84;
        setData(target);
        MotionTracker.bluetoothSerial.send(command);
    }

    private void setData(int data) {
        command[2] = (byte) (data & 0x7F);
        command[3] = (byte) ((data >> 7) & 0x7F);
    }

    public int getSpeed() {
        return speed;
    }

    public int getAcceleration() {
        return acceleration;
    }

    public int getTarget() {
        return target;
    }
}
