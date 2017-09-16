package com.tmaproject.tarekkma.easybluetooth.listeners;

import com.tmaproject.tarekkma.easybluetooth.BtDevice;

/**
 * Created by tarekkma on 9/15/17.
 */

public interface OnDiscoveringListener {
  void onDiscovered(BtDevice device);
  void onDiscoveredFinished();
}
