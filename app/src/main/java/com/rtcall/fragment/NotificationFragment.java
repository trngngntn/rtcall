package com.rtcall.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rtcall.R;
import com.rtcall.entity.Notification;
import com.rtcall.net.ServerSocket;
import com.rtcall.net.message.NetMessage;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

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
        rootView = inflater.inflate(R.layout.fragment_notification, container, false);
        // Inflate the layout for this fragment
        recViewContact = rootView.findViewById(R.id.frag_preferences);
        layoutManager = new LinearLayoutManager(getActivity());
        setLayoutManager(layoutManager);
        ServerSocket.queueMessage(NetMessage.Client.reqNotifMessage());
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
                    case NetMessage.Server.MSG_ALL_NOTIF: {
                        JsonArray jsonArray = null;
                        try {
                            jsonArray = msg.getData().getAsJsonArray("notifList");
                        } catch (Exception e) {
                            //e.printStackTrace();
                        }
                        List<Notification> notifList = new ArrayList<>();
                        if(jsonArray != null){
                            for (int i = 0; i < jsonArray.size(); i++) {
                                JsonObject jObj = jsonArray.get(i).getAsJsonObject();
                                notifList.add(new Notification(
                                        jObj.get("id").getAsInt(),
                                        jObj.get("timestamp").getAsString(),
                                        jObj.getAsJsonObject("data"),
                                        jObj.get("status").getAsInt()
                                ));
                            }
                        }
                        NotificationAdapter adapter = new NotificationAdapter(notifList);
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