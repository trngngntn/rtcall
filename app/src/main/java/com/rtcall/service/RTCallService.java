package com.rtcall.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.rtcall.RTCallApplication;
import com.rtcall.net.ServerSocket;
import com.rtcall.net.message.NetMessage;

public class RTCallService extends Service {
    private static final String TAG = "SERVICE_BACKGROUND";
    private Thread socketThread;

    @Override
    public void onCreate() {
        RTCallApplication.application = (RTCallApplication) getApplication();
        RTCallApplication.application.initLocalBroadcastReceiver();
        super.onCreate();
        Log.d("SERVICE", "Service created");
        if (!ServerSocket.isConnected()) {
            ServerSocket.prepare(getApplicationContext());
            socketThread = new Thread(() -> {
                ServerSocket.connect();
                while (!ServerSocket.isConnected()) {
                    if (ServerSocket.failed) {
                        ServerSocket.connect();
                    }
                }
                selfConnect();
            });
            socketThread.start();
        }

    }

    private void selfConnect() {
        SharedPreferences prefs = getSharedPreferences("localData", MODE_PRIVATE);
        if (prefs.contains("loggedUid")) {
            String uid = prefs.getString("loggedUid", "");
            if (!uid.equals("")) {
                Log.e("Service", "Self-connect");
                ServerSocket.queueMessage(NetMessage.Client.connectMessage(uid));
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Service", "Service started");
        SharedPreferences prefs = getSharedPreferences("localData", MODE_PRIVATE);
        Log.e("Service", "INTENT " + (intent == null));
        //selfConnect();
        if (intent != null) {
            Bundle bundle = intent.getExtras();

            if ((bundle != null && bundle.getBoolean("logout")) || !ServerSocket.isConnected()) {
                Log.d("SERVICE", "Logout");
                ServerSocket.close();
                try {
                    socketThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                socketThread = new Thread(ServerSocket::connect);
                socketThread.start();
            }
            Log.d("SERVICE", "REJECTING CALL " + (bundle==null));
            if(bundle != null){
                Log.d("SERVICE", "REJECTING CALL " + bundle.getBoolean("reject", false));
                if(bundle.getBoolean("reject", false))
                ServerSocket.queueMessage(NetMessage.Relay.declineCallMessage());
            }
        }
        if (prefs.contains("loggedUid")) {
            Log.e("Service", "STICKY");
            return START_STICKY;
        } else {
            Log.e("Service", "NON_STICKY");
            return START_NOT_STICKY;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ServerSocket.close();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
