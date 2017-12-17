package com.spacestar.quadrocopter;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public abstract class UdpClient {

    private InetAddress mServerIp; //server IP address
    private int mServerPort;
    private int mPacketMaxLength;
    // UDP socket
    private DatagramSocket mSocket;

    public UdpClient(String serverIP, int serverPort, int inputMaxLength) {
        try {
            mServerIp = InetAddress.getByName(serverIP);
            this.mServerPort = serverPort;
            mPacketMaxLength = inputMaxLength;

            startClient();
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
        if (mSocket != null) {
            try {
                DatagramPacket packet = new DatagramPacket(data, data.length, mServerIp, mServerPort);
                mSocket.send(packet);
            } catch (IOException e) {
                Log.e("Send", "Error while sending mData");
            }
        }
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {
        //the mSocket must be closed. It is not possible to reconnect to this mSocket
        // after it is closed, which means a new mSocket instance has to be created.
        if (mSocket != null) {
            mSocket.close();
        }
    }

    private void startClient() {
        AsyncTask<Void, Integer, Void> asyncTask = new UdpTask(this);
        asyncTask.execute();
    }

    abstract public void onReceive(byte[] data, int size);

    private static class UdpTask extends AsyncTask<Void, Integer, Void> {
        private UdpClient mClient;
        private byte[] mData;

        public UdpTask(UdpClient client) {
            mClient = client;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mData = new byte[mClient.mPacketMaxLength];
            DatagramPacket packet = new DatagramPacket(mData, mData.length);

            try {
                //connect
                mClient.mSocket = new DatagramSocket();
                //receive
                while (mClient.mSocket != null) {
                    mClient.mSocket.receive(packet);
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
            mClient.onReceive(mData, values[0]);
        }
    }
}