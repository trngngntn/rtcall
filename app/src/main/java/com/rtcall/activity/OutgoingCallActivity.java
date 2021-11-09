package com.rtcall.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.rtcall.R;
import com.rtcall.entity.User;
import com.rtcall.net.RTConnection;
import com.rtcall.net.ServerSocket;
import com.rtcall.net.message.NetMessage;

public class OutgoingCallActivity extends AppCompatActivity {
    private static final int DELAY = 1500;
    final Handler handler = new Handler();
    User callee;

    TextView txtCalleeName;
    TextView txtCallStatus;
    Button btEndCall;
    Runnable timeout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outcoming_call);

        RTConnection.initLocalReceiver();

        callee = (User) getIntent().getExtras().get("callee");
        ServerSocket.queueMessage(NetMessage.Client.dialMessage(callee.getUid()));

        txtCalleeName = findViewById(R.id.txt_callee_name);
        txtCallStatus = findViewById(R.id.txt_call_status);
        btEndCall = findViewById(R.id.bt_end_call);

        txtCalleeName.setText(callee.getDisplayName());

        btEndCall.setOnClickListener(view -> {
            ServerSocket.queueMessage(NetMessage.Relay.preEndCallMessage());
            txtCallStatus.setText("End call");
            handler.postDelayed(this::finish, DELAY);
        });

        initLocalBroadcastReceiver();

        timeout = new Runnable() {
            @Override
            public void run() {
                ServerSocket.queueMessage(NetMessage.Relay.preEndCallMessage());
                txtCallStatus.setText("No answer");
                handler.postDelayed(() -> {
                    finish();
                }, DELAY);
            }
        };

        handler.postDelayed(timeout, 30000);

        initLocalBroadcastReceiver();
    }

    private void initLocalBroadcastReceiver() {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                NetMessage msg = (NetMessage) intent.getExtras().get("message");
                Log.v("OUTCALL", "Received message " + msg.getType());
                switch (msg.getType()) {
                    case NetMessage.Relay.MSG_CALL_ACCEPTED: {
                        Intent i = new Intent(getApplicationContext(), CallActivity.class);
                        i.putExtra("callee", callee);
                        finish();
                        handler.removeCallbacks(timeout);
                        startActivity(i);
                    }
                    break;
                    case NetMessage.Relay.MSG_CALL_DECLINED: {
                        txtCallStatus.setText("Call declined");
                        handler.postDelayed(() -> {
                            finish();
                        }, DELAY);
                    }
                    break;
                    /*case NetMessage.Relay.MSG_CALL_ENDED: {
                        txtCallStatus.setText("Call ended");
                        handler.postDelayed(() -> {
                            finish();
                        }, DELAY);
                    }
                    break;*/
                    case NetMessage.Server.MSG_CALLEE_BUSY: {
                        txtCallStatus.setText("User is busy");
                        handler.postDelayed(() -> {
                            finish();
                        }, DELAY);
                    }
                    break;
                    case NetMessage.Server.MSG_CALLEE_OFF: {
                        txtCallStatus.setText("User is offline");
                        handler.postDelayed(() -> {
                            finish();
                        }, DELAY);
                    }
                    break;
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("SERVICE_MESSAGE");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, filter);
    }
}