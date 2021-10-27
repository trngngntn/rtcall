package com.rtcall.activities;

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
import com.rtcall.net.message.S2CMessage;

public class OutgoingCallActivity extends AppCompatActivity {

    TextView txtCalleeName;
    Button btEndCall;

    private class TestBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v("LOG", "Received intent");
            S2CMessage msg = (S2CMessage) intent.getExtras().get("message");
            try {
                if(msg.getType() == S2CMessage.MSG_CALL_ACCEPTED){
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outcoming_call);

        User callee = (User) getIntent().getExtras().get("callee");

        txtCalleeName = findViewById(R.id.txt_callee_name);
        btEndCall = findViewById(R.id.bt_end_call);

        txtCalleeName.setText(callee.getDisplayName());
    }

    protected void initLocalBroadcastReceiver(){
        TestBroadcastReceiver testBrd = new TestBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("SERVICE_MESSAGE");
        LocalBroadcastManager.getInstance(this).registerReceiver(testBrd, filter);
    }
}