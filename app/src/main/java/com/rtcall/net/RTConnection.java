package com.rtcall.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.rtcall.net.message.NetMessage;

import org.json.JSONException;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
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

    public static void setAppContext(Context context) {
        appContext = context;
        initLocalReceiver();
    }

    private static void initLocalReceiver() {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                NetMessage msg = (NetMessage) intent.getExtras().get("message");
                switch (msg.getType()) {
                    case NetMessage.Relay.MSG_WEBRTC_CANDIDATE: {
                        IceCandidate candidate = new IceCandidate(
                                msg.getData().get("sdpMid").getAsString(),
                                msg.getData().get("sdpMLineIndex").getAsInt(),
                                msg.getData().get("sdp").getAsString());
                        peerConn.addIceCandidate(candidate);
                    }
                    break;
                    case NetMessage.Relay.MSG_WEBRTC_OFFER: {
                        //only in caller
                        Log.v("OBSERVER", "Received OFFER");
                        SessionDescription sessionDescription = new SessionDescription(
                                SessionDescription.Type.OFFER,
                                msg.getData().get("description").getAsString());

                        peerConn.setRemoteDescription(new mObserver(), sessionDescription);
                        wait = Executors.newSingleThreadExecutor();
                        Runnable task = new Runnable() {
                            @Override
                            public void run() {
                                while (connectionReady != true) ;
                                answer();
                            }
                        };
                        wait.execute(task);
                    }
                    break;
                    case NetMessage.Relay.MSG_WEBRTC_ANSWER: {
                        //only in callee
                        Log.v("OBSERVER", "Received ANSWER");
                        SessionDescription sessionDescription = new SessionDescription(
                                SessionDescription.Type.ANSWER,
                                msg.getData().get("description").getAsString());

                        peerConn.setRemoteDescription(new mObserver(), sessionDescription);
                    }
                    break;
                    default:
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("SERVICE_MESSAGE");
        LocalBroadcastManager.getInstance(appContext).registerReceiver(broadcastReceiver, filter);
    }

    public static PeerConnectionFactory initPeerConnFactory() {
        //create peerConn factory
        PeerConnectionFactory.InitializationOptions initOptions =
                PeerConnectionFactory.InitializationOptions.builder(appContext)
                        .setEnableInternalTracer(true)
                        .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
                        .createInitializationOptions();

        PeerConnectionFactory.initialize(initOptions);

        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        options.disableEncryption = true;
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

    public static void createPeerConnection() {
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        PeerConnection.IceServer external = PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer();
        iceServers.add(external);

        PeerConnection.RTCConfiguration rtcConf = new PeerConnection.RTCConfiguration(iceServers);

        peerConn = peerConnFactory.createPeerConnection(rtcConf, new RTConnectionObserver());

        connectionReady = true;
    }

    public static void offer() {
        Log.v("OBSERVER", "do CALL");
        MediaConstraints mediaConstraints = new MediaConstraints();
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        peerConn.createOffer(new CalleeSdpObserver(), mediaConstraints);
    }

    public static void answer() {
        Log.v("OBSERVER", "do ANSWER");
        peerConn.createAnswer(new CallerSdpObserver(), new MediaConstraints());
    }


}
