package com.rtcall.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.rtcall.net.ServerSocket;

public class RTCallService extends Service {
    private static final String TAG = "SERVICE_BACKGROUND";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("SERVICE", "Service created");
        if (!ServerSocket.isConnected()) {
            ServerSocket.prepare(getApplicationContext());
            new Thread(ServerSocket::connect).start();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Service", "Service started");
        if (intent == null) {
            // open a TCP socket to server
        }
        SharedPreferences prefs = getSharedPreferences("localData", MODE_PRIVATE);
        if (prefs.contains("logged")) {
            return START_STICKY;
        } else {
            return START_NOT_STICKY;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
