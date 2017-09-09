package com.tmaproject.tarekkma.easybluetooth;

import android.bluetooth.BluetoothAdapter;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by tarekkma on 9/9/17.
 */

public class BluetoothStates {


  public static final int STATE_NONE              = 0;  // we're doing nothing
  public static final int STATE_CONNECTING        = 2;  // now initiating an outgoing connection
  public static final int STATE_CONNECTED         = 3;  // now onConnected to a remote device
  public static final int STATE_CONNECTION_FAILED = 4;  // connection failed
  public static final int STATE_DISCONNECTED      = 5;  // disconnected from a onConnected device

}
