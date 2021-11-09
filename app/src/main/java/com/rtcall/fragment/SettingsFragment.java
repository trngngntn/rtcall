package com.rtcall.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.rtcall.R;
import com.rtcall.RTCallApplication;
import com.rtcall.activity.LoginActivity;
import com.rtcall.activity.SplashActivity;
import com.rtcall.service.RTCallService;

public class SettingsFragment extends Fragment {

    private View rootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        View btLogout = rootView.findViewById(R.id.bt_logout);
        btLogout.setOnClickListener(view -> {
            SharedPreferences prefs = RTCallApplication.application.getSharedPreferences("localData", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("loggedUid");
            editor.apply();
            Intent intentS = new Intent(RTCallApplication.application, RTCallService.class);
            intentS.putExtra("logout", true);
            RTCallApplication.application.startService(intentS);
            Intent intent = new Intent(RTCallApplication.application, LoginActivity.class);
            getActivity().finish();
            getActivity().startActivity(intent);

        });
        return rootView;
    }
}