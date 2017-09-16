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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TwoLineListItem;
import com.tmaproject.tarekkma.easybluetooth.BtDevice;
import com.tmaproject.tarekkma.easybluetooth.EasyBluetooth;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnDiscoveringListener;
import com.tmaproject.tarekkma.easybluetooth.listeners.OnEnableChangedListener;
import com.tmaproject.tarekkma.easybluetooth.receviers.BluetoothDiscoveringReceiver;
import com.tmaproject.tarekkma.easybluetooth.receviers.BluetoothEnableStateReceiver;
import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {

  public static final String EXTRA_DEVICE_ADDRESS = "EXTRA_DEVICE_ADDRESS";
  public static final String EXTRA_DEVICE_NAME = "EXTRA_DEVICE_NAME";

  EasyBluetooth easyBluetooth;

  private TextView statusTextView;
  private ListView listView;
  private ProgressBar progressBar;

  private List<BtDevice> deviceList = new ArrayList<>();
  private ArrayAdapter<BtDevice> arrayAdapter;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_list);

    easyBluetooth = new EasyBluetooth();

    statusTextView = (TextView) findViewById(R.id.status_text);
    listView = (ListView) findViewById(R.id.device_list);
    progressBar = (ProgressBar) findViewById(R.id.progress);

    arrayAdapter = new ArrayAdapter<BtDevice>(this, android.R.layout.simple_expandable_list_item_2,
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
        BtDevice selectedDevice = deviceList.get(position);
        intent.putExtra(EXTRA_DEVICE_ADDRESS, selectedDevice.getAddress());
        intent.putExtra(EXTRA_DEVICE_NAME, selectedDevice.getName());
        setResult(RESULT_OK, intent);
        easyBluetooth.stop();
        finish();
      }
    });

    if (!easyBluetooth.isSupported()) {
      statusTextView.setText("Bluetooth is not supported");
      return;
    }

    if (easyBluetooth.isEnabled()) {
      startDiscovery();
    }else {
      statusTextView.setText("Please Enable your bluetooth");
    }

    BluetoothEnableStateReceiver enableStateReceiver =
        new BluetoothEnableStateReceiver(new OnEnableChangedListener() {
          @Override public void onChanged(int state) {
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
        });
    registerReceiver(enableStateReceiver, BluetoothDiscoveringReceiver.getIntentFilter());
  }

  private void startDiscovery() {
    BluetoothDiscoveringReceiver discoveringReceiver =
        new BluetoothDiscoveringReceiver(new OnDiscoveringListener() {
          @Override public void onDiscovered(BtDevice device) {
            arrayAdapter.add(device);
          }

          @Override public void onDiscoveredFinished() {
            progressBar.setVisibility(View.GONE);
            statusTextView.setText("Finished Searching");
          }
        });
    registerReceiver(discoveringReceiver, BluetoothDiscoveringReceiver.getIntentFilter());
  }

  @Override protected void onDestroy() {
    super.onDestroy();
  }

  @Override protected void onResume() {
    super.onResume();
  }

  @Override protected void onPause() {
    super.onPause();
  }
}
