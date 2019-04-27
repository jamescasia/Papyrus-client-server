package com.aetherapps.papyrus;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.notbytes.barcode_reader.BarcodeReaderActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import com.aetherapps.papyrus.Utils.*;

public class MainActivity extends AppCompatActivity {
    private static final int BARCODE_READER_ACTIVITY_REQUEST = 1208;
    private FileObserver observer;
    private Button serverBtn;
    private Button sendBtn;
    private Button clientBtn;
    private Activity self;
    private File receiptFile;
    private String invoicesPath;
    private ImageView qrCodeView;
    private TextView macAddressLabel;
    private EditText macEditText;
    public static final String appName = "Papyrus-Sender";
    public static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private final IntentFilter intentFilter = new IntentFilter();
    private String btMacAddress;
    private WifiConfiguration wifiConfiguration;
    private OutputStream out;
    private InputStream in;
    Server server;
    Client client;
    private WifiManager wifiManager;
    private String hotspotSSID;
    private String hotspotPSK;
    private String device_ip;


    private String scanned_hotspotSSID;
    private String scanned_hotspotPSK;
    private String scanned_device_ip;
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

        qrCodeView = findViewById(R.id.qrCodeView);
        macAddressLabel = findViewById(R.id.macAdressLabel);
        macEditText = findViewById(R.id.macEditText);
        btMacAddress = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(), "bluetooth_address");
        macAddressLabel.setText(btMacAddress);


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
//            startActivities(enableRequests);
        }

        initializeWifi();
        setupBtns();


    }

    private void setupBtns() {
        serverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serverSetup();
            }
        });
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                client.sendSomething("sent from client" , null);
                if (server != null && server.client_ready)
                {
                    server.sendSomething("i love you jihyo twice from server", null);
                } else {
                    client.sendSomething("from client", null);
                }

            }
        });
        clientBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clientSetup();

            }
        });
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
    private Boolean checkOnHotspot(){
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("getWifiApState");
            method.setAccessible(true);
            int apWifiState = (Integer) method.invoke(wifiManager, (Object[]) null);
            if (apWifiState == 13 || apWifiState == 12) {
                return true;
            }
            else return false;

        }
        catch (Exception e){
            System.out.println("error hotstpot");
            return false;

        }


    }

    private Boolean checkWifi() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return (mWifi.isConnected());

    }

    private void startServer() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);


        server = new Server(5050, "haha", self);
        server.start();
    }

    private void startClient(String ip) {


        client = new Client(ip, 5050, self, invoicesPath);
        client.start();


    }

    private Boolean changeStateWifiAp(boolean activated) {
        Method method;
        try {

            method = wifiManager.getClass().getDeclaredMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            method.invoke(wifiManager, wifiConfiguration, activated);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void initializeWifi() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiConfiguration = new WifiConfiguration();
        macAddressLabel.setText(device_ip);

    }

    private boolean createAndSetupHotspot() {
        try {
            if(checkOnHotspot()){
                changeStateWifiAp(false);
            }

            wifiManager.setWifiEnabled(false);
            generateCredentials();

            wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

            device_ip = Utils.getIPAddress(true);
            Toast.makeText(self, "ip: " + device_ip, Toast.LENGTH_SHORT).show();
            generateQRCode(device_ip, hotspotSSID, hotspotPSK);
            changeStateWifiAp(true);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }


    }

    static String generateRandomString(int n) {
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            int index
                    = (int) (AlphaNumericString.length()
                    * Math.random());

            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }

    private void generateCredentials() {
        wifiConfiguration.SSID = generateSSID();
        wifiConfiguration.preSharedKey = generateKey();

    }

    private String generateKey() {
        hotspotPSK = generateRandomString(14);
//        passView.setText(hotspotPSK);
        return hotspotPSK;
    }

    private String generateSSID() {
        hotspotSSID = generateRandomString(10);
//        ssidView.setText(hotspotSSID);
        return hotspotSSID;
    }

    private void generateQRCode(String ip, String SSID, String Key) {
        String string = ip + ":" + SSID + ":" + Key;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = multiFormatWriter.encode(string, BarcodeFormat.QR_CODE, 170, 170);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
        qrCodeView.setImageBitmap(bitmap);


    }

    private void prepareBeforeConnect() {

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("getWifiApState");
            method.setAccessible(true);
            int apWifiState = (Integer) method.invoke(wifiManager, (Object[]) null);
            if (apWifiState == 13 || apWifiState == 12) {
                changeStateWifiAp(false);
            }
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    class ConnectHotspotThread extends Thread {
        WifiManager wifiManager;
//        WifiConfiguration wifiConfig;
        String SSID;
        String passKey;

        ConnectHotspotThread(WifiManager wifiManager, String SSID, String passKey) {
            this.wifiManager = wifiManager;
            this.SSID = SSID;
            this.passKey = passKey;
        }

        public void run() {
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = String.format("\"%s\"", SSID);
            wifiConfig.preSharedKey = String.format("\"%s\"", passKey);
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

            int netId = wifiManager.addNetwork(wifiConfig);
            while (netId == -1) {
                wifiConfig = new WifiConfiguration();
                wifiConfig.SSID = String.format("\"%s\"", SSID);
                wifiConfig.preSharedKey = String.format("\"%s\"", passKey);
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//
//                wifiConfig = new WifiConfiguration();
//                wifiConfig.SSID = String.format("\"%s\"", SSID);
//                wifiConfig.preSharedKey = String.format("\"%s\"", passKey);
//                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

                System.out.println("SSID:" + SSID +" PSK:" + passKey);
                netId = wifiManager.addNetwork(wifiConfig);
            }
            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();


        }
    }


    private void scanQrCodeWifi() {
        Intent launchIntent = BarcodeReaderActivity.getLaunchIntent(this, true, false);
        startActivityForResult(launchIntent, BARCODE_READER_ACTIVITY_REQUEST);
//        Intent launchIntent = BarcodeReaderActivity.getLaunchIntent(this, true, false);
//        startActivityForResult(launchIntent, BARCODE_READER_ACTIVITY_REQUEST);
//        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(getApplicationContext()).setBarcodeFormats(Barcode.QR_CODE).build();
//
//        Frame frame = new Frame.Builder();


    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(this, "error in  scanning", Toast.LENGTH_SHORT).show();
            return;
        }

        if (requestCode == BARCODE_READER_ACTIVITY_REQUEST && data != null) {
            Barcode barcode = data.getParcelableExtra(BarcodeReaderActivity.KEY_CAPTURED_BARCODE);
            macAddressLabel.setText(barcode.rawValue);
            Toast.makeText(this, barcode.rawValue, Toast.LENGTH_SHORT).show();
            scanned_device_ip = barcode.rawValue.split(":")[0];
            scanned_hotspotSSID = barcode.rawValue.split(":")[1];
            scanned_hotspotPSK= barcode.rawValue.split(":")[2];

            connectToHotSpot(scanned_hotspotSSID, scanned_hotspotPSK);
        }

    }

    private void serverSetup() {

        qrCodeView.setVisibility(View.VISIBLE);
        if (createAndSetupHotspot() && server == null) {

            startServer();
            if(server.client_ready){
                server.sendSomething("server setupped: I fancy you Park JiHyo", null);

            }
        }

    }

    private void clientSetup() {
        qrCodeView.setVisibility(View.INVISIBLE);
        scanQrCodeWifi();
//        startClient(scanned_device_ip);

    }


    private void connectToHotSpot(String SSID, String passKey) {
        prepareBeforeConnect();
        MainActivity.ConnectHotspotThread cn = new MainActivity.ConnectHotspotThread(wifiManager, SSID, passKey);
        cn.start();
//      new ConnectHotspotTask().execute();


    }


}
