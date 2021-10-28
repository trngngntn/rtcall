package com.rtcall.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.rtcall.R;
import com.rtcall.entity.User;
import com.rtcall.net.RTConnection;
import com.rtcall.net.ServerSocket;
import com.rtcall.net.message.NetMessage;

public class IncomingCallActivity extends AppCompatActivity {

    User caller;
    Vibrator vibrator;

    TextView txtCaller;
    Button btAccept;
    Button btDecline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        RTConnection.setAppContext(getApplicationContext());

        caller = (User) getIntent().getExtras().get("caller");

        txtCaller = findViewById(R.id.txt_callee_name);
        btAccept = findViewById(R.id.bt_accept_call);
        btDecline = findViewById(R.id.bt_end_call);

        txtCaller.setText(caller.getDisplayName());

        btAccept.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), CallActivity.class);
            i.putExtra("caller", caller);
            ServerSocket.queueMessage(NetMessage.Relay.acceptCallMessage());
            startActivity(i);
        });

        btDecline.setOnClickListener(view -> {
            ServerSocket.queueMessage(NetMessage.Relay.declineCallMessage());
            finish();
        });

        initLocalBroadcastReceiver();

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 500, 350, 500, 350, 500, 1000};
        vibrator.vibrate(pattern, 0);
    }

    private void initLocalBroadcastReceiver() {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v("LOG", "Received intent");
                NetMessage msg = (NetMessage) intent.getExtras().get("message");
                switch (msg.getType()) {
                    case NetMessage.Relay.MSG_CALL_ENDED: {
                        finish();
                    }
                    break;
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("SERVICE_MESSAGE");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onStop() {
        vibrator.cancel();
        super.onStop();
    }
}