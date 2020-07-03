package com.adthedev.astromate;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity {

    public static final String CHANNEL_ID="Main Channel";

    private SharedPreferences mPreferences;
    private String sharedPrefFile =
            "com.adthedev.astromate.sharedprefs";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);

        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        boolean firstRun = mPreferences.getBoolean("first_run", true);
        mPreferences.edit().putBoolean("first_run", false).apply();

        if(firstRun)
        {
            mPreferences.edit().putBoolean("ip_valid",false).commit();
        }

        setSupportActionBar(toolbar);
        createNotifChannels();
        final NotificationManagerCompat notificationManager
                = NotificationManagerCompat.from(this);

        Switch sw=findViewById(R.id.switch1);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    Intent intent=new Intent(MainActivity.this,sendDataToPc.class);
                    PendingIntent pendingIntent=PendingIntent.getService(MainActivity.this,0,intent,0);

                    Notification n=new NotificationCompat.Builder(MainActivity.this,CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_stat_name)
                            .setContentTitle("AstroMate notification service is now running")
                            .setContentText("Tap to launch horoscope on the connected computer")
                            .setOngoing(true)
                            .setContentIntent(pendingIntent)
                            .build();

                    notificationManager.notify(1,n);
                    mPreferences.edit().putBoolean("notif_status",true).apply();

                } else {
                    // The toggle is disabled
                    notificationManager.cancel(1);
                    mPreferences.edit().putBoolean("notif_status",false).apply();

                }
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Switch sw=findViewById(R.id.switch1);
        Boolean notif_status=mPreferences.getBoolean("notif_status",false);
        sw.setChecked(notif_status);
    }

    private void createNotifChannels()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("This channel offers the " +
                    "main functionality of AstroMate");

            NotificationManager m = getSystemService(NotificationManager.class);
            m.createNotificationChannel(channel);
        }
    }

    public void openDeviceSelection(View v)
    {
        //Toast.makeText(MainActivity.this, "Button Pressed", 5).show();

        Intent i=new Intent(this,DeviceSelectionActivity.class);
        startActivity(i);
    }

}
