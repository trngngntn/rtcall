package com.rtcall.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import com.google.common.util.concurrent.ListenableFuture;
import com.rtcall.R;
import com.rtcall.net.ServerSocket;
import com.rtcall.net.message.S2CMessage;
import com.rtcall.services.RTStreamService;

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

import java.util.concurrent.ExecutionException;

public class CallActivity extends AppCompatActivity {
    String callerUid;

    boolean isCaller = false;

    Activity thisActivity;

    PreviewView srfRecord;
    SurfaceView srfPlay;
    SurfaceViewRenderer srfPlayRenderer;


    EglBase eglBase;

    private class TestBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v("LOG", "Received intent");
            if (intent.getAction().equals("SERVICE_MESSAGE")) {
                S2CMessage msg = (S2CMessage) intent.getExtras().get("message");
                if (msg.getType() == S2CMessage.MSG_PEER_ADDR && isCaller) {

                }
            } else {
                initiateConnection();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        thisActivity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        srfRecord = findViewById(R.id.srf_record);

        callerUid = getIntent().getExtras().getString("caller");
        Log.v("CALL_ACT", "-----------------Ccaller: " + callerUid);

        // local broadcast receiver
        CallActivity.TestBroadcastReceiver testBrd = new CallActivity.TestBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("SERVICE_MESSAGE");
        LocalBroadcastManager.getInstance(this).registerReceiver(testBrd, filter);

        //webrtc related
        eglBase = EglBase.create();
        srfPlayRenderer.setMirror(true);
        srfPlayRenderer.setEnableHardwareScaler(true);
        srfPlayRenderer.init(eglBase.getEglBaseContext(), null);


    }

    PeerConnectionFactory peerConnFactory;

    private void createCapturer() {
        VideoCapturer videoCapturer = null;
        CameraEnumerator camEnum;
        if (Camera2Enumerator.isSupported(this)) {
            camEnum = new Camera2Enumerator(this);
        } else {
            camEnum = new Camera1Enumerator(true);
        }

        String[] deviceList = camEnum.getDeviceNames();

        for (String device : deviceList) {
            if (camEnum.isFrontFacing(device)) {
                videoCapturer = camEnum.createCapturer(device, null);
                if (videoCapturer != null) {
                    break;
                }
            }
        }

        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.getEglBaseContext());
        VideoSource videoSource = peerConnFactory.createVideoSource(videoCapturer.isScreencast());
        videoCapturer.initialize(surfaceTextureHelper, getApplicationContext(), videoSource.getCapturerObserver());
        videoCapturer.startCapture(1280, 720, 30);/*W,H,fps*/

        VideoTrack videoTrack = peerConnFactory.createVideoTrack("ID", videoSource);
        videoTrack.setEnabled(true);
        MyVideoSink sink = new MyVideoSink();
        videoTrack.addSink(sink);
        sink.setTarget(srfPlayRenderer);

        MediaConstraints audioContraints = new MediaConstraints();

        AudioSource audioSource = peerConnFactory.createAudioSource(new MediaConstraints());
        AudioTrack audioTrack = peerConnFactory.createAudioTrack("ID", audioSource);

        /* To Streaming */

        MediaStream mediaStream = peerConnFactory.createLocalMediaStream("LABEL");
        mediaStream.addTrack(audioTrack);
        mediaStream.addTrack(videoTrack);


    }



    private class MyVideoSink implements VideoSink{
        private VideoSink target;
        @Override
        public void onFrame(VideoFrame videoFrame) {
            if(target == null){
                return;
            }
            target.onFrame(videoFrame);
        }
        synchronized public void setTarget(VideoSink target){
            this.target = target;
        }
    }

    ListenableFuture<ProcessCameraProvider> pcp;

    private void initiateConnection() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PackageManager.PERMISSION_GRANTED);
        }
        pcp = ProcessCameraProvider.getInstance(this);
        pcp.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = pcp.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));

        Intent startStreamServiceIntent = new Intent(this, RTStreamService.class);
        startService(startStreamServiceIntent);
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        preview.setSurfaceProvider(srfRecord.getSurfaceProvider());

        CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

        //binding camera
        try {
            cameraProvider.unbindAll();
            Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview);
        } catch (Exception e) {

        }
    }

    private void initSurfaces() {
        EglBase eglBase = EglBase.create();
        //srfPlay.init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //cameraExecutor.shutdown();
    }

}