package com.rtcall.fragment;

import android.content.Intent;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rtcall.R;
import com.rtcall.RTCallApplication;
import com.rtcall.activity.MainActivity;
import com.rtcall.activity.OutgoingCallActivity;
import com.rtcall.entity.Notification;
import com.rtcall.entity.User;
import com.rtcall.net.ServerSocket;
import com.rtcall.net.message.NetMessage;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    public interface NotificationItem{
        void setNotification(Notification notification);
    }

    public static class MissedCallNotifViewHolder extends RecyclerView.ViewHolder implements NotificationItem{
        protected static View lastItem;
        private Notification notification;

        private Button btCall;
        private TextView txtInfo;

        public MissedCallNotifViewHolder(@NonNull View itemView) {
            super(itemView);
            btCall = itemView.findViewById(R.id.bt_call_now);
            txtInfo = itemView.findViewById(R.id.txt_notif_info);

            itemView.setOnClickListener(view -> {
                if(lastItem != null) {
                    ViewGroup.LayoutParams lastLp = lastItem.getLayoutParams();
                    lastLp.height = (int) RTCallApplication.application
                            .getApplicationContext().getResources().getDimension(R.dimen.default_row_item);
                    lastItem.setLayoutParams(lastLp);
                }
                ViewGroup.LayoutParams lp = itemView.getLayoutParams();
                lp.height = (int) RTCallApplication.application.getApplicationContext().getResources().getDimension(R.dimen.expaned_row_item);
                itemView.setLayoutParams(lp);
                lastItem = itemView;
            });

            btCall.setOnClickListener(view -> {
                Intent i = new Intent(RTCallApplication.application, OutgoingCallActivity.class);
                User user = User.getUser(notification.getData().get("fromUid").getAsString());
                i.putExtra("callee", user);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                RTCallApplication.application.startActivity(i);
            });
        }

        @Override
        public void setNotification(Notification notification) {
            this.notification = notification;
            String user = notification.getData().get("fromUid").getAsString();
            String notifInfo = "Missed call from " + notification.getData().get("userDisplay").getAsString();
            SpannableStringBuilder stringBuilder = new SpannableStringBuilder(notifInfo);
            StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
            stringBuilder.setSpan(boldSpan, 17, notifInfo.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            txtInfo.setText(stringBuilder);
        }
    }

    public static class PendingContactNotifViewHolder extends RecyclerView.ViewHolder implements NotificationItem{
        protected static View lastItem;
        private Notification notification;

        private Button btApprove;
        private Button btReject;
        private TextView txtInfo;

        public PendingContactNotifViewHolder(@NonNull View itemView) {
            super(itemView);
            btApprove = itemView.findViewById(R.id.bt_accept_contact);
            btReject = itemView.findViewById(R.id.bt_decline_contact);
            txtInfo = itemView.findViewById(R.id.txt_notif_info);

            itemView.setOnClickListener(view -> {
                if(lastItem != null) {
                    ViewGroup.LayoutParams lastLp = lastItem.getLayoutParams();
                    lastLp.height = (int) RTCallApplication.application
                            .getApplicationContext().getResources().getDimension(R.dimen.default_row_item);
                    lastItem.setLayoutParams(lastLp);
                }
                ViewGroup.LayoutParams lp = itemView.getLayoutParams();
                lp.height = (int) RTCallApplication.application.getApplicationContext().getResources().getDimension(R.dimen.expaned_row_item);
                itemView.setLayoutParams(lp);
                lastItem = itemView;
            });

            btApprove.setOnClickListener(view -> {
                String uid = notification.getData().get("fromUid").getAsString();
                ServerSocket.queueMessage(NetMessage.Relay.approveContactMessage(uid, notification.getId()));
                NotificationAdapter adapter = (NotificationAdapter) getBindingAdapter();
                adapter.localDataSet.remove(notification);
                adapter.notifyItemRemoved(getAbsoluteAdapterPosition());
                Intent intent = new Intent("SWITCH_FRAGMENT");
                intent.putExtra("frag", MainActivity.CONTACT_FRAG);
                LocalBroadcastManager.getInstance(RTCallApplication.application).sendBroadcast(intent);
            });

            btReject.setOnClickListener(view -> {
                String uid = notification.getData().get("fromUid").getAsString();
                ServerSocket.queueMessage(NetMessage.Relay.rejectContactMessage(uid, notification.getId()));
                NotificationAdapter adapter = (NotificationAdapter) getBindingAdapter();
                adapter.localDataSet.remove(notification);
                adapter.notifyItemRemoved(getAbsoluteAdapterPosition());
            });
        }

        @Override
        public void setNotification(Notification notification) {
            this.notification = notification;
            String name = notification.getData().get("userDisplay").getAsString();
            String notifInfo = "Pending contact from " + name;
            SpannableStringBuilder stringBuilder = new SpannableStringBuilder(notifInfo);
            StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
            stringBuilder.setSpan(boldSpan, 20, notifInfo.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            txtInfo.setText(stringBuilder);
        }
    }

    protected List<Notification> localDataSet;

    public NotificationAdapter(List<Notification> notifList) {
        this.localDataSet = notifList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType){
            case Notification.TYPE_MISSED_CALL:{
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_notif_missed_call, parent, false);
                return new MissedCallNotifViewHolder(view);
            }
            case Notification.TYPE_PENDING_CONTACT:{
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_notif_contact_pending, parent, false);
                return new PendingContactNotifViewHolder(view);
            }
            default:
        }
        Log.e("NOTIF", "VIEWTYPE: " + viewType);
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((NotificationItem)holder).setNotification(localDataSet.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        return localDataSet.get(position).getData().get("type").getAsInt();
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}
