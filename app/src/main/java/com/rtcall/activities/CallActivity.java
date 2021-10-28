package com.rtcall.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;

import com.rtcall.R;
import com.rtcall.entity.User;
import com.rtcall.net.RTStream;
import com.rtcall.net.RTConnection;

import org.webrtc.EglBase;

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

        RTStream.srfLocalStream= findViewById(R.id.srf_local_stream);
        RTStream.srfRemoteStream = findViewById(R.id.srf_remote_stream);
        btEndCall = findViewById(R.id.bt_end_call);

        Object temp = getIntent().getExtras().get("caller");
        otherPeer = (User) (temp == null ?  getIntent().getExtras().get("callee") : temp);
        offerFirst = temp != null;

        //webrtc related
        eglBase = EglBase.create();
        RTConnection.eglBaseContext = eglBase.getEglBaseContext();
        RTConnection.initPeerConnFactory();

        RTStream.appContext = getApplicationContext();
        RTStream.eglBaseContext = eglBase.getEglBaseContext();
        RTStream.initSurface();

        RTStream.prepareLocalMedia();
        RTStream.startLocalStream();

        RTConnection.createPeerConnection();

        if(offerFirst){
            RTConnection.offer();
        }

    }
}