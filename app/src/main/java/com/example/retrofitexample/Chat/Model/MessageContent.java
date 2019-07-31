package com.example.retrofitexample.Chat.Model;

public class MessageContent {

    //multi viewtype용 변수
    public static final int MY_TYPE=0;
    public static final int OTHER_TYPE=1;

    private int id;
    private int mode;
    private int roomNo;

    private String senderEmail; // 사용자 메일
    private String img_path; // 사진
    private String content; // 내용
    private String chatDate; // 날짜
    private String chatTime; // 시간
    private int count;

    public MessageContent(int mode, String profile, String senderEmail, String content, String chatDate, String chatTime, int count) {
        //this.type = type; //view type 상수를 보유합니다
        //this.data = data; // data 변수는 우리가 채울 각 데이터를 저장하는 데 사용됩니다.이상적으로는 드로어 블 또는 raw 타입 리소스를 포함합니다.
        this.mode = mode;
        this.roomNo = roomNo;
        this.img_path = profile;
        this.senderEmail = senderEmail;
        this.content = content;
        this.chatDate = chatDate;
        this.chatTime = chatTime;
        this.count = count;
    }

    public int getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(int roomNo) {
        this.roomNo = roomNo;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public String getImg_path() {
        return img_path;
    }

    public void setImg_path(String img_path) {
        this.img_path = img_path;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String name) {
        this.senderEmail = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String msg) {
        this.content = msg;
    }

    public String getChatDate() {
        return chatDate;
    }

    public void setChatDate(String date) {
        this.chatDate = date;
    }

    public String getChatTime() {
        return chatTime;
    }

    public void setChatTime(String time) {
        this.chatTime = time;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
