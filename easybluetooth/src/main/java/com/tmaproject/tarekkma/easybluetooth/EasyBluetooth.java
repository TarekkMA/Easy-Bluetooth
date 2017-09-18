package com.tmaproject.tarekkma.easybluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnConnectionChangedListener;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnDiscoveringListener;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnEnableChangedListener;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnMessageReceived;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnStateChangedListener;
import com.tmaproject.tarekkma.easybluetooth.receviers.BluetoothDiscoveringReceiver;
import com.tmaproject.tarekkma.easybluetooth.receviers.BluetoothEnableStateReceiver;
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

  public static final int REQUEST_ENABLE_BT = 7676;
  private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
  private OnConnectionChangedListener onConnectionChangedListener;
  private OnStateChangedListener onStateChangedListener;
  private OnMessageReceived onMessageReceived;
  private BtDevice connectedDevice = null;
  private ConnectedThread mConnectedThread;
  private ConnectThread mConnectThread;
  private int mState;
  private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

  /**
   * Stopping all threads
   */
  @Override public void stop() {
    if (mConnectThread != null) {
      mConnectThread.cancel();
      mConnectThread = null;
    }

    if (mConnectedThread != null) {
      mConnectedThread.cancel();
      mConnectedThread = null;
    }

    setState(STATE_NONE);
  }

  /**
   * Checks if bluetooth is supported or not
   * @return is bluetooth supported
   */
  @Override public boolean isSupported() {
    return bluetoothAdapter != null;
  }

  /**
   * Checks if bluetooth is enabled or not
   * @return is bluetooth enabled
   */
  @Override public boolean isEnabled() {
    return bluetoothAdapter.isEnabled();
  }

  /**
   * Requests from user to enable bluetooth and returning result to the activity
   * @param activity the activity which will have results
   */
  @Override public void requestEnableWithResults(Activity activity) {
    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
  }

  /**
   * request from user to enable bluetooth
   * @param context context that will start request activity
   */
  @Override public void requestEnable(Context context) {
    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    context.startActivity(enableBtIntent);
  }

  /**
   * Register Broadcast Receiver for new discovered devices
   * @param context context to register on
   * @param listener notify listeners to events like when device discovered
   * @return the registered BroadcastReceiver , remember to unregister it using unregisterReceiver(receiver);
   */
  @Override public BroadcastReceiver registerDiscoingReceiver(Context context,OnDiscoveringListener listener) {
    BluetoothDiscoveringReceiver receiver = new BluetoothDiscoveringReceiver(listener);
    context.registerReceiver(receiver,BluetoothDiscoveringReceiver.getIntentFilter());
    return receiver;
  }

  /**
   * Register Broadcast Receiver for listening to bluetooth enable state
   * @param context context to register on
   * @param listener notify listeners to events
   * @return the registered BroadcastReceiver , remember to unregister it using unregisterReceiver(receiver);
   */
  @Override public BroadcastReceiver registerEnableStateReciver(Context context,
      OnEnableChangedListener listener) {
    BluetoothEnableStateReceiver receiver = new BluetoothEnableStateReceiver(listener);
    context.registerReceiver(receiver,BluetoothEnableStateReceiver.getIntentFilter());
    return receiver;
  }

  /**
   * Gets current device name
   * @return device name
   */
  @Override public String getMyDeviceName() {
    return bluetoothAdapter.getName();
  }

  /**
   * Gets current device MAC address
   * @return device MAC address
   */
  @Override public String getMyDeviceAddress() {
    return bluetoothAdapter.getAddress();
  }

  /**
   * Gets list of paired ,bonded, devices
   * @return List of paired devices
   */
  @Override public List<BtDevice> getPairedDevices() {
    List<BtDevice> paired = new ArrayList<>();
    Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
    for (BluetoothDevice device : bondedDevices) {
      paired.add(new BtDevice(device));
    }
    return paired;
  }

  /**
   * Connect to device using it's MAC address
   * @param address MAC Address
   */
  @Override public void connect(String address) {
    connect(new BtDevice(bluetoothAdapter.getRemoteDevice(address)));
  }

  /**
   * Connect to BtDevice
   * @param btDevice
   */
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

  /**
   * Gets connected status
   * @return connected status
   */
  @Override public boolean isConnected() {
    return connectedDevice != null;
  }

  private void connected(BluetoothSocket socket){

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

  /**
   * Send data to connected device
   * @param bytes
   */
  @Override public void write(byte[] bytes) {
    if (mState != STATE_CONNECTED) return;
    mConnectedThread.write(bytes);
  }

  /**
   * Send data to connected device
   * @param string
   */
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
          connected(connectedBundle.socket);
          connectedDevice = new BtDevice(connectedBundle.device);
          if (onConnectionChangedListener != null) {
            onConnectionChangedListener.onConnected(connectedDevice);
          }
          break;
        case HandlerKeys.CONNECTION_FAILED:
          mState = STATE_NONE;
          if (onConnectionChangedListener != null) {
            onConnectionChangedListener.onConnectionFailed();
          }
          break;
        case HandlerKeys.CONNECTION_LOST:
          connectedDevice = null;
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
