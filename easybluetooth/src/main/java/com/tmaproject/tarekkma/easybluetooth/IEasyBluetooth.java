package com.tmaproject.tarekkma.easybluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnDiscoveringListener;
import java.util.List;

/**
 * Created by tarekkma on 9/9/17.
 */

public interface IEasyBluetooth {

  void start();
  void stop();


  boolean isSupported();
  boolean isEnabled();

  String getMyDeviceName();
  String getMyDeviceAddress();

  void requestEnable(Context context);

  List<BtDevice> getPairedDevices();

  void connect(String address);
  void connect(BtDevice btDevice);

  void write(String string);
  void write(byte[] bytes);

  boolean isConnected();
  BtDevice getConnectedDevice();


}
