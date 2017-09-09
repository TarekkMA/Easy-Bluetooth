package com.tmaproject.tarekkma.easybluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnBluetoothEnableChangedListener;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnDeviceDiscoverListener;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnConnectionStateChangeListener;

/**
 * Created by tarekkma on 9/9/17.
 */

public interface IEasyBluetooth {

  void stop();

  int getState();
  void setOnConnectionStateChangedListener(OnConnectionStateChangeListener listener);

  boolean isBluetoothSupported();
  boolean isBluetoothEnabled();

  void requestEnableBluetooth(Activity activity);

  void setOnBluetoothEnableChangedListener(OnBluetoothEnableChangedListener listener);

  void startDiscovery();
  void stopDiscovery();
  void setOnDiceDiscoverListener(OnDeviceDiscoverListener listener);

  void connect(BluetoothDevice device);
  void connect(String macAddress);

  void write(String string);
  void writeln(String string);

}
