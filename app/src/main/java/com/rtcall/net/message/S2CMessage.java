package com.rtcall.net.message;

import org.json.JSONObject;

public class S2CMessage extends NetMessage{
    public final static int MSG_LOGGED_IN = 0x01;
    public final static int MSG_PEER_ADDR = 0x02;
    public final static int MSG_REQUEST_CALL = 0x0B;

    private int type;
    private String data;

    public S2CMessage(NetMessage msg){
        super(msg.getType(), msg.getData());
    }

    protected S2CMessage(int type, JSONObject data) {
        super(type, data);
    }
}
