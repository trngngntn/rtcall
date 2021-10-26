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
import com.rtcall.net.ServerSocket;
import com.rtcall.net.message.C2SMessage;
import com.rtcall.net.message.S2CMessage;

import org.json.JSONException;

public class IncomingCallActivity extends AppCompatActivity {


    TextView txtCaller;
    Button btAccept;
    Button btDecline;
    String callerUid;

    Vibrator vibrator;

    Activity thisActivity;

    private class TestBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v("LOG", "Received intent");
            S2CMessage msg = (S2CMessage) intent.getExtras().get("message");
            try {
                switch (msg.getType()) {
                    /*case S2CMessage.MSG_PEER_ADDR: {
                        Intent i = new Intent(thisActivity, CallActivity.class);
                        i.putExtra("callerUid", callerUid);
                        i.putExtra("peerAddr", msg.getData().getString("peerAddr"));
                        startActivity(i);
                    }
                    break;*/
                    case S2CMessage.MSG_REQUEST_CALL: {
                        //log.setText("\n Request call from UID: " + intent.getExtras().getString("caller"));
                        Intent i = new Intent(thisActivity, IncomingCallActivity.class);
                        callerUid = intent.getExtras().getString("caller");
                        i.putExtra("caller", callerUid);
                        startActivity(i);
                    }
                    break;
                    default: {
                        //log.setText("\n Unknown action");
                    }
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
        getSupportActionBar().hide();
        txtCaller = findViewById(R.id.txt_caller);
        btAccept = findViewById(R.id.bt_accept_call);
        btDecline = findViewById(R.id.bt_decline_call);

        String callerUid = getIntent().getExtras().getString("caller");
        txtCaller.setText(callerUid);

        btAccept.setOnClickListener(view -> {
            Intent i = new Intent(thisActivity, CallActivity.class);
            i.putExtra("doPing", true);
            i.putExtra("caller", callerUid);
            Log.v("CALL_ACT", "-----------------ICcaller: " + callerUid);
            startActivity(i);
            //ServerSocket.ping(thisActivity, callerUid);
        });

        btDecline.setOnClickListener(view -> {
            ServerSocket.queueMessage(C2SMessage.createDeclineCallMessage(""));
            finish();
        });

        TestBroadcastReceiver testBrd = new IncomingCallActivity.TestBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("SERVICE_MESSAGE");

        LocalBroadcastManager.getInstance(this).registerReceiver(testBrd, filter);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 500, 350, 500, 350, 500, 1000};
        vibrator.vibrate(pattern, 0);


    }

    @Override
    protected void onStop() {
        vibrator.cancel();
        super.onStop();
    }
}