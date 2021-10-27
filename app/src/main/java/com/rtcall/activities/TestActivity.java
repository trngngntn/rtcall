package com.rtcall.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.rtcall.R;
import com.rtcall.entity.User;
import com.rtcall.net.ServerSocket;
import com.rtcall.net.message.C2SMessage;
import com.rtcall.net.message.S2CMessage;
import com.rtcall.services.RTCallService;

import org.json.JSONException;

public class TestActivity extends AppCompatActivity {

    EditText edtUID;
    EditText edtDial;
    Button btLogin;
    Button btDial;
    TextView log;

    Activity thisActivity;

    private class TestBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v("LOG", "Received intent");
            S2CMessage msg = (S2CMessage) intent.getExtras().get("message");
            try {
                switch (msg.getType()) {
                    case S2CMessage.MSG_LOGGED_IN: {
                        log.setText("\n Logged in with UID: " + msg.getData().getString("uid"));
                    }
                    break;
                    case S2CMessage.MSG_REQUEST_CALL: {
                        //log.setText("\n Request call from UID: " + intent.getExtras().getString("caller"));
                        Intent i = new Intent(thisActivity, IncomingCallActivity.class);
                        i.putExtra("caller", msg.getData().getString("caller"));
                        startActivity(i);
                    }
                    break;
                    default: {
                        log.setText("\n Unknown action");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    TestBroadcastReceiver testBrd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        thisActivity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        getSupportActionBar().hide();

        Intent service = new Intent(this, RTCallService.class);
        startService(service);

        edtUID = findViewById(R.id.edt_test_username);
        edtDial = findViewById(R.id.edt_test_dial);

        btLogin = findViewById(R.id.bt_test_login);
        btDial = findViewById(R.id.bt_test_dial);

        log = findViewById(R.id.txt_Log);

        btLogin.setOnClickListener((view) -> {
            if (edtUID.getText().toString().equals("")) {
                Toast.makeText(this.getBaseContext(), "Invalid UID", Toast.LENGTH_SHORT).show();
            } else {
                ServerSocket.queueMessage(C2SMessage.createLoginMessage(edtUID.getText().toString(), ""));
            }
        });

        btDial.setOnClickListener((view) -> {
            if (edtDial.getText().toString().equals("")) {
                Toast.makeText(this.getBaseContext(), "Invalid UID", Toast.LENGTH_SHORT).show();
            } else {
                String calleeUid = edtDial.getText().toString();

                ServerSocket.queueMessage(C2SMessage.createDialMessage(calleeUid));
                Intent i = new Intent(thisActivity, OutgoingCallActivity.class);
                i.putExtra("callee",new User(calleeUid, calleeUid));
                startActivity(i);
            }
        });
        testBrd = new TestBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("SERVICE_MESSAGE");

        LocalBroadcastManager.getInstance(this).registerReceiver(testBrd, filter);
    }
}