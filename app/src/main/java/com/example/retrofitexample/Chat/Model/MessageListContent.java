package com.example.retrofitexample.Chat.Model;

public class MessageListContent {
    int id;
    int roomNo;
    int count;

    String img_path;
    String title;
    String yourUser;
    String content;
    String chatTime;
    String chatDate;
    String email;

    public MessageListContent(){

    }

    public MessageListContent(int roomNo, String img_path, String title, String content, String chatTime, int count) {
        //this.type = type; //view type 상수를 보유합니다
        //this.data = data; // data 변수는 우리가 채울 각 데이터를 저장하는 데 사용됩니다.이상적으로는 드로어 블 또는 raw 타입 리소스를 포함합니다.
        this.id = id;
        this.roomNo = roomNo;
        this.img_path = img_path;
        this.title = title;
        this.content = content;
        this.chatTime = chatTime;
        this.count = count;
    }

    public int getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(int roomNo) {
        this.roomNo = roomNo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImg_path() {
        return img_path;
    }

    public void setImg_path(String img_path) {
        this.img_path = img_path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYourUser() {
        return yourUser;
    }

    public void setYourUser(String yourUser) {
        this.yourUser = yourUser;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getChatTime() {
        return chatTime;
    }

    public void setChatTime(String chatTime) {
        this.chatTime = chatTime;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
