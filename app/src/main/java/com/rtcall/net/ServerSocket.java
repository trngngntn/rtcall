package com.rtcall.net;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.rtcall.net.message.C2SMessage;
import com.rtcall.net.message.NetMessage;
import com.rtcall.net.message.S2CMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

public class ServerSocket {
    private static final String SERVER_HOST = "trngngntn.duckdns.org";
    private static final int SERVER_PORT = 42069;

    private static Socket socket;
    private static Context currentContext;

    private static Queue<C2SMessage> msgQueue;

    private static Thread listener;
    private static Thread queueProcesser;

    private static InputStream reader;
    private static OutputStream writer;

    private static DatagramSocket fSocket;

    /**
     *  Set context for LocalBroadcaster to work
     *
     * @param context
     */
    public static void setContext(Context context) {
        currentContext = context;
    }

    /**
     * Initiate a socket connection to server in both TCP and UDP protocol
     *
     * @return true if able to connect to server
     */
    public static boolean connect() {
        if (socket == null) {
            try {
                socket = new Socket(SERVER_HOST, SERVER_PORT);
                reader = socket.getInputStream();
                writer = socket.getOutputStream();
                Log.d("NetService", "Socket created");
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        msgQueue = new ArrayDeque<>();
        listener = new Thread(() -> {

            while (socket != null && socket.isClosed() == false) {
                try {
                    if (reader.available() > 0) {
                        S2CMessage msg = read();

                        Intent intent = new Intent("SERVICE_MESSAGE");
                        intent.putExtra("message", msg);
                        if (currentContext != null)
                            LocalBroadcastManager.getInstance(currentContext).sendBroadcast(intent);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        queueProcesser = new Thread(() -> {
            while (socket != null && socket.isClosed() == false) {
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

        return true;
    }

    /**
     *
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
    public static void queueMessage(C2SMessage msg) {
        msgQueue.add(msg);
        Log.v("Net", "Queued new message");
    }

    /**
     *
     * @param peerUid
     */
    public static void ping(Context context, String peerUid) {
        Log.d("NET", "Ping server!");
        new Thread(() -> {
            try {
                if(fSocket == null){
                    fSocket = new DatagramSocket();
                    Log.d("NET", "Created DatagramSocket");
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("SOCKET_CREATED"));
                }

                InetSocketAddress addr = new InetSocketAddress(SERVER_HOST, SERVER_PORT);
                byte[] b = C2SMessage.createPingMessage(peerUid).byteArray();
                DatagramPacket packet = new DatagramPacket(b, b.length, addr);
                fSocket.send(packet);
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }

    public static DatagramSocket getDatagramSocket() {
        return fSocket;
    }

    private static S2CMessage read() {
        try {
            byte[] buffer = new byte[4];
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
            reader.read(buffer);
            int size = byteBuffer.getInt();
            byteBuffer = ByteBuffer.allocate(size + 4);
            reader.read(buffer);
            byteBuffer.put(buffer);
            reader.read(byteBuffer.array(), 4, size);
            return new S2CMessage(NetMessage.parseMessage(byteBuffer.array()));
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

    public static void creD() {
        try {
            fSocket = new DatagramSocket(42069);
            //new InetSocketAddress()
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
