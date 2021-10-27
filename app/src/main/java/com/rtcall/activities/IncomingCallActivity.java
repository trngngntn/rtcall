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
import com.rtcall.net.ServerSocket;
import com.rtcall.net.message.C2SMessage;
import com.rtcall.net.message.S2CMessage;

public class IncomingCallActivity extends AppCompatActivity {


    TextView txtCaller;
    Button btAccept;
    Button btDecline;

    User caller;

    Vibrator vibrator;

    Activity thisActivity;

    private class TestBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v("LOG", "Received intent");
            S2CMessage msg = (S2CMessage) intent.getExtras().get("message");
            try {
                if(msg.getType() == S2CMessage.MSG_CALL_ENDED){
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        thisActivity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        caller = (User) getIntent().getExtras().get("caller");

        txtCaller = findViewById(R.id.txt_callee_name);
        btAccept = findViewById(R.id.bt_accept_call);
        btDecline = findViewById(R.id.bt_end_call);

        txtCaller.setText(caller.getDisplayName());

        btAccept.setOnClickListener(view -> {
            Intent i = new Intent(thisActivity, CallActivity.class);
            i.putExtra("caller", caller);
            Log.v("CALL_ACT", "-----------------ICcaller: " + caller.getUid());
            startActivity(i);
            //ServerSocket.ping(thisActivity, callerUid);
        });

        btDecline.setOnClickListener(view -> {
            ServerSocket.queueMessage(C2SMessage.createDeclineCallMessage(""));
            finish();
        });

        initLocalBroadcastReceiver();

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 500, 350, 500, 350, 500, 1000};
        vibrator.vibrate(pattern, 0);


    }

    protected void initLocalBroadcastReceiver(){
        TestBroadcastReceiver testBrd = new IncomingCallActivity.TestBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("SERVICE_MESSAGE");
        LocalBroadcastManager.getInstance(this).registerReceiver(testBrd, filter);
    }

    @Override
    protected void onStop() {
        vibrator.cancel();
        super.onStop();
    }
}