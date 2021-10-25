package com.rtcall;

import android.app.Application;
import android.media.MediaPlayer;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.CameraXConfig;

public class RTCallApplication extends Application implements CameraXConfig.Provider {
    @NonNull
    @Override
    public CameraXConfig getCameraXConfig() {
        return Camera2Config.defaultConfig();
    }

    public SurfaceView srfStream;

    public MediaPlayer mediaPlayer;
}
