package com.tmaproject.tarekkma.easybluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnConnectionLostListener;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnConnectionStateChangeListener;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnMessageReceivedListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.tmaproject.tarekkma.easybluetooth.BluetoothStates.STATE_CONNECTED;

/**
 * Created by tarekkma on 9/9/17.
 */

public class ConnectedThread extends Thread {

  private static final String TAG = "ConnectedThread";

  private boolean shouldDisconnect = false;

  private final BluetoothSocket socket;
  private final InputStream inputStream;
  private final OutputStream outputStream;

  private OnConnectionStateChangeListener connectionStateChangeListener;
  private OnConnectionLostListener connectionLostListener;
  private OnMessageReceivedListener messageReceivedListener;

  public ConnectedThread(BluetoothSocket socket) {
    Log.d(TAG, "create ConnectedThread: ");
    this.socket = socket;
    InputStream tmpIn = null;
    OutputStream tmpOut = null;

    // Get the BluetoothSocket input and output streams
    try {
      tmpIn = socket.getInputStream();
      tmpOut = socket.getOutputStream();
    } catch (IOException e) {
      Log.e(TAG, "temp sockets not created", e);
    }

    inputStream = tmpIn;
    outputStream = tmpOut;

    if (connectionStateChangeListener != null) {
      connectionStateChangeListener.connectionStateChanged(STATE_CONNECTED);
    }
  }

  public void setConnectionLostListener(OnConnectionLostListener connectionLostListener) {
    this.connectionLostListener = connectionLostListener;
  }

  public void setMessageReceivedListener(OnMessageReceivedListener messageReceivedListener) {
    this.messageReceivedListener = messageReceivedListener;
  }

  public void setConnectionStateChangeListener(
      OnConnectionStateChangeListener connectionStateChangeListener) {
    this.connectionStateChangeListener = connectionStateChangeListener;
  }

  public void disconnect(){
    shouldDisconnect = true;
  }

  public void run() {
    Log.i(TAG, "BEGIN mConnectedThread");
    byte[] buffer = new byte[1024];
    int bytes;

    // Keep listening to the InputStream while onConnected
    while (!shouldDisconnect) {
      try {
        // Read from the InputStream
        bytes = inputStream.read(buffer);

        if (messageReceivedListener != null) {
          messageReceivedListener.messageReceived(new String(buffer, 0, bytes));
        }
      } catch (IOException e) {
        Log.e(TAG, "disconnected", e);
        connectionLostListener.connectionLost();
        break;
      }
    }
  }

  /**
   * Write to the onConnected OutStream.
   *
   * @param buffer The bytes to write
   */
  public void write(byte[] buffer) {
    try {
      outputStream.write(buffer);
    } catch (IOException e) {
      Log.e(TAG, "Exception during write", e);
    }
  }

  public void cancel() {
    try {
      socket.close();
    } catch (IOException e) {
      Log.e(TAG, "close() of connect socket failed", e);
    }
  }
}