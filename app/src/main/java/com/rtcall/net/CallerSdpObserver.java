package com.rtcall.net;

import com.rtcall.net.message.NetMessage;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

public class CallerSdpObserver implements SdpObserver {

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {

        RTConnection.peerConn.setLocalDescription(this, sessionDescription);
        ServerSocket.queueMessage(NetMessage.Relay.answerMessage(sessionDescription.description));
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
