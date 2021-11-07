package com.rtcall.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rtcall.R;
import com.rtcall.entity.User;

public class ContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    public static final int TYPE_ADD_CONTACT = 0;
    public static final int TYPE_CONTACT = 1;

    public static class ContactViewHolder extends RecyclerView.ViewHolder{
        private final TextView txtContact;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            txtContact = itemView.findViewById(R.id.txt_contact);
        }

        public void setContact(User user){
            txtContact.setText(user.getDisplayName());
        }
    }

    public static class AddContactViewHolder extends RecyclerView.ViewHolder{
        private final View btAddContact;

        public AddContactViewHolder(@NonNull View itemView) {
            super(itemView);
            btAddContact = itemView.findViewById(R.id.bt_add_contact);
            btAddContact.setOnClickListener(view -> {
                ContactFragment.AddContactDialog dialog = new ContactFragment.AddContactDialog(view.getContext());
                dialog.show();
            });
        }
    }

    private User[] localDataSet;

    public ContactAdapter(User[] contactList) {
        this.localDataSet = contactList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType){
            case TYPE_ADD_CONTACT:{
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_add_contact, parent, false);
                return new AddContactViewHolder(view);
            }
            case TYPE_CONTACT:{
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_contact, parent, false);
                return new ContactViewHolder(view);
            }
            default:
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(position > 0){
            ((ContactViewHolder)holder).setContact(localDataSet[position-1]);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position > 0 ? 1 : 0;
    }

    @Override
    public int getItemCount() {
        return localDataSet.length + 1;
    }

}
