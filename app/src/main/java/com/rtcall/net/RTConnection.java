package com.rtcall.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.rtcall.net.message.NetMessage;

import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RTConnection {
    private static boolean connectionReady = false;

    public static PeerConnection peerConn;
    public static PeerConnectionFactory peerConnFactory;
    private static Context appContext;

    public static EglBase.Context eglBaseContext;

    private static ExecutorService wait;

    public static void setAppContext(Context context){
        appContext = context;
        initLocalReceiver();
    }

    private static void initLocalReceiver(){
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v("LOG", "Received intent");
                NetMessage msg = (NetMessage) intent.getExtras().get("message");
                switch (msg.getType()){
                    case NetMessage.Relay.MSG_WEBRTC_OFFER:{
                        //only in caller
                        wait = Executors.newSingleThreadExecutor();
                        Runnable task = new Runnable() {
                            @Override
                            public void run() {
                                while(connectionReady != true);
                                answer();
                            }
                        };
                        wait.execute(task);
                    }
                    break;
                    case NetMessage.Relay.MSG_WEBRTC_ANSWER:{
                        //only in callee

                    }
                    default:
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("SERVICE_MESSAGE");
        LocalBroadcastManager.getInstance(appContext).registerReceiver(broadcastReceiver, filter);
    }

    public static PeerConnectionFactory initPeerConnFactory(){
        //create peerConn factory
        PeerConnectionFactory.InitializationOptions initOptions =
                PeerConnectionFactory.InitializationOptions.builder(appContext)
                        .createInitializationOptions();

        PeerConnectionFactory.initialize(initOptions);

        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        options.disableEncryption = false;
        options.disableNetworkMonitor = true;

        VideoEncoderFactory videoEncoderFactory = new DefaultVideoEncoderFactory(eglBaseContext, true, true);
        VideoDecoderFactory videoDecoderFactory = new DefaultVideoDecoderFactory(eglBaseContext);


        peerConnFactory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(videoEncoderFactory)
                .setVideoDecoderFactory(videoDecoderFactory)
                .setOptions(options)
                .createPeerConnectionFactory();

        return peerConnFactory;
    }

    public static void createPeerConnection(){
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        PeerConnection.IceServer external = PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer();
        iceServers.add(external);

        PeerConnection.RTCConfiguration rtcConf = new PeerConnection.RTCConfiguration(iceServers);

        peerConn = peerConnFactory.createPeerConnection(rtcConf, new RTConnectionObserver());

        connectionReady = true;
    }

    public static void offer(){
        MediaConstraints mediaConstraints = new MediaConstraints();
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        peerConn.createOffer(new CallerSdpObserver(), mediaConstraints);
    }

    public static void answer(){
        peerConn.createAnswer(new CalleeSdpObserver(), new MediaConstraints());
    }


}
