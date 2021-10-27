package com.rtcall.entity;

import java.io.Serializable;

public class User implements Serializable {
    private String uid;
    private String displayName;

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
}
