package com.tmaproject.tarekkma.easybluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnBluetoothEnableChangedListener;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnConnectionLostListener;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnDeviceConnectedListener;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnDeviceDiscoverListener;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnConnectionStateChangeListener;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnMessageReceivedListener;

import static android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED;
import static android.bluetooth.BluetoothAdapter.EXTRA_STATE;
import static android.bluetooth.BluetoothDevice.ACTION_NAME_CHANGED;

/**
 * Created by tarekkma on 9/9/17.
 */

/**
 * https://github.com/googlesamples/android-BluetoothChat/blob/master/Application/src/main/java/com/example/android/bluetoothchat/BluetoothChatService.java
 */

public class EasyBluetooth implements IEasyBluetooth {
  private static final String TAG = "EasyBluetooth";

  /**
   * UUID for communicating with SerialDevices
   *
   * @see <a href="https://developer.android.com/reference/android/bluetooth/BluetoothDevice.html#createRfcommSocketToServiceRecord(java.util.UUID)">createRfcommSocketToServiceRecord(java.util.UUID)</a>
   */
  public static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
  private static final int REQUEST_ENABLE_BT = 912;

  private BluetoothAdapter bluetoothAdapter;
  private Context context;
  private int state;

  private ConnectThread connectThread = null;
  private ConnectedThread connectedThread = null;

  private OnBluetoothEnableChangedListener stateChangedListener;
  private OnDeviceDiscoverListener deviceDiscoverListener;
  private OnConnectionStateChangeListener connectionStateChangeListener;
  private OnDeviceConnectedListener deviceConnectedListener;
  private OnMessageReceivedListener messageReceivedListener;
  private OnConnectionLostListener connectionLostListener;

  private final BroadcastReceiver stateChangedReciver = new BroadcastReceiver() {
    @Override public void onReceive(Context context, Intent intent) {
      if (ACTION_STATE_CHANGED.equals(intent.getAction())) {
        if (stateChangedListener != null) {
          int state = intent.getIntExtra(EXTRA_STATE, -1);
          stateChangedListener.changed(state);
        }
      }
    }
  };

  private final BroadcastReceiver deviceDiscoverReceiver = new BroadcastReceiver() {
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (BluetoothDevice.ACTION_FOUND.equals(action) || ACTION_NAME_CHANGED.equals(action)) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        deviceDiscoverListener.discoverd(device);
      }
    }
  };

  public EasyBluetooth(Context context) {
    this.context = context;
    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    setState(BluetoothStates.STATE_NONE);
  }

  private synchronized void setState(int state) {
    this.state = state;
    if (connectionStateChangeListener != null) {
      connectionStateChangeListener.connectionStateChanged(state);
    }
  }

  @Override public int getState() {
    return state;
  }

  @Override
  public void setOnConnectionStateChangedListener(OnConnectionStateChangeListener listener) {
    connectionStateChangeListener = listener;
  }

  @Override public boolean isBluetoothSupported() {
    return bluetoothAdapter != null;
  }

  @Override public boolean isBluetoothEnabled() {
    return bluetoothAdapter.isEnabled();
  }

  @Override
  public void setOnBluetoothEnableChangedListener(OnBluetoothEnableChangedListener listener) {
    stateChangedListener = listener;
    IntentFilter filter = new IntentFilter(ACTION_STATE_CHANGED);
    context.registerReceiver(stateChangedReciver, filter);
  }

  public void setDeviceConnectedListener(OnDeviceConnectedListener deviceConnectedListener) {
    this.deviceConnectedListener = deviceConnectedListener;
  }

  public void setConnectionLostListener(OnConnectionLostListener connectionLostListener) {
    this.connectionLostListener = connectionLostListener;
  }

  public void setMessageReceivedListener(OnMessageReceivedListener messageReceivedListener) {
    this.messageReceivedListener = messageReceivedListener;
  }

  @Override public void requestEnableBluetooth(Activity activity) {
    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
  }

  @Override public void startDiscovery() {
    if (!bluetoothAdapter.isDiscovering()) {
      bluetoothAdapter.startDiscovery();
    }
    // ACTION_FOUND need ACCESS_COARSE_LOCATION permission
    // ACTION_NAME_CHANGED may do the same as ACTION_FOUND without permission
    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    filter.addAction(ACTION_NAME_CHANGED);
    context.registerReceiver(deviceDiscoverReceiver, filter);
  }

  @Override public void stopDiscovery() {
    if (bluetoothAdapter.isDiscovering()) {
      bluetoothAdapter.cancelDiscovery();
    }
    context.unregisterReceiver(deviceDiscoverReceiver);
  }

  @Override public void setOnDiceDiscoverListener(OnDeviceDiscoverListener listener) {
    deviceDiscoverListener = listener;
  }

  @Override public void stop() {
    context.unregisterReceiver(stateChangedReciver);
    stopDiscovery();
  }

  @Override public void connect(BluetoothDevice device) {

    Log.d(TAG,
        "Trying to connect to device name :" + device.getName() + ", MAC :" + device.getAddress());

    // Cancel any thread attempting to make a connection
    if (getState() == BluetoothStates.STATE_CONNECTING && connectThread != null) {
      connectThread.cancel();
      connectThread = null;
      Log.d(TAG, "connect: Canceled connectThread");
    }

    // Cancel any thread currently running a connection
    if (connectedThread != null) {
      connectedThread.cancel();
      connectedThread = null;
      Log.d(TAG, "connect: Canceled connectedThread");
    }

    // Setup the thread to connect with the given device
    connectThread = new ConnectThread(SPP_UUID, device, bluetoothAdapter);

    connectThread.setStateChangeListener(new OnConnectionStateChangeListener() {
      @Override public void connectionStateChanged(int state) {
        setState(state);
      }
    });

    connectThread.setConnectedListener(new ConnectThread.ConnectedListener() {
      @Override public void onConnected(BluetoothDevice device, BluetoothSocket socket) {
        Log.d(TAG,
            "Connected to device name :" + device.getName() + ", MAC :" + device.getAddress());
        setState(BluetoothStates.STATE_CONNECTED);
        connected(socket,device);
      }
    });

    connectThread.setConnectionFailedListener(new ConnectThread.ConnectionFailedListener() {
      @Override public void connectionFailed(Exception e) {
        Log.e(TAG, "connectionFailed: ", e);
        setState(BluetoothStates.STATE_CONNECTION_FAILED);
      }
    });
    // Start connect thread
    connectThread.start();
  }

  @Override public void connect(String macAddress) {
    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);
    connect(device);
  }

  public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
    // Cancel the thread that completed the connection
    if (connectThread != null) {
      connectThread.cancel();
      connectThread = null;
    }

    // Cancel any thread currently running a connection
    if (connectedThread != null) {
      connectedThread.disconnect();
      connectedThread.cancel();
      connectedThread = null;
    }

    // Start the thread to manage the connection and perform transmissions
    connectedThread = new ConnectedThread(socket);

    connectedThread.setConnectionStateChangeListener(new OnConnectionStateChangeListener() {
      @Override public void connectionStateChanged(int state) {
        setState(state);
      }
    });

    connectedThread.setConnectionLostListener(new OnConnectionLostListener() {
      @Override public void connectionLost() {
        setState(BluetoothStates.STATE_DISCONNECTED);
        if (connectionLostListener != null) {
          connectionLostListener.connectionLost();
        }
      }
    });

    connectedThread.setMessageReceivedListener(new OnMessageReceivedListener() {
      @Override public void messageReceived(String string) {
        if(messageReceivedListener != null){
          messageReceivedListener.messageReceived(string);
        }
      }
    });

    connectedThread.start();

    // Send the name of the onConnected device back to the UI Activity
    if (deviceConnectedListener != null) {
      deviceConnectedListener.connected(device);
    }
  }

  @Override public void write(String string) {
    if (connectedThread != null) {
      connectedThread.write(string.getBytes());
    }
  }

  @Override public void writeln(String string) {
    write(string + "\r\n");
  }
}
