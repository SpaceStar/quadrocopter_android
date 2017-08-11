package a383.quadrocopter;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class TcpClient {

    public static final String SERVER_IP = "192.168.0.11"; //server IP address
    public static final int SERVER_PORT = 8888;
    public static final boolean TCP_NODELAY = true;
    // used to send messages
    private PrintWriter mBufferOut;
    private Socket socket;

    /**
     * Constructor of the class
     */
    public TcpClient() {
        new Thread(() -> start_client()).start();
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(String message) {
        if (mBufferOut != null && !mBufferOut.checkError()) {
            mBufferOut.println(message);
            mBufferOut.flush();
        }
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {
        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        //the socket must be closed. It is not possible to reconnect to this socket
        // after it is closed, which means a new socket instance has to be created.
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mBufferOut = null;
    }

    private void start_client() {

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

            Log.e("TCP Client", "C: Connecting...");

            //create a socket to make the connection with the server
            socket = new Socket(serverAddr, SERVER_PORT);
            socket.setTcpNoDelay(TCP_NODELAY);

            //sends the message to the server
            mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

            Log.e("TCP Client", "C: Connected");
        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
        }
    }

}