package com.adthedev.astromate;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class sendDataToPc extends Service {
    public sendDataToPc() {
    }

    private static final int SERVERPORT = 12523;
    private SharedPreferences mPreferences;
    private String sharedPrefFile =
            "com.adthedev.astromate.sharedprefs";
    private boolean ip_valid;
    private String SERVER_IP;
    private ClientThread clientThread;
    private Thread thread;
    private Boolean socket_except=false;
    private String socket_except_err_msg;

    class ClientThread implements Runnable {

        private Socket socket;
        private BufferedReader input;


        @Override
        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
               // socket = new Socket(serverAddr, SERVERPORT);
                socket = new Socket();
                socket.connect(new InetSocketAddress(serverAddr,SERVERPORT), 2000);

                while (!Thread.currentThread().isInterrupted()) {

                    this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String message = input.readLine();
//                    if (null == message || "Disconnect".contentEquals(message)) {
//                        Thread.interrupted();
//                        message = "Server Disconnected.";
//
//                        break;
//                    }

                }

            } catch (UnknownHostException e1) {
                //Toast.makeText(sendDataToPc.this,"Host not found",Toast.LENGTH_SHORT).show();
                socket_except=true;
                socket_except_err_msg="Host not found";
                e1.printStackTrace();
            } catch (IOException e1) {
                //Toast.makeText(sendDataToPc.this,"Unable to send message. Make sure wifi is enabled.",Toast.LENGTH_SHORT).show();
                socket_except=true;
                socket_except_err_msg="Unable to send message. Make sure wifi is enabled and PC client is running";
                e1.printStackTrace();
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

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        ip_valid=mPreferences.getBoolean("ip_valid",false);
        //Toast.makeText(this,"Astromate Service Started",Toast.LENGTH_SHORT).show();
        if(ip_valid)
        {

            ClipboardManager clipboardManager=(ClipboardManager)getSystemService(this.CLIPBOARD_SERVICE);
            ClipData pData = clipboardManager.getPrimaryClip();
            if(pData!=null) {
                SERVER_IP=mPreferences.getString("ip","none");
                clientThread=new ClientThread();
                thread = new Thread(clientThread);
                thread.start();
                ClipData.Item item = pData.getItemAt(0);
                String txtpaste = item.getText().toString();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(!socket_except) {
                    clientThread.sendMessage(txtpaste);
                    Toast.makeText(this, "Message sent to PC", Toast.LENGTH_SHORT).show();
                    thread.interrupt();
                }
                else
                {
                    Toast.makeText(this,socket_except_err_msg,Toast.LENGTH_LONG).show();
                }
            }
            else{
                Toast.makeText(this,"No text copied",Toast.LENGTH_SHORT).show();
            }

        }
        else
        {
            Toast.makeText(this,"No valid IP available",Toast.LENGTH_SHORT).show();
        }

        return START_STICKY;
    }


}
