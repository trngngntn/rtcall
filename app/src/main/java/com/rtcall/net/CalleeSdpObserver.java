package com.rtcall.net;

import com.rtcall.net.message.C2SMessage;

import org.webrtc.PeerConnection;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

public class CalleeSdpObserver implements SdpObserver {
    PeerConnection peerConn;

    public CalleeSdpObserver(PeerConnection peerConn) {
        this.peerConn = peerConn;
    }

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        peerConn.setLocalDescription(this, sessionDescription);
        ServerSocket.queueMessage(C2SMessage.createAnswerMessage(sessionDescription.description));
    }

    @Override
    public void onSetSuccess() {

    }

    @Override
    public void onCreateFailure(String s) {

    }

    @Override
    public void onSetFailure(String s) {

    }
}
