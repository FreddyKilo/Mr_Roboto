package net.ddns.freddykilo.mr_roboto;

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
    private UUID uuid;

    private static final String DEVICE_NAME = "Mr_Roboto";

    public BluetoothSerial() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connect() throws IOException {
        uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        bluetoothDevice = getDeviceByName(bluetoothAdapter, DEVICE_NAME);
        bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
        bluetoothSocket.connect();
        outputStream = bluetoothSocket.getOutputStream();
    }

    public void disconnect() throws IOException {
        bluetoothSocket.close();
        outputStream.close();
    }

    public void sendData(byte[] data) throws IOException {
        outputStream.write(data);
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
