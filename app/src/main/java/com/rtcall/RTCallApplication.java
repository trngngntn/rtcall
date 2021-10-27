package com.rtcall;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.CameraXConfig;

import com.rtcall.net.RTStream;

public class RTCallApplication extends Application implements CameraXConfig.Provider {
    private RTStream stream;

    @NonNull
    @Override
    public CameraXConfig getCameraXConfig() {
        return Camera2Config.defaultConfig();
    }

    public void createStream(){
        stream = new RTStream();
    }

    public RTStream getStream() {
        return stream;
    }

    public void endStream(){
        stream = null;
    }
}
