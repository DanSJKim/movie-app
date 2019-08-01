package com.example.retrofitexample.Chat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.retrofitexample.BoxOffice.ProfileActivity;
import com.example.retrofitexample.Chat.Model.DatabaseHelper;
import com.example.retrofitexample.Chat.Model.MessageListContent;
import com.example.retrofitexample.Chat.Model.Room;
import com.example.retrofitexample.LoginRegister.SharedPref;
import com.example.retrofitexample.R;
import com.example.retrofitexample.Retrofit.Api;
import com.example.retrofitexample.Retrofit.ApiClient;
import com.example.retrofitexample.VideoCall.CallActivity;
import com.example.retrofitexample.VideoCall.ReceiveCallActivity;
import com.facebook.stetho.inspector.protocol.module.Database;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.retrofitexample.BoxOffice.ProfileActivity.loggedUseremail;

//service
public class ChatService extends Service {

    private static final String TAG = "ChatService: ";

    IBinder mBinder = new LocalBinder();
    // Registered callbacks
    private ServiceCallbacks serviceCallbacks;

    ReceiveThread receive;
    SocketClient sc;

    RunningThread runningThread;

    public static Socket socket; // 다른 액티비티에서도 사용할 수 있게 전역변수로 저장
    public static DataOutputStream dos;

    String receivedMsg = null; // 서버로 부터 수신한 채팅 메세지

    public static int currentRoomNo; // 현재 방 번호

    Notification Notifi;
    NotificationManager Notifi_M;

    static int i = 0; // 노티 번호

    public class LocalBinder extends Binder {
        public ChatService getService() { // 서비스 객체를 리턴
            return ChatService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "서비스 onBind 에서 시작");
        return mBinder;
    }

    public interface ServiceCallbacks {
        void ChatdoSomething();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");

        // OREO 이상 버전일 경우 startForeGround를 실행해 준다.
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "default";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("알람시작")
                    .setContentText("알람음이 재생됩니다.")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .build();

            startForeground(1, notification);
        }

        //currentRoomNo  = -1;


    }


    String getMsg() { // 값을 리턴하는 메서드
        return receivedMsg;
    }

    public void setCallbacks(ServiceCallbacks callbacks) {
        serviceCallbacks = callbacks;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "서비스 시작");
        Log.d(TAG, "onStartCommand: ");


        String loggedUseremail = SharedPref.getInstance(ChatService.this).LoggedInEmail();


        //처음 시작하면 계속 구동
        sc = new SocketClient(loggedUseremail);
        sc.start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "서비스 종료");
        super.onDestroy();
        //stopForeground(true);
    }

    // 내부클래스   ( 접속용 )
    class SocketClient extends Thread{

        DataInputStream in = null;        //Server로부터 데이터를 읽어들이기 위한 입력스트림

        DataOutputStream out = null;

        String userEmail;

        public SocketClient(String userEmail){
            this.userEmail = userEmail;
        }

        public void run(){
            try {
                Log.d(TAG, "SocketClient run: ");
                // 채팅 서버에 접속 ( 연결 )  ( 서버쪽 ip와 포트 )
                socket = new Socket("192.168.0.70",6075);

                // 메세지를 서버에 전달 할 수 있는 통로 ( 만들기 )
                out = new DataOutputStream(socket.getOutputStream());
                dos = out;
                in = new DataInputStream(socket.getInputStream());

                String profileImage = SharedPref.getInstance(getApplicationContext()).StoredProfileImage(); // 내 프로필 이미지

                // 서버에 초기 데이터 전송
                Log.d(TAG, "run: loggedUseremail: " + userEmail);
                out.writeUTF(userEmail);
                Log.d(TAG, "클라이언트 : 메세지 전송 완료");

                // (메세지 수신용 쓰레드 생성 ) 리시브 쓰레드 시작
                receive = new ReceiveThread(socket);
                receive.start();

                // (메세지 수신용 쓰레드 생성 ) 리시브 쓰레드 시작
                runningThread = new RunningThread();
                runningThread.start();

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    } //JoinThread 끝



    // ( 메세지 수신용 )   -  서버로부터 받아서, 핸들러에서 처리하도록 할 거.
    class ReceiveThread extends Thread{

        Socket rcSocket = null;
        DataInputStream input = null;

        public ReceiveThread(Socket socket) {
            this.rcSocket = socket;

            Log.d(TAG, "ReceiveThread: ");
            try {

                // 채팅 서버로부터 메세지를 받기 위한 스트림 생성.
                input = new DataInputStream(socket.getInputStream());
                Log.d(TAG, "ReceiveThread: input: " + input);
            }catch (Exception e){

                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {

                while (input != null){

                    // 채팅 서버로 부터 받은 메세지
                    receivedMsg =input.readUTF();
                    Log.d(TAG, "수신 메세지: " + receivedMsg);

                    if (receivedMsg != null){

                            // JSON 형식의 데이터 추출
                        JSONObject jsonObject= null;
                        try {
                            jsonObject = new JSONObject(receivedMsg);
                            String roomNo = jsonObject.getString("roomNo");
                            int roomNoToInt = Integer.parseInt(roomNo);
                            String mode = jsonObject.getString("mode");
                            int modeToInt = Integer.parseInt(mode);


                            // 사용자가 현재 보고 있는 액티비티
                            Log.d(TAG, "run: currentRoomNo: " + currentRoomNo);

                            // 보낸 쪽의 방 번호와 사용자가 보고 있는 액티비티가 일치하거나 대기실일때 실행한다.
                            if((roomNoToInt == currentRoomNo) || (currentRoomNo == 0)){
                                Log.d(TAG, "run: is chatlist or chatroom " + serviceCallbacks);
                                serviceCallbacks.ChatdoSomething();

                                if(modeToInt == 4){
                                    Log.d(TAG, "run: mode is: " + modeToInt);

                                    String callID = jsonObject.getString("callID");

                                    String senderEmail = jsonObject.getString("myEmail");
                                    String senderProfile = jsonObject.getString("myProfile");

                                    Intent callintent = new Intent(ChatService.this, ReceiveCallActivity.class);
                                    callintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    callintent.putExtra("senderEmail", senderEmail);
                                    callintent.putExtra("senderProfile", senderProfile);
                                    callintent.putExtra("callID", callID);
                                    callintent.putExtra("roomNo", roomNoToInt);
                                    startActivity(callintent);

                                }
                            }

                            // 영상통화 화면일 때
                            if(currentRoomNo == -2){
                                Log.d(TAG, "run: is callactivity");
                                String senderEmail = jsonObject.getString("myEmail");
                                String date = jsonObject.getString("date");
                                String time = jsonObject.getString("time");

                                sendMessageToMySQL(5, roomNoToInt, loggedUseremail, senderEmail, "영상통화 취소", date, time);

                                serviceCallbacks.ChatdoSomething();

                            }

                            // 채팅방이나 채팅목록 화면이 아닐 때 알림을 실행
                            if((roomNoToInt != currentRoomNo) && (currentRoomNo != 0) && (currentRoomNo != -2)){
                                Log.d(TAG, "run: no chatlist, chatroom");

                                String myEmail = jsonObject.getString("myEmail"); // 메세지 작성자의 이메일
                                String message = jsonObject.getString("message");

                                // 오레오에 대응하기 위한 notification channel 생성
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    /**
                                     * 오레오 이상 노티처리
                                     */

                                    /**
                                     * 오레오 버전부터 노티를 처리하려면 채널이 존재해야합니다.
                                     */

                                    int importance = NotificationManager.IMPORTANCE_HIGH;
                                    String Noti_Channel_ID = "Noti";
                                    String Noti_Channel_Group_ID = "Noti_Group";

                                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                    NotificationChannel notificationChannel = new NotificationChannel(Noti_Channel_ID,Noti_Channel_Group_ID,importance);

//                    notificationManager.deleteNotificationChannel("testid"); 채널삭제

                                    /**
                                     * 채널이 있는지 체크해서 없을경우 만들고 있으면 채널을 재사용 한다.
                                     */
                                    if(notificationManager.getNotificationChannel(Noti_Channel_ID) != null){
                                        Log.d(TAG, "run: 채널이 이미 존재 한다.");
                                    }
                                    else{
                                        Log.d(TAG, "run: 채널이 없어서 만든다.");
                                        notificationManager.createNotificationChannel(notificationChannel);
                                    }

                                    notificationManager.createNotificationChannel(notificationChannel);
//                    Log.e("로그확인","===="+notificationManager.getNotificationChannel("testid1"));
//                    notificationManager.getNotificationChannel("testid");

                                    // 클릭 시 실행 할 액티비티
                                    Log.d(TAG, "run: Service roomNoToInt: " + roomNoToInt);
                                    Intent intent = new Intent(ChatService.this, ChatRoomActivity.class);
                                    intent.putExtra("roomNo", roomNoToInt);
                                    intent.putExtra("yourEmail", myEmail);
                                    intent.putExtra("backbuttonflag", 1);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    PendingIntent pendingIntent = PendingIntent.getActivity(ChatService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),Noti_Channel_ID)
                                            .setLargeIcon(null).setSmallIcon(R.mipmap.ic_launcher)
                                            .setWhen(System.currentTimeMillis()).setShowWhen(true).
                                                    setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_MAX)
                                            .setContentTitle(myEmail)
                                            .setContentText(message)
                                            .setContentIntent(pendingIntent);

                                    notificationManager.notify(i,builder.build());
                                    i++;
                                } //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 끝
                            } //if((roomNoToInt != currentRoomNo) && (currentRoomNo != 0)) 끝

                            if(modeToInt == 4){
                                Log.d(TAG, "run: mode is: " + modeToInt);
                                String callID = jsonObject.getString("callID");

                                String senderEmail = jsonObject.getString("myEmail");
                                String senderProfile = jsonObject.getString("myProfile");

                                Intent callintent = new Intent(ChatService.this, ReceiveCallActivity.class);
                                callintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                callintent.putExtra("senderEmail", senderEmail);
                                callintent.putExtra("senderProfile", senderProfile);
                                callintent.putExtra("callID", callID);
                                callintent.putExtra("roomNo", roomNoToInt);
                                startActivity(callintent);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                            //Log.e("My App", "Could not parse malformed JSON: \"" + receivedMsg + "\"");
                    }

                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    } //ReceiveThread


    class RunningThread extends Thread{

        Socket rcSocket = null;
        DataInputStream input = null;

        public RunningThread() {

            Log.d(TAG, "RunningThread: ");
            try {

            }catch (Exception e){

                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {

                for(int i=0; i < 5000; i++){

                    Log.d(TAG, loggedUseremail + " service is running " + i);
                    // 1초간 중지시킨다.(단위 : 밀리세컨드)
                    Thread.sleep(10000);

                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    } //RunningThread


    public void sendMessageToMySQL(final int mode, final int roomNo, final String myEmail, final String yourEmail, final String message, final String date, final String time){
        Log.d(TAG, "sendMessageToMySQL: mode: " + mode);

        Api api = ApiClient.getClient().create(Api.class);

        Call<Room> call = api.sendMessage(mode, roomNo, myEmail, yourEmail, message, date, time);
        call.enqueue(new Callback<Room>() {

            @Override
            public void onResponse(Call<Room> call, final Response<Room> response) {
                Log.d("TAG", "onResponse: " + response.body().getResult());

            }

            @Override
            public void onFailure(Call<Room> call, Throwable t) {
                Log.d("ToMySQL Error",t.getMessage());
            }
        });
    }


}