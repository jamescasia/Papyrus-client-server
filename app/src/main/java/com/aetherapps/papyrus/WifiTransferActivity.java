//package com.aetherapps.papyrus;
//
//import android.Manifest;
//import android.bluetooth.BluetoothAdapter;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//import android.net.Uri;
//import android.net.wifi.WifiConfiguration;
//import android.net.wifi.WifiManager;
//import android.os.AsyncTask;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Environment;
//import android.os.FileObserver;
//import android.provider.Settings;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.res.ResourcesCompat;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.google.zxing.BarcodeFormat;
//import com.google.zxing.MultiFormatWriter;
//import com.google.zxing.WriterException;
//import com.google.zxing.common.BitMatrix;
//import com.journeyapps.barcodescanner.BarcodeEncoder;
//import com.aetherapps.papyrus.TFTPExample;
//
//import org.apache.commons.net.ftp.FTP;
//import org.apache.commons.net.ftp.FTPClient;
//import org.apache.commons.net.tftp.TFTP;
//import org.apache.commons.net.tftp.TFTPClient;
//import org.apache.ftpserver.FtpServer;
//import org.apache.ftpserver.FtpServerFactory;
//import org.apache.ftpserver.ftplet.Authority;
//import org.apache.ftpserver.ftplet.FtpException;
//import org.apache.ftpserver.ftplet.FtpReply;
//import org.apache.ftpserver.ftplet.FtpRequest;
//import org.apache.ftpserver.ftplet.FtpSession;
//import org.apache.ftpserver.ftplet.Ftplet;
//import org.apache.ftpserver.ftplet.FtpletContext;
//import org.apache.ftpserver.ftplet.FtpletResult;
//import org.apache.ftpserver.ftplet.UserManager;
//import org.apache.ftpserver.listener.ListenerFactory;
//import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
//import org.apache.ftpserver.usermanager.SaltedPasswordEncryptor;
//import org.apache.ftpserver.usermanager.impl.BaseUser;
//import org.apache.ftpserver.usermanager.impl.WritePermission;
//
//import java.io.BufferedInputStream;
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.io.PrintWriter;
//import java.lang.reflect.Method;
//import java.net.InetAddress;
//import java.net.NetworkInterface;
//import java.net.Socket;
//import java.net.SocketException;
//import java.net.UnknownHostException;
//import java.util.ArrayList;
//import java.util.Enumeration;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class WifiTransferActivity extends AppCompatActivity {
//
//    private Button wifiBtn;
//    private Button connectBtn;
//    private Button sendBtn;
//    private ImageView qrCodeView;
//    private TextView ssidView;
//    private TextView passView;
//    WifiManager wifiManager;
//    WifiConfiguration wifiConfiguration;
//    String hotspotPSK;
//    String hotspotSSID;
//    private FileObserver observer;
//    private File receiptFile;
//    private Socket socket;
//    private String invoicesPath;
//    private TFTPClient tftpClient;
//    private com.aetherapps.papyrus.TFTPClient tftpClient2;
//    private TFTPServer tftpServer;
//    private TFTPExample tftp;
//    boolean justStarted = true;
//
//    FtpServerFactory serverFactory = new FtpServerFactory();
//    ListenerFactory factory = new ListenerFactory();
//    PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
//    FtpServer finalServer;
//
//
//
//    private String[] permissions = {Manifest.permission.WRITE_SETTINGS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.INTERNET};
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_wifi_transfer);
//
//        wifiBtn = findViewById(R.id.wifiBtn);
//        sendBtn = findViewById(R.id.sendBtn);
//        ssidView = findViewById(R.id.ssidView);
//        passView = findViewById(R.id.passView);
//        qrCodeView = findViewById(R.id.qrCodeView);
//        connectBtn = findViewById(R.id.connectBtn);
//
////        requestPermissions();
//
//        sendBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                sendTCP(null);
//                sendFile(new File(android.os.Environment.getExternalStorageDirectory().toString() + "/Papyrus Invoices/irene.jpg"));
//
//            }
//        });
//
//        invoicesPath = android.os.Environment.getExternalStorageDirectory().toString() + "/Papyrus Invoices";
//        trackPath();
//        if (!checkPermissions()) {
//            requestPermissions();
//        } else {
//            Toast.makeText(WifiTransferActivity.this, "has permissions", Toast.LENGTH_SHORT).show();
//        }
//
//        initializeWifi();
////        if (!checkBluetooth() || !checkWifi()) {
////            startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
////            Intent[] enableRequests = {new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), new Intent(Settings.ACTION_WIRELESS_SETTINGS)};
////            startActivities(enableRequests);
////        }
//        connectBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                connectToHotSpot("hello", "12345678");
//                receiveFile();
////                tftpClient2.getFile("baejoohyun.jpg", invoicesPath+generateRandomString(5));
//
//            }
//        });
//
//        wifiBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                createAndSetupHotspot();
//
//            }
//        });
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (Settings.System.canWrite(this.getApplicationContext())) {
//
//            } else {
//
//                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
//                intent.setData(Uri.parse("package:" + this.getPackageName()));
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//            }
//        }
//
//
//    }
//
//    private Boolean checkPermissions() {
//        Toast.makeText(WifiTransferActivity.this, "checking permis", Toast.LENGTH_SHORT).show();
//        for (String p : permissions) {
//            if ((ActivityCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED)) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    private void requestPermissions() {
//        ActivityCompat.requestPermissions(WifiTransferActivity.this, permissions, 1);
//
//    }
//
//    private void trackPath() {
//        observer = new FileObserver(invoicesPath) {
//            @Override
//            public void onEvent(int event, final String file) {
//
//                if(event == FileObserver.CREATE){
//                WifiTransferActivity.this.runOnUiThread(new Runnable() {
//                    public void run() {
//                        receiptFile = new File(invoicesPath + "/" + file);
//                        Toast.makeText(getApplicationContext(), receiptFile.getAbsolutePath() + " was saved!", Toast.LENGTH_LONG).show();
//                        sendFile(receiptFile);
////                        sendTCP(receiptFile);
////                        tftpClient2.sendFile(receiptFile.getAbsolutePath(), "irene.jpg");
//                    }
//                });
//            }}
//        };
//        observer.startWatching(); //START OBSERVING
//    }
//
//    private Boolean checkBluetooth() {
//        return BluetoothAdapter.getDefaultAdapter().isEnabled();
//    }
//
//    private Boolean checkWifi() {
//        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//        return (mWifi.isConnected());
//
//    }
//
//    private Boolean changeStateWifiAp(boolean activated) {
//        Method method;
//        try {
//            method = wifiManager.getClass().getDeclaredMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
//            method.invoke(wifiManager, wifiConfiguration, activated);
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    private void initializeWifi() {
//
//        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
////        initializeTCP();
//        setupTFTP();
//    }
//
//    private void createAndSetupHotspot() {
//        try {
//            Method method = wifiManager.getClass().getDeclaredMethod("getWifiApState");
//            method.setAccessible(true);
//            int apWifiState = (Integer) method.invoke(wifiManager, (Object[]) null);
//            if (apWifiState == 13 || apWifiState == 12) {
//                changeStateWifiAp(false);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//
//        }
//        wifiManager.setWifiEnabled(false);
//        wifiConfiguration = new WifiConfiguration();
//        generateCredentials();
//
//        wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
//        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
//        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//        generateQRCode(hotspotSSID, hotspotPSK);
//        changeStateWifiAp(true);
//
//    }
//
//    static String generateRandomString(int n) {
//        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
//                + "0123456789"
//                + "abcdefghijklmnopqrstuvxyz";
//        StringBuilder sb = new StringBuilder(n);
//        for (int i = 0; i < n; i++) {
//            int index
//                    = (int) (AlphaNumericString.length()
//                    * Math.random());
//
//            sb.append(AlphaNumericString
//                    .charAt(index));
//        }
//
//        return sb.toString();
//    }
//
//    private void generateCredentials() {
//        wifiConfiguration.SSID = generateSSID();
//        wifiConfiguration.preSharedKey = generateKey();
//
//    }
//
//    private String generateKey() {
//        hotspotPSK = generateRandomString(14);
//        passView.setText(hotspotPSK);
//        return hotspotPSK;
//    }
//
//    private String generateSSID() {
//        hotspotSSID = generateRandomString(10);
//        ssidView.setText(hotspotSSID);
//        return hotspotSSID;
//    }
//
//    private void generateQRCode(String SSID, String Key) {
//        String string = SSID + "|" + Key;
//        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
//        BitMatrix bitMatrix = null;
//        try {
//            bitMatrix = multiFormatWriter.encode(string, BarcodeFormat.QR_CODE, 500, 500);
//        } catch (WriterException e) {
//            e.printStackTrace();
//        }
//        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
//        Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
//        qrCodeView.setImageBitmap(bitmap);
//
//
//    }
//
//    private void prepareBeforeConnect() {
//
//        if (!wifiManager.isWifiEnabled()) {
//            wifiManager.setWifiEnabled(true);
//        }
//        try {
//            Method method = wifiManager.getClass().getDeclaredMethod("getWifiApState");
//            method.setAccessible(true);
//            int apWifiState = (Integer) method.invoke(wifiManager, (Object[]) null);
//            if (apWifiState == 13 || apWifiState == 12) {
//                changeStateWifiAp(false);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//
//        }
//
//    }
//
//    public void onDoneConnect() {
//        Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show();
//
//    }
//
//    private void connectToHotSpot(String SSID, String passKey) {
//        prepareBeforeConnect();
//        ConnectHotspotThread cn = new ConnectHotspotThread(wifiManager, SSID, passKey);
//        cn.start();
////      new ConnectHotspotTask().execute();
//
//
//    }
//
//    private void setupTFTP() {
//
////        finalServer = serverFactory.createServer();
//
//
//        tftpServer = new TFTPServer(8080,invoicesPath  );
//
//        tftpClient = new TFTPClient();
////        setupStart();
////        serverControl();
//
//
////        tftpClient2 = new com.aetherapps.papyrus.TFTPClient(8080,"127.0.0.1");
//
//
//    }
//    void serverControl() {
//
//        if (finalServer.isStopped()) {
//
////            mUser.setEnabled(false);
////            mPasswd.setEnabled(false);
////            mDirChooser.setEnabled(false);
//
//            String user = "user";
//            String passwd = "12345678";
//            if (user.isEmpty()) {
//                user = "ftp";
//            }
//            if (passwd.isEmpty()) {
//                passwd = "ftp";
//            }
//            String subLoc = invoicesPath;
////            String subLoc = mDirAddress.getText().toString().substring(20);
//
////            pass = passwd;
////
////            StringBuilder strB = new StringBuilder("Password: ");
////            for (int i=0; i < pass.length(); i++) {
////                strB.append('*');
////            }
////            mPasswdDisp.setText(strB.toString());
////
////            mUserDisp.setText(String.format("Username: %s", user));
////
////            mUserDisp.setVisibility(View.VISIBLE);
////            mUserParent.setVisibility(View.INVISIBLE);
////
////            mPasswdParent.setVisibility(View.INVISIBLE);
////            mPasswdDisp.setVisibility(View.VISIBLE);
//
//            try {
//                setupStart(user, passwd, subLoc);
//            } catch (FileNotFoundException fnfe) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
////                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
////                    builder.setMessage(R.string.dialog_message_error).setTitle(R.string.dialog_title);
////                    builder.setPositiveButton("OK", (dialog, id) -> {
////                        dialog.dismiss();
////                        justStarted = false;
//////                        ActivityCompat.requestPermissions(WifiTransferActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST);
////                    });
////                    builder.show();
//                } else {
////                    ActivityCompat.requestPermissions(WifiTransferActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST);
//                }
//            }
//
//            try {
//
//                finalServer.start();
////                mAddrReg.setText(String.format("ftp://%s:2121", wifiIpAddress(this)));
////                mAddriOS.setText(String.format("ftp://%s:%s@%s:2121", user, passwd, wifiIpAddress(this)));
//
//            } catch (FtpException e) {
//                e.printStackTrace();
//            }
////            toolbar.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorGreen, null));
////
////            mPrompt.setVisibility(View.VISIBLE);
////            mAddr1.setVisibility(View.VISIBLE);
////            mAddr2.setVisibility(View.VISIBLE);
////
////            mTogglePass.setEnabled(true);
//
//        } else if (finalServer.isSuspended()) {
//
//            finalServer.resume();
////            toolbar.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorGreen, null));
////
////            mPrompt.setVisibility(View.VISIBLE);
////            mAddr1.setVisibility(View.VISIBLE);
////            mAddr2.setVisibility(View.VISIBLE);
//
//        } else {
//
//            finalServer.suspend();
////            toolbar.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorRed, null));
////
////            mPrompt.setVisibility(View.INVISIBLE);
////
////            mAddr1.setVisibility(View.INVISIBLE);
////            mAddr2.setVisibility(View.INVISIBLE);
//
//        }
//
//    }
//    private void setupStart(String username, String password, String subLoc) throws FileNotFoundException {
//        factory.setPort(2121);
//        serverFactory.addListener("default", factory.createListener());
//
//        File files = new File(invoicesPath + "/users.properties");
//        if (!files.exists()) {
//            try {
//                files.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        userManagerFactory.setFile(files);
//        userManagerFactory.setPasswordEncryptor(new SaltedPasswordEncryptor());
//        UserManager um = userManagerFactory.createUserManager();
//        BaseUser user = new BaseUser();
//        user.setName(username);
//        user.setPassword(password);
//        String home =  subLoc;
//        user.setHomeDirectory(home);
//
//        List<Authority> auths = new ArrayList<>();
//        Authority auth = new WritePermission();
//        auths.add(auth);
//        user.setAuthorities(auths);
//
//        try {
//            um.save(user);
//        } catch (FtpException e1) {
//            e1.printStackTrace();
//        }
//
//        serverFactory.setUserManager(um);
//        Map<String, Ftplet> m = new HashMap<>();
//        m.put("miaFtplet", new Ftplet()
//        {
//
//            @Override
//            public void init(FtpletContext ftpletContext) throws FtpException {
//
//            }
//
//            @Override
//            public void destroy() {
//
//            }
//
//            @Override
//            public FtpletResult beforeCommand(FtpSession session, FtpRequest request) throws FtpException, IOException
//            {
//                return FtpletResult.DEFAULT;
//            }
//
//            @Override
//            public FtpletResult afterCommand(FtpSession session, FtpRequest request, FtpReply reply) throws FtpException, IOException
//            {
//                return FtpletResult.DEFAULT;
//            }
//
//            @Override
//            public FtpletResult onConnect(FtpSession session) throws FtpException, IOException
//            {
//                return FtpletResult.DEFAULT;
//            }
//
//            @Override
//            public FtpletResult onDisconnect(FtpSession session) throws FtpException, IOException
//            {
//                return FtpletResult.DEFAULT;
//            }
//        });
//        serverFactory.setFtplets(m);
//    }
//
//    private void initializeTCP() {
//
////        try {
////            socket = new Socket(getLocalIpAddress(), 8080);
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
//
//
//        new Thread(new ClientThread()).start();
//    }
//
//    private void sendTCP(File file) {
//        try {
//
//            OutputStream out = socket.getOutputStream();
//            PrintWriter output = new PrintWriter(out);
//
//            output.write("Irene is life");
//
//            out.flush();
//            out.close();
//
//            socket.close();
//        } catch (Exception e) {
//            System.out.println("erros"  );
//            e.printStackTrace();
//        }
//    }
//
//    private String getLocalIpAddress() {
//
//
////        System.out.println("the ip address is:" +wifiManager.getConnectionInfo().getIpAddress());
////
////
//        try {
//            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
//                NetworkInterface intf = en.nextElement();
//                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
//                    InetAddress inetAddress = enumIpAddr.nextElement();
//                    if (!inetAddress.isLoopbackAddress()) {
//
//
//                        System.out.println("the ip address is:" + inetAddress.getHostAddress().toString());
////                        return "localhost";
//                        return inetAddress.getHostAddress().toString();
//                    }
//                }
//            }
//
//        } catch (SocketException ex) {
//            Log.e("ServerActivity", ex.toString());
//        }
//        return null;
//    }
//
//    class ClientThread implements Runnable {
//
//        @Override
//        public void run() {
//                System.out.println("COOOO");
////            Toast.makeText(WifiTransferActivity.this, "", Toast.LENGTH_SHORT).show();
//            try {
//                InetAddress serverAddr = InetAddress.getByName(getLocalIpAddress());
//                socket = new Socket("127.0.0.1", 8080);
//
//                System.out.println("COOOO"+socket.getPort());
//
//            } catch (UnknownHostException e1) {
//
//                System.out.println("ERRORHOOSTT");
//                e1.printStackTrace();
//            } catch (IOException e1) {
//                e1.printStackTrace();
//                System.out.println("ERRORHioOOSTT");
//            }
//            try {
//
//
//                InputStream in = socket.getInputStream();
//                BufferedReader br = new BufferedReader(new InputStreamReader(in));
//                String line;
//                while ((line = br.readLine()) != null) {
//                    System.out.println(line + "buffered");
//
//                    Toast.makeText(WifiTransferActivity.this, "", Toast.LENGTH_SHORT).show();
//                }
//
//                //in.close();
//
//            } catch (UnknownHostException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//    }
//
//    private void sendFile(File file) {
////
////        public static boolean send(int transferMode, String hostname, String localFilename, String remoteFilename,
//        SendFileThread sendFileThread =  new SendFileThread(tftpClient,file);
//        sendFileThread.start();
////        System.o
//    }
//
//    class SendFileThread extends Thread {
//        File file;
//        TFTPClient tftpClient;
//
//         SendFileThread(TFTPClient tftpClient , File file){
//            this.file = file;
//            this.tftpClient = tftpClient;
//
//        }
//        public void run( ){
////             tftpClient.sendFile(file.getAbsolutePath(),TFTP.BINARY_MODE,);
////             TFTPClient.
//           System.out.println("def" +  TFTPExample.send(TFTP.BINARY_MODE, "127.0.0.1:8080", file.getAbsolutePath(), "baejoohyun", tftpClient) );
////            Toast.makeText(this, "sentfiles"+getLocalIpAddress(), Toast.LENGTH_SHORT).show();
//            System.out.println("sending");
////            tftpClient.sendFile(, TFTP.ASCII_MODE);
//        }
//    }
//
//
//
//    private void receiveFile() {
//        RecvFileThread r = new RecvFileThread(tftpClient);
//        r.start();
//     }
//
//    class RecvFileThread extends Thread {
//        File file;
//        TFTPClient tftpClient;
//
//        RecvFileThread(TFTPClient tftpClient  ){
//            this.tftpClient = tftpClient;
//
//        }
//        public void run( ){
//            System.out.println(  "success"+ TFTPExample.receive(TFTP.BINARY_MODE, "127.0.0.1:8080", invoicesPath + "/" + generateRandomString(5), "baejoohyun", tftpClient)  );
//
//            System.out.println("recbvd");
//        }
//    }
//
//
//
//    class ConnectHotspotThread extends Thread {
//        WifiManager wifiManager;
//        WifiConfiguration wifiConfig;
//        String SSID;
//        String passKey;
//
//        ConnectHotspotThread(WifiManager wifiManager, String SSID, String passKey) {
//            this.wifiManager = wifiManager;
//            this.SSID = SSID;
//            this.passKey = passKey;
//        }
//
//        public void run() {
//            WifiConfiguration wifiConfig = new WifiConfiguration();
//            wifiConfig.SSID = String.format("\"%s\"", SSID);
//            wifiConfig.preSharedKey = String.format("\"%s\"", passKey);
//            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//
//            int netId = wifiManager.addNetwork(wifiConfig);
//            while (netId == -1) {
//                wifiConfig = new WifiConfiguration();
//                wifiConfig.SSID = String.format("\"%s\"", SSID);
//                wifiConfig.preSharedKey = String.format("\"%s\"", passKey);
//                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//
//                wifiConfig = new WifiConfiguration();
//                wifiConfig.SSID = String.format("\"%s\"", SSID);
//                wifiConfig.preSharedKey = String.format("\"%s\"", passKey);
//                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//
//                System.out.println("running");
//                netId = wifiManager.addNetwork(wifiConfig);
//            }
//            wifiManager.enableNetwork(netId, true);
//            wifiManager.reconnect();
//
//
//        }
//    }
//
//
//    private class ConnectHotspotTask extends AsyncTask {
//
//        @Override
//        protected Object doInBackground(Object[] objects) {
//            WifiConfiguration wifiConfig = new WifiConfiguration();
//            wifiConfig.SSID = String.format("\"%s\"", hotspotSSID);
//            wifiConfig.preSharedKey = String.format("\"%s\"", hotspotPSK);
//            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//
//
//            int netId = wifiManager.addNetwork(wifiConfig);
//            while (netId == -1) {
//                wifiConfig = new WifiConfiguration();
//                wifiConfig.SSID = String.format("\"%s\"", hotspotSSID);
//                wifiConfig.preSharedKey = String.format("\"%s\"", hotspotPSK);
//                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//
//                System.out.println("running");
//                netId = wifiManager.addNetwork(wifiConfig);
//            }
//            wifiManager.enableNetwork(netId, true);
//            wifiManager.reconnect();
////            }
//            Toast.makeText(WifiTransferActivity.this, "connected", Toast.LENGTH_SHORT).show();
//
//            onPostExecute();
//            return null;
//        }
//
//
//        protected void onProgressUpdate(Integer... progress) {
////            setProgressPercent(progress[0]);
//        }
//
//        protected void onPostExecute() {
//            onDoneConnect();
//
////            Toast.makeText(WifiTransferActivity.this, "", Toast.LENGTH_SHORT).show();
////            showDialog("Downloaded " + result + " bytes");
//        }
//    }
//
//
//
//
//}
