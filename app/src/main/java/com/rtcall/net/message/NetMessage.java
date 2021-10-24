package com.rtcall.net.message;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class NetMessage implements Serializable {
    private static final String TAG = "NET_MESSAGE";

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
