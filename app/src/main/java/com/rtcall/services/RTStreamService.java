package com.rtcall.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.SurfaceHolder;

import com.rtcall.RTCallApplication;
import com.rtcall.net.ServerSocket;

import java.io.IOException;
import java.net.DatagramSocket;

public class RTStreamService extends Service {
    private static final String TAG = "RTC_STREAM_SERVICE";
    public RTStreamService() {
    }

    private ParcelFileDescriptor parcelFd;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;

    private void initRecorder() throws IOException {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

        mediaRecorder.setOutputFile(parcelFd.getFileDescriptor());

        //mediaRecorder.setOutputFile("/sdcard/video.mp4");

        mediaRecorder.prepare();
    }

    private void initPlayer() throws IOException {
        //mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(parcelFd.getFileDescriptor());
        //mediaPlayer.setDisplay(sh);
        mediaPlayer.prepare();
        mediaPlayer.start();
    }

    public void start(){
        mediaRecorder.start();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "Stream service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(ServerSocket.getDatagramSocket() == null){
            Log.e(TAG, "DatagramSocket is null.");
        }
        try {
            parcelFd = ParcelFileDescriptor.fromDatagramSocket(ServerSocket.getDatagramSocket());
            initRecorder();
            mediaPlayer = ((RTCallApplication)getApplication()).mediaPlayer;
            initPlayer();
            start();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to init stream");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}