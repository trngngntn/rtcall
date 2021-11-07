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
import android.widget.EditText;
import android.widget.TextView;

import com.rtcall.R;
import com.rtcall.net.ServerSocket;
import com.rtcall.net.message.NetMessage;

public class RegisterActivity extends AppCompatActivity {

    EditText edtDisplayName;
    EditText edtUsername;
    EditText edtPassword;
    Button btRegister;
    TextView txtSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtUsername = findViewById(R.id.edt_usernname);
        edtPassword = findViewById(R.id.edt_password);
        btRegister = findViewById(R.id.bt_login);
        txtSignIn = findViewById(R.id.txt_signin);

        btRegister.setOnClickListener(view -> {
            ServerSocket.queueMessage(NetMessage.Client.loginMessage(
                    edtUsername.getText().toString(),
                    edtPassword.getText().toString()));
        });

        txtSignIn.setOnClickListener(view -> {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            finish();
            startActivity(loginIntent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
        initLocalBroadcastReceiver();
    }

    private void initLocalBroadcastReceiver() {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v("LOG", "Received intent");
                NetMessage msg = (NetMessage) intent.getExtras().get("message");
                switch (msg.getType()) {
                    case NetMessage.Server.MSG_REGISTERED: {
                        Intent homeIntent = new Intent(getApplication(), MainActivity.class);
                        finish();
                        startActivity(homeIntent);
                    }
                    case NetMessage.Server.MSG_REGISTER_FAILED: {

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