package com.tmaproject.tarekkma.easybluetooth;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnDiscoveringListener;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnEnableChangedListener;
import java.util.List;

/**
 * Created by tarekkma on 9/9/17.
 */

public interface IEasyBluetooth {

  void stop();


  boolean isSupported();
  boolean isEnabled();

  String getMyDeviceName();
  String getMyDeviceAddress();

  void requestEnableWithResults(Activity activity);
  void requestEnable(Context context);

  List<BtDevice> getPairedDevices();

  BroadcastReceiver registerDiscoingReceiver(Context context,OnDiscoveringListener listener);
  BroadcastReceiver registerEnableStateReciver(Context context,OnEnableChangedListener listener);

  void connect(String address);
  void connect(BtDevice btDevice);

  void write(String string);
  void write(byte[] bytes);

  boolean isConnected();
  BtDevice getConnectedDevice();


}
