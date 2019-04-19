package com.aetherapps.papyrus;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.FileObserver;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private FileObserver observer;
    private Activity self;
    private File receiptFile;
    private String invoicesPath;
    private String[] permissions={ Manifest.permission.ACCESS_NETWORK_STATE,  Manifest.permission.READ_EXTERNAL_STORAGE   , Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.INTERNET,   Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        invoicesPath  =android.os.Environment.getExternalStorageDirectory().toString() +"/Papyrus Invoices";
        self = this;
        if(!checkPermissions()){
            requestPermissions();
        }
        if(!checkBluetooth() || !checkWifi()){
            startActivity( new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            Intent[] enableRequests= { new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), new Intent(Settings.ACTION_WIRELESS_SETTINGS)};
            startActivities( enableRequests );
        }

        trackPath();


    }

    private void trackPath( ){
        observer = new FileObserver(invoicesPath) { // set up a file observer to watch this directory on sd card

            @Override
            public void onEvent(int event, final String file) {
                self.runOnUiThread(new Runnable() {
                    public void run() {

                        receiptFile = new File(invoicesPath+"/" + file);
                        Toast.makeText(getApplicationContext(), receiptFile.getAbsolutePath() + " was saved!", Toast.LENGTH_LONG).show();

                    }
                });
                //}
            }
        };
        observer.startWatching(); //START OBSERVING
    }
    private Boolean checkPermissions(){
        for(String p:permissions){
            if((ActivityCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED)){
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(MainActivity.this,permissions, 1);

    }
    private Boolean checkBluetooth(){
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }
    private Boolean checkWifi(){
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return (mWifi.isConnected());

    }
}
