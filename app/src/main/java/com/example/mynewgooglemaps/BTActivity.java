package com.example.mynewgooglemaps;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class BTActivity extends AppCompatActivity {

    //ListView listView;
    TextView BTDevices;
    //ArrayList<String> stringArrayList = new ArrayList<>();
    //ArrayAdapter<String> arrayAdapter;
    private static final String TAG = "MainActivity";
    BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 99; // Any positive integer should work.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_b_t);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //listView = findViewById(R.id.listBT);
        BTDevices = findViewById(R.id.textView);


        if(mBluetoothAdapter == null){
            Toast.makeText(this, "Device not support Bluetooth", Toast.LENGTH_LONG).show();
        } else if (!mBluetoothAdapter.isEnabled()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            startActivityForResult(intent,REQUEST_ENABLE_BT);
        }

        scanDevices();

        //arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, stringArrayList);
        //listView.setAdapter(arrayAdapter);
    }

    private void scanDevices() {
        mBluetoothAdapter.startDiscovery();

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Toast.makeText(context,deviceName + "  " + deviceHardwareAddress, Toast.LENGTH_LONG).show();
                //stringArrayList.add(deviceName);
                //arrayAdapter.notifyDataSetChanged();
                BTDevices.append(deviceName + "  " + deviceHardwareAddress + "\n");
            }

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_ENABLE_BT){
            if (resultCode == RESULT_OK){
                Toast.makeText(this, "Bluetooth is enabled", Toast.LENGTH_LONG).show();
                scanDevices();
            } else if (resultCode == RESULT_CANCELED){
                Toast.makeText(this, "Bluetooth enabling cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        unregisterReceiver(receiver);
        //mBluetoothAdapter.cancelDiscovery();
    }


}
