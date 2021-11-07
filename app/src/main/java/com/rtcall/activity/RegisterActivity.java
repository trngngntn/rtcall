package com.rtcall.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Dialog;
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

public class RegisterActivity extends AppCompatActivity {

    private AppCompatActivity current;

    public class RegisteredDialog extends Dialog {

        public RegisteredDialog(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.dialog_registered);
            Button btToLogin = findViewById(R.id.bt_to_login);
            btToLogin.setOnClickListener(view -> {
                Intent loginIntent = new Intent(current, LoginActivity.class);
                current.finish();
                current.startActivity(loginIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }
    }


    EditText edtDisplayName;
    EditText edtUsername;
    EditText edtPassword;
    Button btRegister;
    TextView txtSignIn;
    TextView txtError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        current = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtDisplayName = findViewById(R.id.edt_display_name);
        edtUsername = findViewById(R.id.edt_username);
        edtPassword = findViewById(R.id.edt_password);
        btRegister = findViewById(R.id.bt_login);
        txtSignIn = findViewById(R.id.txt_signin);
        txtError = findViewById(R.id.txt_signup_error);

        btRegister.setOnClickListener(view -> {
            txtError.setVisibility(View.INVISIBLE);
            ServerSocket.queueMessage(NetMessage.Client.registerMessage(
                    edtDisplayName.getText().toString(),
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
                        RegisteredDialog dialog = new RegisteredDialog(current);
                        dialog.show();
                    }
                    case NetMessage.Server.MSG_REGISTER_FAILED: {
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