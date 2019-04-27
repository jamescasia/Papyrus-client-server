package com.aetherapps.papyrus;

/**
 * Created by James on 27/04/2019.
 */

import android.app.Activity;
import android.widget.Toast;

import java.net.*;
import java.io.*;

public class Server extends Thread{
    int port;
    ServerSocket ss;
    Socket s;
    Activity self;
    String send;
    PrintWriter pr;


    Server(int port, String send, Activity self){
        this.port = port;
        this.self = self;
        this.send = send;


    }
    public void run(){
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        listenForData();

    }
    class sendThread extends Thread{
        String string;
        File file;
        sendThread(String string, File file){
            this.string = string;
            this.file = file;
        }
        public void run(){
            pr.println(string);
            pr.flush();

        }
    }

    public void sendSomething(String string, File file){
        sendThread s = new sendThread(string, file);
        s.start();


    }

    public void listenForData(){
        InputStreamReader in = null;
        try {
            in = new InputStreamReader(s.getInputStream());
        } catch (IOException e) {
            System.out.println("Error initializing inputstream reader");
            e.printStackTrace();
        }
        BufferedReader bf = new BufferedReader(in);

        String str = null;


        while(true){
            try {
                str = bf.readLine();

                if(str!= null){
                    System.out.println("Message" + str);
                    showToast("Message: " + str);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    private void showToast(final String string){
        self.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(self, string, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
