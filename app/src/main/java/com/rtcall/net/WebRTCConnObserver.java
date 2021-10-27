package com.rtcall.net;

import android.util.Log;

import com.rtcall.RTCallApplication;
import com.rtcall.net.message.C2SMessage;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

public class WebRTCConnObserver implements PeerConnection.Observer {
    private static final String TAG = "NET_OBSERVER";

    public WebRTCConnObserver() {
    }

    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        Log.d(TAG, "signalingChanged");
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        Log.d(TAG, "iceConnectionChanged");
    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {
        Log.d(TAG, "iceConnectionReceivingChanged");
    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        Log.d(TAG, "iceGatheringChanged");
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        Log.d(TAG, "IceCandidate");
        C2SMessage msg = C2SMessage.createCandidateMessage(
                iceCandidate.sdpMid,
                iceCandidate.sdpMLineIndex,
                iceCandidate.sdp);
        ServerSocket.queueMessage(msg);
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {

    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        //application.getStream().setRemoteMeidaStream(mediaStream);
    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {

    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {

    }

    @Override
    public void onRenegotiationNeeded() {

    }

    @Override
    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {

    }
}
