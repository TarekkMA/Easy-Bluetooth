package com.tmaproject.tarekkma.bluetoothservice;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TwoLineListItem;
import com.tmaproject.tarekkma.easybluetooth.BluetoothStates;
import com.tmaproject.tarekkma.easybluetooth.EasyBluetooth;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnBluetoothEnableChangedListener;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnDeviceDiscoverListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ListActivity extends AppCompatActivity {

  public static final String EXTRA_DEVICE_ADDRESS = "EXTRA_DEVICE_ADDRESS";
  public static final String EXTRA_DEVICE_NAME = "EXTRA_DEVICE_NAME";
  private EasyBluetooth easyBluetooth;
  private TextView statusTextView;
  private ListView listView;

  private List<BluetoothDevice> deviceList = new ArrayList<>();
  private ArrayAdapter<BluetoothDevice> arrayAdapter;

  private OnDeviceDiscoverListener deviceDiscoverListener = new OnDeviceDiscoverListener() {
    @Override public void discoverd(BluetoothDevice device) {
      arrayAdapter.add(device);
    }
  };

  private OnBluetoothEnableChangedListener bluetoothEnableChangedListener =
      new OnBluetoothEnableChangedListener() {
        @Override public void changed(int state) {
          switch (state) {
            case BluetoothAdapter.STATE_ON:
              statusTextView.setText("Bluetooth is now on");
              startDiscovery();
              break;
            case BluetoothAdapter.STATE_TURNING_ON:
              statusTextView.setText("Bluetooth is turing on");
              break;
            case BluetoothAdapter.STATE_OFF:
              statusTextView.setText("Bluetooth is now off");
              break;
            case BluetoothAdapter.STATE_TURNING_OFF:
              statusTextView.setText("Bluetooth is turing off");
              break;
          }
        }
      };

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_list);

    easyBluetooth = new EasyBluetooth(this);

    statusTextView = (TextView) findViewById(R.id.status_text);
    listView = (ListView) findViewById(R.id.device_list);

    arrayAdapter =
        new ArrayAdapter<BluetoothDevice>(this, android.R.layout.simple_expandable_list_item_2,
            android.R.id.text1, deviceList) {
          @NonNull @Override
          public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            TwoLineListItem view = (TwoLineListItem) super.getView(position, convertView, parent);

            TextView name = view.findViewById(android.R.id.text1);
            TextView mac = view.findViewById(android.R.id.text2);

            name.setText(deviceList.get(position).getName());
            mac.setText(deviceList.get(position).getAddress());

            return view;
          }
        };

    listView.setAdapter(arrayAdapter);

    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        BluetoothDevice selectedDevice = deviceList.get(position);
        intent.putExtra(EXTRA_DEVICE_ADDRESS, selectedDevice.getAddress());
        intent.putExtra(EXTRA_DEVICE_NAME, selectedDevice.getName());
        setResult(RESULT_OK, intent);
        finish();
      }
    });

    if (!easyBluetooth.isBluetoothSupported()) {
      statusTextView.setText("Bluetooth is not supported");
      return;
    }

    easyBluetooth.setOnBluetoothEnableChangedListener(bluetoothEnableChangedListener);
    easyBluetooth.setOnDiceDiscoverListener(deviceDiscoverListener);

    if (easyBluetooth.isBluetoothEnabled()) {
      startDiscovery();
    } else {
      easyBluetooth.requestEnableBluetooth(this);
    }
  }

  private void startDiscovery() {
    statusTextView.setText("Searching for deceives");
    easyBluetooth.startDiscovery();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    easyBluetooth.stop();
  }
}
