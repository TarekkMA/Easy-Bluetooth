package com.tmaproject.tarekkma.easybluetooth.receviers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnEnableChangedListener;

import static android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED;
import static android.bluetooth.BluetoothAdapter.EXTRA_STATE;

/**
 * Created by tarekkma on 9/15/17.
 */

public class BluetoothEnableStateReceiver extends BroadcastReceiver {

  public static IntentFilter getIntentFilter(){
    return new IntentFilter(ACTION_STATE_CHANGED);
  }

  private OnEnableChangedListener stateChangedListener;

  public BluetoothEnableStateReceiver(OnEnableChangedListener stateChangedListener) {
    this.stateChangedListener = stateChangedListener;
  }

  @Override public void onReceive(Context context, Intent intent) {
    if (ACTION_STATE_CHANGED.equals(intent.getAction())) {
      int state = intent.getIntExtra(EXTRA_STATE, -1);
      stateChangedListener.onChanged(state);
    }
  }
}
