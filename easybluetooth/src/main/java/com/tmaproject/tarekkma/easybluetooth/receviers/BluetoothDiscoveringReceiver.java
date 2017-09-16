package com.tmaproject.tarekkma.easybluetooth.receviers;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.tmaproject.tarekkma.easybluetooth.BtDevice;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnDiscoveringListener;

import static android.bluetooth.BluetoothDevice.ACTION_FOUND;
import static android.bluetooth.BluetoothDevice.ACTION_NAME_CHANGED;

/**
 * Created by tarekkma on 9/15/17.
 */

public class BluetoothDiscoveringReceiver extends BroadcastReceiver {

  public static IntentFilter getIntentFilter(){
    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    filter.addAction(ACTION_NAME_CHANGED);
    return filter;
  }

  private OnDiscoveringListener deviceDiscoverListener;

  public BluetoothDiscoveringReceiver(OnDiscoveringListener deviceDiscoverListener) {
    this.deviceDiscoverListener = deviceDiscoverListener;
  }

  @Override public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    if (ACTION_FOUND.equals(action) || ACTION_NAME_CHANGED.equals(action)) {
      BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
      deviceDiscoverListener.onDiscovered(new BtDevice(device));
    }
  }
}
