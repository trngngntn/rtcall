package com.rtcall.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.rtcall.net.ServerSocket;

import java.io.IOException;
import java.net.DatagramSocket;

public class RTStreamService extends Service {
    private static final String TAG = "SERVICE_STREAM";

    public RTStreamService() {
    }

    public void start() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "Stream service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}