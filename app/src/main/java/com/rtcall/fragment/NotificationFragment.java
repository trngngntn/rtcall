package com.rtcall.fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.rtcall.R;
import com.rtcall.RTCallApplication;
import com.rtcall.entity.User;
import com.rtcall.net.ServerSocket;
import com.rtcall.net.message.NetMessage;

public class NotificationFragment extends Fragment {
    private RTCallApplication app;

    private View rootView;
    private RecyclerView recViewContact;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_contact, container, false);
        // Inflate the layout for this fragment
        recViewContact = rootView.findViewById(R.id.frag_preferences);
        layoutManager = new LinearLayoutManager(getActivity());
        setLayoutManager(layoutManager);
        app = (RTCallApplication) requireActivity().getApplication();
        ServerSocket.queueMessage(NetMessage.Client.reqContactMessage());
        initLocalBroadcastReceiver();
        return rootView;
    }

    private void initLocalBroadcastReceiver() {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v("LOG", "Received intent");
                NetMessage msg = (NetMessage) intent.getExtras().get("message");
                switch (msg.getType()) {
                    case NetMessage.Server.MSG_CONTACT_LIST: {
                        JsonArray jsonArray = null;
                        try {
                            jsonArray = msg.getData().getAsJsonArray("contactList");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (jsonArray != null) {
                            app.contacts = new User[jsonArray.size()];
                            for (int i = 0; i < jsonArray.size(); i++) {
                                app.contacts[i] = new User(
                                        jsonArray.get(i).getAsJsonObject().get("uid").getAsString(),
                                        jsonArray.get(i).getAsJsonObject().get("displayName").getAsString()
                                );
                            }
                        } else {
                            app.contacts = new User[0];
                        }
                        ContactAdapter adapter = new ContactAdapter(app.contacts);
                        recViewContact.setAdapter(adapter);
                    }
                    break;
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("SERVICE_MESSAGE");
        LocalBroadcastManager.getInstance(rootView.getContext()).registerReceiver(broadcastReceiver, filter);
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        if (recViewContact.getLayoutManager() == null) {

        } else {

        }

        recViewContact.setLayoutManager(layoutManager);
    }
}