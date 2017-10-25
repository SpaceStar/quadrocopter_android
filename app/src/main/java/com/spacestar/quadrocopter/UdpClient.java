package com.spacestar.quadrocopter;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class UdpClient {

    private InetAddress SERVER_IP; //server IP address
    private int SERVER_PORT;
    // UDP socket
    private DatagramSocket socket;

    /**
     * Constructor of the class
     */
    public UdpClient(String ServerIP, int ServerPort) {
        try {
            SERVER_IP = InetAddress.getByName(ServerIP);
            SERVER_PORT = ServerPort;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        new Thread(() -> start_client()).start();
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
        try {
            Log.d("UDP Client", "C: Connecting...");

            //create a socket to make the connection with the server
            socket = new DatagramSocket();

            Log.d("UDP Client", "C: Connected");
        } catch (Exception e) {
            Log.e("UDP", "C: Error", e);
        }
    }

}