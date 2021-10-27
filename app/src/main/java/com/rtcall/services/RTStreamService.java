package com.rtcall.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.rtcall.activities.CallActivity;
import com.rtcall.net.ServerSocket;

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

import java.io.IOException;
import java.net.DatagramSocket;

public class RTStreamService extends Service {
    private static final String TAG = "SERVICE_STREAM";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "Created!");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}