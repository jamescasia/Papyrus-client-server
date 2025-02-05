package com.aetherapps.papyrus;

import android.app.Activity;
import android.renderscript.ScriptGroup;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by James on 27/04/2019.
 */

public class Client extends Thread {
    String ip;
    int port;
    Socket s;
    PrintWriter pr;
    Activity self;
    String filePath;
    int fileSize;
    int bytesRead;
    int current = 0;
    FileOutputStream fos = null;
    BufferedOutputStream bos = null;
    String ext;

    Client(String ip, int port, Activity self, String filePath) {
        this.ip = ip;
        this.port = port;
        this.self = self;
        this.filePath = filePath + "/" + generateRandomString(6);


    }

    public void run() {
        try {
            s = new Socket(ip, port);
        } catch (Exception e) {
            System.out.print("error creating socket");
            e.printStackTrace();
        }

        try {
            pr = new PrintWriter(s.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        pr.println("init");
        pr.flush();
        listenForData();
        listenForFile();

    }

    class sendThread extends Thread {
        String string;
        File file;

        sendThread(String string, File file) {
            this.string = string;
            this.file = file;
        }

        public void run() {
            pr.println(string);

            pr.flush();

        }
    }

    public void sendSomething(String string, File file) {
        sendThread s = new sendThread(string, file);
        s.start();

    }

    public void listenForFile() {
        try{

        byte [] b = new byte[2000000];
        InputStream is = s.getInputStream();
        FileOutputStream fr = new FileOutputStream(filePath+"." +ext);
        is.read(b, 0, b.length);
        fr.write(b, 0, b.length);}
        catch (Exception e){
            showToast("failed to receive");
        }


//        try {
//            System.out.println("Connecting...");
//
//            // receive file
//            byte[] mybytearray = new byte[fileSize];
//            InputStream is = s.getInputStream();
//            fos = new FileOutputStream(filePath + "." + ext);
//            bos = new BufferedOutputStream(fos);
//            bytesRead = is.read(mybytearray, 0, mybytearray.length);
//            current = bytesRead;
//
//            do {
//                bytesRead = is.read(mybytearray, current, (mybytearray.length - current));
//                if (bytesRead >= 0) current += bytesRead;
//            } while (current < fileSize);
//
//            bos.write(mybytearray, 0, current);
//            bos.flush();
////            Toast.makeText(self, "bytes: "+ current, Toast.LENGTH_SHORT).show();
//            showToast("bytes" + current);
//            System.out.println("File " + filePath
//                    + " downloaded (" + current + " bytes read)");
//        } catch (Exception e) {
//            System.out.println("error");
//            e.printStackTrace();
//
//        } finally {
//            try {
//                if (fos != null) fos.close();
//                if (bos != null) bos.close();
//                if (s != null) s.close();
//            } catch (Exception e) {
//                System.out.print("error flushing");
//                e.printStackTrace();
//            }
//        }
    }

    public void listenForData() {
        InputStreamReader in = null;
        try {
            in = new InputStreamReader(s.getInputStream());
        } catch (IOException e) {
            System.out.println("Error initializing inputstream reader");
            e.printStackTrace();
        }
        BufferedReader bf = new BufferedReader(in);

        String str = null;


        while (true) {
            try {
                str = bf.readLine();

                if (str != null) {

                    System.out.println("Message" + str);
                    showToast("Message: " + str);
                    if (str.contains("size:")) {
                        fileSize = 100 * Integer.parseInt(str.split(":")[1]);
                        fileSize = 20000000;
                        showToast("size" + fileSize);
                    }
                    if (str.contains("ext:")) {
                        ext = str.split(":")[1];
                        break;

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    private void showToast(final String string) {
        self.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(self, string, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public String generateRandomString(int n) {
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
}
