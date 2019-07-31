package com.example.retrofitexample.Service;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class SocketManager extends Application {
    private static final SocketManager instance = new SocketManager();
    private static Context context = null;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("SocketManager", "onServiceConnected()");
            binder = IConnectionService.Stub.asInterface(service);
            // we use instance, need to set instance's binder
            instance.setBinder(binder);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("SocketManager", "onServiceDisconnected()");
        }
    };
    private IConnectionService binder = null;


    public SocketManager() {
        Log.i("SocketManager", "SocketManager()");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("SocketManager", "onCreate()");

        // get context
        context = getApplicationContext();

        // bind service
        Intent intent = new Intent(context, ConnectionService.class);
        context.bindService(intent, connection, BIND_AUTO_CREATE);
    }

    public static SocketManager getInstance() {
        return instance;
    }

    public void setBinder(IConnectionService binder) {
        this.binder = binder;
    }

    int getStatus() throws RemoteException {
        return binder.getStatus();
    }

    void setSocket(String ip) throws RemoteException {
        binder.setSocket(ip);
    }

    void connect() throws RemoteException {
        binder.connect();

    }

    void disconnect() throws RemoteException {
        binder.disconnect();
    }

    void send() throws RemoteException {
        binder.send();
    }

    void receive() throws RemoteException {
        binder.receive();
    }
}