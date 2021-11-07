package com.rtcall.net.message;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class NetMessage implements Serializable {
    private static final String TAG = "NET_MESSAGE";
    private static final Gson gson = new Gson();

    public static class Client {
        private static final String TAG = "NET_CLIENT_MESSAGE";

        public final static int MSG_LOGIN = 0x01;
        public final static int MSG_REGISTER = 0x02;
        public final static int MSG_DIAL = 0x03;
        public final static int MSG_REQ_CONTACT = 0x04;
        public final static int MSG_ADD_CONTACT = 0x05;
        public final static int MSG_APPROVE_CONTACT = 0x06;
        public final static int MSG_REJECT_CONTACT = 0x07;

        public static NetMessage loginMessage(String username, String password) {
            JsonObject data = new JsonObject();
            data.addProperty("username", username);
            data.addProperty("password", password);
            return new NetMessage(MSG_LOGIN, data);
        }

        public static NetMessage registerMessage(String display, String username, String password){
            JsonObject data = new JsonObject();
            data.addProperty("display", display);
            data.addProperty("username", username);
            data.addProperty("password", password);
            return new NetMessage(MSG_REGISTER, data);
        }

        public static NetMessage dialMessage(String calleeUid) {
            JsonObject data = new JsonObject();
            data.addProperty("uid", calleeUid);
            return new NetMessage(MSG_DIAL, data);
        }

        public static NetMessage reqContactMessage() {
            JsonObject data = new JsonObject();
            return new NetMessage(MSG_REQ_CONTACT, data);
        }

        public static NetMessage addContactMessage(String username) {
            JsonObject data = new JsonObject();
            data.addProperty("uid", username);
            return new NetMessage(MSG_ADD_CONTACT, data);
        }
    }

    public static class Server {
        private static final String TAG = "NET_SERVER_MESSAGE";

        public final static int MSG_BAD_IDENTITY = 0x00;
        public final static int MSG_LOGGED_IN = 0x01;

        public static final int MSG_REGISTERED = 0x02;
        public static final int MSG_REGISTER_FAILED = 0x03;

        public final static int MSG_CONTACT_LIST = 0x04;
        public final static int MSG_CONTACT_INVALID = 0x05;
        public final static int MSG_CONTACT_PENDING = 0x06;
        public final static int MSG_CONTACT_APPROVED = 0x07;

        public final static int MSG_ALL_NOTIF = 0x08;
        public final static int MSG_UNREAD_NOTIF = 0x09;
        public final static int MSG_NEW_NOTIF = 0x10;

        public final static int MSG_REQUEST_CALL = 0x11;
    }

    public static class Relay {
        private static final String TAG = "NET_RELAY_MESSAGE";

        public static final int MSG_CALL_ACCEPTED = 0x20;
        public static final int MSG_CALL_DECLINED = 0x21;
        public static final int MSG_CALL_ENDED = 0x22;

        public static final int MSG_WEBRTC_CANDIDATE = 0x30;
        public static final int MSG_WEBRTC_OFFER = 0x31;
        public static final int MSG_WEBRTC_ANSWER = 0x32;

        public static NetMessage acceptCallMessage() {
            JsonObject data = new JsonObject();
            data.addProperty("timestamp", "");
            return new NetMessage(MSG_CALL_ACCEPTED, data);
        }

        public static NetMessage declineCallMessage() {
            JsonObject data = new JsonObject();
            data.addProperty("timestamp", "");
            return new NetMessage(MSG_CALL_DECLINED, data);
        }

        public static NetMessage endCallMessage() {
            JsonObject data = new JsonObject();
            data.addProperty("timestamp", "");
            return new NetMessage(MSG_CALL_ENDED, data);
        }

        public static NetMessage candidateMessage(String sdpMid, int sdpMLineIndex, String sdp) {
            JsonObject data = new JsonObject();
            data.addProperty("sdpMid", sdpMid);
            data.addProperty("sdpMLineIndex", sdpMLineIndex);
            data.addProperty("sdp", sdp);
            return new NetMessage(MSG_WEBRTC_CANDIDATE, data);
        }

        public static NetMessage offerMessage(String description) {
            JsonObject data = new JsonObject();
            data.addProperty("description", description);
            return new NetMessage(MSG_WEBRTC_OFFER, data);
        }

        public static NetMessage answerMessage(String description) {
            JsonObject data = new JsonObject();
            data.addProperty("description", description);
            return new NetMessage(MSG_WEBRTC_ANSWER, data);
        }
    }

    private int type;
    private JsonObject data;

    private NetMessage() {
    }

    protected NetMessage(int type, JsonObject data) {
        Log.d(TAG, String.format("Message type=%x, length=%d", type, data.toString().getBytes(StandardCharsets.UTF_8).length));
        this.type = type;
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public JsonObject getData() {
        return data;
    }

    public byte[] byteArray() {
        byte[] byteData = data.toString().getBytes();
        int size = byteData.length;
        ByteBuffer buffer = ByteBuffer.allocate(size + 8);
        buffer.putInt(size);
        buffer.putInt(type);
        buffer.put(byteData);
        return buffer.array();
    }

    public static NetMessage parseMessage(byte[] byteData) {
        ByteBuffer buffer = ByteBuffer.wrap(byteData);
        NetMessage result = new NetMessage();
        result.type = buffer.getInt();
        //Log.v(TAG, "type: " + result.type);
        String stringData = new String(byteData, 4, byteData.length - 4);
        //Log.v(TAG, stringData);
        result.data = gson.fromJson(stringData, JsonObject.class);
        return result;
    }
}
