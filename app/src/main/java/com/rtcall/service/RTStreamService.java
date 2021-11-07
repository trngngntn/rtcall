package com.rtcall.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class RTStreamService extends Service {
    private static final String TAG = "SERVICE_STREAM";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "Created!");
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