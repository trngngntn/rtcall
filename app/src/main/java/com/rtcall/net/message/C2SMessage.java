package com.rtcall.net.message;

import org.json.JSONException;
import org.json.JSONObject;

public class C2SMessage extends NetMessage {
    private final static int MSG_LOGIN = 0x02;
    private final static int MSG_DIAL = 0x04;
    private final static int MSG_PING = 0x05;
    private final static int MSG_CANDIDATE = 0x06;
    private final static int MSG_DECLINE_CALL = 0x1B;

    protected C2SMessage(int type, JSONObject data) {
        super(type, data);
    }

    public static C2SMessage createLoginMessage(String username, String password) {
        JSONObject data = new JSONObject();
        try {
            data.put("uid", username);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new C2SMessage(MSG_LOGIN, data);
    }

    public static C2SMessage createDialMessage(String uid) {
        JSONObject data = new JSONObject();
        try {
            data.put("uid", uid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new C2SMessage(MSG_DIAL, data);
    }

    public static C2SMessage createDeclineCallMessage(String message) {
        JSONObject data = new JSONObject();
        try {
            if (message.equals(""))
                data.put("hasMessage", false);
            else {
                data.put("hasMessage", true);
                data.put("message", message);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new C2SMessage(MSG_DECLINE_CALL, data);
    }

    public static C2SMessage createPingMessage(String callerUid) {
        JSONObject data = new JSONObject();
        try {
            data.put("otherPeer", callerUid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new C2SMessage(MSG_DECLINE_CALL, data);
    }

    public static C2SMessage createCandidateMessage(String sdpId, int sdpMLineIndex, String sdp){
        JSONObject data = new JSONObject();
        try {
            data.put("sdpId", sdpId);
            data.put("label", sdpMLineIndex);
            data.put("sdp", sdp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new C2SMessage(MSG_CANDIDATE, data);
    }
}
