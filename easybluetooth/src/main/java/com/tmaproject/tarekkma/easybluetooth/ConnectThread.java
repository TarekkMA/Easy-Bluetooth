package com.tmaproject.tarekkma.easybluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnConnectionStateChangeListener;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by tarekkma on 9/9/17.
 */

public class ConnectThread extends Thread {

  public interface ConnectedListener {
    void onConnected(BluetoothDevice device, BluetoothSocket socket);
  }

  public interface ConnectionFailedListener {
    void connectionFailed(Exception e);
  }

  private static final String TAG = "ConnectThread";

  private final BluetoothSocket socket;
  private final BluetoothDevice device;
  private final BluetoothAdapter bluetoothAdapter;

  private ConnectedListener connectedListener;
  private ConnectionFailedListener connectionFailedListener;
  private OnConnectionStateChangeListener stateChangeListener;

  public ConnectThread(String SPP_UUID, BluetoothDevice device, BluetoothAdapter bluetoothAdapter) {
    this.device = device;
    this.bluetoothAdapter = bluetoothAdapter;

    BluetoothSocket tmp = null;
    try {
      tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
    } catch (IOException e) {
      e.printStackTrace();
    }
    socket = tmp;
  }

  public void setConnectedListener(ConnectedListener connectedListener) {
    this.connectedListener = connectedListener;
  }

  public void setConnectionFailedListener(ConnectionFailedListener connectionFailedListener) {
    this.connectionFailedListener = connectionFailedListener;
  }

  public void setStateChangeListener(OnConnectionStateChangeListener stateChangeListener) {
    this.stateChangeListener = stateChangeListener;
  }

  @Override public void run() {
    setName("ConnectThread");

    Log.i(TAG, "BEGIN " + getName());

    // Always cancel discovery because it will slow down a connection
    bluetoothAdapter.cancelDiscovery();

    // Make a connection to the BluetoothSocket
    try {
      // This is a blocking call and will only return on a
      // successful connection or an exception
      socket.connect();
    } catch (IOException e) {
      // Close the socket
      try {
        socket.close();
      } catch (IOException e2) {
        Log.e(TAG, "unable to close() socket during connection failure", e2);
      }
      if (connectionFailedListener != null) connectionFailedListener.connectionFailed(e);
      return;
    }

    if (connectedListener != null) connectedListener.onConnected(device, socket);
  }

  public void cancel() {
    try {
      socket.close();
    } catch (IOException e) {
      Log.e(TAG, "close() of connect socket failed", e);
    }
  }
}