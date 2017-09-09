package com.tmaproject.tarekkma.bluetoothservice;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.tmaproject.tarekkma.easybluetooth.EasyBluetooth;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnBluetoothEnableChangedListener;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnConnectionStateChangeListener;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnDeviceConnectedListener;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnMessageReceivedListener;

import static com.tmaproject.tarekkma.easybluetooth.BluetoothStates.*;

public class MainActivity extends AppCompatActivity {

  private static final int REQUEST_BT_DISCOVER = 99;

  private EasyBluetooth easyBluetooth;

  EditText input;
  TextView terminal;
  Button connect;
  Button send;

  private BluetoothDevice connectedDevice;

  private OnBluetoothEnableChangedListener bluetoothEnableChangedListener =
      new OnBluetoothEnableChangedListener() {
        @Override public void changed(int state) {
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

  private OnConnectionStateChangeListener connectionStateChangeListener = new OnConnectionStateChangeListener() {
    @Override public void connectionStateChanged(int state) {
      switch (state) {
        case STATE_NONE:
          break;
        case STATE_CONNECTION_FAILED:
          appendToTerminal("Connection Failed !");
          break;
        case STATE_CONNECTING:
          appendToTerminal("Connecting...");
          break;
        case STATE_CONNECTED:
          appendToTerminal("Connected");
          break;
        case STATE_DISCONNECTED:
          appendToTerminal("Disconnected !");
          break;
      }
    }
  };

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    easyBluetooth = new EasyBluetooth(this);

    input = (EditText) findViewById(R.id.input);
    terminal = (TextView) findViewById(R.id.terminal_text);
    connect = (Button) findViewById(R.id.connect);
    send = (Button) findViewById(R.id.send);

    connect.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        startActivityForResult(new Intent(MainActivity.this,ListActivity.class),REQUEST_BT_DISCOVER);
      }
    });

  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if(requestCode == REQUEST_BT_DISCOVER){
      if(resultCode == RESULT_OK){
        String mac = data.getStringExtra(ListActivity.EXTRA_DEVICE_ADDRESS);
        String name = data.getStringExtra(ListActivity.EXTRA_DEVICE_NAME);
        if(mac!=null){
          easyBluetooth.connect(mac);
          appendToTerminal("Trying to connect to " + name + " ["+mac+"]");
        }
      }
    }
  }

  private void appendToTerminal(String s){
    terminal.append("\n"+s);
  }

  @Override protected void onPause() {
    super.onPause();

    easyBluetooth.stop();
  }

  @Override protected void onResume() {
    super.onResume();

    easyBluetooth.setOnBluetoothEnableChangedListener(bluetoothEnableChangedListener);
    easyBluetooth.setOnConnectionStateChangedListener(connectionStateChangeListener);

    easyBluetooth.setDeviceConnectedListener(new OnDeviceConnectedListener() {
      @Override public void connected(BluetoothDevice device) {
        appendToTerminal("Connected to " + device.getName() + " ["+device.getAddress()+"]");
        connectedDevice = device;
      }
    });
    easyBluetooth.setOnConnectionStateChangedListener(new OnConnectionStateChangeListener() {
      @Override public void connectionStateChanged(int state) {
        appendToTerminal("Connection Lost !");
        connectedDevice = null;
      }
    });
    easyBluetooth.setMessageReceivedListener(new OnMessageReceivedListener() {
      @Override public void messageReceived(String string) {
        appendToTerminal(connectedDevice.getName() + " : " + string);
      }
    });
  }
}
