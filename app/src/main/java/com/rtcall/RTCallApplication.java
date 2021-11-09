package com.rtcall;

import android.app.Application;
import android.app.Notification;
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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rtcall.activity.CallActivity;
import com.rtcall.activity.IncomingCallActivity;
import com.rtcall.activity.MainActivity;
import com.rtcall.entity.User;
import com.rtcall.net.RTConnection;
import com.rtcall.net.RTStream;
import com.rtcall.net.ServerSocket;
import com.rtcall.net.message.NetMessage;
import com.rtcall.service.RTCallService;

public class RTCallApplication extends Application implements CameraXConfig.Provider {
    public static RTCallApplication application;

    @NonNull
    @Override
    public CameraXConfig getCameraXConfig() {
        return Camera2Config.defaultConfig();
    }

    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
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
                NetMessage msg = (NetMessage) intent.getExtras().get("message");
                Log.v("LOG_SERVICE", String.format("Received intent %x" ,msg.getType()));
                switch (msg.getType()) {
                    case NetMessage.Server.MSG_REQUEST_CALL: {
                        String callerUid = msg.getData().get("caller").getAsString();
                        String callerName = msg.getData().get("callerName").getAsString();
                        User caller = new User(callerUid, callerName);

                        Log.d("TAG", caller.getDisplayName() + "is calling");

                        Intent fullscreen = new Intent(getApplicationContext(), IncomingCallActivity.class);
                        PendingIntent pendingFullscreen = PendingIntent
                                .getActivity(getApplicationContext(), 408, fullscreen, PendingIntent.FLAG_UPDATE_CURRENT);

                        Intent intentCall = new Intent(getApplicationContext(), CallActivity.class);
                        intentCall.putExtra("caller", caller);
                        intentCall.putExtra("incoming", "true");
                        PendingIntent pendingIntentCall = PendingIntent.getActivity(getApplicationContext(), 405, intentCall, 0);

                        Intent intentDecline = new Intent(getApplicationContext(), RTCallService.class);
                        intentDecline.putExtra("reject", true);
                        PendingIntent pendingIntentDecline = PendingIntent.getService(getApplicationContext(), 400, intentDecline, 0);

                        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(getApplicationContext(), "NOTIF")
                                .setSmallIcon(R.drawable.ic_round_notifications_24)
                                .setContentTitle("Incoming call")
                                .setContentText("[User]" + " is calling you.")
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setCategory(NotificationCompat.CATEGORY_CALL)
                                .addAction(R.drawable.ic_baseline_phone_24, "ANSWER", pendingIntentCall)
                                .addAction(R.drawable.ic_baseline_call_end_24, "DECLINE", pendingIntentDecline)
                                .setAutoCancel(true)
                                .setFullScreenIntent(pendingFullscreen, true);
                        RTConnection.initLocalReceiver();
                        NotificationManagerCompat notifManager = NotificationManagerCompat.from(getApplicationContext());
                        notifManager.notify(-1, notifBuilder.build());
                    }
                    break;
                    case NetMessage.Server.MSG_NEW_NOTIF: {
                        try {
                            JsonObject jObj = msg.getData().get("notif").getAsJsonObject();
                            com.rtcall.entity.Notification notif = new com.rtcall.entity.Notification(
                                    jObj.get("id").getAsInt(),
                                    jObj.get("timestamp").getAsString(),
                                    jObj.getAsJsonObject("data"),
                                    jObj.get("status").getAsInt()
                            );


                            Intent mainIntent = new Intent(application, MainActivity.class);
                            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mainIntent.putExtra("flag", MainActivity.NOTIF_FRAG);
                            PendingIntent pendingIntent = PendingIntent.getActivity(application, 411, mainIntent, 0);

                            NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(getApplicationContext(), "NOTIF")
                                    .setSmallIcon(R.drawable.ic_round_notifications_24)
                                    .setContentTitle("RTCall")
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setDefaults(Notification.DEFAULT_ALL)
                                    .setContentIntent(pendingIntent)
                                    .setAutoCancel(true);

                            switch (notif.getData().get("type").getAsInt()) {
                                case com.rtcall.entity.Notification.TYPE_PENDING_CONTACT:
                                    notifBuilder.setContentText("You have a pending contact request from " + notif.getData().get("userDisplay").getAsString());
                                    break;
                                case com.rtcall.entity.Notification.TYPE_MISSED_CALL:
                                    notifBuilder.setContentText("Missed call from " + notif.getData().get("userDisplay").getAsString());
                                    break;
                            }
                            NotificationManagerCompat notifManager = NotificationManagerCompat.from(getApplicationContext());
                            notifManager.notify(notif.getId(), notifBuilder.build());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                    case NetMessage.Relay.MSG_APPROVE_CONTACT:{
                        Intent mainIntent = new Intent(application, MainActivity.class);
                        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mainIntent.putExtra("flag", MainActivity.NOTIF_FRAG);
                        PendingIntent pendingIntent = PendingIntent.getActivity(application, 411, mainIntent, 0);

                        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(getApplicationContext(), "NOTIF")
                                .setSmallIcon(R.drawable.ic_round_notifications_24)
                                .setContentTitle("RTCall")
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true)
                                .setContentText(msg.getData().get("uid").getAsString() + " has approved your contact request");
                        NotificationManagerCompat notifManager = NotificationManagerCompat.from(getApplicationContext());
                        notifManager.notify(0, notifBuilder.build());
                    }
                    break;
                    case NetMessage.Relay.MSG_REJECT_CONTACT:{
                        Intent mainIntent = new Intent(application, MainActivity.class);
                        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mainIntent.putExtra("flag", MainActivity.NOTIF_FRAG);
                        PendingIntent pendingIntent = PendingIntent.getActivity(application, 411, mainIntent, 0);

                        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(getApplicationContext(), "NOTIF")
                                .setSmallIcon(R.drawable.ic_round_notifications_24)
                                .setContentTitle("RTCall")
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true)
                                .setContentText(msg.getData().get("uid").getAsString() + " has rejected your contact request");
                        NotificationManagerCompat notifManager = NotificationManagerCompat.from(getApplicationContext());
                        notifManager.notify(0, notifBuilder.build());
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
