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

    Server(int port, String send, Activity self) {
        this.port = port;
        this.self = self;
        this.send = send;
        this.started = true;


    }

    public void run() {
        try {
            ss = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Error creating server");
            e.printStackTrace();
        }
        try {
            s = ss.accept();
        } catch (IOException e) {

            System.out.println("Error accepting request");
            e.printStackTrace();
        }
        try {
            pr = new PrintWriter(s.getOutputStream());
            client_ready = true;
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
                byte[] byteArr = new byte[(int) file.length()];
                try {
                    fis = new FileInputStream(file);
                    bis = new BufferedInputStream(fis);
                    bis.read(byteArr, 0, byteArr.length);
                    os = s.getOutputStream();
                    System.out.println("Sending " + file.getAbsolutePath() + "(" + byteArr.length + " bytes)");
                    os.write(byteArr, 0, byteArr.length);
                    os.flush();
                } catch (Exception e) {
                    System.out.println("failed ");
                    e.printStackTrace();

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

}
