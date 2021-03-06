package com.rtcall.net;

import android.util.Log;

import com.rtcall.net.message.NetMessage;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

public class CalleeSdpObserver implements SdpObserver {

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        RTConnection.peerConn.setLocalDescription(this, sessionDescription);
        Log.e("DESC_OFFER", sessionDescription.description);
        ServerSocket.queueMessage(NetMessage.Relay.offerMessage(sessionDescription.description));
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
