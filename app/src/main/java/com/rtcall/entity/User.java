package com.rtcall.entity;

import java.io.Serializable;

public class User implements Serializable {
    private String uid;
    private String displayName;
    private boolean online;

    public User(String uid, String displayName) {
        this.uid = uid;
        this.displayName = displayName;
    }

    public String getUid() {
        return uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setStatus(boolean online){
        this.setStatus(online);
    }

    public boolean isOnline(){
        return online;
    }
}
