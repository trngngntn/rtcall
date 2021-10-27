package com.rtcall.net.message;

import org.json.JSONObject;

public class S2CMessage extends NetMessage{
    public final static int MSG_LOGGED_IN = 0x01;
    public final static int MSG_PEER_ADDR = 0x02;
    public final static int MSG_REQUEST_CALL = 0x0B;

    public final static int MSG_CALL_ENDED = 0x09;
    public final static int MSG_CALL_REJECTED = 0x09;
    public static final int MSG_CALL_ACCEPTED = 0x10;

    private int type;
    private String data;

    public S2CMessage(NetMessage msg){
        super(msg.getType(), msg.getData());
    }

    protected S2CMessage(int type, JSONObject data) {
        super(type, data);
    }
}
