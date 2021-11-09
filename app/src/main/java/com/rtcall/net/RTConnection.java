package com.rtcall.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.rtcall.RTCallApplication;
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

    public static EglBase.Context eglBaseContext;

    private static ExecutorService wait;

    private static boolean localReceiverInit = false;

    public static void initLocalReceiver() {
        if (localReceiverInit) return;
        wait = Executors.newSingleThreadExecutor();
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
                        Runnable task = new Runnable() {
                            @Override
                            public void run() {
                                while (!connectionReady) ;
                                peerConn.addIceCandidate(candidate);
                            }
                        };
                        wait.execute(task);
                    }
                    break;
                    case NetMessage.Relay.MSG_WEBRTC_OFFER: {
                        //only in caller
                        Log.v("OBSERVER", "Received OFFER");
                        SessionDescription sessionDescription = new SessionDescription(
                                SessionDescription.Type.OFFER,
                                msg.getData().get("description").getAsString());
                        Runnable task = new Runnable() {
                            @Override
                            public void run() {
                                while (!connectionReady) ;
                                peerConn.setRemoteDescription(new mObserver(), sessionDescription);
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
        LocalBroadcastManager.getInstance(ServerSocket.appContext).registerReceiver(broadcastReceiver, filter);
        localReceiverInit = true;
    }

    public static PeerConnectionFactory initPeerConnFactory() {
        //create peerConn factory
        PeerConnectionFactory.InitializationOptions initOptions =
                PeerConnectionFactory.InitializationOptions.builder(RTCallApplication.application.getApplicationContext())
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


    public static void close() {
        peerConn.close();
        peerConnFactory.dispose();
    }
}
