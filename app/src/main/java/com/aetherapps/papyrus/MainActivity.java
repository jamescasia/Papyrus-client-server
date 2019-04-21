package com.aetherapps.papyrus;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.FileObserver;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private FileObserver observer;
    private Button pairBtn;
    private Button sendBtn;
    private Button recvBtn;
    private Activity self;
    private File receiptFile;
    private String invoicesPath;
    private TextView macAddressLabel;
    private BluetoothAdapter myBt;
    private BluetoothServerSocket myServerSocket;
    private EditText macEditText;
    private BluetoothDevice btDevice;
    public static final String appName = "Papyrus-Sender";
    public static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private final IntentFilter intentFilter = new IntentFilter();
    private String btMacAddress;
    private OutputStream out;
    private InputStream in;
    private BroadcastReceiver mReceiver;
    //    private ArrayAdapter<BluetoothDevice> discoveredBtDevices = new ArrayAdapter<BluetoothDevice>();
    private BluetoothSocket mSocket;
    private String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION , Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.INTERNET, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,  Manifest.permission.BLUETOOTH_PRIVILEGED};
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(self, "granted"+grantResults.toString(), Toast.LENGTH_SHORT).show();

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pairBtn = findViewById(R.id.pairBtn);
        sendBtn= findViewById(R.id.send);
        recvBtn= findViewById(R.id.recv);
        macAddressLabel = findViewById(R.id.macAdressLabel);
        macEditText = findViewById(R.id.macEditText);
        btMacAddress = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(), "bluetooth_address");
        macAddressLabel.setText(btMacAddress);
        recvBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                read();
            }
        });
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    write("haha");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        pairBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                connectBluetoothGivenMacAddress(macEditText.getText().toString());
                discoverDevices();

            }
        });
        invoicesPath = android.os.Environment.getExternalStorageDirectory().toString() + "/Papyrus Invoices";
        self = this;
        requestPermissions();
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            Toast.makeText(self, "has permissions", Toast.LENGTH_SHORT).show();
        }
        if (!checkBluetooth() || !checkWifi()) {
            startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            Intent[] enableRequests = {new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), new Intent(Settings.ACTION_WIRELESS_SETTINGS)};
            startActivities(enableRequests);
        }
        initializeBt();

//        trackPath();


    }

    private void trackPath() {
        observer = new FileObserver(invoicesPath) {
            @Override
            public void onEvent(int event, final String file) {
                self.runOnUiThread(new Runnable() {
                    public void run() {
                        receiptFile = new File(invoicesPath + "/" + file);
                        Toast.makeText(getApplicationContext(), receiptFile.getAbsolutePath() + " was saved!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        };
        observer.startWatching(); //START OBSERVING
    }

    private Boolean checkPermissions() {
        Toast.makeText(self, "checking permis", Toast.LENGTH_SHORT).show();
        for (String p : permissions) {
            if ((ActivityCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED)) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);

    }

    private Boolean checkBluetooth() {
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    private Boolean checkWifi() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return (mWifi.isConnected());

    }


    private void initializeBt() {
//        BluetoothConnectionService b = new BluetoothConnectionService(self);
        myBt = BluetoothAdapter.getDefaultAdapter();
//        waitPairRequest();
        try {
            BluetoothServerSocket btSS = myBt.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);
//             mSocket =  btSS.accept();
        } catch (IOException e) {
            Toast.makeText(self, "failed to listen", Toast.LENGTH_SHORT).show();
            System.out.println("failed to listen");
            e.printStackTrace();
        }
//
//        BluetoothServerSocket tmp = null;
//
//        try{
//            tmp = myBt.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);
//
////            Log.d(TAG, "AcceptThread: Setting up Server using: " + MY_UUID_INSECURE);
//
//        }catch (IOException e){
////            Log.e(TAG, "AcceptThread: IOException: " + e.getMessage() );
//            Toast.makeText(self, e.getStackTrace().toString(), Toast.LENGTH_SHORT).show();
//        }
//        myServerSocket = tmp;
////        myServerSocket.
    }

    private void connectBluetoothGivenMacAddress(String address) {
        Toast.makeText(self, "" + address, Toast.LENGTH_SHORT).show();
//        myBt.getBondedDevices()


        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
        BluetoothSocket tmp = null;
        BluetoothSocket mmSocket = null;
        Toast.makeText(self, "connected?; " + device.getName() + device.getAddress(), Toast.LENGTH_SHORT).show();


        try {
            tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
//            tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
            mSocket = tmp;
            mSocket.connect();
            try {
                write("A");
            } catch (IOException e) {
                e.printStackTrace();
            }
            run();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(self, "failed to create rfcommsocket" + e.getStackTrace(), Toast.LENGTH_SHORT).show();
        }


//        try {
//            tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
//            Method m = null;
//            try {
//                m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] {int.class});
//            } catch (NoSuchMethodException e) {
//                e.printStackTrace();
//                Toast.makeText(self, "1st"+e.getStackTrace().toString(), Toast.LENGTH_SHORT).show();
//            }
//            if (m != null) {
//                try {
//                    tmp = (BluetoothSocket) m.invoke(device, 1);
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                    Toast.makeText(self, "2nd"+e.getStackTrace().toString(), Toast.LENGTH_SHORT).show();
//                } catch (InvocationTargetException e) {
//                    e.printStackTrace();
//                    Toast.makeText(self, "3rd"+e.getStackTrace().toString(), Toast.LENGTH_SHORT).show();
//                }
//            }
//        } catch (IOException e) {
//            Toast.makeText(self, "4th"+e.getStackTrace().toString(), Toast.LENGTH_SHORT).show();
////            Log.e(TAG, "create() failed", e);
//        }
//        mmSocket = tmp;


//        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
//        BluetoothSocket tmp = null;
//        BluetoothSocket mmSocket = null;
//
//// Get a BluetoothSocket for a connection with the
//// given BluetoothDevice
//        try {
//            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
//            Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
//            tmp = (BluetoothSocket) m.invoke(device, 1);
//        } catch (IOException e) {
//            Log.e(TAG, "create() failed", e);
//        }
//        mmSocket = tmp;


    }

    public void write(String s) throws IOException {
        try {

            out = mSocket.getOutputStream();
            out.write(s.getBytes());
            System.out.print("Successful write");
        } catch (Exception e) {
            Toast.makeText(self, "failed to write", Toast.LENGTH_SHORT).show();

        }
    }
    public void read(){
        final int BUFFER_SIZE = 1024;
        byte[] buffer = new byte[BUFFER_SIZE];
        int length = 0;
        try {
            in = mSocket.getInputStream();
        } catch (IOException e) {
            System.out.println("failed read in socket");
            e.printStackTrace();
        }
        try {
            length = in.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String text = new String(buffer, 0, length);
        Toast.makeText(self, ""+text+"the text received", Toast.LENGTH_SHORT).show();

    }

    public void run() {
        final int BUFFER_SIZE = 1024;
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytes = 0;
        try {
            in = mSocket.getInputStream();
            while (true) {
                System.out.print("running");
                try {
                    bytes = in.read(buffer, bytes, BUFFER_SIZE - bytes);
                    System.out.print("in: ");
                    System.out.print(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(self, "error eading", Toast.LENGTH_SHORT).show();
        }
        int b = BUFFER_SIZE;


    }

    private void unsecurePairDevice(BluetoothDevice device){

        BluetoothSocket tmp = null;
//        Toast.makeText(self, "connected?; " + device.getName() + device.getAddress(), Toast.LENGTH_SHORT).show();
//        Toast.makeText(self, "uuid; " + device.getUuids()[0].getUuid() , Toast.LENGTH_SHORT).show();



        try {
            tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
            mSocket = tmp;
//            System.out.println("the type" + mSocket.);
//            mSocket
            mSocket.connect();
            System.out.println("hahass"+ mSocket.getRemoteDevice().getName());
            Toast.makeText(self, ""+mSocket.isConnected(), Toast.LENGTH_SHORT).show();
//            try {
//                write("A");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            run();
        } catch (IOException e) {
            System.out.println("failed to create rfcomm");
            e.printStackTrace();
            Toast.makeText(self, "failed to create rfcommsocket" + e.getStackTrace(), Toast.LENGTH_SHORT).show();
            Log.e("","trying fallback...");
            try {
                mSocket  = device.createRfcommSocketToServiceRecord(MY_UUID_INSECURE);
            } catch (IOException e1) {
                System.out.println("asad catch failed");
                e1.printStackTrace();
            }
            try {
                mSocket.connect();
            } catch (IOException e1) {
                System.out.println("second catch failed");
                e1.printStackTrace();
            }

            Log.e("","Connected");
        }
    }
    private void pairVariant(BluetoothDevice device){
        String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";
        Intent intent = new Intent(ACTION_PAIRING_REQUEST);
        String EXTRA_DEVICE = "android.bluetooth.device.extra.DEVICE";
        intent.putExtra(EXTRA_DEVICE, device);
        String EXTRA_PAIRING_VARIANT = "android.bluetooth.device.extra.PAIRING_VARIANT";
        int PAIRING_VARIANT_PIN = 0;
        intent.putExtra(EXTRA_PAIRING_VARIANT, PAIRING_VARIANT_PIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);
    }

    private void pairDevice(BluetoothDevice device) {
//        Toast.makeText(self, "paired to" + device.getName(), Toast.LENGTH_SHORT).show();
//        device.setPairingConfirmation(device, false);
//        device.
//        IBl
        device.setPin("0000".getBytes());
//        device.setPin(BluetoothDevice.PAIRING_VARIANT_PIN)
        device.createBond();
//        try {
//
//
//            device.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(device, true);
//            device.getClass().getMethod("cancelPairingUserInput").invoke(device);
//            Method method = device.getClass().getMethod("createBond", (Class[]) null);
//            method.invoke(device, (Object[]) null);
//        } catch (Exception e) {
//            e.printStackTrace();
//
//            Toast.makeText(self, "err", Toast.LENGTH_SHORT).show();
//        }
    }
    private void pair(){
        IntentFilter filter = new IntentFilter(
                "android.bluetooth.device.action.PAIRING_REQUEST");


        /*
         * Registering a new BTBroadcast receiver from the Main Activity context
         * with pairing request event
         */
        registerReceiver(
                new PairingRequest(), filter);
    }
    private void waitPairRequest(){
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        BroadcastReceiver a = new PairingRequest();
        registerReceiver(a ,filter );


    }

    private void discoverDevices() {
        myBt.startDiscovery();
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                //Finding devices
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
                    Toast.makeText(getApplicationContext(), "found device" + device.getName(), Toast.LENGTH_SHORT).show();
//                    discoveredBtDevices.add(device );
                    if (device.getAddress().equals(macEditText.getText().toString())) {
                        Toast.makeText(getApplicationContext(), "pairing" + device.getName(), Toast.LENGTH_SHORT).show();
//
                        System.out.println("pairing" + device.getName());
                        myBt.cancelDiscovery();
                        pairDevice(device);
//                        pairVariant(device);
//                        unsecurePairDevice(device);
//                        pair();


                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);


    }

}
