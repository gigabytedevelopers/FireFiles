package com.gigabytedevelopersinc.app.explorer.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import com.crashlytics.android.Crashlytics;
import com.gigabytedevelopersinc.app.explorer.misc.LogUtils;
import com.gigabytedevelopersinc.app.explorer.model.RootInfo;
import com.gigabytedevelopersinc.app.explorer.network.NetworkConnection;
import com.gigabytedevelopersinc.app.explorer.network.NetworkServiceHandler;

import static com.gigabytedevelopersinc.app.explorer.misc.ConnectionUtils.ACTION_FTPSERVER_FAILEDTOSTART;
import static com.gigabytedevelopersinc.app.explorer.misc.Utils.EXTRA_ROOT;

public abstract class NetworkServerService extends Service {

    private static final String TAG = NetworkServerService.class.getSimpleName();
    public static final int MSG_START = 1;
    public static final int MSG_STOP = 2;

    private Looper serviceLooper;
    private NetworkServiceHandler serviceHandler;
    private NetworkConnection networkConnection;
    private RootInfo root;

    protected abstract NetworkServiceHandler createServiceHandler(
            Looper serviceLooper,
            NetworkServerService service);

    public abstract Object getServer();

    public abstract boolean launchServer();

    public abstract void stopServer();

    protected void handleServerStartError(Exception e) {
        LogUtils.LOGD(TAG, "could not start server", e);
        Crashlytics.logException(e);
        sendBroadcast(new Intent(ACTION_FTPSERVER_FAILEDTOSTART));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread(
                "ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        serviceLooper = thread.getLooper();
        serviceHandler = createServiceHandler(serviceLooper, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            LogUtils.LOGD(TAG, "intent is null in onStartCommand()");
            return START_REDELIVER_INTENT;
        }

        // get parameters
        Bundle extras = intent.getExtras();
        root = extras.getParcelable(EXTRA_ROOT);
        if(null == root){
            networkConnection = NetworkConnection.getDefaultServer(getApplicationContext());
        } else {
            networkConnection = NetworkConnection.fromRootInfo(getApplicationContext(), root);
        }
        // send start message (to handler)
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = MSG_START;
        serviceHandler.sendMessage(msg);


        // we don't want the system to kill the ftp server
        //return START_NOT_STICKY;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // send stop message (to handler)
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = MSG_STOP;
        serviceHandler.sendMessage(msg);
    }

    public RootInfo getRootInfo() {
        return root;
    }

    public NetworkConnection getNetworkConnection() {
        return networkConnection;
    }
}
