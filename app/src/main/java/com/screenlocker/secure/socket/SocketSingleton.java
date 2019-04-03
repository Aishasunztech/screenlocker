package com.screenlocker.secure.socket;


import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS;
import static com.screenlocker.secure.utils.AppConstants.GET_APPLIED_SETTINGS;
import static com.screenlocker.secure.utils.AppConstants.GET_SYNC_STATUS;
import static com.screenlocker.secure.utils.AppConstants.SOCKET_SERVER_URL;

public class SocketSingleton {

    private static Socket socket;

    public static Socket getSocket(String device_id, String token) {

        if (socket != null) {
            return socket;
        } else {

            IO.Options opts = new IO.Options();
            opts.forceNew = true;
            opts.reconnectionDelay = 1000;
            opts.reconnection = true;
            opts.query = "device_id=" + device_id + "&token=" + token;
            try {
                socket = IO.socket(SOCKET_SERVER_URL, opts);
                socket.connect();
            } catch (URISyntaxException e) {
                Timber.e("error : %S", e.getMessage());
            }
        }

        return socket;
    }


    public static boolean isSocketConnected() {
        if (socket != null) {
            return socket.connected();
        }
        return false;
    }

    public static void closeSocket(String device_id) {


        try {
            socket.off(GET_SYNC_STATUS + device_id);
            socket.off(GET_APPLIED_SETTINGS + device_id);
            socket.off(DEVICE_STATUS + device_id);
            socket.off(Socket.EVENT_ERROR);
            socket.off(Socket.EVENT_CONNECT);
            socket.off(Socket.EVENT_DISCONNECT);
            socket.disconnect();
            socket = null;

        } catch (Exception ignored) {

        }
    }


}
