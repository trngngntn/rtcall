package com.rtcall.net;

import android.content.Context;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

public class RTStream {

    private static class MyVideoSink implements VideoSink {
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

    public static Context appContext;

    private static VideoCapturer videoCapturer;

    private static VideoSource videoSource;
    private static AudioSource audioSource;

    private static VideoTrack localVideoTrack;
    private static AudioTrack localAudioTrack;

    public static SurfaceViewRenderer srfLocalStream;
    public static SurfaceViewRenderer srfRemoteStream;

    public static MediaStream localMediaStream;
    public static MediaStream remoteMeidaStream;
    public static EglBase.Context eglBaseContext;

    public static void prepareLocalMedia() {
        CameraEnumerator camEnumerator;
        if (Camera2Enumerator.isSupported(appContext)) {
            camEnumerator = new Camera2Enumerator(appContext);
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

        videoSource = RTConnection.peerConnFactory.createVideoSource(videoCapturer.isScreencast());
        videoCapturer.initialize(surfaceTextureHelper, appContext, videoSource.getCapturerObserver());
        videoCapturer.startCapture(1280, 720, 24);/*W,H,fps*/
        localVideoTrack = RTConnection.peerConnFactory.createVideoTrack("ID", videoSource);
        localVideoTrack.setEnabled(true);
        MyVideoSink localSink = new MyVideoSink();
        localVideoTrack.addSink(localSink);
        localSink.setTarget(srfLocalStream);

        MediaConstraints audioContraints = new MediaConstraints();
        audioSource = RTConnection.peerConnFactory.createAudioSource(audioContraints);
        localAudioTrack = RTConnection.peerConnFactory.createAudioTrack("ID", audioSource);
    }

    public static void initSurface(){
        srfLocalStream.setMirror(true);
        srfLocalStream.setEnableHardwareScaler(true);
        srfLocalStream.init(eglBaseContext, null);

        srfRemoteStream.setMirror(true);
        srfRemoteStream.setEnableHardwareScaler(true);
        srfRemoteStream.init(eglBaseContext, null);
    }

    public static void startLocalStream() {
        localMediaStream = RTConnection.peerConnFactory.createLocalMediaStream("LABEL");
        localMediaStream.addTrack(localAudioTrack);
        localMediaStream.addTrack(localVideoTrack);
    }

    public static void startRemoteStream() {
        VideoTrack remoteVideoTrack = remoteMeidaStream.videoTracks.get(0);
        AudioTrack remoteAudioTrack = remoteMeidaStream.audioTracks.get(0);
        remoteVideoTrack.setEnabled(true);
        remoteAudioTrack.setEnabled(true);
        MyVideoSink remoteSink = new MyVideoSink();
        remoteVideoTrack.addSink(remoteSink);
        remoteSink.setTarget(srfRemoteStream);

    }
}
