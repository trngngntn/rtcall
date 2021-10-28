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
import com.rtcall.net.message.NetMessage;
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
            NetMessage msg = (NetMessage) intent.getExtras().get("message");
            switch (msg.getType()) {
                case NetMessage.Server.MSG_LOGGED_IN: {
                    log.setText("\n Logged in with UID: " + msg.getData().get("uid"));
                }
                break;
                case NetMessage.Server.MSG_REQUEST_CALL: {
                    //log.setText("\n Request call from UID: " + intent.getExtras().getString("caller"));
                    Intent i = new Intent(thisActivity, IncomingCallActivity.class);
                    String caller = msg.getData().get("caller").getAsString();
                    i.putExtra("caller", new User(caller, caller));
                    startActivity(i);
                }
                break;
                default: {
                    log.setText("\n Unknown action");
                }
            }
        }
    }

    TestBroadcastReceiver testBrd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        thisActivity = this;
        ServerSocket.prepare(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

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
                ServerSocket.queueMessage(NetMessage.Client.loginMessage(edtUID.getText().toString(), ""));
            }
        });

        btDial.setOnClickListener((view) -> {
            if (edtDial.getText().toString().equals("")) {
                Toast.makeText(this.getBaseContext(), "Invalid UID", Toast.LENGTH_SHORT).show();
            } else {
                String calleeUid = edtDial.getText().toString();

                ServerSocket.queueMessage(NetMessage.Client.dialMessage(calleeUid));
                Intent i = new Intent(thisActivity, OutgoingCallActivity.class);
                i.putExtra("callee", new User(calleeUid, calleeUid));
                startActivity(i);
            }
        });
        testBrd = new TestBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("SERVICE_MESSAGE");

        LocalBroadcastManager.getInstance(this).registerReceiver(testBrd, filter);
    }
}