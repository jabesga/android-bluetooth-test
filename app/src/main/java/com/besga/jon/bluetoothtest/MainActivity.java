package com.besga.jon.bluetoothtest;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Set;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "DEBUG";
    private final static String DEVICE_MAC = "1C:52:16:EC:19:E4";
    private final static UUID my_uuid = UUID.fromString("00000000-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice foundDevice;

    private final static int REQUEST_ENABLE_BT = 1;
    private final static int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1;

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, device.getName() + " | " + device.getAddress());
                if(device.getAddress().equals(DEVICE_MAC)){
                    Log.d(TAG, "Device found");
                    foundDevice = device;
                    tvDiscoverDevices.setText("Device found");
                    tvDiscoverDevices.setTextColor(Color.GREEN);
                    mBluetoothAdapter.cancelDiscovery();
                }
            }
        }
    };

    private final BroadcastReceiver mReceiverDiscoveryStarted = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (mBluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d(TAG, "Discovery started");
            }
        }
    };

    private final BroadcastReceiver mReceiverDiscoveryFinished = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (mBluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "Discovery finished");
                mBluetoothAdapter.cancelDiscovery();
                Log.d(TAG, "Discovery canceled");
            }
        }
    };


    private void askForDangerousPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION
                },
                REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
        }
    }

    private void askForEnableBluetooth(){
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    private  void discoverDevices(){
        mBluetoothAdapter.startDiscovery();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                updateBluetoothState();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_ACCESS_COARSE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    tvGrantPermissions.setText("Permissions granted");
                    tvGrantPermissions.setTextColor(Color.GREEN);
                }
                else{
                    tvGrantPermissions.setText("Permissions denied");
                    tvGrantPermissions.setTextColor(Color.RED);
                }
            }
        }
    }

    TextView tvGrantPermissions;
    TextView tvEnableBluetooth;
    TextView tvDiscoverDevices;
    TextView tvPairedDevices;
    TextView tvConnectToDevice;
    Button btnGrantPermissions;
    Button btnEnableBluetooth;
    Button btnDiscoverDevices;

    private void lookPairedDevices(){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                Log.d(TAG, device.getAddress());
                if(device.getAddress().equals(DEVICE_MAC)) {
                    Log.d(TAG, "Device found");
                    foundDevice = device;
                    tvPairedDevices.setText("Device found in paired devices");
                    tvPairedDevices.setTextColor(Color.GREEN);
                }
            }
        }
    }

    private void connectToDevice(){
        if(foundDevice != null) {
            Log.d(TAG, "Listening...");
            try{
                BluetoothServerSocket mBluetoothServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("BluetoothTest", my_uuid);
                BluetoothSocket mBluetoothSocket = mBluetoothServerSocket.accept();
                Log.d(TAG, mBluetoothSocket.toString());
            }
            catch (Exception e){
                Log.e(TAG, e.toString());
            }

        }
    }
    private void updateBluetoothState(){
        if (!mBluetoothAdapter.isEnabled()) {
            tvEnableBluetooth.setText("Bluetooth disabled.");
            tvEnableBluetooth.setTextColor(Color.RED);
            btnEnableBluetooth.setEnabled(true);
            btnDiscoverDevices.setEnabled(false);
        }
        else{
            tvEnableBluetooth.setText("Bluetooth enabled.");
            tvEnableBluetooth.setTextColor(Color.GREEN);
            btnEnableBluetooth.setEnabled(false);
            btnDiscoverDevices.setEnabled(true);
        }
    }

    private void updatePermissionsState(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            tvGrantPermissions.setText("No granted permissions.");
            tvGrantPermissions.setTextColor(Color.BLUE);
            btnGrantPermissions.setEnabled(true);
        } else{
            tvGrantPermissions.setText("Permissions granted.");
            tvGrantPermissions.setTextColor(Color.GREEN);
            btnGrantPermissions.setEnabled(false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvGrantPermissions = (TextView) findViewById(R.id.tvGrantPermissions);
        btnGrantPermissions = (Button) findViewById(R.id.btnGrantPermissions);
        btnGrantPermissions.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                askForDangerousPermissions();
            }
        });

        tvEnableBluetooth = (TextView) findViewById(R.id.tvEnableBluetooth);
        btnEnableBluetooth = (Button) findViewById(R.id.btnEnableBluetooth);
        btnEnableBluetooth.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                askForEnableBluetooth();
            }
        });

        tvDiscoverDevices = (TextView) findViewById(R.id.tvDiscoverDevices);
        btnDiscoverDevices = (Button) findViewById(R.id.btnDiscoverDevices);
        btnDiscoverDevices.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                discoverDevices();
            }
        });


        tvPairedDevices = (TextView) findViewById(R.id.tvPairedDevices);
        final Button btnPairedDevices = (Button) findViewById(R.id.btnPairedDevices);
        btnPairedDevices.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                lookPairedDevices();
            }
        });

        tvConnectToDevice = (TextView) findViewById(R.id.tvConnectToDevice);
        final Button btnConnectToDevice = (Button) findViewById(R.id.btnConnectToDevice);
        btnConnectToDevice.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                connectToDevice();
            }
        });



        IntentFilter filterStarted = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        IntentFilter filterFinished = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

        registerReceiver(mReceiverDiscoveryStarted, filterStarted); // Don't forget to unregister during onDestroy
        registerReceiver(mReceiverDiscoveryFinished, filterFinished); // Don't forget to unregister during onDestroy
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        updatePermissionsState();
        updateBluetoothState();

    }
}

