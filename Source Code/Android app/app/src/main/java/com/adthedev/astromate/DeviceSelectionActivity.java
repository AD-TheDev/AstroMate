package com.adthedev.astromate;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import android.widget.TextView;
import android.widget.Toast;

import android.content.SharedPreferences;

import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class DeviceSelectionActivity extends AppCompatActivity {

    private SharedPreferences mPreferences;
    private String sharedPrefFile =
            "com.adthedev.astromate.sharedprefs";
    private String SERVER_IP;
    private static final int SERVERPORT = 12523;

    private ClientThread clientThread;
    private Thread thread;
    private Boolean ip_exists=true;
    private Boolean ip_checked=false;
    private String error_toast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_selection);
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);

    }

    @Override
    protected void onResume(){
        super.onResume();
        String ip=mPreferences.getString("ip","none");
        TextView tv=findViewById(R.id.deviceInfo);
        tv.setText("Current Selected Device IP: "+ip);

    }


    class ClientThread implements Runnable {

        private Socket socket;
//        private BufferedReader input;

        @Override
        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

                socket = new Socket();
                socket.connect(new InetSocketAddress(serverAddr,SERVERPORT), 2000);
                ip_checked=true;
                ip_exists=true;
//                while (!Thread.currentThread().isInterrupted()) {
//
//                    this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                    String message = input.readLine();
////                    if (null == message || "Disconnect".contentEquals(message)) {
////                        Thread.interrupted();
////                        message = "Server Disconnected.";
////
////                        break;
////                    }
//
//                }

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
                ip_checked=true;
                ip_exists=false;
                error_toast="Invalid IP";
            } catch (IOException e1) {
                ip_checked=true;
                ip_exists=false;
                e1.printStackTrace();
                error_toast="Not able to connect to the IP. Check whether the PC and the android device are on the same network";
            }

        }

        void sendMessage(final String message) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (null != socket) {
                            PrintWriter out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(socket.getOutputStream())),
                                    true);
                            out.println(message);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public void checkIP(View v)
    {
        TextInputEditText input=findViewById(R.id.inputHost);
        SERVER_IP=input.getText().toString();

        clientThread=new ClientThread();
        thread = new Thread(clientThread);
        thread.start();

       while(!ip_checked) {
           try {
               Thread.sleep(100);
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
       }


        if(ip_exists) {
            clientThread.sendMessage("TEST");
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putBoolean("ip_valid",true);
            editor.putString("ip", SERVER_IP);
            editor.commit();
            TextView tv = findViewById(R.id.deviceInfo);
            Toast.makeText(this,"Successfully connected. IP registered for further use",Toast.LENGTH_LONG);
            tv.setText("Current Selected Device IP: " + SERVER_IP);
        }
        else{
            Toast.makeText(this,error_toast,Toast.LENGTH_LONG).show();
        }
        ip_checked=false;
        thread.interrupt();
    }



}
