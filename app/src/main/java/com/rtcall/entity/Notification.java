package com.rtcall.entity;

import com.google.gson.JsonObject;

import java.sql.Timestamp;

public class Notification {
    public static final int STATUS_HIDDEN = -1;
    public static final int STATUS_UNREAD = 0;
    public static final int STATUS_READ = 1;

    private int id;
    private String uid;
    private Timestamp timestamp;
    private JsonObject data;
    private int status;
}
