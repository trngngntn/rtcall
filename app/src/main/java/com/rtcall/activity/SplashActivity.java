package com.rtcall.activity;

import androidx.annotation.NonNull;
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
import android.content.SharedPreferences;
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

    boolean connected = false;
    boolean perm = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        RTCallApplication.application = (RTCallApplication) getApplication();
        RTCallApplication.application.createNotificationChannel();
        RTCallApplication.application.initLocalBroadcastReceiver();

        if (!checkPermission()) {
            ActivityCompat.requestPermissions(this, permissions, 12);
        } else {
            perm = true;

        }

        Log.d("SPLASH", "SS: " + ServerSocket.isConnected() + perm);

        if (ServerSocket.isConnected() && perm) {
            prep();
        } else {
            Intent service = new Intent(this, RTCallService.class);
            startService(service);
            initLocalBroadcastReceiver();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        perm = true;
        if (connected || ServerSocket.isConnected()) {
            prep();
        } else {
            Intent service = new Intent(this, RTCallService.class);
            startService(service);
            initLocalBroadcastReceiver();
        }
    }

    private boolean checkPermission() {
        for (String perm : permissions) {
            int result = ContextCompat.checkSelfPermission(this, perm);
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void prep() {
        SharedPreferences prefs = getSharedPreferences("localData", MODE_PRIVATE);
        if (!prefs.contains("loggedUid")) { // not login yet
            Intent loginIntent = new Intent(RTCallApplication.application, LoginActivity.class);
            finish();
            startActivity(loginIntent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            Intent homeIntent = new Intent(RTCallApplication.application, MainActivity.class);
            finish();
            startActivity(homeIntent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    private void initLocalBroadcastReceiver() {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {


            @Override
            public void onReceive(Context context, Intent intent) {
                int infoCode = (int) intent.getExtras().get("info");
                if (infoCode == ServerSocket.INFO_CONNECTED) {
                    connected = true;
                    if (perm) {
                        prep();
                    }
                } else {

                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("SERVICE_INFO");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, filter);
    }

}