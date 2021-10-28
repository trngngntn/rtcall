package com.rtcall.net;

import android.util.Log;

import com.rtcall.net.message.NetMessage;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

public class CallerSdpObserver implements SdpObserver {

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        RTConnection.peerConn.setLocalDescription(this, sessionDescription);
        Log.e("DESC_ANSWER", sessionDescription.description);
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
