package com.screenlocker.secure.socket;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.liveClientChat.LiveClientChatActivity;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.service.AppExecutor;
import com.screenlocker.secure.socket.interfaces.OnSocketConnectionListener;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS;
import static com.screenlocker.secure.utils.AppConstants.GET_APPLIED_SETTINGS;
import static com.screenlocker.secure.utils.AppConstants.GET_PUSHED_APPS;
import static com.screenlocker.secure.utils.AppConstants.GET_SYNC_STATUS;


public class SocketManager {

    /**
     * The constant STATE_CONNECTING.
     */
    public static final int STATE_CONNECTING = 1;
    /**
     * The constant STATE_CONNECTED.
     */
    public static final int STATE_CONNECTED = 2;
    /**
     * The constant STATE_DISCONNECTED.
     */
    public static final int STATE_DISCONNECTED = 3;

    private String notify = "";


    private static SocketManager instance;

    private SocketManager() {
    }


    /**
     * Gets instance.
     *
     * @return the instance
     */

    public synchronized static SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;
    }


    /**
     * The constant TAG.
     */
    public static final String TAG = SocketManager.class.getSimpleName();
    private Socket socket;
    private List<OnSocketConnectionListener> onSocketConnectionListenerList;

    private Socket clientChatSocket;

    /**
     * Connect socket.
     *
     * @param token     the token
     * @param device_id the device id
     * @param url       the host url
     */
    public synchronized void connectSocket(String token, String device_id, String url) {
        try {
            if (socket == null) {
                IO.Options opts = new IO.Options();
                opts.reconnectionDelay = 5000;
//                opts.reconnectionDelay = 10 * 60 * 60 * 1000L;
                opts.forceNew = true;
                opts.reconnection = true;
                opts.reconnectionAttempts = 1000;
                opts.secure = true;
                opts.query = "device_id=" + device_id + "&token=" + token;

                socket = IO.socket(url.replaceAll("/mobile/", ""), opts);

                socket.on(Socket.EVENT_CONNECT, args -> {
                    fireSocketStatus(SocketManager.STATE_CONNECTED);
                    Timber.i("socket connected");
                }).on(Socket.EVENT_RECONNECTING, args -> {
                    Timber.e("Socket reconnecting");
                    fireSocketStatus(SocketManager.STATE_CONNECTING);
                }).on(Socket.EVENT_RECONNECT_FAILED, args -> {
                    Timber.e("Socket reconnection failed");
                    fireSocketStatus(SocketManager.STATE_DISCONNECTED);
                }).on(Socket.EVENT_RECONNECT_ERROR, args -> {
                    Log.e(TAG, "Socket reconnection error");
                    fireSocketStatus(SocketManager.STATE_DISCONNECTED);
                }).on(Socket.EVENT_CONNECT_ERROR, args -> {
                    Log.e(TAG, "Socket connect error");
                    fireSocketStatus(SocketManager.STATE_DISCONNECTED);
                    if (socket != null)
                        socket.disconnect();
                }).on(Socket.EVENT_DISCONNECT, args -> {
                    Log.e(TAG, "Socket disconnect event");
                    fireSocketStatus(SocketManager.STATE_DISCONNECTED);
                }).on(Socket.EVENT_ERROR, args -> {
                    try {
                        final String error = (String) args[0];
                        Log.e(TAG + " error EVENT_ERROR ", error);
                        if (error.contains("Unauthorized") && !socket.connected()) {

                            if (onSocketConnectionListenerList != null) {
                                for (final OnSocketConnectionListener listener : onSocketConnectionListenerList) {
                                    new Handler(Looper.getMainLooper())
                                            .post(listener::onSocketEventFailed);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Timber.e(e.getMessage() != null ? e.getMessage() : "");
                    }
                }).on("Error", args -> Timber.d(" Error"));
                socket.connect();
            } else if (!socket.connected()) {
                socket.connect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void connectClientChatSocket(String device_id, String url) {
        try {
            if (clientChatSocket == null) {
                IO.Options opts = new IO.Options();
                opts.reconnectionDelay = 60 * 60 * 1000L;
//                opts.reconnectionDelay = 5000;
                opts.forceNew = true;
                opts.reconnection = true;
                opts.reconnectionAttempts = 1000;
                opts.secure = true;
                opts.query = "device_id=" + device_id;


                clientChatSocket = IO.socket(url, opts);

                clientChatSocket.on(Socket.EVENT_CONNECT, args -> {
                    Timber.i("clientChatSocket connected");
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), AppConstants.CLIENT_CHAT_SOCKET, true);

                    notify = device_id;
                    clientChatSocket.on(notify, args1 -> {

                        Timber.d("djvnkjdvndsvd notify ");
                        AppExecutor.getInstance().getMainThread().execute(new Runnable() {
                            @Override
                            public void run() {
                                Notification notification = null;
                                try {

                                    JSONObject data = (JSONObject) args1[1];


                                    Intent resultIntent = new Intent(MyApplication.getAppContext(), LiveClientChatActivity.class);
                                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(MyApplication.getAppContext());
                                    stackBuilder.addNextIntentWithParentStack(resultIntent);
                                    PendingIntent resultPendingIntent =
                                            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                                    notification = new NotificationCompat.Builder(MyApplication.getAppContext(), MyApplication.CHANNEL_1_ID)
                                            .setContentText("")
                                            .setContentTitle(data.getString("msg"))
                                            .setSmallIcon(R.drawable.ic_screen_lock)
                                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                                            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                            .setContentIntent(resultPendingIntent)
                                            .build();


                                    NotificationManager notificationManager = (NotificationManager) MyApplication.getAppContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                    notificationManager.notify((int) (System.currentTimeMillis() - 10000000), notification);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    });
                }).on(Socket.EVENT_RECONNECTING, args -> {
                    Timber.e("clientChatSocket reconnecting");
                }).on(Socket.EVENT_RECONNECT_FAILED, args -> {
                    Timber.e("clientChatSocket reconnection failed");
                }).on(Socket.EVENT_RECONNECT_ERROR, args -> {
                    Log.e(TAG, "clientChatSocket reconnection error");

                }).on(Socket.EVENT_CONNECT_ERROR, args -> {
                    Log.e(TAG, "clientChatSocket connect error");
                    if (clientChatSocket != null)
                        clientChatSocket.disconnect();
                }).on(Socket.EVENT_DISCONNECT, args -> {
                    Log.e(TAG, "clientChatSocket disconnect event");
                    clientChatSocket.off(notify);
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), AppConstants.CLIENT_CHAT_SOCKET, false);
                }).on(Socket.EVENT_ERROR, args -> {
                    try {
                        final String error = (String) args[0];
                        Log.e(TAG + " error EVENT_ERROR ", error);
                        if (error.contains("Unauthorized") && !clientChatSocket.connected()) {

                        }
                    } catch (Exception e) {
                        Timber.e(e.getMessage() != null ? e.getMessage() : "");
                    }
                }).on("Error", args -> Timber.d(" Error"));
                clientChatSocket.connect();
            } else if (!clientChatSocket.connected()) {
                clientChatSocket.connect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int lastState = -1;

    /**
     * Fire socket status intent.
     *
     * @param socketState the socket state
     */
    public synchronized void fireSocketStatus(final int socketState) {
        if (onSocketConnectionListenerList != null && lastState != socketState) {
            lastState = socketState;
            new Handler(Looper.getMainLooper()).post(() -> {
                for (OnSocketConnectionListener listener : onSocketConnectionListenerList) {
                    listener.onSocketConnectionStateChange(socketState);
                }
            });
            new Handler(Looper.getMainLooper()).postDelayed(() -> lastState = -1, 1000);
        }
    }

    /**
     * Fire internet status intent.
     *
     * @param socketState the socket state
     */
    public synchronized void fireInternetStatusIntent(final int socketState) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (onSocketConnectionListenerList != null) {
                for (OnSocketConnectionListener listener : onSocketConnectionListenerList) {
                    listener.onInternetConnectionStateChange(socketState);
                }
            }
        });
    }

    public void destroyClientChatSocket() {

        clientChatSocket.off(notify);
        clientChatSocket.disconnect();
        clientChatSocket.close();
        clientChatSocket = null;
    }

    /**
     * Gets socket.
     *
     * @return the socket
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * Sets socket.
     *
     * @param socket the socket
     */
    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    /**
     * Destroy.
     */
    public void destroy() {

        Log.d("SocketManager", "destroy");
        if (socket != null) {
            socket.disconnect();
            socket.close();
            socket = null;
        }
    }

    /**
     * Sets socket connection listener.
     *
     * @param onSocketConnectionListenerListener the on socket connection listener listener
     */
    public void setSocketConnectionListener(OnSocketConnectionListener onSocketConnectionListenerListener) {
        if (onSocketConnectionListenerList == null) {
            onSocketConnectionListenerList = new ArrayList<>();
            onSocketConnectionListenerList.add(onSocketConnectionListenerListener);
        } else if (!onSocketConnectionListenerList.contains(onSocketConnectionListenerListener)) {
            onSocketConnectionListenerList.add(onSocketConnectionListenerListener);
        }
    }

    /**
     * Remove socket connection listener.
     *
     * @param onSocketConnectionListenerListener the on socket connection listener listener
     */
    public void removeSocketConnectionListener(OnSocketConnectionListener onSocketConnectionListenerListener) {
        if (onSocketConnectionListenerList != null) {
            onSocketConnectionListenerList.remove(onSocketConnectionListenerListener);
        }
    }

    /**
     * Remove all socket connection listener.
     */
    public void removeAllSocketConnectionListener() {
        if (onSocketConnectionListenerList != null) {
            onSocketConnectionListenerList.clear();
        }
    }

    /**
     * The type Net receiver.
     */
    public static class NetReceiver extends BroadcastReceiver {

        /**
         * The Tag.
         */
        public final String TAG = NetReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();

            SocketManager.getInstance().fireInternetStatusIntent(
                    isConnected ? SocketManager.STATE_CONNECTED : SocketManager.STATE_DISCONNECTED);
            if (isConnected) {
                if (SocketManager.getInstance().getSocket() != null
                        && !SocketManager.getInstance().getSocket().connected()) {
                    SocketManager.getInstance().fireSocketStatus(SocketManager.STATE_CONNECTING);
                }
                PowerManager powerManager =
                        (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                boolean isScreenOn;
                isScreenOn = powerManager.isInteractive();

                if (isScreenOn && SocketManager.getInstance().getSocket() != null) {
                    Log.d(TAG, "NetReceiver: Connecting Socket");
                    if (!SocketManager.getInstance().getSocket().connected()) {
                        SocketManager.getInstance().getSocket().connect();
                    }
                }
            } else {
                SocketManager.getInstance().fireSocketStatus(SocketManager.STATE_DISCONNECTED);
                if (SocketManager.getInstance().getSocket() != null) {
                    Log.d(TAG, "NetReceiver: disconnecting socket");
                    SocketManager.getInstance().getSocket().disconnect();
                }
            }
        }
    }


}
