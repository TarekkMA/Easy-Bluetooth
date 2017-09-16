package com.tmaproject.tarekkma.easybluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnConnectionChangedListener;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnDiscoveringListener;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnMessageReceived;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnStateChangedListener;
import com.tmaproject.tarekkma.easybluetooth.threads.ConnectThread;
import com.tmaproject.tarekkma.easybluetooth.threads.ConnectedThread;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.tmaproject.tarekkma.easybluetooth.States.STATE_CONNECTED;
import static com.tmaproject.tarekkma.easybluetooth.States.STATE_CONNECTING;
import static com.tmaproject.tarekkma.easybluetooth.States.STATE_NONE;

/**
 * Created by tarekkma on 9/15/17.
 */

public class EasyBluetooth implements IEasyBluetooth {

  private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
  private OnConnectionChangedListener onConnectionChangedListener;
  private OnStateChangedListener onStateChangedListener;
  private OnMessageReceived onMessageReceived;
  private BtDevice connectedDevice = null;
  private ConnectedThread mConnectedThread;
  private ConnectThread mConnectThread;
  private int mState;
  private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

  @Override public void start() {

  }

  @Override public void stop() {

  }

  @Override public boolean isSupported() {
    return bluetoothAdapter != null;
  }

  @Override public boolean isEnabled() {
    return bluetoothAdapter.isEnabled();
  }

  @Override public void requestEnable(Context context) {

  }

  @Override public String getMyDeviceName() {
    return bluetoothAdapter.getName();
  }

  @Override public String getMyDeviceAddress() {
    return bluetoothAdapter.getAddress();
  }

  @Override public List<BtDevice> getPairedDevices() {
    List<BtDevice> paired = new ArrayList<>();
    Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
    for (BluetoothDevice device : bondedDevices) {
      paired.add(new BtDevice(device));
    }
    return paired;
  }

  @Override public void connect(String address) {
    connect(new BtDevice(bluetoothAdapter.getRemoteDevice(address)));
  }

  @Override public void connect(BtDevice btDevice) {
    // Cancel any thread attempting to make a connection
    if (mState == STATE_CONNECTING) {
      if (mConnectThread != null) {
        mConnectThread.cancel();
        mConnectThread = null;
      }
    }

    // Cancel any thread currently running a connection
    if (mConnectedThread != null) {
      mConnectedThread.cancel();
      mConnectedThread = null;
    }

    // Start the thread to connect with the given device
    mConnectThread = new ConnectThread(btDevice.getDevice(), bluetoothAdapter, SPP_UUID, mHandler);
    mConnectThread.start();
  }

  @Override public boolean isConnected() {
    return connectedDevice != null;
  }

  private void conneceted(BluetoothSocket socket){

    // Cancel the thread that completed the connection
    if (mConnectThread != null) {
      mConnectThread.cancel();
      mConnectThread = null;
    }

    // Cancel any thread currently running a connection
    if (mConnectedThread != null) {
      mConnectedThread.cancel();
      mConnectedThread = null;
    }

    // Start the thread to manage the connection and perform transmissions
    mConnectedThread = new ConnectedThread(socket,mHandler);
    mConnectedThread.start();

  }

  @Override public void write(byte[] bytes) {
    if (mState != STATE_CONNECTED) return;
    mConnectedThread.write(bytes);
  }

  @Override public void write(String string) {
    write(string.getBytes());
  }

  public void setOnConnectionChangedListener(
      OnConnectionChangedListener onConnectionChangedListener) {
    this.onConnectionChangedListener = onConnectionChangedListener;
  }

  public void setOnStateChangedListener(OnStateChangedListener onStateChangedListener) {
    this.onStateChangedListener = onStateChangedListener;
  }

  public void setOnMessageReceived(OnMessageReceived onMessageReceived) {
    this.onMessageReceived = onMessageReceived;
  }

  private void setState(int mState) {
    this.mState = mState;
    if (onStateChangedListener != null) {
      onStateChangedListener.onChanged(mState);
    }
  }

  @Override public BtDevice getConnectedDevice() {
    return connectedDevice;
  }

  private final Handler mHandler = new Handler() {
    @Override public void handleMessage(Message msg) {
      super.handleMessage(msg);
      switch (msg.what) {
        case HandlerKeys.STATE:
          setState((Integer) msg.obj);
          break;
        case HandlerKeys.CONNECTED:
          mConnectThread = null;
          ConnectThread.ConnectedBundle connectedBundle = (ConnectThread.ConnectedBundle) msg.obj;
          conneceted(connectedBundle.socket);
          if (onConnectionChangedListener != null) {
            onConnectionChangedListener.onConnected(new BtDevice(connectedBundle.device));
          }
          break;
        case HandlerKeys.CONNECTION_FAILED:
          mState = STATE_NONE;
          if (onConnectionChangedListener != null) {
            onConnectionChangedListener.onConnectionFailed();
          }
          break;
        case HandlerKeys.CONNECTION_LOST:
          if (onConnectionChangedListener != null) {
            onConnectionChangedListener.onDisconnected();
          }
          break;
        case HandlerKeys.MESSAGE_RECEIVED:
          if (onMessageReceived != null) {
            onMessageReceived.onReceived((byte[]) msg.obj);
          }
          break;
      }
    }
  };
}
