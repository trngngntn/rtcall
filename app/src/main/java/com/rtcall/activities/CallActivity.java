package com.rtcall.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
//import androidx.camera.video.ActiveRecording;
//import androidx.camera.video.FileDescriptorOutputOptions;
//import androidx.camera.video.PendingRecording;
//import androidx.camera.video.QualitySelector;
//import androidx.camera.video.Recorder;
//import androidx.camera.video.VideoCapture;
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
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;
import com.rtcall.R;
import com.rtcall.net.ServerSocket;
import com.rtcall.net.message.S2CMessage;
import com.rtcall.services.RTStreamService;

import java.net.DatagramSocket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CallActivity extends AppCompatActivity {
    String callerUid;

    boolean isCaller = false;

    Activity thisActivity;
    ExecutorService cameraExecutor;

    PreviewView srfRecord;
    PreviewView srfStream;

    private class TestBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v("LOG", "Received intent");
            if(intent.getAction().equals("SERVICE_MESSAGE")){
                S2CMessage msg = (S2CMessage) intent.getExtras().get("message");
                if(msg.getType() == S2CMessage.MSG_PEER_ADDR && isCaller) {
                    ServerSocket.ping(thisActivity,callerUid);
                    Log.e("PING", "caller: " + callerUid);
                }
            } else {
                initiateConnection();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        thisActivity = this;
        ServerSocket.setContext(this.getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        srfRecord = findViewById(R.id.srf_record);

        callerUid = getIntent().getExtras().getString("caller");
        Log.v("CALL_ACT", "-----------------Ccaller: " + callerUid);

        // local broadcast receiver
        CallActivity.TestBroadcastReceiver testBrd = new CallActivity.TestBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("SERVICE_MESSAGE");
        filter.addAction("SOCKET_CREATED");
        LocalBroadcastManager.getInstance(this).registerReceiver(testBrd, filter);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PackageManager.PERMISSION_GRANTED);
        }

        //from call receiver only
        boolean doPing = getIntent().getBooleanExtra("doPing", false);
        if(doPing){
            ServerSocket.ping(thisActivity, callerUid);
        } else {
            isCaller = true;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //cameraExecutor.shutdown();
    }

    /**
     * Unusable
     */
    /*private void bindPreviewX(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        preview.setSurfaceProvider(srfRecord.getSurfaceProvider());

        CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;


        QualitySelector qualitySelector = QualitySelector.firstTry(QualitySelector.QUALITY_HD).finallyTry(QualitySelector.QUALITY_SD);
        Recorder recorder = new Recorder.Builder()
                .setQualitySelector(qualitySelector)
                .build();
        VideoCapture videoCapture = VideoCapture.withOutput(recorder);

        //binding camera
        try {
            cameraProvider.unbindAll();
            Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture);
        } catch (Exception e) {

        }

        DatagramSocket socket = ServerSocket.getDatagramSocket();

        ParcelFileDescriptor parcelFd = ParcelFileDescriptor.fromDatagramSocket(socket);

        FileDescriptorOutputOptions fdOutputOptions = new FileDescriptorOutputOptions.Builder(parcelFd).build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        PendingRecording pr = recorder.prepareRecording(this, fdOutputOptions).withAudioEnabled();

        ActiveRecording ar = pr.start();
    }*/
}