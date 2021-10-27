package com.rtcall.net;

import android.content.Context;

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

public class WebRTCConn {
    private static PeerConnection peerConn;
    private static PeerConnectionFactory peerConnFactory;
    private static Context appContext;

    private static EglBase.Context eglBaseContext;



    public static void initPeerConnFactory(){
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

        /* */

        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        PeerConnection.IceServer external = PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer();
        iceServers.add(external);

        PeerConnection.RTCConfiguration rtcConf = new PeerConnection.RTCConfiguration(iceServers);

    }

    private void call(){
        MediaConstraints mediaConstraints = new MediaConstraints();
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        peerConn.createOffer(new CallerSdpObserver(peerConn), mediaConstraints);
    }

    private void answer(){
        peerConn.createAnswer(new CalleeSdpObserver(peerConn), new MediaConstraints());
    }


}
