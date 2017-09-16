package com.tmaproject.tarekkma.easybluetooth.threads;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;
import com.tmaproject.tarekkma.easybluetooth.HandlerKeys;
import java.io.IOException;
import java.util.UUID;

import static com.tmaproject.tarekkma.easybluetooth.States.STATE_CONNECTING;

/**
 * This thread runs while attempting to make an outgoing connection
 * with a device. It runs straight through; the connection either
 * succeeds or fails.
 *
 * @author github.com/googlesamples/android-BluetoothChat
 */
public class ConnectThread extends Thread {
  private static final String TAG = "ConnectThread";

  private final BluetoothSocket mmSocket;
  private final BluetoothDevice mmDevice;
  private final BluetoothAdapter bluetoothAdapter;
  private final Handler handler;

  public ConnectThread(BluetoothDevice device,BluetoothAdapter bluetoothAdapter,UUID uuid,Handler handler) {
    mmDevice = device;
    this.bluetoothAdapter = bluetoothAdapter;
    this.handler = handler;
    BluetoothSocket tmp = null;

    // Get a BluetoothSocket for a connection with the
    // given BluetoothDevice
    try {
      tmp = device.createRfcommSocketToServiceRecord(uuid);
    } catch (IOException e) {
      Log.e(TAG, "create() failed", e);
    }
    mmSocket = tmp;
    handler.obtainMessage(HandlerKeys.STATE,STATE_CONNECTING).sendToTarget();
  }

  public void run() {
    Log.i(TAG, "BEGIN mConnectThread");
    setName("ConnectThread");

    // Always cancel discovery because it will slow down a connection
    bluetoothAdapter.cancelDiscovery();

    // Make a connection to the BluetoothSocket
    try {
      // This is a blocking call and will only return on a
      // successful connection or an exception
      mmSocket.connect();
    } catch (IOException e) {
      // Close the socket
      try {
        mmSocket.close();
      } catch (IOException e2) {
        Log.e(TAG, "unable to close() socket during connection failure", e2);
      }
      handler.obtainMessage(HandlerKeys.CONNECTION_FAILED).sendToTarget();
      return;
    }

    // Start the connected thread
    handler.obtainMessage(HandlerKeys.CONNECTED,new ConnectedBundle(mmDevice,mmSocket)).sendToTarget();
  }

  public void cancel() {
    try {
      mmSocket.close();
    } catch (IOException e) {
      Log.e(TAG, "close() of connect socket failed", e);
    }
  }

  public static class ConnectedBundle{
    public final BluetoothDevice device;
    public final BluetoothSocket socket;

    public ConnectedBundle(BluetoothDevice device, BluetoothSocket socket) {
      this.device = device;
      this.socket = socket;
    }
  }
}
