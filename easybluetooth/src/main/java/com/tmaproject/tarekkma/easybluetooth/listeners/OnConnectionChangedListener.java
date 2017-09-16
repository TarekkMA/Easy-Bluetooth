package com.tmaproject.tarekkma.easybluetooth.listeners;

import com.tmaproject.tarekkma.easybluetooth.BtDevice;

/**
 * Created by tarekkma on 9/16/17.
 */

public interface OnConnectionChangedListener {
  void onConnected(BtDevice device);
  void onDisconnected();
  void onConnectionFailed();
}
