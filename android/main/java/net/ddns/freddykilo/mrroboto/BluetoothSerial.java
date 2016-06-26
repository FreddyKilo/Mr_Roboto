package net.ddns.freddykilo.mrroboto;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Freddy on 4/8/2016.
 */
public class BluetoothSerial {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;

    private static final String DEVICE_NAME = "Mr_Roboto";

    public BluetoothSerial() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean connectOK() {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        bluetoothDevice = getDeviceByName(bluetoothAdapter, DEVICE_NAME);
        try {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void disconnect() {
        try {
            if (outputStream != null) outputStream.close();
            if (bluetoothSocket != null) bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        bluetoothDevice = null;
        bluetoothAdapter = null;
    }

    public void send(byte[] data) {
        try {
            outputStream.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BluetoothDevice getDeviceByName(BluetoothAdapter adapter, String name) {
        for (BluetoothDevice device : adapter.getBondedDevices()) {
            String thisDevice = device.getName();
            if (name.matches(thisDevice)) {
                return device;
            }
        }
        return null;
    }

}
