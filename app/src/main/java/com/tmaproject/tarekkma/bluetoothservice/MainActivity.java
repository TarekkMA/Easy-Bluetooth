package com.tmaproject.tarekkma.bluetoothservice;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.tmaproject.tarekkma.easybluetooth.BtDevice;
import com.tmaproject.tarekkma.easybluetooth.EasyBluetooth;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnConnectionChangedListener;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnEnableChangedListener;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnMessageReceived;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnStateChangedListener;
import com.tmaproject.tarekkma.easybluetooth.receviers.BluetoothDiscoveringReceiver;
import com.tmaproject.tarekkma.easybluetooth.receviers.BluetoothEnableStateReceiver;

import static com.tmaproject.tarekkma.easybluetooth.States.*;

public class MainActivity extends AppCompatActivity {

  private static final int REQUEST_BT_DISCOVER = 99;

  private EasyBluetooth easyBluetooth;

  EditText input;
  TextView terminal;
  Button connect;
  Button send;

  private BtDevice connectedDevice;

  private OnEnableChangedListener onEnableChangedListener = new OnEnableChangedListener() {
    @Override public void onChanged(int state) {
      switch (state) {
        case BluetoothAdapter.STATE_ON:
          appendToTerminal("Bluetooth is now on");
          break;
        case BluetoothAdapter.STATE_TURNING_ON:
          appendToTerminal("Bluetooth is turing on");
          break;
        case BluetoothAdapter.STATE_OFF:
          appendToTerminal("Bluetooth is now off");
          break;
        case BluetoothAdapter.STATE_TURNING_OFF:
          appendToTerminal("Bluetooth is turing off");
          break;
      }
    }
  };

  private OnStateChangedListener onStateChangedListener = new OnStateChangedListener() {
    @Override public void onChanged(int state) {
      switch (state) {
        case STATE_NONE:
          break;
        case STATE_CONNECTING:
          appendToTerminal("Connecting...");
          break;
        case STATE_CONNECTED:
          appendToTerminal("Connected");
          break;
      }
    }
  };

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    easyBluetooth = new EasyBluetooth();

    input = (EditText) findViewById(R.id.input);
    terminal = (TextView) findViewById(R.id.terminal_text);
    connect = (Button) findViewById(R.id.connect);
    send = (Button) findViewById(R.id.send);

    connect.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        startActivityForResult(new Intent(MainActivity.this, ListActivity.class),
            REQUEST_BT_DISCOVER);
      }
    });

    send.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if(!TextUtils.isEmpty(input.getText())){
          easyBluetooth.write(input.getText().toString());
          input.setText("");
        }
      }
    });
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_BT_DISCOVER) {
      if (resultCode == RESULT_OK) {
        String mac = data.getStringExtra(ListActivity.EXTRA_DEVICE_ADDRESS);
        String name = data.getStringExtra(ListActivity.EXTRA_DEVICE_NAME);
        if (mac != null) {
          easyBluetooth.connect(mac);
          appendToTerminal("Trying to connect to " + name + " [" + mac + "]");
        }
      }
    }
  }

  private void appendToTerminal(String s) {
    terminal.append("\n" + s);
  }

  @Override protected void onPause() {
    super.onPause();

    easyBluetooth.stop();
  }

  @Override protected void onResume() {
    super.onResume();

    BluetoothEnableStateReceiver enableStateReceiver = new BluetoothEnableStateReceiver(onEnableChangedListener);
    registerReceiver(enableStateReceiver, BluetoothDiscoveringReceiver.getIntentFilter());

    easyBluetooth.setOnConnectionChangedListener(new OnConnectionChangedListener() {
      @Override public void onConnected(BtDevice device) {
        appendToTerminal("Connected to " + device.getName() + " [" + device.getAddress() + "]");
        connectedDevice = device;
      }

      @Override public void onDisconnected() {
        appendToTerminal("Connection Lost !");
        connectedDevice = null;
      }

      @Override public void onConnectionFailed() {
        appendToTerminal("Connection Failed !");
      }
    });

    easyBluetooth.setOnMessageReceived(new OnMessageReceived() {
      @Override public void onReceived(byte[] message) {
        appendToTerminal(connectedDevice.getName() + " : " + new String(message));
      }
    });
  }
}
