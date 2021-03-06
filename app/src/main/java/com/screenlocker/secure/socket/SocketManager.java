package com.screenlocker.secure.socket;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.github.nkzawa.engineio.client.EngineIOException;
import com.github.nkzawa.engineio.client.transports.WebSocket;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.secure.launcher.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.service.AppExecutor;
import com.screenlocker.secure.socket.interfaces.OnSocketConnectionListener;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import timber.log.Timber;

import static com.screenlocker.secure.app.MyApplication.getAppContext;
import static com.screenlocker.secure.utils.AppConstants.IS_LIVE_CLIENT_VISIBLE;
import static com.screenlocker.secure.utils.AppConstants.NUMBER_OF_NOTIFICATIONS;


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
    private ToneGenerator toneGen1;
    private PrefUtils prefUtils;


    private static SocketManager instance;

    private NotificationManager notificationManager = (NotificationManager) getAppContext().getSystemService(Context.NOTIFICATION_SERVICE);


    private SocketManager() {
        prefUtils = PrefUtils.getInstance(MyApplication.getAppContext());
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
                opts.reconnectionDelay = 1000L;
//                opts.reconnectionDelay = 5000;
                opts.forceNew = true;
                opts.reconnection = true;
                opts.reconnectionAttempts = 1000;
                opts.secure = true;
                opts.transports = new String[]{WebSocket.NAME};
                opts.query = "device_id=" + device_id + "&token=" + token;

                socket = IO.socket(url, opts);


                socket.on(Socket.EVENT_CONNECT, args -> {

                    Timber.i("socket connected");
                    fireSocketStatus(SocketManager.STATE_CONNECTED);
//                    try {
//                        socket.emit("authentication", new JSONObject().put("token", token));
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    socket.on("authenticated", args1 -> {
//                        Timber.i("socket authenticated");
//
//                    });
                }).on(Socket.EVENT_RECONNECTING, args -> {
                    Timber.e("Socket reconnecting");
                    fireSocketStatus(SocketManager.STATE_CONNECTING);
                }).on(Socket.EVENT_RECONNECT_FAILED, args -> {
                    Timber.e("Socket reconnection failed");
                    fireSocketStatus(SocketManager.STATE_DISCONNECTED);
                }).on(Socket.EVENT_RECONNECT_ERROR, args -> {
                    fireSocketStatus(SocketManager.STATE_DISCONNECTED);
                    if (args[0] instanceof EngineIOException){
                        Timber.e((EngineIOException)args[0], "Socket reconnection error");
                    }

                }).on(Socket.EVENT_CONNECT_ERROR, args -> {
                    fireSocketStatus(SocketManager.STATE_DISCONNECTED);
                    if (socket != null) {
                        socket.disconnect();
                    }
                    Timber.d("Socket connect error");
                    if (args[0] instanceof EngineIOException){
                        Timber.e((EngineIOException)args[0], "Socket connect error");
                    }
                }).on(Socket.EVENT_DISCONNECT, args -> {
                    Timber.e("Socket disconnect event");
                    fireSocketStatus(SocketManager.STATE_DISCONNECTED);
                }).on(Socket.EVENT_CONNECT_TIMEOUT, args -> {
                    Timber.e("Socket Connection Timeout");
                }).on(Socket.EVENT_ERROR, args -> {
                    try {
                        if (args[0] instanceof  EngineIOException){
                            EngineIOException exception = (EngineIOException) args[0];
                            if (onSocketConnectionListenerList != null) {
                                for (final OnSocketConnectionListener listener : onSocketConnectionListenerList) {
                                    new Handler(Looper.getMainLooper())
                                            .post(listener::onSocketEventFailed);
                                }
                            }
                            Timber.e(exception);

                        }else if (args[0] instanceof String){
                            final String error = (String) args[0];
                            Timber.e(error);
                            if (error.contains("Unauthorized") && !socket.connected()) {
                                if (onSocketConnectionListenerList != null) {
                                    for (final OnSocketConnectionListener listener : onSocketConnectionListenerList) {
                                        new Handler(Looper.getMainLooper())
                                                .post(listener::onSocketEventFailed);
                                    }
                                }
                            }
                        }

                    } catch (Exception e) {
                        Timber.e(e);
                    }
                }).on("Error", args -> Timber.d(" Error"));
                socket.connect();
            } else if (!socket.connected()) {
                socket.connect();
            }
        } catch (Exception e) {
            Timber.e(e);
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
                opts.transports = new String[]{WebSocket.NAME};
                opts.secure = true;
                opts.query = "device_id=" + device_id;


                clientChatSocket = IO.socket(url, opts);

                clientChatSocket.on(Socket.EVENT_CONNECT, args -> {
                    Timber.i("clientChatSocket connected");
                    prefUtils.saveBooleanPref( AppConstants.CLIENT_CHAT_SOCKET, true);

                    notify = device_id;
                    clientChatSocket.on(notify, args1 -> {
                        AppExecutor.getInstance().getMainThread().execute(new Runnable() {
                            @Override
                            public void run() {
                                Timber.i("clientChatSocket notify");

                                Notification notification = null;
                                try {

                                    boolean isLiveActivityVisible = prefUtils.getBooleanPref( IS_LIVE_CLIENT_VISIBLE);
                                    JSONObject data = (JSONObject) args1[1];
                                    if (!data.getString("msg").equals("")) {
                                        if (!isLiveActivityVisible) {
                                            notification = new NotificationCompat.Builder(getAppContext(), MyApplication.CHANNEL_1_ID)
                                                    .setContentText("")
                                                    .setContentTitle(data.getString("msg"))
                                                    .setSmallIcon(R.drawable.ic_chat)
                                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                                    .build();


                                            notificationManager.notify((int) System.currentTimeMillis(), notification);
                                            int numberOfNotifications = prefUtils.getIntegerPref( NUMBER_OF_NOTIFICATIONS);
                                            prefUtils.saveIntegerPref( NUMBER_OF_NOTIFICATIONS, ++numberOfNotifications);

                                        } else {


                                            Handler handler = new Handler();

                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                                                    toneGen1.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 200);
                                                    new Handler().postDelayed(() -> {
                                                        toneGen1.release();
                                                        toneGen1 = null;
                                                    }, 500);
                                                }
                                            }, 2000);
                                        }
                                    }
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
                    Timber.e("clientChatSocket reconnection error");

                }).on(Socket.EVENT_CONNECT_ERROR, args -> {
                    Timber.e("clientChatSocket connect error");
                    if (clientChatSocket != null)
                        clientChatSocket.disconnect();
                }).on(Socket.EVENT_DISCONNECT, args -> {
                    Timber.e("clientChatSocket disconnect event");
                    if (clientChatSocket != null) {
                        clientChatSocket.off(notify);
                    }

                    prefUtils.saveBooleanPref( AppConstants.CLIENT_CHAT_SOCKET, false);

                }).on(Socket.EVENT_ERROR, args -> {
                    try {
                        final String error = (String) args[0];
                        Timber.e(error);
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
        if (clientChatSocket != null) {
            clientChatSocket.off(notify);
            clientChatSocket.disconnect();
            clientChatSocket.close();
            clientChatSocket = null;
        }
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
        if (onSocketConnectionListenerList != null
                && onSocketConnectionListenerList.contains(onSocketConnectionListenerListener)) {
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


//    public static class NetReceiver extends BroadcastReceiver {
//
//        /**
//         * The Tag.
//         */
//        public final String TAG = NetReceiver.class.getSimpleName();
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            ConnectivityManager cm =
//                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//            boolean isConnected = activeNetwork != null &&
//                    activeNetwork.isConnectedOrConnecting();
//
//            SocketManager.getInstance().fireInternetStatusIntent(
//                    isConnected ? SocketManager.STATE_CONNECTED : SocketManager.STATE_DISCONNECTED);
//            if (isConnected) {
//                if (SocketManager.getInstance().getSocket() != null
//                        && !SocketManager.getInstance().getSocket().connected()) {
//                    SocketManager.getInstance().fireSocketStatus(SocketManager.STATE_CONNECTING);
//                }
//                PowerManager powerManager =
//                        (PowerManager) context.getSystemService(Context.POWER_SERVICE);
//                boolean isScreenOn;
//                isScreenOn = powerManager.isInteractive();
//
//                if (isScreenOn && SocketManager.getInstance().getSocket() != null) {
//                    Log.d(TAG, "NetReceiver: Connecting Socket");
//                    if (!SocketManager.getInstance().getSocket().connected()) {
//                        SocketManager.getInstance().getSocket().connect();
//                    }
//                }
//            } else {
//                SocketManager.getInstance().fireSocketStatus(SocketManager.STATE_DISCONNECTED);
//                if (SocketManager.getInstance().getSocket() != null) {
//                    Log.d(TAG, "NetReceiver: disconnecting socket");
//                    SocketManager.getInstance().getSocket().disconnect();
//                }
//            }
//        }
//    }


}
