package com.tmaproject.tarekkma.easybluetooth;

/**
 * Created by tarekkma on 9/16/17.
 */

public interface States {
  // Constants that indicate the current connection state
  int STATE_NONE = 0;       // we're doing nothing
  int STATE_LISTEN = 1;     // now listening for incoming connections
  int STATE_CONNECTING = 2; // now initiating an outgoing connection
  int STATE_CONNECTED = 3;  // now connected to a remote device
}
