package com.example.retrofitexample.Service;

import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.retrofitexample.R;

public class MainActivity extends AppCompatActivity {

    // 소켓의 상태를 표현하기 위한 상수
    final int STATUS_DISCONNECTED = 0;
    final int STATUS_CONNECTED = 1;
    // 서버의 ip
    String ip = "192.168.0.78"; // 원래는 다른 view를 통해 사용자에게서 입력받아야 한다.
    // ConnectionService의 binder를 가지고 있는 SocketManager instance
    SocketManager manager = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("MainActivity", "onCreate()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("MainActivity", "onResume()");

        // Activity가 전환될 수 있으므로 onResume() 내에서 SocketManager의 instance를 얻는다.
        // get SocketManager instance
        manager = SocketManager.getInstance();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    // ip주소를 이용해 서버에 접속하는 메소드
    public void connectToServer(View v) throws RemoteException {
        manager.setSocket(ip);
        manager.connect();
    }

    // 소켓을 통해 간단한 문자열을 전송하는 메소드. 원래라면 필요한 데이터를 입력받아야 한다.
    public void sendData(View v) throws RemoteException {
        if(manager.getStatus() == STATUS_CONNECTED){
            manager.send();
        }
        else {
            Toast.makeText(this, "not connected to server", Toast.LENGTH_SHORT).show();
        }
    }

    // 서버로 부터 데이터를 받는 메소드
    public void receiveData(View v) throws RemoteException {
        if(manager.getStatus() == STATUS_CONNECTED){
            manager.receive();
        }
        else {
            Toast.makeText(this, "not connected to server", Toast.LENGTH_SHORT).show();
        }

    }
}
