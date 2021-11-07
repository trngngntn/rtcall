package com.rtcall.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.rtcall.R;
import com.rtcall.net.ServerSocket;
import com.rtcall.net.message.NetMessage;

public class LoginActivity extends AppCompatActivity {

    private EditText edtUsername;
    private EditText edtPassword;
    private Button btLogin;
    private TextView txtSignUp;
    private TextView txtError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtUsername = findViewById(R.id.edt_usernname);
        edtPassword = findViewById(R.id.edt_password);
        btLogin = findViewById(R.id.bt_login);
        txtSignUp = findViewById(R.id.txt_signup);
        txtError = findViewById(R.id.txt_error);

        btLogin.setOnClickListener(view -> {
            txtError.setVisibility(View.INVISIBLE);
            ServerSocket.queueMessage(NetMessage.Client.loginMessage(
                    edtUsername.getText().toString(),
                    edtPassword.getText().toString()));
        });

        txtSignUp.setOnClickListener(view -> {
            Intent registerIntent = new Intent(this, RegisterActivity.class);
            finish();
            startActivity(registerIntent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        findViewById(R.id.txtTest).setOnClickListener(view -> {
            startActivity(new Intent(this, TestActivity.class));
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
                    case NetMessage.Server.MSG_LOGGED_IN: {
                        Intent homeIntent = new Intent(getApplication(), MainActivity.class);
                        finish();
                        startActivity(homeIntent);
                    }
                    case NetMessage.Server.MSG_BAD_IDENTITY: {
                        txtError.setVisibility(View.VISIBLE);
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