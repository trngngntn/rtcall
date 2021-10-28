package com.rtcall.net.message;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class NetMessage implements Serializable {
    private static final String TAG = "NET_MESSAGE";



    public static class Client{
        private static final String TAG = "NET_CLIENT_MESSAGE";

        public final static int MSG_LOGIN = 0x01;
        public final static int MSG_DIAL = 0x02;

        public static NetMessage loginMessage(String username, String password) {
            JSONObject data = new JSONObject();
            try {
                data.put("uid", username);
                data.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return new NetMessage(MSG_LOGIN, data);
        }
        public static NetMessage dialMessage(String calleeUid) {
            JSONObject data = new JSONObject();
            try {
                data.put("uid", calleeUid);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return new NetMessage(MSG_DIAL, data);
        }

    }

    public static class Server{
        private static final String TAG = "NET_SERVER_MESSAGE";

        public final static int MSG_BAD_IDENTITY = 0x00;
        public final static int MSG_LOGGED_IN = 0x01;
        public final static int MSG_CONTACT_LIST = 0x02;
        public final static int MSG_FRIEND_REQUEST = 0x03;

        public final static int MSG_REQUEST_CALL = 0x10;
    }

    public static class Relay{
        private static final String TAG = "NET_RELAY_MESSAGE";
        
        public static final int MSG_CALL_ACCEPTED = 0x20;
        public static final int MSG_CALL_DECLINED = 0x21;
        public static final int MSG_CALL_ENDED = 0x22;

        public static final int MSG_WEBRTC_CANDIDATE = 0x30;
        public static final int MSG_WEBRTC_OFFER = 0x31;
        public static final int MSG_WEBRTC_ANSWER = 0x32;

        public static NetMessage acceptCallMessage() {
            return new NetMessage(MSG_CALL_ACCEPTED, new JSONObject());
        }

        public static NetMessage declineCallMessage() {
            return new NetMessage(MSG_CALL_DECLINED, new JSONObject());
        }

        public static NetMessage endCallMessage() {
            return new NetMessage(MSG_CALL_ENDED, new JSONObject());
        }

        public static NetMessage candidateMessage(String sdpMid, int sdpMLineIndex, String sdp) {
            JSONObject data = new JSONObject();
            try {
                data.put("sdpMid", sdpMid);
                data.put("sdpMLineIndex", sdpMLineIndex);
                data.put("sdp", sdp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return new NetMessage(MSG_WEBRTC_CANDIDATE, data);
        }

        public static NetMessage offerMessage(String description) {
            JSONObject data = new JSONObject();
            try {
                data.put("description", description);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return new NetMessage(MSG_WEBRTC_OFFER, data);
        }

        public static NetMessage answerMessage(String description) {
            JSONObject data = new JSONObject();
            try {
                data.put("description", description);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return new NetMessage(MSG_WEBRTC_ANSWER, data);
        }
    }

    private int type;
    private JSONObject data;

    private NetMessage(){ }

    protected NetMessage(int type, JSONObject data) {
        this.type = type;
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public JSONObject getData() {
        return data;
    }

    public byte[] byteArray(){
        Log.d(TAG, data.toString());
        byte[] byteData = data.toString().getBytes(StandardCharsets.UTF_8);
        int size = byteData.length ;
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
        Log.v(TAG, "type: " + result.type);
        String stringData = new String(byteData, 4, byteData.length - 4);
        Log.v(TAG, stringData);
        try {
            result.data = new JSONObject(stringData);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("NET", "Invalid message format");
        }
        return result;
    }
}
