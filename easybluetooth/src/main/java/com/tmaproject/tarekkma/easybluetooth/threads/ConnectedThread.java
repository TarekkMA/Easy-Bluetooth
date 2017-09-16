package com.tmaproject.tarekkma.easybluetooth.threads;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.tmaproject.tarekkma.easybluetooth.HandlerKeys;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import static com.tmaproject.tarekkma.easybluetooth.States.STATE_CONNECTED;

/**
 * Created by tarekkma on 9/16/17.
 */

public class ConnectedThread extends Thread {
  private static final String TAG = "ConnectedThread";
  private final BluetoothSocket mmSocket;
  private final InputStream mmInStream;
  private final OutputStream mmOutStream;
  private boolean shouldDisconnect = false;
  private Handler handler;

  public ConnectedThread(BluetoothSocket socket, Handler handler) {
    this.handler = handler;
    Log.d(TAG, "create ConnectedThread: ");
    mmSocket = socket;
    InputStream tmpIn = null;
    OutputStream tmpOut = null;

    // Get the BluetoothSocket input and output streams
    try {
      tmpIn = socket.getInputStream();
      tmpOut = socket.getOutputStream();
    } catch (IOException e) {
      Log.e(TAG, "temp sockets not created", e);
    }

    mmInStream = tmpIn;
    mmOutStream = tmpOut;
    handler.obtainMessage(HandlerKeys.STATE, STATE_CONNECTED).sendToTarget();
  }

  public void run() {
    Log.i(TAG, "BEGIN mConnectedThread");
    byte[] buffer = new byte[1024];
    int bytes;

    // Keep listening to the InputStream while connected
    while (!shouldDisconnect) {
      try {
        // Read from the InputStream
        bytes = mmInStream.read(buffer);

        // Send the obtained bytes to the UI Activity
        handler.obtainMessage(HandlerKeys.MESSAGE_RECEIVED, Arrays.copyOfRange(buffer, 0, bytes))
            .sendToTarget();
      } catch (IOException e) {
        Log.e(TAG, "disconnected", e);
        handler.obtainMessage(HandlerKeys.CONNECTION_LOST).sendToTarget();
        break;
      }
    }
  }

  /**
   * Write to the connected OutStream.
   *
   * @param buffer The bytes to write
   */
  public void write(byte[] buffer) {
    try {
      mmOutStream.write(buffer);

      // Share the sent message back to the UI Activity
      //mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
    } catch (IOException e) {
      Log.e(TAG, "Exception during write", e);
    }
  }

  public void disconnect() {
    shouldDisconnect = true;
  }

  public void cancel() {
    try {
      mmSocket.close();
    } catch (IOException e) {
      Log.e(TAG, "close() of connect socket failed", e);
    }
  }
}