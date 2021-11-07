package com.rtcall.fragment;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rtcall.R;
import com.rtcall.RTCallApplication;
import com.rtcall.entity.Notification;
import com.rtcall.entity.User;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    public interface NotificationItem{
        void setNotification(Notification notification);
    }

    public static class MissedCallNotifViewHolder extends RecyclerView.ViewHolder implements NotificationItem{
        private Notification notification;

        private Button btCall;
        private TextView txtInfo;

        public MissedCallNotifViewHolder(@NonNull View itemView) {
            super(itemView);
            btCall = itemView.findViewById(R.id.bt_call_now);
            txtInfo = itemView.findViewById(R.id.txt_notif_info);

            btCall.setOnClickListener(view -> {

            });
        }

        @Override
        public void setNotification(Notification notification) {
            this.notification = notification;
            String user = notification.getData().get("uid").getAsString();
            String notifInfo = "Missed call from " + User.getUser(user).getDisplayName();
            SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
            StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
            stringBuilder.setSpan(boldSpan, 17, notifInfo.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            txtInfo.setText(stringBuilder);
        }
    }

    public static class PendingContactNotifViewHolder extends RecyclerView.ViewHolder implements NotificationItem{
        private Notification notification;

        private Button btApprove;
        private Button btReject;
        private TextView txtInfo;

        public PendingContactNotifViewHolder(@NonNull View itemView) {
            super(itemView);
            btApprove = itemView.findViewById(R.id.bt_accept_contact);
            btReject = itemView.findViewById(R.id.bt_decline_contact);
            txtInfo = itemView.findViewById(R.id.txt_notif_info);

            btApprove.setOnClickListener(view -> {

            });

            btReject.setOnClickListener(view -> {

            });
        }

        @Override
        public void setNotification(Notification notification) {
            this.notification = notification;
            String user = notification.getData().get("uid").getAsString();
            String notifInfo = "Pending contact from " + User.getUser(user).getDisplayName();
            SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
            StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
            stringBuilder.setSpan(boldSpan, 17, notifInfo.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            txtInfo.setText(stringBuilder);
        }
    }

    private Notification[] localDataSet;

    public NotificationAdapter(Notification[] notifList) {
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
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((NotificationItem)holder).setNotification(localDataSet[position]);
    }

    @Override
    public int getItemViewType(int position) {
        return localDataSet[position].getData().get("type").getAsInt();
    }

    @Override
    public int getItemCount() {
        return localDataSet.length + 1;
    }

}
