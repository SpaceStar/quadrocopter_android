package com.spacestar.quadrocopter;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public abstract class UdpClient {

    private InetAddress SERVER_IP; //server IP address
    private int SERVER_PORT;
    private int PACKET_MAX_LENGTH;
    // UDP socket
    private DatagramSocket socket;

    /**
     * Constructor of the class
     */
    public UdpClient(String ServerIP, int ServerPort, int InputMaxLength) {
        try {
            SERVER_IP = InetAddress.getByName(ServerIP);
            SERVER_PORT = ServerPort;
            PACKET_MAX_LENGTH = InputMaxLength;

            start_client();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(String message) {
        sendBytes(message.getBytes());
    }

    public void sendBytes(byte[] data) {
        if (socket != null) {
            try {
                DatagramPacket packet = new DatagramPacket(data, data.length, SERVER_IP, SERVER_PORT);
                socket.send(packet);
            } catch (IOException e) {
                Log.e("Send", "Error while sending data");
            }
        }
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {
        //the socket must be closed. It is not possible to reconnect to this socket
        // after it is closed, which means a new socket instance has to be created.
        if (socket != null) {
            socket.close();
        }
    }

    private void start_client() {
        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Integer, Void> asyncTask = new AsyncTask<Void, Integer, Void>() {
            private byte[] data;

            @Override
            protected Void doInBackground(Void... voids) {
                data = new byte[PACKET_MAX_LENGTH];
                DatagramPacket packet = new DatagramPacket(data, data.length);

                try {
                    //connect
                    socket = new DatagramSocket();
                    //receive
                    while (socket != null)
                    {
                        socket.receive(packet);
                        publishProgress(packet.getLength());
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                onReceive(data, values[0]);
            }
        };
        asyncTask.execute();
    }

    abstract public void onReceive(byte[] data, int size);

}