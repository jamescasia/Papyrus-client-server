package com.aetherapps.papyrus;

/**
 * Created by James on 27/04/2019.
 */

import android.app.Activity;
import android.widget.Toast;

import java.net.*;
import java.io.*;

public class Server extends Thread {
    int port;
    ServerSocket ss;
    Socket s;
    Activity self;
    String send;
    PrintWriter pr;
    Boolean client_ready = false;
    FileInputStream fis = null;
    BufferedInputStream bis = null;
    OutputStream os = null;
    Boolean started = false;
    OnTaskCompleted callback;
    File file;


    Server(int port, Activity self, File file) {
        this.port = port;
        this.self = self;
        this.started = true;
        this.file = file;


    }

    public void run() {
        try {
            ss = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Error creating server");
            e.printStackTrace();
        }
        try {
            showToast("success server");
            s = ss.accept();
        } catch (IOException e) {

            System.out.println("Error accepting request");
            e.printStackTrace();
        }
        try {
            pr = new PrintWriter(s.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error print");
        }
        listenForData();

    }

    class sendThread extends Thread {
        String string;
        File file;

        sendThread(String string, File file) {
            this.string = string;
            this.file = file;
        }

        public void run() {

            if (s != null) {
                pr.println(string);
                pr.flush();

                if (file != null) {
                    try {

                        FileInputStream fr = new FileInputStream(file);
                        byte b[] = new byte[2000000];
                        fr.read(b, 0, b.length);
                        os = s.getOutputStream();
                        os.write(b, 0, b.length);

                    } catch (Exception e) {
                        System.out.print("error");
                        e.printStackTrace();

                    }


//                    byte[] byteArr = new byte[(int) file.length()];
//                    try {
//                        fis = new FileInputStream(file);
//                        bis = new BufferedInputStream(fis);
//                        bis.read(byteArr, 0, byteArr.length);
//                        os = s.getOutputStream();
//                        System.out.println("Sending " + file.getAbsolutePath() + "(" + byteArr.length + " bytes)");
//                        os.write(byteArr, 0, byteArr.length);
//                        os.flush();
//                    } catch (Exception e) {
//                        System.out.println("failed ");
//                        e.printStackTrace();
//
//                    }
                }
            }

        }
    }

    public void sendSomething(String string, File file) {
        sendThread s = new sendThread(string, file);
        s.start();


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
                    if (str.equals("init")) {
                        clientInitialized();

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    private void clientInitialized() {
        client_ready = true;
        sendSomething("size:" + file.length(), null);
        System.out.println("size of files" + file.length());
        String a[] = file.getAbsolutePath().split("\\.");
        showToast(file.getAbsolutePath() + a[0]);
        sendSomething("ext:" + a[a.length - 1], file);

    }

    private void showToast(final String string) {
        self.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(self, string, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
