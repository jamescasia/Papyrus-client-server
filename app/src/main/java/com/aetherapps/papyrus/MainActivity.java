package com.aetherapps.papyrus;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.FileObserver;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
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
    private Button serverBtn;
    private Button sendBtn;
    private Button clientBtn;
    private Activity self;
    private File receiptFile;
    private String invoicesPath;
    private TextView macAddressLabel;
    private EditText macEditText;
    public static final String appName = "Papyrus-Sender";
    public static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private final IntentFilter intentFilter = new IntentFilter();
    private String btMacAddress;
    private OutputStream out;
    private InputStream in;
    Server server;
    Client client;
    //    WifiManager wifiManager;
    private BroadcastReceiver mReceiver;
    private String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.INTERNET};


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(self, "granted" + grantResults.toString(), Toast.LENGTH_SHORT).show();

                } else {

                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sendBtn = findViewById(R.id.send);
        clientBtn = findViewById(R.id.client);
        serverBtn = findViewById(R.id.server);

        macAddressLabel = findViewById(R.id.macAdressLabel);
        macEditText = findViewById(R.id.macEditText);
        btMacAddress = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(), "bluetooth_address");
        macAddressLabel.setText(btMacAddress);

        serverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startServer();
            }
        });
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                client.sendSomething("sent from client" , null);
                if (server != null)
                {
                    server.sendSomething("i love you jihyo twice from server", null);
                }
                else {
                    client.sendSomething("from client", null);
                }

            }
        });
        clientBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startClient();

            }
        });
        trackPath();
        self = this;
        requestPermissions();
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            Toast.makeText(self, "has permissions", Toast.LENGTH_SHORT).show();
        }
        if (!checkWifi()) {
            Intent[] enableRequests = {new Intent(Settings.ACTION_WIRELESS_SETTINGS)};
            startActivities(enableRequests);
        }


    }

    private void trackPath() {
        invoicesPath = android.os.Environment.getExternalStorageDirectory().toString() + "/Papyrus Invoices";
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
        Toast.makeText(self, "Checking permissions", Toast.LENGTH_SHORT).show();
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

    private Boolean checkWifi() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return (mWifi.isConnected());

    }

    private void startServer() {
//        wifiManager = getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        Toast.makeText(self, "ip: " + ip, Toast.LENGTH_SHORT).show();
        macAddressLabel.setText(ip);


        server = new Server(5050, "haha", self);
        server.start();
    }

    private void startClient() {


        client = new Client(macEditText.getText().toString(), 5050, self);
        client.start();


    }


}
