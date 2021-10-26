package com.rtcall.services;

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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Service", "Service started");

        if(intent == null){
            // open a TCP socket to server
        }
        if(ServerSocket.isConnected() == false){
            new Thread(()->ServerSocket.connect()).start();
        }

        SharedPreferences settings;
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
