package com.aetherapps.papyrus;

import android.app.Activity;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
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

    Client(String ip, int port, Activity self){
        this.ip = ip;
        this.port = port;
        this.self = self;



    }

    public void run(){
        try {
            s = new Socket(ip, port);
        } catch (IOException e) {
            System.out.print("error creating socket");
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
        sendThread s =new sendThread(string, file);
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
