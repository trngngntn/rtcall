package com.rtcall.net;

import android.content.Context;

import com.rtcall.RTCallApplication;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

public class RTStream {

    private class MyVideoSink implements VideoSink {
        private VideoSink target;

        @Override
        public void onFrame(VideoFrame videoFrame) {
            if (target == null) {
                return;
            }
            target.onFrame(videoFrame);
        }

        synchronized public void setTarget(VideoSink target) {
            this.target = target;
        }
    }

    Context context;
    RTCallApplication application;

    VideoCapturer videoCapturer;

    VideoSource videoSource;
    AudioSource audioSource;

    SurfaceViewRenderer srfLocalVideo;
    SurfaceViewRenderer srfRemoteVideo;

    VideoTrack localVideoTrack;
    AudioTrack localAudioTrack;
    MediaStream localMediaStream;

    MediaStream remoteMeidaStream;

    EglBase.Context eglBaseContext;

    PeerConnectionFactory peerConnFactory;

    public RTStream(){
        
    }

    public void prepareLocalMedia() {
        CameraEnumerator camEnumerator;
        if (Camera2Enumerator.isSupported(context)) {
            camEnumerator = new Camera2Enumerator(context);
        } else {
            camEnumerator = new Camera1Enumerator(true);
        }

        String[] deviceList = camEnumerator.getDeviceNames();

        for (String device : deviceList) {
            if (camEnumerator.isFrontFacing(device)) {
                videoCapturer = camEnumerator.createCapturer(device, null);
                if (videoCapturer != null) {
                    break;
                }
            }
        }

        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBaseContext);

        videoSource = peerConnFactory.createVideoSource(videoCapturer.isScreencast());
        videoCapturer.initialize(surfaceTextureHelper, application, videoSource.getCapturerObserver());
        videoCapturer.startCapture(1280, 720, 30);/*W,H,fps*/
        localVideoTrack = peerConnFactory.createVideoTrack("ID", videoSource);
        localVideoTrack.setEnabled(true);
        MyVideoSink sink = new MyVideoSink();
        localVideoTrack.addSink(sink);
        sink.setTarget(srfLocalVideo);

        MediaConstraints audioContraints = new MediaConstraints();
        audioSource = peerConnFactory.createAudioSource(audioContraints);
        localAudioTrack = peerConnFactory.createAudioTrack("ID", audioSource);
    }

    public void startLocalStream() {
        localMediaStream = peerConnFactory.createLocalMediaStream("LABEL");
        localMediaStream.addTrack(localAudioTrack);
        localMediaStream.addTrack(localVideoTrack);
    }

    public void setRemoteMeidaStream(MediaStream stream){
        this.remoteMeidaStream = stream;
    }

}
