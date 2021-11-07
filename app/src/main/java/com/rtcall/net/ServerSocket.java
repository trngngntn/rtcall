package com.rtcall.net;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.rtcall.activity.IncomingCallActivity;
import com.rtcall.entity.User;
import com.rtcall.net.message.NetMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

public class ServerSocket {
    private static final String TAG = "NET_SOCKET";

    public static final int INFO_DISCONNECTED = 0;
    public static final int INFO_CONNECTED = 1;

    private static final String SERVER_HOST = "trngngntn.duckdns.org";
    private static final int SERVER_PORT = 42069;

    private static Socket socket;
    public static Context appContext;

    private static Queue<NetMessage> msgQueue;

    private static Thread listener;
    private static Thread queueProcesser;

    private static DataInputStream reader;
    private static DataOutputStream writer;


    public static void prepare(Context app) {
        appContext = app;
    }

    /**
     * Initiate a socket connection to server in both TCP and UDP protocol
     *
     * @return true if able to connect to server
     */
    public static void connect() {
        if (socket == null) {
            Intent intent = new Intent("SERVICE_INFO");
            try {
                socket = new Socket(SERVER_HOST, SERVER_PORT);
                reader = new DataInputStream(socket.getInputStream());
                writer = new DataOutputStream(socket.getOutputStream());
                Log.d(TAG, "Socket created");
                intent.putExtra("info", INFO_CONNECTED);
                LocalBroadcastManager.getInstance(appContext).sendBroadcast(intent);
            } catch (IOException e) {
                e.printStackTrace();
                intent.putExtra("info", INFO_DISCONNECTED);
                LocalBroadcastManager.getInstance(appContext).sendBroadcast(intent);
                return;
            }
        }
        msgQueue = new ArrayDeque<>();
        listener = new Thread(() -> {

            while (socket != null && !socket.isClosed()) {
                try {
                    if (reader.available() > 0) {
                        NetMessage msg = read();
                        Intent intent = new Intent("SERVICE_MESSAGE");
                        intent.putExtra("message", msg);
                        LocalBroadcastManager.getInstance(appContext).sendBroadcast(intent);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        queueProcesser = new Thread(() -> {
            while (socket != null && !socket.isClosed()) {
                if (!msgQueue.isEmpty()) {
                    try {
                        writer.write(msgQueue.peek().byteArray());
                        msgQueue.poll();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        listener.start();
        queueProcesser.start();
    }

    /**
     * @return
     */
    public static boolean isConnected() {
        return socket != null && reader != null && writer != null;
    }

    /**
     * Put new message to queue
     *
     * @param msg
     */
    public static void queueMessage(NetMessage msg) {
        msgQueue.add(msg);
        //Log.v(TAG, "Queued new message");
    }

    private static NetMessage read() {
        try {
            byte[] buffer = new byte[4];
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
            reader.readFully(buffer);
            int size = byteBuffer.getInt();
            byteBuffer = ByteBuffer.allocate(size + 4);
            reader.readFully(byteBuffer.array());
            return NetMessage.parseMessage(byteBuffer.array());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void close() {
        try {
            socket.getInputStream().close();
            socket.getOutputStream().close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket = null;
        }
    }
}
