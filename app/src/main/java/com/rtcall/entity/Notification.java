package com.rtcall.entity;

import com.google.gson.JsonObject;

import java.io.Serializable;
import java.sql.Timestamp;

public class Notification implements Serializable {

    public static final int TYPE_MISSED_CALL = 0;
    public static final int TYPE_PENDING_CONTACT = 1;

    public static final int STATUS_HIDDEN = -1;
    public static final int STATUS_UNREAD = 0;
    public static final int STATUS_READ = 1;

    private int id;
    private String timestamp;
    private JsonObject data;
    private int status;

    public Notification(int id, String timestamp, JsonObject data, int status) {
        this.id = id;
        this.timestamp = timestamp;
        this.data = data;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public JsonObject getData() {
        return data;
    }

    public int getStatus() {
        return status;
    }

    public void dismiss(){

    }

    public static void markAsRead(){

    }
}
