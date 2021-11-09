package com.rtcall.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.rtcall.R;
import com.rtcall.net.message.NetMessage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    public static final int CONTACT_FRAG = 0;
    public static final int NOTIF_FRAG = 1;

    private NavHostFragment navHostFragment;
    private NavController navController;
    private BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        navController = navHostFragment.getNavController();
        navView = findViewById(R.id.nav_view);

        NavigationUI.setupWithNavController(navView, navController);
        initLocalBroadcastReceiver();

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            int frag = getIntent().getExtras().getInt("frag", 0);
            if(frag == NOTIF_FRAG){
                switchFragment(NOTIF_FRAG);
            }
        }
    }

    public void switchFragment(int frag){
        if (frag == NOTIF_FRAG) {
            navController.navigate(R.id.nav_notifications);
        } else {
            navController.navigate(R.id.nav_contacts);
        }
    }

    private void initLocalBroadcastReceiver() {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int frag = intent.getExtras().getInt("frag");
                switchFragment(frag);
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("SWITCH_FRAGMENT");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, filter);
    }

}