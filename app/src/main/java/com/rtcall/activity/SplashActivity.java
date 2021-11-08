package com.rtcall.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.rtcall.R;
import com.rtcall.RTCallApplication;
import com.rtcall.net.ServerSocket;
import com.rtcall.net.message.NetMessage;
import com.rtcall.service.RTCallService;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private static String[] permissions = {
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.VIBRATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.USE_FULL_SCREEN_INTENT
    };

    Application app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        app = getApplication();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        RTCallApplication.application = (RTCallApplication) getApplication();
        RTCallApplication.application.createNotificationChannel();
        RTCallApplication.application.initLocalBroadcastReceiver();

        if(!checkPermission()){
            ActivityCompat.requestPermissions(this, permissions, 12);
        }

        if(ServerSocket.isConnected()){
            prep();
        } else {
            Intent service = new Intent(this, RTCallService.class);
            startService(service);
            initLocalBroadcastReceiver();
        }
    }

    private boolean checkPermission(){
        for(String perm:permissions){
            int result = ContextCompat.checkSelfPermission(this, perm);
            if(result != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    private void prep(){
        if(true){ // not login yet
            Intent loginIntent = new Intent(app, LoginActivity.class);
            finish();
            startActivity(loginIntent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            Intent homeIntent = new Intent(app, MainActivity.class);
            finish();
            startActivity(homeIntent);
        }
    }

    private void initLocalBroadcastReceiver() {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {


            @Override
            public void onReceive(Context context, Intent intent) {
                int infoCode = (int) intent.getExtras().get("info");
                if(infoCode == ServerSocket.INFO_CONNECTED){
                    prep();
                } else {

                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("SERVICE_INFO");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, filter);
    }

}