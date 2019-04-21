package com.aetherapps.papyrus;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;

public class WifiTransferActivity extends AppCompatActivity {

    private Button wifiBtn;
    private Button connectBtn;
    private ImageView qrCodeView;
    private TextView ssidView;
    private TextView passView;
    WifiManager wifiManager;
    WifiConfiguration wifiConfiguration;
      String hotspotPSK;
      String hotspotSSID;
    private String[] permissions = {Manifest.permission.WRITE_SETTINGS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION , Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.INTERNET };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_transfer);

        wifiBtn = findViewById(R.id.wifiBtn);
        ssidView = findViewById(R.id.ssidView);
        passView = findViewById(R.id.passView);
        qrCodeView = findViewById(R.id.qrCodeView);
        connectBtn = findViewById(R.id.connectBtn);

//        requestPermissions();
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            Toast.makeText(WifiTransferActivity.this, "has permissions", Toast.LENGTH_SHORT).show();
        }

        initializeWifi();
//        if (!checkBluetooth() || !checkWifi()) {
//            startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
//            Intent[] enableRequests = {new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), new Intent(Settings.ACTION_WIRELESS_SETTINGS)};
//            startActivities(enableRequests);
//        }
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToHotSpot();
            }
        });

        wifiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createAndSetupHotspot();

            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(this.getApplicationContext())) {

            } else {

                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }


    }

    private Boolean checkPermissions() {
        Toast.makeText(WifiTransferActivity.this, "checking permis", Toast.LENGTH_SHORT).show();
        for (String p : permissions) {
            if ((ActivityCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED)) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(WifiTransferActivity.this, permissions, 1);

    }

    private Boolean checkBluetooth() {
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    private Boolean checkWifi() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return (mWifi.isConnected());

    }
    private void changeStateWifiAp(boolean activated) {
        Method method;
        try {
            method = wifiManager.getClass().getDeclaredMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            method.invoke(wifiManager, wifiConfiguration, activated);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void initializeWifi(){

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    private void createAndSetupHotspot(){
        wifiManager.setWifiEnabled(false);
        wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = generateSSID();
        wifiConfiguration.preSharedKey = generateKey();
        wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        changeStateWifiAp(true);

    }
    static String generateRandomString(int n)
    {
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());

            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }

    private String generateKey(){
//        hotspotPSK = generateRandomString(12);
        hotspotPSK = "12345678";
        passView.setText(hotspotPSK);
        return hotspotPSK;
    }
    private String generateSSID(){
//        hotspotSSID = generateRandomString(10);
        hotspotSSID = "hello";
        ssidView.setText(hotspotSSID);
        return hotspotSSID;
    }

    private void generateQRCode(String SSID, String Key){


    }
    private void prepareBeforeConnect(){
        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
        }
        try{
            Method method = wifiManager.getClass().getDeclaredMethod("getWifiApState");
            method.setAccessible(true);
            int apWifiState = (Integer) method.invoke(wifiManager, (Object[]) null);
            if(apWifiState == 13 || apWifiState == 12){
                changeStateWifiAp(false);
            }
        }
        catch (Exception e){
            e.printStackTrace();

        }

    }
    public void onDoneConnect(){
        Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show();

    }

    private void connectToHotSpot(){
        prepareBeforeConnect();
        ConnectHotspotThread cn = new ConnectHotspotThread(wifiManager);
        cn.start();
//      new ConnectHotspotTask().execute();

//        while(!(wifiManager.getConnectionInfo().getSSID().contains(hotspotSSID)  || hotspotSSID.contains(wifiManager.getConnectionInfo().getSSID()) )){
//            final WifiConfiguration wifiConfig = new WifiConfiguration();
//            wifiConfig.SSID = String.format("\"%s\"", hotspotSSID);
//            wifiConfig.preSharedKey =String.format("\"%s\"", hotspotPSK);
//            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//
//
//            int netId = wifiManager.addNetwork(wifiConfig);
//            wifiManager.enableNetwork(netId, true  );
//            wifiManager.reconnect();
//        }






//        final Handler handler = new Handler();
//        Runnable run= new Runnable() {
//            public void run() {
//                int netId = wifiManager.addNetwork(wifiConfig);
//
//
//                if(netId == -1) {
//                    System.out.println("didn't work");
//                    handler.postDelayed(this, 0);
//                }
//                wifiManager.enableNetwork(netId, true  );
//                wifiManager.reconnect();
//            }
//        };
//
//        handler.postDelayed(run, 0);



    }
    class ConnectHotspotThread extends Thread {
        WifiManager wifiManager;
        WifiConfiguration wifiConfig;
        ConnectHotspotThread(WifiManager wifiManager ) {
//             this.wifiConfig = wifiConfig;
             this.wifiManager = wifiManager;
        }

        public void run() {
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = String.format("\"%s\"", hotspotSSID);
            wifiConfig.preSharedKey =String.format("\"%s\"", hotspotPSK);
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

            int netId = wifiManager.addNetwork(wifiConfig);
            while (netId == -1){
//            while(!( wifiManager.getConnectionInfo() == null || hotspotSSID.contains(wifiManager.getConnectionInfo().getSSID()) )){
                    wifiConfig = new WifiConfiguration();
                wifiConfig.SSID = String.format("\"%s\"", hotspotSSID);
                wifiConfig.preSharedKey =String.format("\"%s\"", hotspotPSK);
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);





                    wifiConfig = new WifiConfiguration();
                    wifiConfig.SSID = String.format("\"%s\"", hotspotSSID);
                    wifiConfig.preSharedKey =String.format("\"%s\"", hotspotPSK);
                    wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

                    System.out.println("running");
                    netId = wifiManager.addNetwork(wifiConfig);
                }
                wifiManager.enableNetwork(netId, true  );
                wifiManager.reconnect();
//            }
            Toast.makeText(WifiTransferActivity.this, "connected", Toast.LENGTH_SHORT).show();

        }
    }



    private class ConnectHotspotTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = String.format("\"%s\"", hotspotSSID);
            wifiConfig.preSharedKey =String.format("\"%s\"", hotspotPSK);
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);




            int netId = wifiManager.addNetwork(wifiConfig);
            while (netId == -1){
                wifiConfig = new WifiConfiguration();
                wifiConfig.SSID = String.format("\"%s\"", hotspotSSID);
                wifiConfig.preSharedKey =String.format("\"%s\"", hotspotPSK);
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

                System.out.println("running");
                netId = wifiManager.addNetwork(wifiConfig);
            }
            wifiManager.enableNetwork(netId, true  );
            wifiManager.reconnect();
//            }
            Toast.makeText(WifiTransferActivity.this, "connected", Toast.LENGTH_SHORT).show();
//            while(!(wifiManager.getConnectionInfo().getSSID().contains(hotspotSSID)  || hotspotSSID.contains(wifiManager.getConnectionInfo().getSSID()) )){
//                final WifiConfiguration wifiConfig = new WifiConfiguration();
//                wifiConfig.SSID = String.format("\"%s\"", hotspotSSID);
//                wifiConfig.preSharedKey =String.format("\"%s\"", hotspotPSK);
//                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//
//
//                int netId = wifiManager.addNetwork(wifiConfig);
//                wifiManager.enableNetwork(netId, true  );
//                wifiManager.reconnect();
//            }
            onPostExecute();
            return null;
        }


        protected void onProgressUpdate(Integer... progress) {
//            setProgressPercent(progress[0]);
        }

        protected void onPostExecute( ) {
            onDoneConnect();

//            Toast.makeText(WifiTransferActivity.this, "", Toast.LENGTH_SHORT).show();
//            showDialog("Downloaded " + result + " bytes");
        }
    }


}
