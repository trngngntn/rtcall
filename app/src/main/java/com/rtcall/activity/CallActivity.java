package com.rtcall.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.rtcall.R;
import com.rtcall.entity.User;
import com.rtcall.net.RTStream;
import com.rtcall.net.RTConnection;
import com.rtcall.net.ServerSocket;
import com.rtcall.net.message.NetMessage;

import org.webrtc.EglBase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CallActivity extends AppCompatActivity {
    private boolean offerFirst = false;
    private boolean connectionReady = true;

    private User otherPeer;
    private EglBase eglBase;

    private Button btEndCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null && bundle.get("incoming")!=null)
        {
            Log.e("FUCKME", "QUEUEUEUEUEUUEUEUEUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU");
            ServerSocket.queueMessage(NetMessage.Relay.acceptCallMessage());
        }

        RTStream.srfLocalStream= findViewById(R.id.srf_local_stream);
        RTStream.srfRemoteStream = findViewById(R.id.srf_remote_stream);
        btEndCall = findViewById(R.id.bt_end_call);

        btEndCall.setOnClickListener(view -> {

        });

        Object temp = getIntent().getExtras().get("caller");
        otherPeer = (User) (temp == null ?  getIntent().getExtras().get("callee") : temp);
        offerFirst = temp != null;

        eglBase = EglBase.create();
        RTConnection.eglBaseContext = eglBase.getEglBaseContext();
        RTStream.appContext = getApplicationContext();
        RTStream.eglBaseContext = eglBase.getEglBaseContext();
        RTStream.initSurface();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                RTConnection.initPeerConnFactory();

                RTStream.prepareLocalMedia();

                RTConnection.createPeerConnection();

                RTStream.startLocalStream();
            }
        };
        executorService.execute(runnable);

        if(offerFirst){
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    while (connectionReady != true);
                    RTConnection.offer();
                }
            };
            executorService.execute(task);
        }

    }
}