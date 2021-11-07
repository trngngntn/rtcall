package com.rtcall;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.CameraXConfig;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.rtcall.activity.IncomingCallActivity;
import com.rtcall.entity.User;
import com.rtcall.net.RTStream;
import com.rtcall.net.message.NetMessage;

public class RTCallApplication extends Application implements CameraXConfig.Provider {
    public static RTCallApplication application;
    @NonNull
    @Override
    public CameraXConfig getCameraXConfig() {
        return Camera2Config.defaultConfig();
    }

    private void initLocalBroadcastReceiver() {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v("LOG", "Received intent");
                NetMessage msg = (NetMessage) intent.getExtras().get("message");
                switch (msg.getType()) {
                    case NetMessage.Server.MSG_REQUEST_CALL: {
                        Intent i = new Intent(getApplicationContext(), IncomingCallActivity.class);
                        String caller = msg.getData().get("caller").getAsString();
                        intent.putExtra("caller", new User(caller, caller));
                        Log.d("TAG", caller + "is calling");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                    break;
                    case NetMessage.Server.MSG_NEW_NOTIF: {

                    }
                    break;
                    default:
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("SERVICE_MESSAGE");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, filter);
    }
}
