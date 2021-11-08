package com.rtcall;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.CameraXConfig;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
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

    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("NOTIF", "name", importance);
            channel.setDescription("description");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    public void initLocalBroadcastReceiver() {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v("LOG", "Received intent");
                NetMessage msg = (NetMessage) intent.getExtras().get("message");
                switch (msg.getType()) {
                    case NetMessage.Server.MSG_REQUEST_CALL: {
                        Intent i = new Intent(getApplicationContext(), IncomingCallActivity.class);
                        String callerUid = msg.getData().get("caller").getAsString();
                        User caller = User.getUser(callerUid);
                        intent.putExtra("caller", caller);
                        Log.d("TAG", caller + "is calling");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        /*Intent fullscreen = new Intent(getApplicationContext(), IncomingCallActivity.class);
                        PendingIntent pendingFullscreen = PendingIntent
                                .getActivity(getApplicationContext(), 100, fullscreen,PendingIntent.FLAG_UPDATE_CURRENT);
                        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(getApplicationContext(), "NOTIF")
                                .setSmallIcon(R.drawable.ic_round_notifications_24)
                                .setContentTitle("Incoming call")
                                .setContentText("[User]" + " is calling you.")
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setCategory(NotificationCompat.CATEGORY_CALL)
                                .setFullScreenIntent(pendingFullscreen, true);
                        NotificationManagerCompat notifManager = NotificationManagerCompat.from(getApplicationContext());
                        notifManager.notify(-1, notifBuilder.build());*/
                    }
                    break;
                    case NetMessage.Server.MSG_NEW_NOTIF: {
                        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(getApplicationContext(), "NOTIF");
                        notifBuilder.setContentTitle("");
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
