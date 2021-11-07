package com.rtcall.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.rtcall.R;
import com.rtcall.entity.User;
import com.rtcall.net.RTConnection;
import com.rtcall.net.message.NetMessage;

public class OutgoingCallActivity extends AppCompatActivity {

    User callee;

    TextView txtCalleeName;
    Button btEndCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outcoming_call);

        RTConnection.setAppContext(getApplicationContext());

        callee = (User) getIntent().getExtras().get("callee");

        txtCalleeName = findViewById(R.id.txt_callee_name);
        btEndCall = findViewById(R.id.bt_end_call);

        txtCalleeName.setText(callee.getDisplayName());

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
                        startActivity(i);
                    }
                    break;
                    case NetMessage.Relay.MSG_CALL_DECLINED: {
                        finish();
                    }
                    break;
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
}