package com.tmaproject.tarekkma.easybluetooth;

import android.bluetooth.BluetoothDevice;

/**
 * Created by tarekkma on 9/15/17.
 */

public class BtDevice {
  private BluetoothDevice device;

  public BtDevice(BluetoothDevice device) {
    this.device = device;
  }

  public String getName(){
    return device.getName();
  }

  public String getAddress(){
    return device.getAddress();
  }

  public BluetoothDevice getDevice() {
    return device;
  }
}
